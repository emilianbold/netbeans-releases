/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * OutWriter.java
 *
 * Created on February 27, 2004, 7:24 PM
 */

package org.netbeans.core.output2;

import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Utilities;

/**
 * Implementation of OutputWriter backed by an implementation of Storage (memory mapped file, heap array, etc).
 *
 * @author  Tim Boudreau
 */
class OutWriter extends OutputWriter implements Runnable {
    /** A flag indicating an io exception occured */
    private boolean trouble = false;
    /** A flag that indicates some characters have been written */
    private volatile boolean dirty = false;
    /** A change listener.  Changes are fired on calls to flush, close, etc. */
    private ChangeListener listener = null;
    /** A collections-like lineStartList that maps file positions to line numbers */
    IntList lineStartList;
    /** Maps output listeners to the lines they are associated with */
    IntMap linesToListeners;

    private NbIO owner;
    
    private IntList errLines = null;

    //IZ 44375 - Memory mapping fails with bad file handle on win 98
    private static final boolean USE_HEAP_STORAGE = 
        Boolean.getBoolean("nb.output.heap") || Utilities.getOperatingSystem() == //NOI18N
        Utilities.OS_WIN98 || 
        Utilities.getOperatingSystem() == Utilities.OS_WIN95;
    
    private int longestLine = 0;

    /**
     * Byte array used to write the line separator after line writes. 
     */
    static byte[] lineSepBytes = new byte[] { '\0', '\n'}; //XXX Endianness
    private Storage storage;

    /** Creates a new instance of OutWriter */
    public OutWriter(NbIO owner) {
        this();
        this.owner = owner;
    }

    /**
     * Package constructor for unit tests
     */
    OutWriter() {
        super (new DummyWriter());
        getStorage();
        init();
    }

    public Storage getStorage() {
        if (storage == null) {
            storage = USE_HEAP_STORAGE ? (Storage)new HeapStorage() : (Storage)new FileMapStorage();
        }
        return storage;
    }
    
    public int[] allListenerLines() {
        return linesToListeners.getKeys();
    }
    
    public boolean isUnused() {
        return unused;
    }

    private boolean unused = true;
    protected synchronized void init() {
        knownLogicalLineCounts = null;
        storage = null;
        lineStartList = new IntList(100);
        linesToListeners = new IntMap();
        lastWrappedLineCount = -1;
        trouble = false;
        errLines = null;
        longestLine = 0;
        closesRequired = 0;
        //Note we clear the listener here - a new OutputDocument should be created
//        listener = null;
    }

    private synchronized void updateCloseCount () {
        closesRequired = Math.max (1, closesRequired);
    }

    public synchronized void markErr() {
        if (trouble || isClosed()) {
            return;
        }
        if (errLines == null) {
            errLines = new IntList(20);
        }
        errLines.add(lineCount() == 0 ? 0 : lineCount()-1);
    }

    private int closesRequired = 0;
    private ErrWriter err = null;
    public synchronized ErrWriter getErr() {
        if (err == null) {
            err = new ErrWriter(this);
            closesRequired = Math.max(2, closesRequired);
        }
        return err;
    }

    public boolean isErr (int line) {
        return errLines != null ? errLines.contains(line) : false;
    }
    
    public String toString() {
        return "OutWriter@" + System.identityHashCode(this) + " for " + owner + " unused=" + unused + " closed " + " listener=" + System.identityHashCode(listener);
    }

    /**
     * Get a substring from the output file.
     */
    synchronized String substring (int start, int end) {
        if (disposed || trouble) {
            return new String (subrange (start, end, null));
        }
        if (end < start) {
            throw new IllegalArgumentException ("Illogical text range from " + 
                start + " to " + end);
        }
        int fileStart = toByteIndex(start);
        int byteCount = toByteIndex(end - start);
        int available = getStorage().size();
        if (available < fileStart + byteCount) {
            throw new ArrayIndexOutOfBoundsException ("Bytes from " + 
                fileStart + " to " + (fileStart + byteCount) + " requested, " +
                "but storage is only " + available + " bytes long");
        }
        try {
            return getStorage().getReadBuffer(fileStart, byteCount).asCharBuffer().toString();
        } catch (Exception e) {
            handleException (e);
            return new String(new char[end - start]);
        }
    }

    /**
     * Fetch a subrange of the file into an optionally preallocated character
     * array.
     */
     char[] subrange (int start, int end, char[] chars) {
         if (disposed || trouble) {
             //There is a breif window of opportunity for a window to display
             //a disposed document.  Should never appear on screen, but it can
             //be requested to calculate the preferred size this will
             //make sure it's noticable if it does.
             if (Controller.log) {
                 Controller.log (this + "  !!!!!REQUEST FOR SUBRANGE " + start + "-" + end + " AFTER OUTWRITER HAS BEEN DISPOSED!!");
                 
             }
             char[] msg = "THIS OUTPUT HAS BEEN DISPOSED! ".toCharArray();
             if (chars == null) {
                 chars = new char[end - start];
             }
             int pos = 0;
             for (int i=0; i < chars.length; i++) {
                 if (pos == msg.length - 1) {
                     pos = 0;
                 }
                 chars[i] = msg[pos];
                 pos++;
             }
             return chars;
         }
        if (end < start) {
            throw new IllegalArgumentException ("Illogical text range from " + 
                start + " to " + end);
        }
        
        int fileStart = toByteIndex(start);
        int byteCount = toByteIndex(end - start);
        if (chars.length < end - start) {
            chars = new char[end-start];
        }
        try {
            getStorage().getReadBuffer(fileStart, byteCount).asCharBuffer().get(chars, 0, end - start);
            return chars;
        } catch (Exception e) {
            handleException (e);
            return new char[0];
        }
    }

    /**
     * Get a single line as a string.
     */
    String line (int idx) throws IOException {
        if (disposed || trouble) {
            return ""; //NOI18N
        }
        int lineStart = lineStartList.get(idx);
        int lineEnd;
        if (idx != lineStartList.size()-1) {
            lineEnd = lineStartList.get(idx+1);
        } else {
            lineEnd = getStorage().size();
        }
        CharBuffer cb = getStorage().getReadBuffer(lineStart, 
            lineEnd - lineStart).asCharBuffer();

        char chars[] = new char[cb.limit()];
        cb.get (chars);
        return new String (chars);
    }
    
    boolean isLineStart (int chpos) {
        int bpos = toByteIndex(chpos);
        return lineStartList.contains (bpos);
    }


    /**
     * Returns the length of the specified lin characters.
     *
     * @param idx A line number
     * @return The number of characters
     */
    public int length (int idx) {
        if (disposed || trouble) {
            return 0;
        }
        int lineStart = lineStartList.get(idx);
        int lineEnd;
        if (idx != lineStartList.size()-1) {
            lineEnd = lineStartList.get(idx+1);
        } else {
            int result;
            synchronized (this) {
                result = getStorage().size();
            }
            lineEnd = result;
        }
        return toCharIndex(lineEnd - lineStart);
    }



    /**
     * Get the <strong>character</strong> index of a line as a position in
     * the output file.
     */
    public int positionOfLine (int line) {
        if (disposed || trouble) return 0;
        return toCharIndex(lineStartList.get(line));
    }

    /**
     * Get the number of characters that have been written to the file.
     */
    public int charsWritten() {
        if (disposed || trouble) return 0;
        return toCharIndex(getStorage().size());
    }
    
    /** Get the line number of a <strong>character</strong> index in the
     * file (as distinct from a byte position) 
     */
    public int lineForPosition (int position) {
        if (disposed || trouble) return -1;
        int bytePos = toByteIndex (position);
        int i = lineStartList.indexOf (bytePos);
        if (i != -1) {
            return i;
        }
        i = lineStartList.findNearest(bytePos);
        return i;
    }
    
    public int lineCount() {
        if (disposed || trouble) return 0;
        return lineStartList.size();
    }
    
    public void run() {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
    
    public OutputListener listenerForLine (int line) {
        return (OutputListener) linesToListeners.get(line);
    }
    
    public int nextListenerLine (int curr) {
        if (disposed || trouble) return -1;
        return linesToListeners.nearest(curr, false);
    }
    
    public int prevListenerLine (int curr) {
        if (disposed || trouble) return -1;
        return linesToListeners.nearest(curr, true);
    }
    
    public int firstListenerLine () {
        if (disposed || trouble) return -1;
        return linesToListeners.isEmpty() ? -1 : linesToListeners.first();
    }
    
    public int nearestListenerTo (int line, boolean backward) {
        if (disposed || trouble) return -1;
        return linesToListeners.nearest (line, backward);
    }
    
    public synchronized void println(String s, OutputListener l) throws java.io.IOException {
        if (checkError() || disposed || trouble) {
            return;
        }
        int lines = doPrintln (s);
        if (lines == 1) {
            linesToListeners.put (lineCount() - 1, l);
        } else {
            int newCount = lineCount();
            for (int i=newCount - lines; i < newCount; i++) {
                linesToListeners.put (i, l);
            }
        }
    }
    
    public synchronized void println(String s) {
        if (checkError()) {
            return;
        }
        doPrintln(s);
    }
    
    private int doPrintln (String s) {
        try {
            int idx = s.indexOf("\n");
            int result = 1;
            if (idx != -1) { //XXX platform specific line sep?
                //XXX this can be much more efficient by slicing buffers
                StringTokenizer tok = new StringTokenizer (s, "\n"); //NOI18N
                result = 0;
                while (tok.hasMoreTokens()) {
                    doPrintln(tok.nextToken());
                    result++;
                }
            } else {
                ByteBuffer buf;
                synchronized (this) {
                    buf = getStorage().getWriteBuffer(toByteIndex(s.length()));
                }
                buf.asCharBuffer().put(s);
                buf.position (buf.position() + toByteIndex(s.length()));
                write (buf);
            }
            return result;
        } catch (IOException ioe) {
            handleException (ioe);
            return 0;
        }
    }
    
    private void fire() {
        if (disposed || trouble) return;
        if (Controller.log) Controller.log (this + ": Writer firing " + getStorage().size() + " bytes written");
        if (listener != null) {
            Mutex.EVENT.readAccess(this);
        }
    }

    boolean peekDirty() {
        return dirty;
    }
    

    public synchronized void flush() {
        if (checkError() || disposed || trouble) {
            return;
        }
        try {
            getStorage().flush();
            fire();
        } catch (IOException e) {
            handleException (e);
        }
    }

    synchronized void errClosed() {
        closesRequired--;
        if (Controller.log) {
            Controller.log (this + ": Error close");
        }
        if (closesRequired == 0) {
            doClose();
        }
    }

    public synchronized void close() {
        if (unused) {
            doClose();
            return;
        }
        if (checkError() || disposed || trouble) {
            return;
        }
        closesRequired--;
        if (Controller.log) {
            Controller.log (this + ": Output close - remaining streams to close: " + closesRequired);
        }
        if (closesRequired == 0) {
            if (Controller.log) {
                Controller.log (this + ": OUTPUT CLOSE ");
                Controller.logStack();
            }
            doClose();
        }
    }

    public synchronized void doClose() {
        try {
            if (Controller.log) {
                Controller.log (this + ": DO CLOSE: both streams closed, OutWriter is defunct");
            }
            getStorage().close();
            closesRequired = 0;
            markDirty();
            fire();
            if (owner != null) {
                owner.setStreamClosed(true);
            }
        } catch (IOException ioe) {
            handleException (ioe);
        }
    }


    public boolean checkError() {
        updateCloseCount();
        return trouble | cleared;
    }

    protected void setError() {
        trouble = true;
    }

    public synchronized void write(int c) {
        if (checkError()) {
            return;
        }
        try {
            ByteBuffer buf = getStorage().getWriteBuffer(1);
            buf.position (buf.position() + toByteIndex(1));
            if (c == '\n') {
                write(buf);
            }
        } catch (IOException ioe) {
            handleException (ioe);
        }
    }

    public synchronized void write(char data[], int off, int len) {
        if (checkError()) {
            return;
        }
        int count = off;
        int start = off;
        while (count < len + off) {
            char curr = data[count];
            if (count == (off + len) -1 || curr == '\n') { //NOI18N
                println (new String(data, start, (count + 1 - start)));
                start = count;
            }
            count++;
        }
    }

    public synchronized void write(char data[]) {
        write (data, 0, data.length);
    }
    
    /** Generic exception handling, marking the error flag and notifying it with ErrorManager */
    void handleException (Exception e) {
        setError();
	    ErrorManager em = ErrorManager.getDefault();
        if (em.findAnnotations(e) == null || em.findAnnotations(e).length == 0) {
            em.annotate(e, NbBundle.getMessage(OutWriter.class, 
                "MSG_GenericError")); //NOI18N
        }
        if (Controller.log) {
            StackTraceElement[] el = e.getStackTrace();
            Controller.log ("EXCEPTION: " + e.getClass() + e.getMessage());
            for (int i=1; i < el.length; i++) {
                Controller.log (el[i].toString());
            }
        }
        em.notify(e);
    }

    /**
     * Should be called by any method which modifies the contents of the 
     * getWriteBuffer file.
     */
    private void markDirty() {
        dirty = true;
    }
    
    /**
     * Allows clients that wish to poll to see if there is new output to do
     * so.  When any thread writes to the output, the dirty flag is set.
     * Calling this method returns its current value and clears it.  If it 
     * returns true, a view of the data may need to repaint itself or something
     * such.  This mechanism can be used in preference to listener based 
     * notification, by running a timer to poll as long as the output is 
     * open, for cases where otherwise the event queue would be flooded with
     * notifications for small writes.
     */
    public boolean checkDirty() {
        if (disposed || trouble) return false;
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }
    
    /** 
     * Write a portion of a string. 
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public synchronized void write(String s, int off, int len) {
        write (s.toCharArray(), off, len);
    }

    public synchronized void write(String s) {
        write (s.toCharArray());
    }

    /**
     * Add a change listener which will be notified on first write, flush and close operations.
     * The added listener must be able to handle changes being fired on any thread - change
     * firing is synchronous and not thread safe.
     *
     * @param cl A change listener
     * @throws TooManyListenersException If more than one listener is added
     */
    public void addChangeListener (ChangeListener cl) throws TooManyListenersException {
        if (listener != null && listener != cl) {
//            throw new TooManyListenersException();
        }
        this.listener = cl;
    }

    /**
     * Remove a change listener
     *
     * @param cl A change listener
     */
    public void removeChangeListener (ChangeListener cl) {
        if (listener == cl) {
            listener = null;
        }
    }

    /**
     * Save the contents of the buffer to a file, in UTF-8 encoding.
     *
     * @param path The file to save to
     * @param replace Whether the file should be overwritten
     * @throws IOException If there is a problem writing or encoding the data, or if overwrite is false and the
     *    specified file exists
     */
    public synchronized void saveAs(String path, boolean replace) throws IOException {
        if (storage == null) {
            throw new IOException ("Data has already been disposed"); //NOI18N
        }
        File f = new File (path);
        if (f.exists() && !replace) {
                throw new IOException ("File " + path + " exists"); //NOI18N
        }
        CharBuffer cb = getStorage().getReadBuffer(0, getStorage().size()).asCharBuffer();

        FileOutputStream fos = new FileOutputStream (f);
        String encoding = System.getProperty ("file.encoding"); //NOI18N
        if (encoding == null) {
            encoding = "UTF-8"; //NOI18N
        }
        Charset charset = Charset.forName (encoding); //NOI18N
        CharsetEncoder encoder = charset.newEncoder ();
        ByteBuffer bb = encoder.encode (cb);
        FileChannel ch = fos.getChannel();
        ch.write(bb);
        ch.close();
    }

    /**
     * Write the passed buffer to the backing storage, recording the line start in the mapping of lines to
     * byte offsets.
     *
     * @param bb
     * @throws IOException
     */
    public synchronized void write(ByteBuffer bb) throws IOException {
        if (checkError()) {
            return;
        }
        unused = false;
        lastWrappedLineCount = -1;
        lastCharCountForWrapAboveCalculation = -1;
        markDirty();
        matcher = null;
        
        longestLine = Math.max (longestLine, bb.limit());
        try {
            int start = getStorage().write(bb);
            lineStartList.add (start);
        
            if (Controller.verbose) Controller.log (this + ": Wrote " + ((ByteBuffer)bb.flip()).asCharBuffer() + " at " + start);
            if (lineStartList.size() == 20 || lineStartList.size() == 10 || lineStartList.size() == 1) { //Fire again after the first 20 lines
                if (Controller.log) Controller.log ("Firing initial write event");
                fire();
            }
        } catch (java.nio.channels.ClosedByInterruptException cbie) {
            //Execution termination has sent ThreadDeath to the process in the
            //middle of a write
            close();
        }
    }
    
    public int getLongestLineLength() {
        return toCharIndex(longestLine);
    }
    
    /**
     * Get the number of logical lines if character wrapped at the specified
     * width.  Calculates on the fly below 100000 characters, above that builds
     * a cache on the first call and uses that.
     */
    public int getLogicalLineCountAbove (int line, int charCount) {
        if (getStorage().size() < 100000 || !storage.isClosed()) {
            return dynLogicalLineCountAbove(line, charCount);
        } else {
            return cachedLogicalLineCountAbove (line, charCount);
        }
    }
    
    /**
     * Get the number of logical lines above a given physical line if character 
     * wrapped at the specified
     * width.  Calculates on the fly below 100000 characters, above that builds
     * a cache on the first call and uses that.
     */
    public int getLogicalLineCountIfWrappedAt (int charCount) {
        if (getStorage().size() < 100000 || !storage.isClosed()) {
            return dynLogicalLineCountIfWrappedAt(charCount);
        } else {
            return cachedLogicalLineCountIfWrappedAt(charCount);
        }
    }
    
    int knownCharCount = -1;
    private int cachedLogicalLineCountAbove (int line, int charCount) {
        if (charCount != knownCharCount) {
            knownCharCount = charCount;
            calcCharCounts(charCount);
        }
        return line == 0 ? 0 : knownLogicalLineCounts.get(line-1);
    }
    
    private int cachedLogicalLineCountIfWrappedAt (int charCount) {
        if (charCount == 0) {
            return 0;
        }
        if (charCount != knownCharCount ) {
            knownCharCount = charCount;
            calcCharCounts(charCount);
        }
        int lineCount = lineCount();
        int result = knownLogicalLineCounts.get(lineCount-1);
        int len = length (lineCount - 1);
        result += (len / charCount) + 1;
        return result;
    }
    
    private void calcCharCounts(int width) {
        int max = lineStartList.size();
        knownLogicalLineCounts = new IntList(max);
        knownLogicalLineCounts.add(0);
        
        int bcount = toByteIndex(width);
        
        int prev = 0;
        int ct = 1;
        for (int i=1; i < max; i++) {
            int curr = lineStartList.get(i);
            if (curr - prev > bcount) {
                ct += ((curr - prev) / bcount) + 1;
            } else {
                ct++;
            }
            prev = curr;
            knownLogicalLineCounts.add(ct);
        }        
    }
    
    private IntList knownLogicalLineCounts = null;
    
    
    private int lastCharCountForWrapCalculation = -1;
    private int lastWrappedLineCount = -1;
    /**
     * Gets the number of lines the document will require if line wrapped at the
     * specified character index.
     */

    private int dynLogicalLineCountIfWrappedAt (int charCount) {
        int bcount = toByteIndex(charCount);
        if (longestLine <= bcount) {
            return lineStartList.size();
        }
        if (charCount == lastCharCountForWrapCalculation && lastWrappedLineCount != -1) {
            return lastWrappedLineCount;
        }
        if (lineStartList.size() == 0) {
            return 0;
        }
        int max = lineStartList.size();
        int prev = 0;
        lastWrappedLineCount = 1;
        for (int i=1; i < max; i++) {
            int curr = lineStartList.get(i);
            if (curr - prev > bcount) {
                lastWrappedLineCount += ((curr - prev) / bcount) + 1;
            } else {
                lastWrappedLineCount++;
            }
            prev = curr;
        }
        lastCharCountForWrapCalculation = charCount;
        return lastWrappedLineCount;
    }

    private int lastCharCountForWrapAboveCalculation = -1;
    private int lastWrappedAboveLineCount = -1;
    private int lastWrappedAboveLine = -1;
    /**
     * Gets the number of lines that occur *above* a given line if wrapped at the
     * specified char count.
     *
     * @param line The line in question
     * @param charCount The number of characters at which to wrap
     * @return The number of logical wrapped lines above the passed line
     */
    private int dynLogicalLineCountAbove (int line, int charCount) {
        int lineCount = lineCount();
        if (line == 0 || lineCount == 0) {
            return 0;
        }
        int bcount = toByteIndex(charCount);
        if (charCount == lastCharCountForWrapAboveCalculation && lastWrappedAboveLineCount != -1 && line == lastWrappedAboveLine) {
            return lastWrappedAboveLineCount;
        }

        lastWrappedAboveLineCount = 0;

        for (int i=0; i < line; i++) {
            int len = length(i);
            if (len > charCount) {
                lastWrappedAboveLineCount += (len / charCount) + 1;
            } else {
                lastWrappedAboveLineCount++;
            }
        }
        int lineLen = length(line);
        if (lineLen > charCount) {

        }

        lastWrappedAboveLine = line;
        lastCharCountForWrapAboveCalculation = charCount;
        return lastWrappedAboveLineCount;
    }

    public boolean isCleared() {
        return cleared;
    }

    private boolean cleared = false;
    public void clear() {
        if (Controller.log) Controller.log ("OutWriter.clear " + this);
        clearListeners();
        cleared = true;
        if (storage != null) {
            storage.dispose();
        }
        listener = null;
        init();
    }

    /**
     * Dispose this writer.  If reuse is true, the underlying storage will be disposed, but the
     * OutWriter will still be usable.  If reuse if false, note that any current ChangeListener is cleared.
     *
     */
    public void dispose() {
        if (Controller.log) Controller.log (this + ": OutWriter.dispose - owner is " + (owner == null ? "null" : owner.getName()));
        clearListeners();
        if (storage != null) {
            storage.dispose();
        }
        knownLogicalLineCounts = null;
        trouble = true;
        listener = null;
        if (Controller.log) Controller.log (this + ": Setting owner to null, trouble to true, dirty to false.  This OutWriter is officially dead.");
        owner = null;
        dirty = false;
        matcher = null;
        disposed = true;
//        listener = null;
    }
    private boolean disposed = false;

    /**
     * Do a reverse search for the last matched text.  Using this is a little tricky -
     * since there is no direct support for reverse searches in java.util.regex, what
     * we do is take the last pattern and the entire buffer text, and reverse them,
     * and produce a matcher based on that.
     * <p>
     * What this means is that the caller gets the job of flipping things back around,
     * as follows:
     * <pre>
     * int matchStart = getLength() - matcher.end();
     * int matchEnd = getLength() - matcher.start()
     *
     * will return the actual positions in the data of the match.
     *
     * @return A matcher over a reversed version of the data
     */
    public Matcher getReverseMatcher() {
        try {
            if (matcher != null && lastSearchString != null && lastSearchString.length() > 0 && storage.size() > 0) {
                StringBuffer sb = new StringBuffer (lastSearchString);
                sb.reverse();
                CharBuffer buf = storage.getReadBuffer(0, storage.size()).asCharBuffer();
                //This could be very slow for large amounts of data
                StringBuffer data = new StringBuffer (buf.toString());
                data.reverse();

                Pattern pat = Pattern.compile (sb.toString());
                return pat.matcher(data);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    private String lastSearchString = null;
    private Matcher matcher = null;
    /**
     * Get a regular expression matcher over the backing storage.  Note that the resulting Matcher object
     * should not be held across reset or other events that can destroy the contents of the buffer.
     *
     * @param s A pattern as defined in javax.regex
     * @return A pattern matcher object, or null if the pattern is invalid or there is a problem with the
     *   backing storage
     */
    public Matcher find(String s) {
        if (Controller.log) Controller.log (this + ": Executing find for string " + s + " on " + owner.getName());
        if (storage == null) {
            return null;
        }
        if (matcher != null && s.equals(lastSearchString)) {
            return matcher;
        }
        try {
            int size = storage.size();
            if (size > 0) {
                Pattern pat = Pattern.compile (s, Pattern.CASE_INSENSITIVE);
                CharBuffer buf = storage.getReadBuffer(0, size).asCharBuffer();
                Matcher m = pat.matcher(buf);
                if (!m.find(0)) {
                    return null;
                }
                matcher = m;
                lastSearchString = s;
                return matcher;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return null;
    }

    /**
     * Reset this OutWriter, disposing of the backing storage.  Will call NbIO.reset() on the owning
     * instance of NbIO if not null, and fire a change event.
     *
     * @throws IOException
     */
    public synchronized void reset() throws IOException {
        if (unused) {
            if (Controller.log) Controller.log ("Reset on an unused IO.  Ignoring - " + this);
            return;
        }
        if (Controller.log) Controller.log (this + ": OutWriter reset for " + owner.getName());
        clearListeners();
        int oldSize;
        cleared = false;
        knownLogicalLineCounts = null;
        
        if (storage != null) {
            closesRequired = err != null ? 2 : 1;
            oldSize = storage.size();
            if (oldSize > 0) {
                if (owner != null) {
                    owner.reset(false);
                }
                storage.dispose();
                storage = null;
            }
            init();
        }
    }

    private void clearListeners() {
        if (Controller.log) Controller.log (this + ": Sending outputLineCleared to all listeners");
        if (owner == null) {
            //Somebody called reset() twice
            return;
        }
        if (!linesToListeners.isEmpty()) {
            int[] keys = linesToListeners.getKeys();
            Controller.ControllerOutputEvent e = new Controller.ControllerOutputEvent(owner, 0);
            for (int i=0; i < keys.length; i++) {
                OutputListener ol = (OutputListener) linesToListeners.get(keys[i]);
                if (Controller.log) {
                    Controller.log("Clearing listener " + ol);
                }
                e.setLine(keys[i]);
                ol.outputLineCleared(e);
            }
        } else {
            if (Controller.log) Controller.log (this + ": No listeners to clear");
        }
    }

    /** Convert an index from chars to byte count (*2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toByteIndex (int charIndex) {
        return charIndex * 2;
    }

    /** Convert an index from bytes to chars (/2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toCharIndex (int byteIndex) {
        assert byteIndex % 2 == 0 : "bad index: " + byteIndex;  //NOI18N
        int result = byteIndex / 2;
        return result;
    }

    public synchronized boolean isClosed() {
        return disposed || storage == null || (storage.isClosed() && closesRequired == 0);
    }

    /**
     * A useless writer object to pass to the superclass constructor.  We override all methods
     * of it anyway.
     */
    static class DummyWriter extends Writer {
        
        public DummyWriter() {
            super (new Object());
        }
        
        public void close() throws IOException {
        }
        
        public void flush() throws IOException {
        }
        
        public void write(char[] cbuf, int off, int len) throws IOException {
        }
    }
}
