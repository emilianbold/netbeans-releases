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

import java.awt.Color;
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
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.windows.IOColors;
import org.openide.windows.OutputListener;

/**
 * Abstract Lines implementation with handling for getLine wrap calculations, etc.
 */
abstract class AbstractLines implements Lines, Runnable, ActionListener {
    /** A collections-like lineStartList that maps file positions to getLine numbers */
    IntList lineStartList;
    /** Maps output listeners to the lines they are associated with */
    IntMap lineWithListenerToInfo;

    /** line index to LineInfo */
    IntMap linesToInfos;

    /** longest line length (in chars)*/
    private int longestLineLen = 0;

    private int knownCharsPerLine = -1;

    /** cache of logical (wrapped) lines count, used to transform logical (wrapped)
     * line index to physical (real) line index */
    private SparseIntList knownLogicalLineCounts = null;

    /** last storage size (after dispose), in bytes */
    private int lastStorageSize = -1;

    AbstractLines() {
        if (Controller.LOG) Controller.log ("Creating a new AbstractLines");
        init();
    }

    protected abstract Storage getStorage();

    protected abstract boolean isDisposed();

    protected abstract void handleException (Exception e);

    public char[] getText(int start, int end, char[] chars) {
        if (chars == null) {
            chars = new char[end - start];
        }
        if (end < start || start < 0) {
            throw new IllegalArgumentException ("Illogical text range from " + start + " to " + end);
        }
        if (end - start > chars.length) {
            throw new IllegalArgumentException("Array size is too small");
        }
        synchronized(readLock()) {
            if (isDisposed()) {
                // dispose is performed asynchronously, data may be required by
                // events fired before (during) dispose(), return just zeros
                // (output will be cleared soon anyway)
                for (int i = 0; i < end - start; i++) {
                    chars[i] = 0;
                }
                return chars;
            }
            int fileStart = toByteIndex(start);
            int byteCount = toByteIndex(end - start);
            try {
                CharBuffer chb = getStorage().getReadBuffer(fileStart, byteCount).asCharBuffer();
                //#68386 satisfy the request as much as possible, but if there's not enough remaining
                // content, not much we can do..
                int len = Math.min(end - start, chb.remaining());
                chb.get(chars, 0, len);
                return chars;
            } catch (Exception e) {
                handleException (e);
                return new char[0];
            }
        }
    }

    public String getText (int start, int end) {
        if (end < start || start < 0) {
            throw new IllegalArgumentException ("Illogical text range from " + start + " to " + end);
        }
        synchronized(readLock()) {
            if (isDisposed()) {
                // dispose is performed asynchronously, data may be required by
                // events fired before (during) dispose(), return just zeros
                // (output will be cleared soon anyway)
                return new String(new char[end - start]);
            }
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

    void onDispose(int lastStorageSize) {
        this.lastStorageSize = lastStorageSize;
    }

    int getByteSize() {
        synchronized (readLock()) {
            if (lastStorageSize >= 0) {
                return lastStorageSize;
            }
            Storage storage = getStorage();
            return storage == null ? 0 : storage.size();
        }
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
    private final AtomicBoolean newEvent = new AtomicBoolean(false);

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

    public boolean hasListeners() {
        return firstListenerLine() != -1;
    }

    public OutputListener getListener(int pos, int[] range) {
        int line = getLineAt(pos);
        int lineStart = getLineStart(line);
        pos -= lineStart;
        LineInfo info = (LineInfo) lineWithListenerToInfo.get(line);
        if (info == null) {
            return null;
        }
        int start = 0;
        for (LineInfo.Segment seg : info.getLineSegments()) {
            if (pos < seg.getEnd()) {
                if (seg.getListener() != null) {
                    if (range != null) {
                        range[0] = lineStart + start;
                        range[1] = lineStart + seg.getEnd();
                    }
                    return seg.getListener();
                } else {
                    return null;
                }
            }
            start = seg.getEnd();
        }
        return null;
    }

    public boolean isListener(int start, int end) {
        int[] range = new int[2];
        OutputListener l = getListener(start, range);
        return l == null ? false : (range[0] == start && range[1] == end);
    }

    private void init() {
        knownLogicalLineCounts = null;
        lineStartList = new IntList(100);
        lineStartList.add(0);
        linesToInfos = new IntMap();
        lineWithListenerToInfo = new IntMap();
        longestLineLen = 0;
        listener = null;
        dirty = false;
        curDefColors = DEF_COLORS.clone();
    }

    private boolean dirty;

    public boolean checkDirty(boolean clear) {
        if (isDisposed()) {
            return false;
        }
        boolean wasDirty = dirty;
        if (clear) {
            dirty = false;
        }
        return wasDirty;
    }

    public int[] getLinesWithListeners() {
        return lineWithListenerToInfo.getKeys();
    }

    public int getCharCount() {
        return AbstractLines.toCharIndex(getByteSize());
    }

    /**
     * Get a single getLine as a string.
     */
    public String getLine (int idx) throws IOException {
        int lineStart = getCharLineStart(idx);
        int lineEnd = toCharIndex(idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) : getByteSize());
        return getText(lineStart, lineEnd);
    }

    /**
     * Get a length of single line in bytes.
     */
    private int getByteLineLength(int idx) {
        if (idx == lineStartList.size()-1) {
            return Math.max(0, toByteIndex(lastLineLength));
        }
        int lineStart = getByteLineStart(idx);
        int lineEnd = idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) - 2 * OutWriter.LINE_SEPARATOR.length() : getByteSize();
        return lineEnd - lineStart;
    }

    public boolean isLineStart (int chpos) {
        int bpos = toByteIndex(chpos);
        return lineStartList.contains (bpos) || bpos == 0 || (bpos == getByteSize() && lastLineFinished);
    }

    /**
     * Returns the length of the specified lin characters.
     *
     * @param idx A getLine number
     * @return The number of characters
     */
    public int length (int idx) {
        return toCharIndex(getByteLineLength(idx));
    }

    /**
     * Get the <strong>character</strong> index of a getLine as a position in
     * the output file.
     */
    public int getLineStart (int line) {
        return getCharLineStart(line);
    }

    private int getByteLineStart(int line) {
        if (line == lineStartList.size() && lastLineFinished) {
            return getByteSize();
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
        int bytePos = toByteIndex (position);
        if (bytePos >= getByteSize()) {
            return getLineCount() - 1;
        }
        return lineStartList.findNearest(bytePos);
    }

    public int getLineCount() {
        return lineStartList.size();
    }

    public Collection<OutputListener> getListenersForLine(int line) {
        LineInfo info = (LineInfo) lineWithListenerToInfo.get(line);
        if (info == null) {
            return Collections.emptyList();
        }
        return info.getListeners();
    }

    public int firstListenerLine () {
        if (isDisposed()) {
            return -1;
        }
        return lineWithListenerToInfo.isEmpty() ? -1 : lineWithListenerToInfo.first();
    }

    public OutputListener nearestListener(int pos, boolean backward, int[] range) {
        int posLine = getLineAt(pos);
        int line = lineWithListenerToInfo.nearest(posLine, backward);
        if (line < 0) {
            return null;
        }
        LineInfo info = (LineInfo) lineWithListenerToInfo.get(line);
        int lineStart = getLineStart(line);
        OutputListener l = null;
        int[] lpos = new int[2];
        if (posLine == line) {
            if (backward) {
                info.getFirstListener(lpos);
                if (lpos[0] + lineStart > pos) {
                    line = lineWithListenerToInfo.nearest(line - 1, backward);
                    info = (LineInfo) lineWithListenerToInfo.get(line);
                    lineStart = getLineStart(line);
                    l = info.getLastListener(lpos);
                }
            } else {
                info.getLastListener(lpos);
                if (lpos[1] + lineStart <= pos) {
                    line = lineWithListenerToInfo.nearest(line + 1, backward);
                    info = (LineInfo) lineWithListenerToInfo.get(line);
                    lineStart = getLineStart(line);
                    l = info.getFirstListener(lpos);
                }
            }
        } else {
            pos = lineStart;
            l = backward ? info.getLastListener(lpos) : info.getFirstListener(lpos);
        }
        if (l == null) {
            l = backward ? info.getListenerBefore(pos - lineStart, lpos) : info.getListenerAfter(pos - lineStart, lpos);
        }
        if (l != null) {
            range[0] = lpos[0] + lineStart;
            range[1] = lpos[1] + lineStart;
        }
        return l;
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

    private void registerLineWithListener(int line, LineInfo info, boolean important) {
        lineWithListenerToInfo.put(line, info);
        if (important) {
            importantLines.add(line);
        }
    }

    private IntList importantLines = new IntList(10);

    public int firstImportantListenerLine() {
        return importantLines.size() == 0 ? -1 : importantLines.get(0);
    }

    public boolean isImportantLine(int line) {
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
            if (isFinished) {
                charLineLength -= 1;
            }
            updateLastLine(lineStartList.size() - 1, charLineLength);
            if (isFinished) {
                lineStartList.add(lineStart + lineLength);
            }
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
        Storage storage = getStorage();
        if (storage == null) {
            throw new IOException ("Data has already been disposed"); //NOI18N
        }
        File f = new File (path);
        CharBuffer cb = storage.getReadBuffer(0, storage.size()).asCharBuffer();

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

    /** initial default colors */
    static final Color[] DEF_COLORS;

    /** current default colors */
    Color[] curDefColors;

    static {
        Color out = UIManager.getColor("nb.output.foreground"); //NOI18N
        if (out == null) {
            out = UIManager.getColor("textText");
            if (out == null) {
                out = Color.BLACK;
            }
        }

        Color err = UIManager.getColor("nb.output.err.foreground"); //NOI18N
        if (err == null) {
            err = new Color(164, 0, 0);
        }

        Color hyperlink = UIManager.getColor("nb.output.link.foreground"); //NOI18N
        if (hyperlink == null) {
            hyperlink = Color.BLUE.darker();
        }

        Color hyperlinkImp = UIManager.getColor("nb.output.link.foreground.important"); //NOI18N
        if (hyperlinkImp == null) {
            hyperlinkImp = hyperlink;
        }

        DEF_COLORS = new Color[]{out, err, hyperlink, hyperlinkImp};
    }

    public void setDefColor(IOColors.OutputType type, Color color) {
        curDefColors[type.ordinal()] = color;
    }

    public Color getDefColor(IOColors.OutputType type) {
        return curDefColors[type.ordinal()];
    }

    public LineInfo getLineInfo(int line) {
        LineInfo info = (LineInfo) linesToInfos.get(line);
        if (info != null) {
            int lineLength = length(line);
            if (lineLength > info.getEnd()) {
                info.addSegment(lineLength, false, null, null, false);
            }
            return info;
        } else {
            return new LineInfo(this, length(line));
        }
    }

    public LineInfo getExistingLineInfo(int line) {
        return (LineInfo) linesToInfos.get(line);
    }

    private static final int MAX_FIND_SIZE = 16*1024;
    private Pattern pattern;

    private boolean regExpChanged(String pattern, boolean matchCase) {
        return this.pattern != null && (!this.pattern.toString().equals(pattern) || (this.pattern.flags() == Pattern.CASE_INSENSITIVE) == matchCase);
    }

    public int[] find(int start, String pattern, boolean regExp, boolean matchCase) {
        Storage storage = getStorage();
        if (storage == null) {
            return null;
        }
        if (regExp && regExpChanged(pattern, matchCase)) {
            this.pattern = null;
        }
        if (!regExp && !matchCase) {
            pattern = pattern.toLowerCase();
        }
        while (true) {
            int size = getCharCount() - start;
            if (size > MAX_FIND_SIZE) {
                int l = getLineAt(start + MAX_FIND_SIZE);
                size = getLineStart(l) + length(l) - start;
            } else if (size <= 0) {
                break;
            }
            CharBuffer buff = null;
            try {
                buff = storage.getReadBuffer(toByteIndex(start), toByteIndex(size)).asCharBuffer();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (buff == null) {
                break;
            }
            if (regExp) {
                if (this.pattern == null) {
                    this.pattern =  matchCase ? Pattern.compile(pattern) : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                }
                Matcher matcher = this.pattern.matcher(buff);
                if (matcher.find()) {
                    return new int[]{start + matcher.start(), start + matcher.end()};
                }
            } else {
                int idx = matchCase ? buff.toString().indexOf(pattern)
                        : buff.toString().toLowerCase().indexOf(pattern);
                if (idx != -1) {
                    return new int[] {start + idx, start + idx + pattern.length()};
                }
            }
            start += buff.length();
        }
        return null;
    }

    public int[] rfind(int start, String pattern, boolean regExp, boolean matchCase) {
        Storage storage = getStorage();
        if (storage == null) {
            return null;
        }
        if (regExp && regExpChanged(pattern, matchCase)) {
            this.pattern = null;
        }
        if (!regExp && !matchCase) {
            pattern = pattern.toLowerCase();
        }
        while (true) {
            int end = start;
            start = end - MAX_FIND_SIZE;
            if (start < 0) {
                start = 0;
            } else {
                int l = getLineAt(start);
                start = getLineStart(l);
            }
            if (start == end) {
                break;
            }
            CharBuffer buff = null;
            try {
                buff = storage.getReadBuffer(toByteIndex(start), toByteIndex(end - start)).asCharBuffer();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (buff == null) {
                break;
            }
            if (regExp) {
                if (this.pattern == null) {
                    this.pattern =  matchCase ? Pattern.compile(pattern) : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                }
                Matcher matcher = this.pattern.matcher(buff);
                int mStart = -1;
                int mEnd = -1;
                while (matcher.find()) {
                    mStart = matcher.start();
                    mEnd = matcher.end();
                }
                if (mStart != -1) {
                    return new int[]{start + mStart, start + mEnd};
                }
            } else {
                int idx = matchCase ? buff.toString().lastIndexOf(pattern)
                        : buff.toString().toLowerCase().lastIndexOf(pattern);
                if (idx != -1) {
                    return new int[] {start + idx, start + idx + pattern.length()};
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return lineStartList.toString();
    }

    int addSegment(CharSequence s, int offset, int lineIdx, int pos, OutputListener l, boolean important, boolean err, Color c) {
        int len = length(lineIdx);
        if (len > 0) {
            LineInfo info = (LineInfo) linesToInfos.get(lineIdx);
            if (info == null) {
                info = new LineInfo(this);
                linesToInfos.put(lineIdx, info);
            }
            int curEnd = info.getEnd();
            if (pos > 0 && pos != curEnd) {
                info.addSegment(pos, false, null, null, false);
                curEnd = pos;
            }
            if (l != null) {
                int endPos = curEnd + Math.min(s.length() - offset, len);
                int strlen = Math.min(s.length(), offset + len);
                if (s.charAt(strlen - 1) == '\n') {
                    strlen--;
                }
                if (s.charAt(strlen - 1) == '\r') {
                    strlen--;
                }
                int leadingCnt = 0;
                while (leadingCnt + offset < strlen && Character.isWhitespace(s.charAt(offset + leadingCnt))) {
                    leadingCnt++;
                }
                int trailingCnt = 0;
                if (leadingCnt != strlen) {
                    while (trailingCnt < strlen && Character.isWhitespace(s.charAt(strlen - trailingCnt - 1))) {
                        trailingCnt++;
                    }
                }
                if (leadingCnt > 0) {
                    info.addSegment(curEnd + leadingCnt, false, null, null, false);
                }
                info.addSegment(endPos - trailingCnt, err, l, c, important);
                if (trailingCnt > 0) {
                    info.addSegment(endPos, false, null, null, false);
                }
                registerLineWithListener(lineIdx, info, important);
            } else {
                info.addSegment(len, err, l, c, important);
            }
        }
        return len;
    }

    void updateLinesInfo(CharSequence s, int startLine, int startPos, OutputListener l, boolean important, boolean err, Color c) {
        int offset = 0;
        CharSequence noTabsStr = s;
        if (l != null) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '\t') {
                    StringBuilder str = new StringBuilder(s.length() + 100);
                    int start = 0;
                    for (int k = i; k < s.length(); k++) {
                        if (s.charAt(k) == '\t') {
                            str.append(s, start, k);
                            str.append(OutWriter.TAB_REPLACEMENT);
                            start = k + 1;
                        }
                    }
                    str.append(s, start, s.length());
                    noTabsStr = str;
                    break;
                }
            }
        }
        int startLinePos = startPos - getLineStart(startLine);
        for (int i = startLine; i < getLineCount(); i++) {
            offset += addSegment(noTabsStr, offset, i, startLinePos, l, important, err, c) + 1;
            startLinePos = 0;
        }
    }

    void addLineInfo(int idx, LineInfo info, boolean important) {
        linesToInfos.put(idx, info);
        if (!info.getListeners().isEmpty()) {
            registerLineWithListener(idx, info, important);
        }
    }
}
