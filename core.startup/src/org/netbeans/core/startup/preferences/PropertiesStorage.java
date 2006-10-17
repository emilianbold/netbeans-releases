/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.preferences;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 * No synchronization - must be called just from NbPreferences which
 *  ensures proper synchronization
 * @author Radek Matous
 */
class PropertiesStorage implements NbPreferences.FileStorage {
    private static final String USERROOT_PREFIX = "/Preferences";//NOI18N
    private static final String SYSTEMROOT_PREFIX = "/SystemPreferences";//NOI18N
    private final static FileObject SFS_ROOT =
            Repository.getDefault().getDefaultFileSystem().getRoot();
    
    private final String folderPath;
    private String filePath;
    private boolean isModified;
    
    
    static NbPreferences.FileStorage instance(final String absolutePath) {
        return new PropertiesStorage(absolutePath, true);
    }
    
    FileObject preferencesRoot() throws IOException {
        return FileUtil.createFolder(SFS_ROOT, USERROOT_PREFIX);
    }
    
    static NbPreferences.FileStorage instanceReadOnly(final String absolutePath) {
        return new PropertiesStorage(absolutePath, false) {
            public boolean isReadOnly() {
                return true;
            }
            
            public final String[] childrenNames() {
                return new String[0];
            }
            
            public final Properties load() throws IOException {
                return new Properties();
            }
            
            protected FileObject toPropertiesFile(boolean create) throws IOException {
                if (create) {
                    throw new IOException();
                }
                return null;
            }
            
            protected FileObject toFolder(boolean create) throws IOException {
                if (create) {
                    throw new IOException();
                }
                return null;
            }
            
            protected FileObject toPropertiesFile() {
                return null;
            }
            
            protected FileObject toFolder() {
                return null;
            }
            
            FileObject preferencesRoot() throws IOException {
                return FileUtil.createFolder(SFS_ROOT, SYSTEMROOT_PREFIX);
            }
            
        };
    }
    
    /** Creates a new instance */
    private PropertiesStorage(final String absolutePath, boolean userRoot) {
        StringBuffer sb = new StringBuffer();
        String prefix = (userRoot) ? USERROOT_PREFIX : SYSTEMROOT_PREFIX;
        sb.append(prefix).append(absolutePath);
        folderPath = sb.toString();
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    public void markModified() {
        isModified = true;
    }
    
    public final boolean existsNode() {
        return (toPropertiesFile() != null);
    }
    
    public String[] childrenNames() {
        Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.CHILDREN_NAMES, true);
        try {
            FileObject folder = toFolder();
            List<String> folderNames = new ArrayList<String>();
            
            if (folder != null) {
                List<? extends FileObject> folders = Collections.list(folder.getFolders(false));
                
                for (FileObject fo : folders) {
                    folderNames.add(fo.getNameExt());
                }
            }
            
            return folderNames.toArray(new String[folderNames.size()]);
        } finally {
            sw.stop();
        }
    }
    
    public final void removeNode() throws IOException {
        Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.REMOVE_NODE, true);
        try {
            FileObject folder = toFolder();
            if (folder != null && folder.isValid()) {
                folder.delete();
                folder = folder.getParent();
                while (folder != null && folder != preferencesRoot() && folder.getChildren().length == 0) {
                    folder.delete();
                    folder = folder.getParent();
                }
            }
        } finally {
            sw.stop();
        }
    }
    
    public Properties load() throws IOException {
        Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.LOAD, true);
        try {
            Properties retval = new Properties();
            InputStream is = inputStream();
            if (is != null) {
                try {
                    retval.load(is);
                } finally {
                    if (is != null) is.close();
                }
            }
            return retval;
        } finally {
            sw.stop();
        }
    }
    
    public void save(final Properties properties) throws IOException {
        if (isModified) {
            Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.FLUSH, true);
            try {
                isModified = false;
                if (!properties.isEmpty()) {
                    OutputStream os = null;
                    try {
                        os = outputStream();
                        properties.store(os, null);
                    } finally {
                        if (os != null) os.close();
                    }
                } else {
                    FileObject file = toPropertiesFile();
                    if (file != null) {
                        file.delete();
                    }
                    FileObject folder = toFolder();
                    while (folder != null && folder != preferencesRoot() && folder.getChildren().length == 0) {
                        folder.delete();
                        folder = folder.getParent();
                    }
                }
            } finally {
                sw.stop();
            }
        }
    }
    
    private InputStream inputStream() throws IOException {
        FileObject file = toPropertiesFile(false);
        return (file == null) ? null : file.getInputStream();
    }
    
    private OutputStream outputStream() throws IOException {
        FileObject fo = toPropertiesFile(true);
        final FileLock lock = fo.lock();
        final OutputStream os = fo.getOutputStream(lock);
        return new FilterOutputStream(os) {
            public void close() throws IOException {
                super.close();
                lock.releaseLock();
            }
        };
    }
    
    private String folderPath() {
        return folderPath;
    }

    private String filePath() {
        if (filePath == null) {
            String[] all = folderPath().split("/");//NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < all.length-1; i++) {
                sb.append(all[i]).append("/");//NOI18N
            }
            if (all.length > 0) {
                sb.append(all[all.length-1]).append(".properties");//NOI18N
            } else {
                sb.append("root.properties");//NOI18N
            }
            filePath = sb.toString();
        }
        return filePath;
    }        

    protected FileObject toFolder()  {
        return SFS_ROOT.getFileObject(folderPath());
    }

    protected  FileObject toPropertiesFile() {
        return SFS_ROOT.getFileObject(filePath());
    }

    protected FileObject toFolder(boolean create) throws IOException {
        FileObject retval = toFolder();
        if (retval == null && create) {
            retval = FileUtil.createFolder(SFS_ROOT, folderPath);
        }
        assert (retval == null && !create) || (retval != null && retval.isFolder());
        return retval;
    }
    
    protected FileObject toPropertiesFile(boolean create) throws IOException {
        FileObject retval = toPropertiesFile();
        if (retval == null && create) {
            retval = FileUtil.createData(SFS_ROOT,filePath());//NOI18N
        }
        assert (retval == null && !create) || (retval != null && retval.isData());
        return retval;
    }
}
