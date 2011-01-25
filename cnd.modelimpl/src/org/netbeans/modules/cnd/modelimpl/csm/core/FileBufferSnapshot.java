/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class FileBufferSnapshot implements APTFileBuffer {
    
    private final CharSequence absPath;
    private final FileSystem fileSystem;
    private final char[] buffer;
    private final long timeStamp;
    private Reference<int[]> linesRef;
    
    public FileBufferSnapshot(FileSystem fileSystem, CharSequence absPath, char[] buffer, int[] linesCache, long timeStamp) {
        this.absPath = absPath;
        this.fileSystem = fileSystem;
        this.buffer = buffer;
        this.timeStamp = timeStamp;
        cacheLines(linesCache);
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    
    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public char[] getCharBuffer() throws IOException {
        return buffer;
    }

    public int[] getLineColumnByOffset(int offset) throws IOException {
        int[] lineCol = new int[]{1, 1};
        int line = _getLineByOffset(offset);
        int start = _getStartLineOffset(line);
        lineCol[0] = line;
        // find line and column
        int TABSIZE = ModelSupport.getTabSize();
        for (int curOffset = start; curOffset < offset; curOffset++) {
            char curChar = buffer[curOffset];
            switch (curChar) {
                case '\n':
                    // just increase line number
                    lineCol[0] += 1;
                    lineCol[1] = 1;
                    break;
                case '\t':
                    int col = lineCol[1];
                    int newCol = (((col - 1) / TABSIZE) + 1) * TABSIZE + 1;
                    lineCol[1] = newCol;
                    break;
                default:
                    lineCol[1] += 1;
                    break;
            }
        }
        return lineCol;
    }

    public int getOffsetByLineColumn(int line, int column) throws IOException {
        int startOffset = _getStartLineOffset(line);
        int TABSIZE = ModelSupport.getTabSize();
        int currCol = 1;
        int outOffset;
        loop:
        for (outOffset = startOffset; outOffset < buffer.length; outOffset++) {
            if (currCol >= column) {
                break;
            }
            char curChar = buffer[outOffset];
            switch (curChar) {
                case '\n':
                    break loop;
                case '\t':
                    int col = currCol;
                    int newCol = (((col - 1) / TABSIZE) + 1) * TABSIZE + 1;
                    currCol = newCol;
                    break;
                default:
                    currCol++;
                    break;
            }
        }
        return outOffset;
    }

    private int _getStartLineOffset(int line) throws IOException {
        line--;
        int[] list = getLineOffsets();
        if (line < list.length) {
            return list[line];
        }
        return list[list.length - 1];
    }

    private int _getLineByOffset(int offset) throws IOException {
        int[] list = getLineOffsets();
        int low = 0;
        int high = list.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = list[mid];
            if (midVal < offset) {
                if (low == high) {
                    return low + 1;
                }
                low = mid + 1;
            } else if (midVal > offset) {
                if (low == high) {
                    return low;
                }
                high = mid - 1;
            } else {
                return mid + 1;
            }
        }
        return low;
    }

    private int[] getLineOffsets() throws IOException {
        Reference<int[]> aLines = linesRef;
        int[] res = null;
        if (aLines != null) {
            res = aLines.get();
        }
        if (res == null) {
            char[] charBuffer = getCharBuffer();
            int length = charBuffer.length;
            ArrayList<Integer> list = new ArrayList<Integer>(length / 10);
            // find line and column
            list.add(Integer.valueOf(0));
            for (int curOffset = 0; curOffset < length; curOffset++) {
                char curChar = charBuffer[curOffset];
                if (curChar == '\n') {
                    list.add(Integer.valueOf(curOffset + 1));
                }
            }
            res = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = list.get(i);
            }
            cacheLines(res);
        }
        return res;
    }    
    
    private void cacheLines(int[] lines) {
        this.linesRef = new WeakReference<int[]>(lines);
    }

    public String getText(int start, int end) {
        return new String(buffer, start, end - start);
    }
    
    public String getText() {
        return new String(buffer);
    }
}
