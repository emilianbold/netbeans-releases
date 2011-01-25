/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.support.InvalidFileObjectSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class AbstractFileBuffer implements FileBuffer {
    private final CharSequence absPath;
    private final FileSystem fileSystem;
    private Charset encoding;

    protected AbstractFileBuffer(FileObject fileObject) {
        this.absPath = FilePathCache.getManager().getString(CndFileUtils.getNormalizedPath(fileObject));
        this.fileSystem = getFileSystem(fileObject);
// remote link file objects are just lightweight delegating wrappers, so they have multiple instances
//        if (CndUtils.isDebugMode()) {
//            FileObject fo2 = fileSystem.findResource(absPath.toString());
//            CndUtils.assertTrue(fileObject == fo2, "File objects differ: " + fileObject + " vs " + fo2); //NOI18N
//        }
    }

    private static FileSystem getFileSystem(FileObject fileObject) {
        try {
            return fileObject.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return InvalidFileObjectSupport.getDummyFileSystem();
        }       
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    @Override
    public CharSequence getAbsolutePath() {
        return absPath;
    }

    @Override
    public CharSequence getUrl() {
        return CndFileUtils.fileObjectToUrl(getFileObject());
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }    

    @Override
    public FileObject getFileObject() {
        FileObject result = fileSystem.findResource(absPath.toString());
        if (result == null) {
            CndUtils.assertTrueInConsole(false, "can not find file object for " + absPath); //NOI18N
        }
        return result;
    }

    public abstract InputStream getInputStream() throws IOException;
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    // final is important here - see PersistentUtils.writeBuffer/readBuffer
    public final void write(DataOutput output) throws IOException {
        assert this.absPath != null;
        PersistentUtils.writeUTF(absPath, output);
        PersistentUtils.writeFileSystem(fileSystem, output);
    }  
    
    protected AbstractFileBuffer(DataInput input) throws IOException {
        this.absPath = PersistentUtils.readUTF(input, FilePathCache.getManager());
        this.fileSystem = PersistentUtils.readFileSystem(input);
        assert this.absPath != null;
    }

    @Override
    public int[] getLineColumnByOffset(int offset) throws IOException {
        int[] lineCol = new int[]{1, 1};
        int line = _getLineByOffset(offset);
        int start = _getStartLineOffset(line);
        lineCol[0] = line;
        // find line and column
        String text = getText();
        int TABSIZE = ModelSupport.getTabSize();
        for (int curOffset = start; curOffset < offset; curOffset++) {
            char curChar = text.charAt(curOffset);
            switch (curChar) {
                case '\n':
                    // just increase line number
                    lineCol[0] = lineCol[0] + 1;
                    lineCol[1] = 1;
                    break;
                case '\t':
                    int col = lineCol[1];
                    int newCol = (((col - 1) / TABSIZE) + 1) * TABSIZE + 1;
                    lineCol[1] = newCol;
                    break;
                default:
                    lineCol[1]++;
                    break;
            }
        }
        return lineCol;
    }

    @Override
    public int getOffsetByLineColumn(int line, int column) throws IOException {
        int startOffset = _getStartLineOffset(line);
        String text = getText();
        int TABSIZE = ModelSupport.getTabSize();
        int currCol = 1;
        int outOffset;
        loop:for (outOffset = startOffset; outOffset < text.length(); outOffset++) {
            if (currCol >= column) {
                break;
            }
            char curChar = text.charAt(outOffset);
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
        return list[list.length-1];
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
    
    private WeakReference<Object> lines = new WeakReference<Object>(null);
    private int[] getLineOffsets() throws IOException {
        WeakReference<Object> aLines = lines;
        int[] res = null;
        if (aLines != null) {
            res = (int[]) aLines.get();
        }
        if (res == null) {
            String text = getText();
            int length = text.length();
            ArrayList<Integer> list = new ArrayList<Integer>(length/10);
            // find line and column
            list.add(Integer.valueOf(0));
            for (int curOffset = 0; curOffset < length; curOffset++) {
                char curChar = text.charAt(curOffset);
                if (curChar == '\n') {
                    list.add(Integer.valueOf(curOffset+1));
                }
            }
            res = new int[list.size()];
            for (int i = 0; i < list.size(); i++){
                res[i] = list.get(i);
            }
            lines = new WeakReference<Object>(res);
        }
        return res;
    }

    protected void clearLineCache() {
        WeakReference<Object> aLines = lines;
        if (aLines != null) {
            aLines.clear();
        }
    }
}
