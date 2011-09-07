/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.logging.Logger;
import sun.nio.cs.ThreadLocalCoders;

/**
 * The {@code BufferedCharSequence} class provides the {@code CharSequence}
 * interface for a text file.
 * <p>
 * <b>Important notes:</b>
 * <ul>
 *   <li> This implementation doesn't support files with a size more than 2GB,
 *        i.e. {@code Integer.MAX_VALUE} bytes.</li>
 *   <li> This implementation may provide incorrect results if underlying file
 *        will be changed since instantiation of this class until invocation of
 *        the method {@link BufferedCharSequence.close() }.</li>
 *   <li> Any method of this class may throw the run-time exception
 *        {@link BufferedCharSequence.SourceIOException} if the
 *        {@link IOException} will happen during I/O operation.</li>
 * </ul>
 * </p>
 *
 * @author Victor G. Vasilyev mailto:vvg@netbeans.org
 * @version 1.1
 */
public class BufferedCharSequence implements CharSequence {

    // TODO Counting lines. See a cycle in the method
    // BasicSearchCriteria.getTextDetails(BufferedCharSequence,
    //                                    DataObject, SearchPattern)
    
    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.BufferedCharSequence"); //NOI18N

    /** K = 2**10 = 1024 */
    public static final int K = 1024;

    // Implementation limits
    /**
     * Max file size that can be processed by this implementation.
     */
    public static final int MAX_FILE_SIZE = Integer.MAX_VALUE;

    // Default values
    /**
     * Max capacity of the source buffer that will be used by default.
     *
     * @see #setMaxBufferSize(int) 
     */
    private static final int MAX_SOURCE_BUFFER_SIZE = 4 * K;
    /**
     * Max subsequence length that will be processed by this implementation by
     * default.
     */
    public static final int MAX_SUBSEQUENCE_LENGTH = 4 * K;
    /**
     * Min sink buffer size that will be used.
     */
    private static final int MIN_SINK_BUFFER_SIZE = 16;

    /**
     * Definitions of the Unicode Line Terminators.
     */
    interface UnicodeLineTerminator {
        /** Unicode line feed (0x000A) */
        char LF = '\n'; //NOI18N
        /** Unicode carriage return (0x000D) */
        char CR = '\r'; //NOI18N
        /** Unicode line separator (0x2028) */
        char LS = 0x2028;
        /** Unicode paragraph separator (0x2029) */
        char PS = 0x2029;
        /** Unicode next line (0x0085) */
        char NEL = 0x0085;
        /** Unicode form feed (0x000C) */
        char FF = 0x000C;
    }

    // Internal state
    private Source source;
    private Sink sink;
    private final CharsetDecoder decoder;
    private CoderResult coderResult;
    private boolean isClosed = false;
    private int position = 0; // Invariants: position <= length

    /**
     * Creates {@code BufferedCharSequence} for the specified {@code stream}.
     * @param stream is a stream that will be buffered and represented as a
     *        {@code CharSequence}.
     * @param charset is a named mapping that will be used to decode a sequence
     *                of bytes from the {@code stream}.
     * @param size is the size of the file.
     */
    public BufferedCharSequence(final InputStream stream, Charset charset, long size) {
        // TODO charset.name() is used instead of charset due to a bug in the
        // org.netbeans.api.queries.FileEncodingQuery.ProxyCharset.ProxyDecoder
        // The IllegalStateException may be thrown after correct actions.
        // See #169804
        this(stream,
            ThreadLocalCoders.decoderFor(charset.name())
                             .onMalformedInput(CodingErrorAction.REPLACE)
                             .onUnmappableCharacter(CodingErrorAction.REPLACE), size);
    }


    public BufferedCharSequence(final InputStream stream,
                                CharsetDecoder decoder, long size) {
        this.source = new Source(stream, size);
        this.decoder = decoder;
        this.sink = new Sink(this.source);
        LOG.finer("<init> " + this.source + "; decoder = " + this.decoder +
                "; " + this.sink); // NOI18N
    }

    /**
     * The copying constructor.
     * @param bufferedCharSequence the {@code BuBufferedCharSequence} instance
     * that will be copied.
     */
    private BufferedCharSequence(BufferedCharSequence bufferedCharSequence) {
        this.source = bufferedCharSequence.source;
        this.sink = bufferedCharSequence.sink;
        this.decoder = bufferedCharSequence.decoder;
    }

    /**
     * Ensures that the {@link #close()} method is called when there are no more
     * references to the instance of this class.
     *
     * @exception  IOException if an I/O error occurs.
     * @see        BufferedCharSequence#close()
     */
    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
    /**
     * Gets maximal size of the source buffer.
     * @return the size of the source buffer.
     */
    public int getMaxBufferSize() {
        return source.maxBufferSize;
    }

    /**
     * Sets maximal size of the source buffer.
     * @param maxBufferSize the size (must be equal or greater than 1).
     * @throws IllegalArgumentException if the specified size is less than 1.
     */
    public void setMaxBufferSize(int maxBufferSize) {
        if(maxBufferSize < 1) throw new IllegalArgumentException();
        source.maxBufferSize = maxBufferSize;
    }

    /**
     * Resets all resources that support {@link CharSequence} interface.
     * Current position is not changed.
     * 
     * @return the underlying instance of this class.
     * @throws SourceIOException
     */
    public BufferedCharSequence reset() throws SourceIOException {        
        source.reset();
        sink.reset();
        decoder.reset();
        coderResult = CoderResult.UNDERFLOW;
        return this;
    }

    /**
     * Provides to stop use of underlying instance of this class and releases 
     * all involved resources.
     * @return the underlying instance of this class.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized BufferedCharSequence close() throws IOException {
        if (!isClosed) {
            reset();
            source.close();
            source = null;
            sink = null;
            coderResult = null;
            isClosed = true;
        }
        return this;
    }

    /**
     * Creates a new {@code BufferedCharSequence} that shares this char
     * sequence's content.
     * 
     * @return The new {@code BufferedCharSequence}
     */
    public BufferedCharSequence duplicate() {
        checkState();
        return new BufferedCharSequence(this);
    }

    // CharSequence interface

    @Override
    public int length() {
        checkState();
        reset();
        int length = 0;
        while(sink.next()) {  }
        length = sink.buffer.scope.end;        
        return length;
    }

    @Override
    public char charAt(int index) throws IndexOutOfBoundsException {
        checkState();
        String errMsg = check(index);
        if(errMsg != null) {
            throw new IndexOutOfBoundsException(errMsg);
        }
        // We should try before to decide to throw IOBE if index > length.
        return getCharAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end)
            throws IndexOutOfBoundsException {
        checkState();
        String errMsg = check(start, end);
        if(errMsg != null) {
            throw new IndexOutOfBoundsException(errMsg);
        }
        // We should try before to decide to throw IOBE if end > length.
        // So, we won't check it here.
        int subSequenceLength = getLength(start, end);
        if(subSequenceLength > MAX_SUBSEQUENCE_LENGTH) {
            throw new IndexOutOfBoundsException(
                    "requested subSequence has a big length (" +
                    subSequenceLength + ") > " +
                    MAX_SUBSEQUENCE_LENGTH); // NOI18N
        }
        CharSequence subSequence = getSubSequence(start, end);
        return subSequence;
    }

    @Override
    public String toString() {
        checkState();
        return subSequence(0, length()).toString();
    }

    // End of the CharSequence interface

    /**
     * Returns the current position.
     * @return the current position.
     */
    public final int position() {
        checkState();
	return position;
    }

    /**
     * Sets the specified position.
     * @param newPosition new position.
     * @return old position.
     */
    public final int changePosition(int newPosition) {
        int oldPosition = position;
        position = newPosition;
	return oldPosition;
    }

    /**
     * Gets a char at the currect position and increments the position.
     * @return a char.
     * @throws IndexOutOfBoundsException if EOF is reached.
     */
    public char nextChar() throws IndexOutOfBoundsException {
        checkState();
        return charAt(position++);
    }

    /**
     * Sets position to 0.
     * @return the underlying instance of this class.
     */
    public final BufferedCharSequence rewind() {
        checkState();
	position = 0;
	return this;
    }

    /**
     * Returns a line started from the current position. Sets the position at
     * the start of the next line.
     *
     * @return text from the current position up to the first occurrence of any
     * <ul>
     *   <li>'\n' - Unicode line feed (0x000A)</li>
     *   <li>'\r' - Unicode carriage return (0x000D)</li>
     *   <li>"\r\n" - A sequence of Unicode line feed (0x000A) and 
     *                Unicode carriage return (0x000D)</li>
     *   <li>Unicode line separator (0x2028)</li>
     *   <li>Unicode paragraph separator (0x2029)</li>
     * </ul>
     */
    public String nextLineText() {
        StringBuilder sb = new StringBuilder();
        try { // try-catch avoids use length() in required expression
              // like as position < length() in the while condition.
            while (true) {
                char c = nextChar();
                switch (c) {
                    case UnicodeLineTerminator.LF:
                    case UnicodeLineTerminator.PS:
                    case UnicodeLineTerminator.LS:
                    case UnicodeLineTerminator.FF:
                    case UnicodeLineTerminator.NEL:
                        return sb.toString();
                    case UnicodeLineTerminator.CR:
                        if (charAt(position) == UnicodeLineTerminator.LF) {
                            // process "\r\n".
                            nextChar();
                        }
                        return sb.toString();
                    default:
                        sb.append(c);
                }
            }
        } catch (IndexOutOfBoundsException ioobe) {
            // It is OK. It means that EOF is reached.
        }
        return sb.toString();
    }

    /**
     * Returns a line started from the current position.
     * The current position is not changed. This method is based on the method
     * {@link #nextLineText()}.
     *
     * @param start the start offset of the text line.
     * @return a text of the line.
     */
    public String getLineText(int start) {
        int oldPosition = changePosition(start);
        String text = nextLineText();        
        changePosition(oldPosition);
        return text;
    }

    private char getCharAt(int index) throws IndexOutOfBoundsException {        
        if(sink.buffer.scope.isBefore(index)) {            
            reset();
        }
        while(!sink.buffer.scope.isInside(index)) {
            boolean hasNext = sink.next();
            if(!hasNext) {
                throw new IndexOutOfBoundsException("index is " +
                        index + " > lenght"); // NOI18N
            }
        }        
        return sink.charAt(index);
    }

    private CharSequence getSubSequence(int start, int end) 
                                              throws IndexOutOfBoundsException {
        StringBuilder sb = new StringBuilder(getLength(start, end));
        for(int i = start; i < end; i++) {
            sb.append(charAt(i));
        }
        return sb.toString();
    }

    private int getLength(int start, int end) {
        return end - start;
    }

    private void checkState() {
        if(isClosed) {
            String msg = "BufferedCharSequence is closed";  // NOI18N
            throw new IllegalStateException(msg);
        }
    }

    private String check(int index) {
        if(index < 0) {
            return "index = " + index; // NOI18N
        }
        return null; // OK
    }

    private String check(int start, int end) {
        if(start < 0 || end < 0 || start > end) {
            return "start = " + start + ", end = " + end; // NOI18N
        }
        return null; // OK
    }


    /**
     * The source buffer.
     */
       private class Source {
        private int maxBufferSize = MAX_SOURCE_BUFFER_SIZE;

        private ByteBuffer buffer;
        private BufferedInputStream bstream;
        private int bufferSize;
        
        public Source(InputStream inputStream, long bufferSize) {
            this.bstream = new BufferedInputStream(inputStream);
            this.bstream.mark(Integer.MAX_VALUE);
            this.bufferSize = getBufferSize(bufferSize);
            buffer = newBuffer();
            buffer.position(buffer.limit());
        }

        @Override
        public String toString() {
            return "source=[stream = " + bstream.toString() + ", buffer = " + buffer + "]";
        }

        private ByteBuffer newBuffer() {
            return ByteBuffer.allocate(bufferSize);
        }

       public void reset() {        
            try {                
                bstream.reset();
            } catch (IOException ex) {
                throw new SourceIOException(ex);
            }            
            buffer.clear();
            buffer.position(buffer.limit());
        }

        /**
         * Reads a sequence of bytes from the source stream. Bytes are read
         * starting at the stream's current position, and then the
         * position is updated with the number of bytes actually read.
         *
         * @return The number of bytes read, possibly zero, or -1 if 
         * the end-of-stream is reached
         *
         * @throws ProcessException If some other I/O error occurs
         */
        private int read() {
            try {
                if(buffer.hasArray()) {
                    int res = bstream.read(buffer.array(), buffer.position(), buffer.remaining());
                    if(res > 0) {
                        buffer.position(res + buffer.position());
                    }
                    return res;
                }
                throw new IOException("No byte array");
            } catch (IOException ex) {
                throw new SourceIOException(ex);
            }
        }

        public void close() throws IOException {
            bstream.close();
        }

        public int getSize(long size) {
            if (size > Integer.MAX_VALUE) {
                LOG.warning("File size is " + size + "bytes. " +
                            "Only first " + MAX_FILE_SIZE +
                            " bytes will be processed.");
                return MAX_FILE_SIZE;
            }
            return (int) size;
        }

        private int getBufferSize(long bufferSize) {
            int size = Math.min(getSize(bufferSize), maxBufferSize);
            return size;
        }

        /**
         *
         * @return {@code true} if EOF, otherwise {@code false}.
         */
        public boolean readNext() {
            buffer.compact();
            int status = read();            
            buffer.flip();            
            return status == -1;
        }

        private int getCapacity() {
            return buffer.capacity();
        }

    } // Source
    /**
     * The sink buffer.
     */

    private class Sink {

        private Buffer buffer;

        public Sink(Source source) {
            int sourceCapacity = source.getCapacity();
            this.buffer = newBuffer(sourceCapacity);
        }

        @Override
        public String toString() {
            return "sink = [" + buffer + "]";
        }

        public void reset() {            
            buffer.reset();
        }

        public char charAt(int index) throws IndexOutOfBoundsException {
            assert index >= 0;
            return buffer.getCharAt(index);
        }

        /**
         * Obtains a next decoded portion of data. This method implements a
         * <a href=
*"http://java.sun.com/javase/6/docs/api/java/nio/charset/CharsetDecoder.html#steps"
         * >decoding operation</a>.
         * @return {@code true} is successful, otherwise {@code false}.
         */
        private boolean next() {
            CharBuffer out = buffer.clear();           
            boolean endOfInput = false;
            if(coderResult == CoderResult.UNDERFLOW) {
                endOfInput = source.readNext();
            }
            while((coderResult =
                    decoder.decode(source.buffer, out, endOfInput))
                    == CoderResult.OVERFLOW) {
                out = buffer.growBuffer();                
            }
            if(endOfInput) {
                while((coderResult = decoder.flush(out))
                        == CoderResult.OVERFLOW) {
                    out = buffer.growBuffer();
                }
            }
            buffer.adjustScope();
            return !buffer.scope.isEmpty();
        }

        private Buffer newBuffer(int sourceCapacity) {
            return new Buffer(sourceCapacity);
        }

        /**
         * A character buffer.
         * @see CharBuffer
         * @see Scope
         */
        private class Buffer {

            private CharBuffer charBuffer;
            private Scope scope = new Scope();

            public Buffer(int sourceCapacity) {
                allocate(sourceCapacity);
                reset();
            }

            @Override
            public String toString() {
                return "buffer[capacity = " + charBuffer.capacity() + "]";
            }

            /**
             * Makes the buffer capacity two times more.
             * @return the buffer.
             */
            public CharBuffer growBuffer() {
                int capacity = charBuffer.capacity();
                CharBuffer o = CharBuffer.allocate(capacity << 1);
                charBuffer.flip();
                o.put(charBuffer);
                charBuffer = o;
                LOG.finer("The sink char buffer capacity has been grown: " +
                        capacity + " -> " + charBuffer.capacity());
                return charBuffer;
            }

            private void allocate(int sourceCapacity) {
                if (sourceCapacity == 0) {
                    charBuffer = CharBuffer.allocate(0);
                    return;
                }
                int bufferSize = bufferSize(sourceCapacity);
                charBuffer = CharBuffer.allocate(bufferSize);
            }

            private int bufferSize(int sourceCapacity) {
                 int n = (int) (sourceCapacity * decoder.averageCharsPerByte());
                 return n < MIN_SINK_BUFFER_SIZE ? MIN_SINK_BUFFER_SIZE : n;
            }

            private char getCharAt(int index) {
                int position = charBuffer.position();
                charBuffer.position(0);
                char c = charBuffer.charAt(index - scope.start);
                charBuffer.position(position);
                return c;
            }

            private void reset() {               
                scope.reset();
                charBuffer.clear();
            }

            private void flip() {
                charBuffer.flip();
            }

            private CharBuffer clear() {
                charBuffer.clear();
                return charBuffer;
            }

            private void adjustScope() {             
                scope.start = scope.end == -1 ? 0 : scope.end;              
                flip();                                                 
                scope.end = scope.start + charBuffer.limit();                              
            }

            /**
             * Scope of the {@link Buffer}.
             */
            private class Scope {

                public static final int EOF = -1;

                private int start;
                private int end;

                /**
                 * Determines if the specified {@code index} points inside the
                 * scope.
                 *
                 * @param index the index being tested.
                 *
                 * @return {@code true} if {@code index} points inside the
                 * scope, otherwise {@code false}.
                 */
                public boolean isInside(int index) {
                    return index >= start && index < end;
                }

                /**
                 * Determines if the specified {@code index} points before the
                 * scope.
                 *
                 * @param index the index being tested.
                 *
                 * @return {@code true} if {@code index} &lt; {@code start} or
                 * the scope is not initialized yet, otherwize {@code false}.
                 */
                public boolean isBefore(int index) {
                    return start == EOF || index < start;
                }

                /**
                 * Sets both start and end of the {@code Scope} to the
                 * {@link BufferedCharSequence.​Sink.​Buffer.​Scope#EOF}.
                 */
                public void reset() {
                    start = end = EOF;
                }

                /**
                 * Checks whether the {@code Scope} is empty.
                 * @return {@code true} if empty (i.e {@code start == end}), 
                 * otherwise {@code false}.
                 */
                private boolean isEmpty() {
                    return start == end;
                }

            } // Scope

        } // Buffer

    } // Sink
    /**
     * Signals that an I/O exception of some sort has occurred.
     * Instance of this class wraps a cause {@link IOException} that is the 
     * checked exception to represent it as a unchecked exception.
     */
    public static class SourceIOException extends RuntimeException {

        /**
         * Creates new {@code SourceIOException}.
         * @param cause a cause {@link IOException}.
         */
        public SourceIOException(IOException cause) {
            super(cause);
        }

    } // SourceIOException

} // BufferedCharSequence
