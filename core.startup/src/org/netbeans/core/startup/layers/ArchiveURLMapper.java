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

package org.netbeans.core.startup.layers;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public class ArchiveURLMapper extends URLMapper {
    private static boolean warningAlreadyReported = false;
    private static final String JAR_PROTOCOL = "jar";   //NOI18N

    private static Map<File,SoftReference<JarFileSystem>> mountRoots = new HashMap<File,SoftReference<JarFileSystem>>();

    public URL getURL(FileObject fo, int type) {
        assert fo != null;
        if (type == URLMapper.EXTERNAL || type == URLMapper.INTERNAL) {
            if (fo.isValid()) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (fs instanceof JarFileSystem) {
                        JarFileSystem jfs = (JarFileSystem) fs;
                        File archiveFile = jfs.getJarFile();
                        if (isRoot(archiveFile)) {
// XXX: commented out to create same URLs as URLMapper.DefaultURLMapper.
//                            StringTokenizer tk = new StringTokenizer (fo.getPath(),"/");            //NOI18N
//                            StringBuffer offset = new StringBuffer ();
//                            while (tk.hasMoreTokens()) {
//                                offset.append('/');                                                 //NOI18N
//                                // The encoding is needed to create a valid URI.
//                                // Otherwise the URI constructor throws URISyntaxException
//                                // Todo: It causes problems with JarURLConnection
//                                // which determines the entryName by String.substring(int,int)
//                                offset.append(URLEncoder.encode(tk.nextToken(),"UTF-8"));           //NOI18N
//                            }
//                            if (offset.length()==0) {
//                                offset.append('/');                                                 //NOI18N
//                            }
                            return new URL ("jar:"+archiveFile.toURI()+"!/"+fo.getPath()+
                                    ((fo.isFolder() && !fo.isRoot()) ? "/" : "")); // NOI18N
                        }
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        return null;
    }

    public FileObject[] getFileObjects(URL url) {
        assert url != null;
        String protocol  = url.getProtocol ();
        if (JAR_PROTOCOL.equals (protocol)) {
            String path = url.getPath();
            int index = path.lastIndexOf ('!');
            if (index>=0) {
                try {
                    URI archiveFileURI = new URI(path.substring(0,index));
                    if (!archiveFileURI.isAbsolute()  || archiveFileURI.isOpaque()) {
                        return null; //Better than to throw IllegalArgumentException
                    }
                    FileObject fo = URLMapper.findFileObject (archiveFileURI.toURL());
                    if (fo == null || fo.isVirtual()) {
                        return null;
                    }
                    File archiveFile = FileUtil.toFile (fo);
                    if (archiveFile == null) {
                        return null;
                    }
                    String offset = path.length()>index+2 ? URLDecoder.decode(path.substring(index+2),"UTF-8"): "";   //NOI18N
                    JarFileSystem fs = getFileSystem(archiveFile);
                    FileObject resource = fs.findResource(offset);
                    if (resource != null) {
                        return new FileObject[] {resource};
                    }
                } catch (IOException e) {                    
                    // Can easily happen if the JAR file is corrupt etc,
                    // it is better for user to log localized message than to dump stack
                    if (warningAlreadyReported) {
                        ModuleLayeredFileSystem.err.log(Level.INFO, null, e);
                    } else {
                        ModuleLayeredFileSystem.err.log(Level.WARNING, null, e);
                        warningAlreadyReported = true;
                    }
                }
                catch (URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        return null;
    }

    private static synchronized boolean isRoot (File file) {
        return mountRoots.containsKey(file);
    }

    private static synchronized JarFileSystem getFileSystem (File file) throws IOException {
        Reference<JarFileSystem> reference = mountRoots.get(file);
        JarFileSystem jfs = null;
        if (reference == null || (jfs = reference.get()) == null) {
            jfs = findJarFileSystemInRepository(file);
            if (jfs == null) {
                try {
                    jfs = new JarFileSystem();
                    File aRoot = FileUtil.normalizeFile(file);
                    jfs.setJarFile(aRoot);
                } catch (PropertyVetoException pve) {
                    throw new AssertionError(pve);
                }
            }
            mountRoots.put(file, new JFSReference(jfs));
        }
        return jfs;
    }

    // More or less copied from URLMapper:
    private static JarFileSystem findJarFileSystemInRepository(File jarFile) {
        @SuppressWarnings("deprecation") // for compat only
        Enumeration<? extends FileSystem> en = Repository.getDefault().getFileSystems();
        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            if (fs instanceof JarFileSystem) {
                JarFileSystem jfs = (JarFileSystem)fs;
                if (jarFile.equals(jfs.getJarFile())) {
                    return jfs;
                }
            }
        }
        return null;
    }

    /**
     * After deleting and recreating of jar file there must be properly
     * refreshed cached map "mountRoots". 
     */ 
    private static class JFSReference extends SoftReference<JarFileSystem> {
        private FileChangeListener fcl; 
        public JFSReference(JarFileSystem jfs) {
            super(jfs);
            final File root = jfs.getJarFile();
            FileObject rootFo = FileUtil.toFileObject(root);
            if (rootFo != null) {
                fcl = new FileChangeAdapter() {
                    public void fileDeleted(FileEvent fe) {
                        releaseMe(root);
                    }

                public void fileRenamed(FileRenameEvent fe) {
                        releaseMe(root);                    
                }
                    
                    
                    
                };                                
                rootFo.addFileChangeListener(FileUtil.weakFileChangeListener(fcl, rootFo));
                
            }
        }
        
        void releaseMe (final File root) {
            JarFileSystem jfs = get();
            if (jfs != null) {
                synchronized (ArchiveURLMapper.class) {
                    File keyToRemove = (root != null) ? root : jfs.getJarFile();
                    mountRoots.remove(keyToRemove);                    
                }
            }
        }
    }

}
