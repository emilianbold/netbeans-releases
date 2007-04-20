/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import sun.security.util.PendingException;
import static java.lang.Math.min;
import static java.nio.charset.CoderResult.OVERFLOW;
import static java.nio.charset.CoderResult.UNDERFLOW;

/**
 *
 * @author  Marian Petras
 */
final class PropertiesEncoding extends FileEncodingQueryImplementation {
    
    private final Charset encoding;
    
    /** Creates a new instance of PropertiesEncoding */
    public PropertiesEncoding() {
        encoding = new PropCharset();
    }
    
    public Charset getEncoding(FileObject file) {
        return encoding;
    }
    
    Charset getEncoding() {
        return encoding;
    }
    
    /**
     */
    static enum NewLineType {
        
        /* the order of elements is significant - see method getNewLineType() */
        
        CR_LF,
        LF,
        CR
    }
    
    /**
     */
    static NewLineType getDefaultNewLineType() {
        return Utilities.isWindows() ? NewLineType.CR_LF : NewLineType.LF;
    }
    
    /**
     *
     */
    static final class PropCharset extends Charset {
        
        PropCharset() {
            super("resource_bundle_charset", null);                     //NOI18N
        }

        public boolean contains(Charset charset) {
            return true;
        }

        public PropCharsetEncoder newEncoder() {
            return new PropCharsetEncoder(this);
        }
        
        public PropCharsetDecoder newDecoder() {
            return new PropCharsetDecoder(this);
        }

    }
    
    /**
     *
     * @author  Marian Petras
     */
    static final class PropCharsetEncoder extends CharsetEncoder {
        
        private static final int avgEncodedTokenLen = 3;
        private static final int maxEncodedTokenLen = 6;
        
        private static final int inBufSize = 8192;
        private static final int outBufSize = inBufSize * avgEncodedTokenLen;
        
        private final char[] inBuf = new char[inBufSize];
        private final byte[] outBuf = new byte[outBufSize];
        
        private final NewLineType nlType;
        
        private int inBufPos, outBufPos;
        
        private boolean emptyIn;
        private boolean fullOut;
        private boolean emptyInBuf;
        
        private boolean backslashPending;
        
        PropCharsetEncoder(Charset charset) {
            this(charset, getDefaultNewLineType());
        }
        
        PropCharsetEncoder(Charset charset, NewLineType nlType) {
            super(charset, avgEncodedTokenLen, maxEncodedTokenLen);
            this.nlType = nlType;
        }
        
        {
            implReset();
        }
        
        protected void implReset() {
            inBufPos = 0;
            outBufPos = 0;
            
            emptyIn = false;
            fullOut = false;
            emptyInBuf = true;
            
            backslashPending = false;
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            emptyIn = false;
            fullOut = false;
            try {
                readInLoop:
                for (;;) {
                    readIn(in);
                    for (;;) {
                        encodeBuf();
                        if (emptyInBuf && !emptyIn) {
                            continue readInLoop;
                        } else if (emptyIn && hasPendingCharacters()) {
                            handlePendingCharacters();
                        }
                        flushOutBuf(out);
                        if (fullOut) {
                            return OVERFLOW;
                        } else if (emptyInBuf && emptyIn && !hasPendingCharacters()) {
                            return UNDERFLOW;
                        }
                    }
                }
            } catch (BufferUnderflowException ex) {
                assert false;                           //this should not happen
                return UNDERFLOW;
            } catch (BufferOverflowException ex) {
                assert false;                           //this should not happen
                return OVERFLOW;
            }
        }
        
        protected CoderResult implFlush(ByteBuffer out) {
            handlePendingCharacters();
            return flushOutBuf(out) ? OVERFLOW
                                    : UNDERFLOW;
        }
        
        /**
         * Reads and stores as many characters from the input buffer as possible.
         * If there are no more characters available in the input buffer,
         * sets flag variable {@link #emptyIn} to {@code true}.
         */
        private void readIn(CharBuffer in) {
            if (emptyIn) {
                return;
            }
            
            int inRemaining = in.remaining();
            if (inRemaining == 0) {
                emptyIn = true;
                return;
            }
            
            int bufRemaining = inBuf.length - inBufPos;
            if (bufRemaining == 0) {
                /* no space in inBuf */
                return;
            }
            
            int length = min(inRemaining, bufRemaining);
            in.get(inBuf, inBufPos, length);
            inBufPos += length;
            emptyInBuf = false;
            
            if (length == inRemaining) {
                assert in.remaining() == 0;
                emptyIn = true;
            }
        }
        
        /**
         * Encodes as many chars from the internal input buffer as possible.
         */
        private void encodeBuf() {
            if (emptyInBuf) {
                return;
            }
            
            int encodingInBufPos = 0;
            while ((encodingInBufPos < inBufPos)
                    && (outBufPos <= outBufSize - maxEncodedTokenLen)) {
                encodeChar(inBuf[encodingInBufPos++]);
            }
            
            int remainder = inBufPos - encodingInBufPos;
            if (remainder != 0) {
                System.arraycopy(inBuf, encodingInBufPos,
                                 inBuf, 0,
                                 remainder);
            }
            inBufPos = remainder;
            emptyInBuf = (inBufPos == 0);
        }
        
        /**
         * Are there any pending characters to be sent to the {@code outBuf}?
         * 
         * @return  {@code true} if there are any pending characters to be
         *          written to the {@link #outBuf}, {@code false} otherwise
         * @see  #handlePendingCharacters
         */
        private boolean hasPendingCharacters() {
            return backslashPending;
        }
        
        /**
         * Sends all pending characters to the {@link #outBuf} if there is enough
         * space for them in the buffer.
         * Success of the operation can be enquired by calling method
         * {@link #hasPendingCharacters}.
         * 
         * @return  number of characters written to the {@link #outBuf}
         */
        private int handlePendingCharacters() {
            if (!hasPendingCharacters()) {
                return 0;
            }
            
            if (outBufPos <= (outBufSize - 1)) {
                outBuf[outBufPos++] = '\\';
                backslashPending = false;
                return 1;
            } else {
                return 0;
            }
        }
        
        /**
         * Writes as many as possible bytes from the {@code outBuf} to the given
         * {@code ByteBuffer} and removes the written bytes from {@code outBuf}.
         * 
         * @return  {@code true} if the given {@code out} buffer is overflown,
         *          {@code false} otherwise
         */
        private boolean flushOutBuf(ByteBuffer out) {
            if (fullOut) {
                return true;
            }
            
            int outRemaining = out.remaining();
            if (outRemaining == 0) {
                fullOut = true;
                return true;
            }
            
            if (outBufPos == 0) {
                /* nothing to flush */
                return false;
            }
            
            int length = min(outRemaining, outBufPos);
            out.put(outBuf, 0, length);
            
            int remainder = outBufPos - length;
            if (remainder != 0) {
                System.arraycopy(outBuf, length,
                                 outBuf, 0,
                                 remainder);
            }
            outBufPos = remainder;
            
            if (length == outRemaining) {
                assert out.remaining() == 0;
                fullOut = true;
            }
            
            return (remainder != 0);
        }
        
        private static final byte zeroByte = (byte) '0';
        private static final String backslashChars = "\t\f\r";          //NOI18N
        private static final byte[] backslashCharsRepl = {'t','f','r'};
        private static final byte[] hexadecimalChars
                = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        
        private int encodeChar(final char c) {
            final int oldPos = outBufPos;
            final int cInt = (int) c;
            
            int index;
            
            if (backslashPending) {
                backslashPending = false;
                if (c == '\\') {
                    outBuf[outBufPos++] = '\\';
                    return 1;
                }
                
                if (c == 'u') {
                    /* Add one 'u' to the unicode escape sequence: */
                    outBuf[outBufPos++] = 'u';
                    outBuf[outBufPos++] = 'u';
                    return 2;
                }
            }
            if (c == '\n') {
                if (nlType != NewLineType.LF) {
                    outBuf[outBufPos++] = (byte) '\r';
                }
                if (nlType != NewLineType.CR) {
                    outBuf[outBufPos++] = (byte) '\n';
                }
            } else if (c == '\\') {
                outBuf[outBufPos++] = (byte) '\\';
                backslashPending = true;
                /*
                 * We do not want to quote the backslash at the end of line.
                 * Remember that the last character was backslash and wait for
                 * the following character - if it is not a newline, finish
                 * quoting the backslash and print the character. If the next
                 * character is a newline char, do not quote the backslash.
                 */
            } else if ((index = backslashChars.indexOf(c)) >= 0) {
                outBuf[outBufPos++] = (byte) '\\';
                outBuf[outBufPos++] = backslashCharsRepl[index];
            } else if ((c < '\u0020') || (c > '\u007e')) {
                outBuf[outBufPos++] = (byte) '\\';
                outBuf[outBufPos++] = (byte) 'u';
                if (c >= '\u0100') {
                    outBuf[outBufPos++] = hexadecimalChars[(cInt >> 12) & 0x000f];
                    outBuf[outBufPos++] = hexadecimalChars[(cInt >>  8) & 0x000f];
                } else {
                    outBuf[outBufPos++] = zeroByte;
                    outBuf[outBufPos++] = zeroByte;
                }
                outBuf[outBufPos++] = hexadecimalChars[(cInt >> 4) & 0x000f];
                outBuf[outBufPos++] = hexadecimalChars[cInt & 0x000f];
            } else {
                outBuf[outBufPos++] = (byte) c;
            }
            
            return outBufPos - oldPos;
        }
        
        byte[] encodeCharForTests(final char c) {
            reset();
            
            final int tokenLength = encodeChar(c) + handlePendingCharacters();
            byte[] result = new byte[tokenLength];
            System.arraycopy(outBuf, 0, result, 0, tokenLength);
            return result;
        }
        
        byte[] encodeStringForTests(final String s) throws CharacterCodingException {
            ByteBuffer resultBuf = encode(CharBuffer.wrap(s));
            byte[] resultBufArray = resultBuf.array();
            int resultBufPos = resultBuf.limit();
            if (resultBufPos == resultBufArray.length) {
                return resultBufArray;
            } else {
                byte[] result = new byte[resultBufPos];
                System.arraycopy(resultBufArray, 0, result, 0, resultBufPos);
                return result;
            }
        }
        
    }

    /**
     *
     */
    static final class PropCharsetDecoder extends CharsetDecoder {

        private static enum State {
            INITIAL,
            CR,
            BACKSLASH,
            UNICODE,
        }
        
        private static final float avgCharsPerByte = 0.33f;
        private static final float maxCharsPerByte = 5.00f;
        /*
         * Five chars are written to the output when a malformed unicode
         * sequence is detected. Unicode sequences are six bytes long;
         * if the first five bytes formed a valid sequence
         * (e.g. <backslash>, "u", "1", "2", "3") and the sixth byte is not
         * a hexadecimal digit, we put transform the first five bytes
         * of the sequence to (five) characters and send them to the output.
         * (The sixth byte is re-read and handled in the next round
         * of the decoding cycle.)
         */
        
        private static final int inBufSize = 8192;
        private static final int outBufSize = inBufSize;
        
        private final byte[] inBuf = new byte[inBufSize];
        private final char[] outBuf = new char[outBufSize];
        
        private int[] nlTypesUsage = new int[NewLineType.values().length];
        
        private int inBufPos, outBufPos;
        
        private boolean emptyIn;
        private boolean fullOut;
        private boolean emptyInBuf;
        
        private State state;
        private int unicodeBytesRead;
        private int unicodeValue;
        
        /** used when flushing a malformed unicode sequence to the out buffer */
        private char[] unicodeValueChars = new char[3];
        
        PropCharsetDecoder(Charset charset) {
            super(charset, avgCharsPerByte, maxCharsPerByte);
        }
        
        {
            implReset();
        }

        protected void implReset() {
            inBufPos = 0;
            outBufPos = 0;
            
            emptyIn = false;
            fullOut = false;
            emptyInBuf = true;
            
            state = State.INITIAL;
            unicodeBytesRead = 0;
            
            for (NewLineType nlType : NewLineType.values()) {
                nlTypesUsage[nlType.ordinal()] = 0;
            }
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            emptyIn = false;
            fullOut = false;
            try {
                readInLoop:
                for (;;) {
                    readIn(in);
                    for (;;) {
                        CoderResult coderResult = decodeBuf();
                        if (coderResult != null) {
                            return coderResult;
                        }
                        if (emptyInBuf && !emptyIn) {
                            continue readInLoop;
                        }
                        flushOutBuf(out);
                        if (fullOut) {
                            return OVERFLOW;
                        } else if (emptyInBuf && emptyIn) {
                            return UNDERFLOW;
                        }
                    }
                }
            } catch (BufferUnderflowException ex) {
                assert false;                           //this should not happen
                return UNDERFLOW;
            } catch (BufferOverflowException ex) {
                assert false;                           //this should not happen
                return OVERFLOW;
            }
        }
        
        protected CoderResult implFlush(CharBuffer out) {
            
            if (state == State.CR) {
                outBuf[outBufPos++] = '\n';
                nlTypesUsage[NewLineType.CR.ordinal()]++;
            }
            state = State.INITIAL;
            return flushOutBuf(out) ? OVERFLOW
                                    : UNDERFLOW;
        }
        
        /**
         * Reads and stores as many characters from the input buffer as possible.
         * If there are no more characters available in the input buffer,
         * sets flag variable {@link #emptyIn} to {@code true}.
         */
        private void readIn(ByteBuffer in) {
            if (emptyIn) {
                return;
            }
            
            int inRemaining = in.remaining();
            if (inRemaining == 0) {
                emptyIn = true;
                return;
            }
            
            int bufRemaining = inBuf.length - inBufPos;
            if (bufRemaining == 0) {
                /* no space in inBuf */
                return;
            }
            
            int length = min(inRemaining, bufRemaining);
            in.get(inBuf, inBufPos, length);
            inBufPos += length;
            emptyInBuf = false;
            
            if (length == inRemaining) {
                assert in.remaining() == 0;
                emptyIn = true;
            }
        }
        
        /**
         * Encodes as many chars from the internal input buffer as possible.
         */
        private CoderResult decodeBuf() {
            if (emptyInBuf) {
                return null;
            }
            
            CoderResult result = null;
            int decodingInBufPos = 0;
            while ((decodingInBufPos < inBufPos) && (outBufPos <= outBufSize - 2)) {
                int decodedChars = decodeByte(inBuf[decodingInBufPos++]);
                if (decodedChars < 0) {
                    /* put back the character following the broken sequence: */
                    decodingInBufPos--;

                    unicodeBytesRead = 0;
                    unicodeValue = 0;
                    state = State.INITIAL;
                    //break;
                }
            }
            int remainder = inBufPos - decodingInBufPos;
            if (remainder != 0) {
                System.arraycopy(inBuf, decodingInBufPos,
                                 inBuf, 0,
                                 remainder);
            }
            inBufPos = remainder;
            emptyInBuf = (inBufPos == 0);
            return result;
        }
        
        /**
         * 
         * @return  {@code true} if the given {@code out} buffer is overflown,
         *          {@code false} otherwise
         */
        private boolean flushOutBuf(CharBuffer out) {
            if (outBufPos == 0) {
                /* nothing to flush */
                return false;
            }
            
            if (fullOut) {      //we know that (outBufPos != null)
                return true;
            }
            
            int outRemaining = out.remaining();
            if (outRemaining == 0) {
                fullOut = true;
                return true;
            }
            
            int length = min(outRemaining, outBufPos);
            out.put(outBuf, 0, length);
            
            int remainder = outBufPos - length;
            if (remainder != 0) {
                System.arraycopy(outBuf, length,
                                 outBuf, 0,
                                 remainder);
            }
            outBufPos = remainder;
            
            if (length == outRemaining) {
                assert out.remaining() == 0;
                fullOut = true;
            }
            
            return (remainder != 0);
        }
        
        private static final String hexadecimalChars
                                    = "0123456789abcdefABCDEF";         //NOI18N
        
        private int decodeByte(final byte b) {
            final int oldPos = outBufPos;
            final int bInt = (int) (b >= 0 ? b : b + 256);
            assert (bInt >= 0) && ((bInt & 0xff) == bInt);
            
            final char bChar = (char) bInt;
            
            switch (state) {
                case INITIAL:
                    if (bChar == '\r') {
                        state = State.CR;
                    } else if (bChar == '\\') {
                        state = State.BACKSLASH;
                    } else {
                        outBuf[outBufPos++] = bChar;
                        /* keep the state at INITIAL */
                    }
                    break;
                case CR:
                    if (bChar == '\r') {
                        outBuf[outBufPos++] = '\n';
                        nlTypesUsage[NewLineType.CR.ordinal()]++;
                        /* keep the state at CR */
                    } else if (bChar == '\n') {
                        outBuf[outBufPos++] = '\n';
                        nlTypesUsage[NewLineType.CR_LF.ordinal()]++;
                        state = State.INITIAL;
                    } else if (bChar == '\\') {
                        outBuf[outBufPos++] = '\n';
                        nlTypesUsage[NewLineType.CR.ordinal()]++;
                        state = State.BACKSLASH;
                    } else {
                        outBuf[outBufPos++] = '\n';
                        outBuf[outBufPos++] = bChar;
                        nlTypesUsage[NewLineType.CR.ordinal()]++;
                        state = State.INITIAL;
                    }
                    break;
                case BACKSLASH:
                    if (bChar == '\r') {
                        outBuf[outBufPos++] = '\\';
                        state = State.CR;
                    } else if (bChar == '\n') {
                        outBuf[outBufPos++] = '\\';
                        outBuf[outBufPos++] = '\n';
                        nlTypesUsage[NewLineType.LF.ordinal()]++;
                        state = State.INITIAL;
                    } else if (bChar == 'u') {
                        state = State.UNICODE;
                    } else {
                        outBuf[outBufPos++] = '\\';
                        outBuf[outBufPos++] = bChar;
                        state = State.INITIAL;
                    }
                    break;
                case UNICODE:
                    boolean malformed = false;
                    if (bChar == 'u') {
                        if (unicodeBytesRead == 0) {
                            //replace "\\uu...." with "\\u....":
                            outBuf[outBufPos++] = '\\';
                            outBuf[outBufPos++] = 'u';
                            state = State.INITIAL;
                        } else {
                            malformed = true;
                        }
                    } else {
                        int index = hexadecimalChars.indexOf(bChar);
                        if (index >= 0) {
                            if (index > 15) {   //one of [A-F] used
                                index -= 6;     //transform to lowercase
                            }
                            assert index <= 15;
                            unicodeValue = (unicodeValue << 4) | index;
                            if (++unicodeBytesRead == 4) {
                                outBuf[outBufPos++] = (char) unicodeValue;

                                unicodeBytesRead = 0;
                                unicodeValue = 0;

                                state = State.INITIAL;
                            }
                            /* else: keep the state at UNICODE */
                        } else {
                            malformed = true;
                        }
                    }
                    if (state != State.UNICODE) {
                        if (malformed) {
                            /*
                             * send the malformed unicode sequence to the output
                             */
                            outBuf[outBufPos++] = '\\';
                            outBuf[outBufPos++] = 'u';
                            for (int i = 0; i < unicodeBytesRead; i++) {
                                outBuf[outBufPos++] = unicodeValueChars[i];
                            }
                        }
                        unicodeBytesRead = 0;
                        unicodeValue = 0;
                        if (malformed) {
                            return -1;
                        }
                    }
                    break;
                default:
                    assert false;
                    break;
            }
            
            return outBufPos - oldPos;
        }
        
        char[] decodeBytesForTests(final byte[] bytes) {
            reset();
            
            int length = 0;
            for (int i = 0; i < bytes.length; i++) {
                length += decodeByte(bytes[i]);
            }

            char[] result = new char[length];
            System.arraycopy(outBuf, 0, result, 0, length);
            return result;
        }
        
        NewLineType getNewLineType() {
            NewLineType nlType = getDefaultNewLineType();
            int nlTypeUsage = nlTypesUsage[nlType.ordinal()];
            
            for (NewLineType testNlType : NewLineType.values()) {
                if (nlTypesUsage[testNlType.ordinal()] > nlTypeUsage) {
                    nlType = testNlType;
                    nlTypeUsage = nlTypesUsage[nlType.ordinal()];
                }
            }
            
            return nlType;
        }
        
    }

}
