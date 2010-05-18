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

package org.netbeans.modules.mobility.cldcplatform.startup;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Adam Sotona
 */
public class LibraryConverter extends FileChangeAdapter {
    
    /** Creates a new instance of LibraryConverter */
    public LibraryConverter() {
        try {
            FileObject rep = FileUtil.createFolder(FileUtil.getConfigRoot(), "org-netbeans-api-project-libraries/Libraries");  //NOI18N
            rep.addFileChangeListener(this);
            FileObject fo[] = rep.getChildren();
            for (int i=0; i < fo.length; i++) {
                convertLibrary(fo[i]);
            }
        } catch (IOException e) {
        }
    }
    
    public void fileDataCreated(FileEvent fe) {
        convertLibrary(fe.getFile());
    }
    
    public void convertLibrary(FileObject fo) {
        if (fo == null || !fo.isData() || !"xml".equals(fo.getExt())) return; //NOI18N
        int size = (int)fo.getSize();
        if (size <= 0) return;
        final byte buffer[] = new byte[size];
        DataInputStream in = null;
        try {
            in = new DataInputStream(fo.getInputStream());
            in.readFully(buffer);
            in.close();
            if (replace(buffer)) {
                final String name = fo.getNameExt();
                final FileObject parent = fo.getParent();
                fo.delete();
                parent.getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        OutputStream out = null;
                        try {
                            out = parent.createData(name).getOutputStream();
                            out.write(buffer);
                        } finally {
                            if (out != null) try {out.close();} catch (IOException e) {}
                        }
                    }
                });
            }
        } catch (IOException ioe) {
            if (in != null) try {in.close();} catch (IOException e) {}
        }
    }

    private static final byte target[] = "<type>j2me</type>".getBytes(); //NOI18N
    
    private boolean replace(byte[] source) {
        if (source.length < target.length) return false;
        byte first  = target[0];
        int max = source.length - target.length;
        for (int i = 0; i <= max; i++) {
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }
            if (i <= max) {
                int j = i + 1;
                int end = j + target.length - 1;
                for (int k = 1; j < end && source[j] == target[k]; j++, k++);
                if (j == end) {
                    /* replace j2me -> j2se */
                    source[i+8] = 's';
                    return true;
                }
            }
        }
        return false;
    }
    
    
}
