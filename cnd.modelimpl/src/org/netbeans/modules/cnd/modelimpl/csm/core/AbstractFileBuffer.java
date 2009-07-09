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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class AbstractFileBuffer implements FileBuffer {
    private final CharSequence absPath;
    private Charset encoding;
    
    protected AbstractFileBuffer(CharSequence absPath) {
        this.absPath = FilePathCache.getManager().getString(absPath);
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public CharSequence getAbsolutePath() {
        return absPath;
    }

    public File getFile() {
        return new File(absPath.toString());
    }
    
    public abstract int getLength();
    public abstract String getText(int start, int end) throws IOException;
    public abstract String getText() throws IOException;
    
    public final Reader getReader() throws IOException {
        if (encoding == null) {
            File file = getFile();
            // file must be normalized
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                encoding = FileEncodingQuery.getEncoding(fo);
            } else {
                encoding = FileEncodingQuery.getDefaultEncoding();
            }
        }
        InputStream is = getInputStream();
        Reader reader = new InputStreamReader(is, encoding);
        return reader;
    }
    
    public abstract InputStream getInputStream() throws IOException;
    public abstract boolean isFileBased();
    public abstract long lastModified();
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    protected void write(DataOutput output) throws IOException {
        assert this.absPath != null;
        PersistentUtils.writeUTF(absPath, output);
    }  
    
    protected AbstractFileBuffer(DataInput input) throws IOException {
        this.absPath = PersistentUtils.readUTF(input, FilePathCache.getManager());
        assert this.absPath != null;
    }    
}
