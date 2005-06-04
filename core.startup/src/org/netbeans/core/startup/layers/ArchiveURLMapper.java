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
package org.netbeans.core.startup.layers;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import org.openide.filesystems.*;

public class ArchiveURLMapper extends URLMapper {

    private static final String JAR_PROTOCOL = "jar";   //NOI18N

    private static ArchiveURLMapper instance;

    private static Map/*<File,SoftReference<JarFileSystem>>*/ mountRoots = new HashMap();

    public URL getURL(FileObject fo, int type) {
        assert fo != null;
        if (type == URLMapper.EXTERNAL || type == URLMapper.INTERNAL) {
            if (fo.isValid()) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (fs instanceof JarFileSystem) {
                        JarFileSystem jfs = (JarFileSystem) fs;
                        File archiveFile = jfs.getJarFile();
                        if (this.isRoot(archiveFile)) {
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
                    ErrorManager.getDefault().notify(e);
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
                    ErrorManager.getDefault().log (ErrorManager.WARNING, e.getLocalizedMessage());
                }
                catch (URISyntaxException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return null;
    }

    public static FileObject getArchiveRoot (FileObject fo) throws IOException {
        if (fo.isVirtual()) {
            return null;
        }
        File file = FileUtil.toFile (fo);
        return getFileSystem(file).getRoot();
    }    


    private static synchronized boolean isRoot (File file) {
        return mountRoots.containsKey(file);
    }

    private static synchronized JarFileSystem getFileSystem (File file) throws IOException {
        Reference reference = (Reference) mountRoots.get (file);
        JarFileSystem jfs = null;
        if (reference==null || (jfs=(JarFileSystem)reference.get())==null) {
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
        Enumeration en = Repository.getDefault().getFileSystems();
        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem)en.nextElement();
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
    private static class JFSReference extends SoftReference {
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
            JarFileSystem jfs = (JarFileSystem)get ();
            if (jfs != null) {
                synchronized (ArchiveURLMapper.class) {
                    File keyToRemove = (root != null) ? root : jfs.getJarFile();
                    mountRoots.remove(keyToRemove);                    
                }
            }
        }
    }

}
