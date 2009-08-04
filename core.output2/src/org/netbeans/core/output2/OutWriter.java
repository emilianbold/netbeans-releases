/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/*
 * OutWriter.java
 *
 * Created on February 27, 2004, 7:24 PM
 */

package org.netbeans.core.output2;

import java.awt.Color;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collection;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Implementation of OutputWriter backed by an implementation of Storage (memory mapped file, heap array, etc).
 *
 * @author  Tim Boudreau
 */
class OutWriter extends PrintWriter {
    /** A flag indicating an io exception occured */
    private boolean trouble = false;

    private NbIO owner;
    
    private boolean disposed = false;

    //IZ 44375 - Memory mapping fails with bad file handle on win 98
    private static final boolean USE_HEAP_STORAGE =
        Boolean.getBoolean("nb.output.heap") || Utilities.getOperatingSystem() == //NOI18N
        Utilities.OS_WIN98 || 
        Utilities.getOperatingSystem() == Utilities.OS_WIN95;

    /**
     * Byte array used to write the line separator after line writes.
     */
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** The read-write backing storage.  May be heap or */
    private Storage storage;
    
    /** The Lines object that will be used for reading data out of the 
     * storage */
    private AbstractLines lines = new LinesImpl();
    
    /** Flag set if a write failed due to disk space limits.  Subsequent
     * instances will use HeapStorage in this case */
    static boolean lowDiskSpace = false;

    
    /**
     * Need to remember the line start and lenght over multiple calls to
     * write(ByteBuffer), needed to facilitate other calls than println()
     */
    private int lineStart;
    private int lineLength;

    /** Creates a new instance of OutWriter */
    OutWriter(NbIO owner) {
        this();
        this.owner = owner;
    }

    /**
     * Package constructor for unit tests
     */
    OutWriter() {
        super (new DummyWriter());
        lineStart = -1;
        lineLength = 0;
    }

    Storage getStorage() {
        if (disposed) {
            throw new IllegalStateException ("Output file has been disposed!");
        }
        if (storage == null) {
            storage = USE_HEAP_STORAGE || lowDiskSpace ? 
                (Storage)new HeapStorage() : (Storage)new FileMapStorage();
        }
        return storage;
    }
    
    boolean hasStorage() {
        return storage != null;
    }
    
    boolean isDisposed() {
        return disposed;
    }
    
    boolean isEmpty() {
        return storage == null ? true : storage.size() == 0;
    }

    @Override
    public String toString() {
        return "OutWriter@" + System.identityHashCode(this) + " for " + owner + " closed ";
    }

    private int errorCount = 0;

    /** Generic exception handling, marking the error flag and notifying it with ErrorManager */
    private void handleException(Exception e) {
        setError();
        if (Controller.LOG) {
            StackTraceElement[] el = e.getStackTrace();
            Controller.log("EXCEPTION: " + e.getClass() + e.getMessage());
            for (int i = 1; i < el.length; i++) {
                Controller.log(el[i].toString());
            }
        }
        if (errorCount++ < 3) {
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Write the passed buffer to the backing storage, recording the line start in the mapping of lines to
     * byte offsets.
     *
     * @param bb
     * @throws IOException
     */
    public synchronized void write(ByteBuffer bb, boolean completeLine) {
        if (checkError()) {
            return;
        }
        closed = false;
        int start = -1;
        try {
            start = getStorage().write(bb);
        } catch (java.nio.channels.AsynchronousCloseException ace) {
            //Execution termination has sent ThreadDeath to the process in the
            //middle of a write
            onWriteException();
        } catch (IOException ioe) {
            //Out of disk space
            if (ioe.getMessage().indexOf("There is not enough space on the disk") != -1) { //NOI18N
                lowDiskSpace = true;
                String msg = NbBundle.getMessage(OutWriter.class, 
                    "MSG_DiskSpace", storage); //NOI18N
                Exceptions.attachLocalizedMessage(ioe, msg);
                Exceptions.printStackTrace(ioe);
                setError();
                storage.dispose();
            } else {
                //Existing output may still be readable - close, but leave
                //open for reads - if there's a problem there too, the error
                //flag will be set when a read is attempted
                Exceptions.printStackTrace(ioe);
                onWriteException();
            }
        }
        if (checkError()) {
            return;
        }
        int length = bb.limit();
        lineLength = lineLength + length;
        if (start >= 0 && lineStart == -1) {
            lineStart = start;
        }
        lines.lineUpdated(lineStart, lineLength, completeLine);
        if (completeLine) {
            lineStart = -1;
            lineLength = 0;
        }
        if (owner != null && owner.isStreamClosed()) {
            owner.setStreamClosed(false);
            lines.fire();
        }
    }

    /**
     * An exception has occurred while writing, which has left us in a readable state, but not
     * a writable one.  Typically this happens when an executing process was sent Thread.stop()
     * in the middle of a write.
     */
    void onWriteException() {
        trouble = true;
        if (Controller.LOG) Controller.log (this + " Close due to termination");
        ErrWriter err = owner.writer().err();
        if (err != null) {
            err.closed=true;
        }
        owner.setStreamClosed(true);
        close();
    }

    /**
     * Dispose this writer.  If reuse is true, the underlying storage will be disposed, but the
     * OutWriter will still be usable.  If reuse if false, note that any current ChangeListener is cleared.
     *
     */
    public synchronized void dispose() {
        if (disposed) {
            //This can happen if a tab was closed, so we were already disposed, but then the
            //ant module tries to reuse the tab -
            return;
        }
        if (Controller.LOG) Controller.log (this + ": OutWriter.dispose - owner is " + (owner == null ? "null" : owner.getName()));
        clearListeners();
        if (storage != null) {
            lines.onDispose(storage.size());
            storage.dispose();
            storage = null;
        }
        if (Controller.LOG) Controller.log (this + ": Setting owner to null, trouble to true, dirty to false.  This OutWriter is officially dead.");
        owner = null;
        disposed = true;
    }


    private void clearListeners() {
        if (Controller.LOG) Controller.log (this + ": Sending outputLineCleared to all listeners");
        if (owner == null) {
            //Somebody called reset() twice
            return;
        }
        synchronized (this) {
            if (lines.hasListeners()) {
                int[] listenerLines = lines.getLinesWithListeners();
                Controller.ControllerOutputEvent e = new Controller.ControllerOutputEvent(owner, 0);
                for (int i=0; i < listenerLines.length; i++) {
                    Collection<OutputListener> ols = lines.getListenersForLine(listenerLines[i]);
                    e.setLine(listenerLines[i]);
                    for (OutputListener ol : ols) {
                        ol.outputLineCleared(e);
                    }
                }
            } else {
                if (Controller.LOG) Controller.log (this + ": No listeners to clear");
            }
        }
    }

    public synchronized boolean isClosed() {
        if (checkError() || storage == null || storage.isClosed()) {
            return true;
        } else {
            return closed;
        }
    }

    public Lines getLines() {
        return lines;
    }

    private boolean closed = false;
    @Override
    public synchronized void close() {
        closed = true;
        try {
            //#49955 - possible (but difficult) to end up with close()
            //called twice
            if (storage != null) {
                storage.close();
            }
            lines.fire();
        } catch (IOException ioe) {
            onWriteException();
        }
    }

    @Override
    public synchronized void println(String s) {
        doWrite(s, 0, s.length());
        println();
    }

    @Override
    public synchronized void flush() {
        if (checkError()) {
            return;
        }
        try {
            getStorage().flush();
            lines.fire();
        } catch (IOException e) {
            onWriteException();
        }
    }

    @Override
    public boolean checkError() {
        return disposed || trouble;
    }

    static class CharArrayWrapper implements CharSequence {

        private char[] arr;
        private int off;
        private int len;

        public CharArrayWrapper(char[] arr) {
            this(arr, 0, arr.length);
        }

        public CharArrayWrapper(char[] arr, int off, int len) {
            this.arr = arr;
            this.off = off;
            this.len = len;
        }

        public char charAt(int index) {
            return arr[off + index];
        }

        public int length() {
            return len;
        }

        public CharSequence subSequence(int start, int end) {
            return new CharArrayWrapper(arr, off + start, end - start);
        }

        @Override
        public String toString() {
            return new String(arr, off, len);
        }
    }

    @Override
    public synchronized void write(int c) {
        doWrite(new String(new char[]{(char)c}), 0, 1);
    }
    
    @Override
    public synchronized void write(char data[], int off, int len) {
        doWrite(new CharArrayWrapper(data), off, len);
    }
    
    /** write buffer size in chars */
    private static final int WRITE_BUFF_SIZE = 16*1024;
    static final String TAB_REPLACEMENT = "        ";
    public synchronized int doWrite(CharSequence s, int off, int len) {
        if (checkError() || len == 0) {
            return 0;
        }

        int lineCount = 0;
        try {
            boolean written = false;
            ByteBuffer byteBuff = getStorage().getWriteBuffer(WRITE_BUFF_SIZE * 2);
            CharBuffer charBuff = byteBuff.asCharBuffer();
            for (int i = off; i < off + len; i++) {
                if (charBuff.position() + TAB_REPLACEMENT.length() >= WRITE_BUFF_SIZE) {
                    write((ByteBuffer) byteBuff.position(charBuff.position() * 2), false);
                    written = true;
                }
                if (written) {
                    byteBuff = getStorage().getWriteBuffer(WRITE_BUFF_SIZE * 2);
                    charBuff = byteBuff.asCharBuffer();
                    written = false;
                }
                char c = s.charAt(i);
                if (c == '\t') {
                    charBuff.put(TAB_REPLACEMENT);
                } else if (c == '\r') {
                    // skip
                } else if (c == '\n') {
                    charBuff.put(LINE_SEPARATOR);
                    write((ByteBuffer) byteBuff.position(charBuff.position() * 2), true);
                    written = true;
                    lineCount++;
                } else {
                    charBuff.put(c);
                }
            }
            if (!written) {
                write((ByteBuffer) byteBuff.position(charBuff.position() * 2), false);
            }
        } catch (IOException ioe) {
            onWriteException();
        }
        lines.delayedFire();
        return lineCount;
    }

    @Override
    public synchronized void write(char data[]) {
        doWrite(new CharArrayWrapper(data), 0, data.length);
    }

    @Override
    public synchronized void println() {
        doWrite("\n", 0, 1);
    }

    /**
     * Write a portion of a string.
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    @Override
    public synchronized void write(String s, int off, int len) {
        doWrite(s, off, len);
    }

    @Override
    public synchronized void write(String s) {
        doWrite(s, 0, s.length());
    }

    public synchronized void println(String s, OutputListener l) {
        println(s, l, false);
    }

    public synchronized void println(String s, OutputListener l, boolean important) {
        print(s, l, important, null, false, true);
    }

    synchronized void print(CharSequence s, OutputListener l, boolean important, Color c, boolean err, boolean addLS) {
        int lastLine = lines.getLineCount() - 1;
        int lastPos = lines.getCharCount();
        doWrite(s, 0, s.length());
        if (addLS) {
            println();
        }
        lines.updateLinesInfo(s, lastLine, lastPos, l, important, err, c);
    }

    synchronized void print(CharSequence s, LineInfo info, boolean important) {
        int line = lines.getLineCount() - 1;
        doWrite(s, 0, s.length());
        if (info != null) {
            lines.addLineInfo(line, info, important);
        }
    }

    /**
     * A useless writer object to pass to the superclass constructor.  We override all methods
     * of it anyway.
     */
    static class DummyWriter extends Writer {
        
        DummyWriter() {
            super (new Object());
        }
        
        public void close() throws IOException {
        }
        
        public void flush() throws IOException {
        }
        
        public void write(char[] cbuf, int off, int len) throws IOException {
        }
    }

    private class LinesImpl extends AbstractLines {
        LinesImpl() {
            super();
        }

        protected Storage getStorage() {
            return OutWriter.this.getStorage();
        }

        protected boolean isDisposed() {
             return OutWriter.this.disposed;
        }

        public Object readLock() {
            return OutWriter.this;
        }

        public boolean isGrowing() {
            return !isClosed();
        }

        protected void handleException (Exception e) {
            OutWriter.this.handleException(e);
        }
    }
}
