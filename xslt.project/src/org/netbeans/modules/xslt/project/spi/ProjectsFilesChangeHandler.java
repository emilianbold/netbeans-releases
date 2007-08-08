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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project.spi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ProjectsFilesChangeHandler {
    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private Project myCurrentProject;
    private ProjectsFileChangeListener myFileChangeListener;
    private static HashMap<String, Object> SUPPORTED_EXTS = 
            new HashMap<String, Object>();
    static {
        SUPPORTED_EXTS.put("wsdl", Boolean.TRUE);
        SUPPORTED_EXTS.put("xsd", Boolean.TRUE);
    }
    private ProjectsFilesChangesSupport myChangesSupport = 
            new ProjectsFilesChangesSupport();


    private LinkedHashSet<FileObject> supportedFos = new LinkedHashSet<FileObject>();
    private Set<FileObject> subscribedFos = new HashSet<FileObject>();
    
    public ProjectsFilesChangeHandler(Project currentProject) {
        if (currentProject == null) {
            throw new IllegalStateException("context project shouldn't be null");
        }

        myCurrentProject = currentProject;
        myFileChangeListener = new ProjectsFileChangeListener();
    }
    
    public void subscribes() {
        subscribes(myCurrentProject);
    }

    public void subscribes(Project project) {
        if (project == null) {
            return;
        }
        
        FileObject[] rootSources = Util.getProjectSources(project);
        if (rootSources == null) {
            return;
        }
        
        for (FileObject fo : rootSources) {
            subscribes(fo);
        }
    }

    private void subscribes(FileObject folder) {
        if (folder == null) {
            return;
        }
        
        folder.addFileChangeListener(myFileChangeListener);
        add2cache(folder);

        FileObject[] fos = null;
        if (folder.isFolder()) {
            fos = folder.getChildren();
        }
        if (fos != null) {
            for (FileObject fo : fos) {
                subscribes(fo);
            }
        }
    }

    public void addProjectsFilesChangeListener(ProjectsFilesChangeListener l) {
        myChangesSupport.addPropertyChangeListener(l);
    }
    
    public void removeProjectsFilesChangeListener(ProjectsFilesChangeListener l) {
        myChangesSupport.removePropertyChangeListener(l);
    }

    private void add2cache(FileObject fo) {
        assert fo != null;
        writeLock.lock();
        try {
            subscribedFos.add(fo);
            if (isSupportedFo(fo)) {
                boolean isAddedSupported = supportedFos.add(fo);
                if (isAddedSupported) {
                    // fire number of supported fos have been changed
                }
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    private void removeFromCache(FileObject fo) {
        assert fo != null;
        writeLock.lock();
        try {
            subscribedFos.remove(fo);
            boolean isRemovedSupported = supportedFos.remove(fo);
            if (isRemovedSupported) {
                // fire number of supported fos have been changed
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    private void clearCache() {
        subscribedFos.clear();
        supportedFos.clear();
    }
    
    public void unsubscribes() {
        for (FileObject fo : subscribedFos) {
            fo.removeFileChangeListener(myFileChangeListener);
        }
        clearCache();
    }

    
    private static boolean isSupportedFo(FileObject fo) {
        if (fo == null || fo.isFolder() || fo.isVirtual()) {
            return false;
        }
        String ext = fo.getExt();
        if (ext != null && SUPPORTED_EXTS.get(ext) != null) {
            return true;
        }

        return false;
    }

    private class ProjectsFileChangeListener
            implements FileChangeListener 
    {
        
        public void fileFolderCreated(FileEvent fe) {
            FileObject fo = fe.getFile();
            subscribes(fo);
    //        throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileDataCreated(FileEvent fe) {
            FileObject fo = fe.getFile();
            add2cache(fo);
            if (isSupportedFo(fo)) {
                myChangesSupport.fireFileAdded(fo);
            }
    //        throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileChanged(FileEvent fe) {
    //        throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileDeleted(FileEvent fe) {
            FileObject fo = fe.getFile();
            System.out.println("file deleted: "+fo);
            if (isSupportedFo(fo)) {
                fo.removeFileChangeListener(this);
                myChangesSupport.fireFileDeleted(fo);
                removeFromCache(fo);
            }
    //        throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileRenamed(FileRenameEvent fe) {
            FileObject fo = fe.getFile();
            if (isSupportedFo(fo)) {
                myChangesSupport.fireFileRenamed(fe);
            }
    //        throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
    //        throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
