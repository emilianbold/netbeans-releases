/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * SourceForBinaryQueryImplementation implementation
 * for items in the maven2 repository. It checks the artifact and
 * looks for the same artifact but of type "sources.jar".
 * 
 * @author  Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=999)
public class RepositorySourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {
    
    /** Creates a new instance of RepositorySourceForBinaryQueryImpl */
    public RepositorySourceForBinaryQueryImpl() {
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL url) {
        URL binRoot = url;
        if ("jar".equals(url.getProtocol())) { //NOI18N
            binRoot = FileUtil.getArchiveFile(url);
        } else {
            // null for directories.
            return null;
        }
        FileObject jarFO = URLMapper.findFileObject(binRoot);
        if (jarFO != null) {
            File jarFile = FileUtil.toFile(jarFO);
            if (jarFile != null) {
//                String name = jarFile.getName();
                File parent = jarFile.getParentFile();
                if (parent != null) {
                    File parentParent = parent.getParentFile();
                    if (parentParent != null) {
                        // each repository artifact should have this structure
                        String artifact = parentParent.getName();
                        String version = parent.getName();
//                        File pom = new File(parent, artifact + "-" + version + ".pom");
//                        // maybe this condition is already overkill..
//                        if (pom.exists()) {
                            File srcs = new File(parent, artifact + "-" + version + "-sources.jar"); //NOI18N
                            if (srcs.exists()) {
                                return new SrcResult(srcs);
                            }
//                        }
                    }
                }
            }
        }
        return null;
                
    }
    
    private class SrcResult implements SourceForBinaryQuery.Result  {
        private static final String ATTR_PATH = "lastRootCheckPath"; //NOI18N
        private static final String ATTR_STAMP = "lastRootCheckStamp"; //NOI18N
        private File file;
        private final List<ChangeListener> listeners;
        
        public SrcResult(File src) {
            file = src;
            listeners = new ArrayList<ChangeListener>();
        }
        public void addChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.add(changeListener);
            }
        }
        
        public void removeChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.remove(changeListener);
            }
        }
        
        public FileObject[] getRoots() {
            if (file.exists()) {
                FileObject fo = FileUtil.toFileObject(file);
                FileObject jarRoot = FileUtil.getArchiveRoot(fo);
                if (jarRoot != null) { //#139894 it seems that sometimes it can return null.
                                  // I suppose it's in the case when the jar/zip file in repository exists
                                  // but is corrupted (not zip, eg. when downloaded from a wrongly
                                  //setup repository that returns html documents on missing jar files.

                    //try detecting the source path root, in case the source jar has the sources not in root.
                    Date date = (Date) fo.getAttribute(ATTR_STAMP);
                    String path = (String) fo.getAttribute(ATTR_PATH);
                    if (date == null || fo.lastModified().after(date)) {
                        path = checkPath(jarRoot, fo);
                    }

                    FileObject[] fos = new FileObject[1];
                    if (path != null) {
                        fos[0] = jarRoot.getFileObject(path);
                    }
                    if (fos[0] == null) {
                        fos[0] = jarRoot;
                    }
                    return fos;
                }
            }
            return new FileObject[0];
        }

        private String checkPath(FileObject jarRoot, FileObject fo) {
            String toRet = null;
            FileObject root = JavadocAndSourceRootDetection.findSourceRoot(jarRoot);
            try {
                if (root != null && !root.equals(jarRoot)) {
                    toRet = FileUtil.getRelativePath(jarRoot, root);
                    fo.setAttribute(ATTR_PATH, toRet);
                }
                fo.setAttribute(ATTR_STAMP, new Date());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return toRet;
        }
        
    }    
    
}
