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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ProjectsFilesChangesSupport {
    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private List<ProjectsFilesChangeListener> myListeners = new ArrayList<ProjectsFilesChangeListener>();
    
    public ProjectsFilesChangesSupport() {
    }

    public void addPropertyChangeListener(ProjectsFilesChangeListener changeListener) {
        assert changeListener != null : "Try to add null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.add(changeListener);
        } finally {
            writeLock.unlock();
        }
    }

    public void removePropertyChangeListener(ProjectsFilesChangeListener changeListener) {
        assert changeListener != null : "Try to remove null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.remove(changeListener);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeAllPropertyChangeListener() {
        writeLock.lock();
        try {
            myListeners.clear();
        } finally {
            writeLock.unlock();
        }
    }
    
    public void fireFileAdded(FileObject fo) 
    {
        ProjectsFilesChangeListener[] tmp = new ProjectsFilesChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (ProjectsFilesChangeListener listener : tmp) {
            listener.fileAdded(fo);
        }
    }    

    public void fireFileRenamed(FileRenameEvent fe) 
    {
        ProjectsFilesChangeListener[] tmp = new ProjectsFilesChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (ProjectsFilesChangeListener listener : tmp) {
            listener.fileRenamed(fe);
        }
    }    

    public void fireFileDeleted(FileObject fo) 
    {
        ProjectsFilesChangeListener[] tmp = new ProjectsFilesChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (ProjectsFilesChangeListener listener : tmp) {
            listener.fileDeleted(fo);
        }
    }    
}
