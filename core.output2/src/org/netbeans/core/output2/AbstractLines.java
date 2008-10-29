/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.core.output2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.windows.OutputListener;

/**
 * Abstract Lines implementation with handling for getLine wrap calculations, etc.
 */
abstract class AbstractLines implements Lines, Runnable, ActionListener {
    /** A collections-like lineStartList that maps file positions to getLine numbers */
    IntList lineStartList;
    /** Maps output listeners to the lines they are associated with */
    IntMap linesToListeners;

    /** longest line length (in chars)*/
    private int longestLineLen = 0;

    private int knownCharsPerLine = -1;
    
    /** cache of logical (wrapped) lines count, used to transform logical (wrapped)
     * line index to physical (real) line index */
    private SparseIntList knownLogicalLineCounts = null;
    private IntList errLines = null;


    AbstractLines() {
        if (Controller.LOG) Controller.log ("Creating a new AbstractLines");
        init();
    }

    protected abstract Storage getStorage();

    protected abstract boolean isDisposed();

    protected abstract boolean isTrouble();

    protected abstract void handleException (Exception e);

    public char[] getText (int start, int end, char[] chars) {
        if (isDisposed() || isTrouble()) {
             //There is a breif window of opportunity for a window to display
             //a disposed document.  Should never appear on screen, but it can
             //be requested to calculate the preferred size this will
             //make sure it's noticable if it does.
             if (Controller.LOG) {
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
        synchronized(readLock()) {
            int fileStart = AbstractLines.toByteIndex(start);
            int byteCount = AbstractLines.toByteIndex(end - start);
            try {
                CharBuffer chb = getStorage().getReadBuffer(fileStart, byteCount).asCharBuffer();
                //#68386 satisfy the request as much as possible, but if there's not enough remaining
                // content, not much we can do..
                int len = Math.min(end - start, chb.remaining());
                if (chars.length < len) {
                    chars = new char[len];
                }
                chb.get(chars, 0, len);
                return chars;
            } catch (Exception e) {
                handleException (e);
                return new char[0];
            }
        }
    }

    public String getText (int start, int end) {
        if (isDisposed() || isTrouble()) {
            return new String (new char[end - start]);
        }
        if (end < start) {
            throw new IllegalArgumentException ("Illogical text range from " +
                start + " to " + end);
        }
        synchronized(readLock()) {
            int fileStart = AbstractLines.toByteIndex(start);
            int byteCount = AbstractLines.toByteIndex(end - start);
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
    }

    private int lastErrLineMarked = -1;
    void markErr() {
        if (isTrouble() || getStorage().isClosed()) {
            return;
        }
        if (errLines == null) {
            errLines = new IntList(20);
        }
        int linecount = getLineCount();
        //Check this - for calls to OutputWriter.write(byte b), we may still be on the same line as last time
        if (linecount != lastErrLineMarked) {
            errLines.add(linecount == 0 ? 0 : linecount - (isLastLineFinished() ? 2 : 1));
            lastErrLineMarked = linecount;
        }
    }

    public boolean isErr(int line) {
        return errLines != null ? errLines.contains(line) : false;
    }

    private ChangeListener listener = null;
    public void addChangeListener(ChangeListener cl) {
        this.listener = cl;
        synchronized(this) {
            if (getLineCount() > 0) {
                //May be a new tab for an old output, hide and reshow, etc.
                fire();
            }
        }
    }

    public void removeChangeListener (ChangeListener cl) {
        if (listener == cl) {
            listener = null;
        }
    }

    private javax.swing.Timer timer = null;
    private AtomicBoolean newEvent = new AtomicBoolean(false);

    public void actionPerformed(ActionEvent e) {
        newEvent.set(false);
        fire();
        synchronized (newEvent) {
            if (!newEvent.get()) {
                timer.stop();
            }
        }
    }
    
    void delayedFire() {
        newEvent.set(true);
        if (listener == null) {
            return;
        }
        if (timer == null) {
            timer = new javax.swing.Timer(200, this);
        }
        
        synchronized (newEvent) {
            if (newEvent.get() && !timer.isRunning()) {
                timer.start();
            }
        }
    }

    public void fire() {
        if (isTrouble()) {
            return;
        }
        if (Controller.LOG) Controller.log (this + ": Writer firing " + getStorage().size() + " bytes written");
        if (listener != null) {
            Mutex.EVENT.readAccess(this);
        }
    }

    public void run() {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    public boolean hasHyperlinks() {
        return firstListenerLine() != -1;
    }

    public boolean isHyperlink (int line) {
        return getListenerForLine(line) != null;
    }

    private void init() {
        knownLogicalLineCounts = null;
        lineStartList = new IntList(100);
        lineStartList.add(0);
        linesToListeners = new IntMap();
        longestLineLen = 0;
        errLines = null;
        matcher = null;
        listener = null;
        dirty = false;
    }

    private boolean dirty;

    public boolean checkDirty(boolean clear) {
        if (isTrouble()) {
            return false;
        }
        boolean wasDirty = dirty;
        if (clear) {
            dirty = false;
        }
        return wasDirty;
    }

    public int[] allListenerLines() {
        return linesToListeners.getKeys();
    }

    void clear() {
        init();
    }

    public int getCharCount() {
        if (isDisposed() || isTrouble()) {
            return 0;
        }
        Storage storage = getStorage();
        return storage == null ? 0 : AbstractLines.toCharIndex(getStorage().size());
    }

    /**
     * Get a single getLine as a string.
     */
    public String getLine (int idx) throws IOException {
        if (isDisposed() || isTrouble()) {
            return ""; //NOI18N
        }
        int lineStart = getByteLineStart(idx);
        int lineEnd = idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) : getStorage().size();
        CharBuffer cb = getStorage().getReadBuffer(lineStart,
            lineEnd - lineStart).asCharBuffer();

        char chars[] = new char[cb.limit()];
        cb.get (chars);
        return new String (chars);
    }

    /**
     * Get a length of single line in bytes.
     */
    private int getLineLength(int idx) {
        if (isDisposed() || isTrouble()) {
            return 0;
        }
        if (idx == lineStartList.size()-1) {
            return Math.max(0, toByteIndex(lastLineLength));
        }
        int lineStart = getByteLineStart(idx);
        int lineEnd = idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) - 2 : getStorage().size();
        return lineEnd - lineStart;
    }

    public boolean isLineStart (int chpos) {
        int bpos = toByteIndex(chpos);
        return lineStartList.contains (bpos) || bpos == 0 || (bpos == getStorage().size() && lastLineFinished);
    }

    /**
     * Returns the length of the specified lin characters.
     *
     * @param idx A getLine number
     * @return The number of characters
     */
    public int length (int idx) {
        return toCharIndex(getLineLength(idx));
    }

    /**
     * Get the <strong>character</strong> index of a getLine as a position in
     * the output file.
     */
    public int getLineStart (int line) {
        if (isDisposed() || isTrouble()) {
            return 0;
        }
        return getCharLineStart(line);
    }

    private int getByteLineStart(int line) {
        if (line == lineStartList.size() && lastLineFinished) {
            return getStorage().size();
        }
        return lineStartList.get(line);
    }

    private int getCharLineStart(int line) {
        return toCharIndex(getByteLineStart(line));
    }

    /** Get the getLine number of a <strong>character</strong> index in the
     * file (as distinct from a byte position)
     */
    public int getLineAt (int position) {
        if (isDisposed() || isTrouble()) {
            return -1;
        }
        int bytePos = toByteIndex (position);
        if (bytePos >= getStorage().size()) {
            return getLineCount() - 1;
        }
        return lineStartList.findNearest(bytePos);
    }

    public int getLineCount() {
        if (isDisposed() || isTrouble()) {
            return 0;
        }
        return lineStartList.size();
    }

    public OutputListener getListenerForLine (int line) {
        return (OutputListener) linesToListeners.get(line);
    }

    public int firstListenerLine () {
        if (isDisposed() || isTrouble()) {
            return -1;
        }
        return linesToListeners.isEmpty() ? -1 : linesToListeners.first();
    }

    public int nearestListenerLine (int line, boolean backward) {
        if (isDisposed() || isTrouble()) {
            return -1;
        }
        return linesToListeners.nearest (line, backward);
    }

    public int getLongestLineLength() {
        return longestLineLen;
    }

    /** recalculate logical (wrapped) line index to physical (real) line index
     *  <p> [in]
     *  info[0] - "global" logical (wrapped) line index
     *  <p> [out]
     *  info[0] - physical (real) line index;
     *  info[1] - index of wrapped line on current realLineIdx;
     *  info[2] - total number of wrapped lines of found physical line
     */
    public void toPhysicalLineIndex(final int[] info, int charsPerLine) {
        int logicalLineIdx = info[0];

        if (logicalLineIdx <= 0) {
            //First getLine never has lines above it
            info[0] = 0;
            info[1] = 0;
            info[2] = lengthToLineCount(length(0), charsPerLine);
            return;
        }

        if (charsPerLine >= longestLineLen || (getLineCount() < 1)) {
            //The doc is empty, or there are no lines long enough to wrap anyway
            info[1] = 0;
            info[2] = 1;
            return;
        }

        // find physical (real) line index which corresponds to logical line idx
        int physLineIdx = Math.min(findPhysicalLine(logicalLineIdx, charsPerLine), getLineCount() - 1);
        
        // compute how many logical lines is above our physical line
        int linesAbove = getLogicalLineCountAbove(physLineIdx, charsPerLine);
        
        int len = length(physLineIdx);
        int wrapCount = lengthToLineCount(len, charsPerLine);

        info[0] = physLineIdx;
        info[1] = logicalLineIdx - linesAbove;
        info[2] = wrapCount;
    }

    private int findPhysicalLine(int logicalLineIdx, int charsPerLine) {
        if (logicalLineIdx == 0) {
            return 0;
        }
        if (charsPerLine != knownCharsPerLine || knownLogicalLineCounts == null) {
            calcLogicalLineCount(charsPerLine);
        }
        return knownLogicalLineCounts.getNextKey(logicalLineIdx);
    }

    /**
     * Get the number of logical lines if character wrapped at the specified width.
     */
    public int getLogicalLineCountAbove(int line, int charsPerLine) {
        if (line == 0) {
            return 0;
        }
        if (charsPerLine >= longestLineLen) {
            return line;
        }
        if (charsPerLine != knownCharsPerLine || knownLogicalLineCounts == null) {
            calcLogicalLineCount(charsPerLine);
        }
        return knownLogicalLineCounts.get(line - 1);
    }

    /**
     * Get the number of logical lines above a given physical getLine if character
     * wrapped at the specified
     */
    public int getLogicalLineCountIfWrappedAt (int charsPerLine) {
        if (charsPerLine >= longestLineLen) {
            return getLineCount();
        }        
        int lineCount = getLineCount();
        if (charsPerLine == 0 || lineCount == 0) {
            return 0;
        }
        synchronized (readLock()) {
            if (charsPerLine != knownCharsPerLine || knownLogicalLineCounts == null) {
                calcLogicalLineCount(charsPerLine);
            }
            return knownLogicalLineCounts.get(lineCount-1);
        }
    }

    public void addListener (int line, OutputListener l, boolean important) {
        if (l == null) {
            //#56826 - debug messaging
            Logger.getLogger(AbstractLines.class.getName()).log(Level.WARNING, "Issue #56826 - Adding a null OutputListener for line: " + line, new NullPointerException());
        } else {
            linesToListeners.put(line, l);
            if (important) {
                importantLines.add(line);
            }
        }
    }
    
    private IntList importantLines = new IntList(10);
    
    public int firstImportantListenerLine() {
        return importantLines.size() == 0 ? -1 : importantLines.get(0);
    }
    
    public boolean isImportantHyperlink(int line) {
        return importantLines.contains(line);
    }
    
    /**
     * We use SparseIntList to create a cache which only actually holds 
     * the counts for lines that *are* wrapped, and interpolates the rest, so 
     * we don't need to create an int[]  as big as the number of lines we have.
     * This presumes that most lines don't wrap.
     */
    private void calcLogicalLineCount(int width) {
        synchronized (readLock()) {
            int lineCount = getLineCount();
            knownLogicalLineCounts = new SparseIntList(30);

            int val = 0;
            for (int i = 0; i < lineCount; i++) {
                int len = length(i);

                if (len > width) {
                    val += lengthToLineCount(len, width);
                    knownLogicalLineCounts.add(i, val);
                } else {
                    val++;
                }
            }
            knownCharsPerLine = width;
        }
    }

    static int lengthToLineCount(int len, int charsPerLine) {
        return len > charsPerLine ? (charsPerLine == 0 ? len : (len + charsPerLine - 1) / charsPerLine) : 1;
    }
    
    void markDirty() {
        dirty = true;
    }
    
    boolean isLastLineFinished() {
        return lastLineFinished;
    }
    
    private boolean lastLineFinished = true;
    private int lastLineLength = -1;

    private void updateLastLine(int lineIdx, int lineLength) {
        synchronized (readLock()) {
            longestLineLen = Math.max(longestLineLen, lineLength);
            if (knownLogicalLineCounts == null) {
                return;
            }
            // nummber of logical lines above for knownLogicalLineCounts
            int aboveLineCount;
            boolean alreadyAdded = knownLogicalLineCounts.lastIndex() == lineIdx;
            if (alreadyAdded) {
                assert lastLineFinished == false;
                if (lineLength <= knownCharsPerLine) {
                    knownLogicalLineCounts.removeLast();
                } else {
                    aboveLineCount = knownLogicalLineCounts.lastAdded()
                            - lengthToLineCount(lastLineLength, knownCharsPerLine)
                            + lengthToLineCount(lineLength, knownCharsPerLine);
                    knownLogicalLineCounts.updateLast(lineIdx, aboveLineCount);
                }
            } else {
                if (lineLength <= knownCharsPerLine) {
                    return;
                }
                if (knownLogicalLineCounts.lastIndex() != -1) {
                    //If the cache already has some entries, calculate the
                    //values from the last entry - this is less expensive
                    //than looking it up
                    aboveLineCount = (lineIdx - (knownLogicalLineCounts.lastIndex() + 1)) + knownLogicalLineCounts.lastAdded();
                } else {
                    //Otherwise, it's just the number of lines above this
                    //one - it's the first entry
                    aboveLineCount = Math.max(0, lineIdx-1);
                }
                //Add in the number of times this getLine will wrap
                aboveLineCount += lengthToLineCount(lineLength, knownCharsPerLine);
                knownLogicalLineCounts.add(lineIdx, aboveLineCount);
            }
        }
    }
    
    public void lineUpdated(int lineStart, int lineLength, boolean isFinished) {
        synchronized (readLock()) {
            int charLineLength = toCharIndex(lineLength);
            updateLastLine(lineStartList.size() - 1, charLineLength);
            if (isFinished) {
                lineStartList.add(lineStart + lineLength);
            }
            matcher = null;
            lastLineFinished = isFinished;
            lastLineLength = isFinished ? -1 : charLineLength;
        }
        markDirty();
    }
    
    /** Convert an index from chars to byte count (*2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toByteIndex (int charIndex) {
        return charIndex << 1;
    }

    /** Convert an index from bytes to chars (/2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toCharIndex (int byteIndex) {
        assert byteIndex % 2 == 0 : "bad index: " + byteIndex;  //NOI18N
        return byteIndex >> 1;
    }

    public void saveAs(String path) throws IOException {
        if (getStorage()== null) {
            throw new IOException ("Data has already been disposed"); //NOI18N
        }
        File f = new File (path);
        CharBuffer cb = getStorage().getReadBuffer(0, getStorage().size()).asCharBuffer();

        FileOutputStream fos = new FileOutputStream (f);
        try {
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
        } finally {
            fos.close();
        }
    }

    private String lastSearchString = null;
    private Matcher matcher = null;
    public Matcher getForwardMatcher() {
        return matcher;
    }

    public Matcher getReverseMatcher() {
        try {
            Storage storage = getStorage();
            if (storage == null) {
                return null;
            }
            if (matcher != null && lastSearchString != null && lastSearchString.length() > 0 && storage.size() > 0) {
                StringBuffer sb = new StringBuffer (lastSearchString);
                sb.reverse();
                CharBuffer buf = storage.getReadBuffer(0, storage.size()).asCharBuffer();
                //This could be very slow for large amounts of data
                StringBuffer data = new StringBuffer (buf.toString());
                data.reverse();

                Pattern pat = escapePattern(sb.toString());
                return pat.matcher(data);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    public Matcher find(String s) {
        if (Controller.LOG) Controller.log (this + ": Executing find for string " + s + " on " );
        Storage storage = getStorage();
        if (storage == null) {
            return null;
        }
        if (matcher != null && s.equals(lastSearchString)) {
            matcher.reset();
            return matcher;
        }
        try {
            int size = storage.size();
            if (size > 0) {
                Pattern pat = escapePattern(s);
                CharBuffer buf = storage.getReadBuffer(0, size).asCharBuffer();
                Matcher m = pat.matcher(buf);
                if (!m.find(0)) {
                    return null;
                }
                matcher = m;
                matcher.reset();
                lastSearchString = s;
                return matcher;
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }
    
    /**
     * escape all the special regexp characters to simulate plain search using regexp..
     *
     */ 
    static Pattern escapePattern(String s) {
        // fix for issue #50170, test for this method created, if necessary refine..
        // [jglick] Probably this would work as well and be a bit more readable:
        // String replacement = "\\Q" + s + "\\E";
        String replacement = s.replaceAll("([\\(\\)\\[\\]\\^\\*\\.\\$\\{\\}\\?\\+\\\\])", "\\\\$1");
        return Pattern.compile(replacement, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String toString() {
        return lineStartList.toString();
    }
}
