/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class FolderArchive implements Archive {

    private static final Logger LOG = Logger.getLogger(FolderArchive.class.getName());
    private static final boolean normalize = Boolean.getBoolean("FolderArchive.normalize"); //NOI18N
    
    final File root;
    final Charset encoding;

    /** Creates a new instance of FolderArchive */
    public FolderArchive (final File root) {
        assert root != null;
        this.root = root;
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "creating FolderArchive for {0}", root.getAbsolutePath());
        }
        
        FileObject file = FileUtil.toFileObject(root);
        
        if (file != null) {
            encoding = FileEncodingQuery.getEncoding(file);
        } else {
            encoding = null;
        }
    }
    
    public Iterable<JavaFileObject> getFiles(String folderName, ClassPath.Entry entry, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter) throws IOException {
        assert folderName != null;
        if (folderName.length()>0) {
            folderName+='/';                                                                            //NOI18N
        }
        if (entry == null || entry.includes(folderName)) {
            File folder = new File (this.root, folderName.replace('/', File.separatorChar));      //NOI18N
            //Issue: #126392 on Mac
            //The problem when File ("A/").listFiles()[0].equals(new File("a/").listFiles[0]) returns false
            //Normalization is slow - turn on this workaround only for users which require it.
            //The problem only happens in case when there is file with wrong case in import.
            if (normalize) {
                folder = FileUtil.normalizeFile(folder);
            }
            if (folder.canRead()) {
                File[] content = folder.listFiles();            
                if (content != null) {
                    List<JavaFileObject> result = new ArrayList<JavaFileObject>(content.length);
                    for (File f : content) {
                        if (f.isFile()) {
                            if (entry == null || entry.includes(f.toURI().toURL())) {
                                if (kinds == null || kinds.contains(FileObjects.getKind(FileObjects.getExtension(f.getName())))) {
                                    result.add(FileObjects.fileFileObject(f,this.root,filter, encoding));
                                }
                            }
                        }
                    }
                    return Collections.unmodifiableList(result);
                }
            }
        }
        return Collections.<JavaFileObject>emptyList();
    }

    public JavaFileObject create (String relativePath, final JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        if (File.separatorChar != '/') {    //NOI18N
            relativePath = relativePath.replace('/', File.separatorChar);
        }
        final File file = new File (root, relativePath);
        return FileObjects.fileFileObject(file, root, filter, encoding);
    }

    public void clear () {

    }
}
