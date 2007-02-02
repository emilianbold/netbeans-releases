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

package org.netbeans.bluej.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.bluej.BluejProject;
import org.netbeans.bluej.options.BlueJSettings;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 */
public class CPImpl implements ClassPathImplementation {
    
    private List resources = null;
    private List listeners = new ArrayList();

    private BluejProject project;
    private FileObject userLib;
    private PropertyChangeListener settingsListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent arg0) {
            fireChange();
        }
    };
    
    private FileChangeListener fileListener = new FileChangeAdapter() {
        public void fileDataCreated(FileEvent arg0) {
            fireChange();
        }

        public void fileDeleted(FileEvent arg0) {
            fireChange();
        }

        public void fileRenamed(FileRenameEvent arg0) {
            fireChange();
        }
    };
    /** Creates a new instance of CPImpl */
    public CPImpl(BluejProject prj) {
        project = prj;
        BlueJSettings.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(settingsListener, BlueJSettings.getDefault()));
    }

    public synchronized List getResources() {
        if (resources == null) {
            resources = new ArrayList();
            FileObject libs = project.getProjectDirectory().getFileObject("+libs");  // NOI18N
            if (libs != null) {
                FileObject[] fos = libs.getChildren();
                for (int i = 0; i < fos.length; i++) {
                    if (FileUtil.isArchiveFile(fos[i])) {
                        resources.add(ClassPathSupport.createResource(URLMapper.findURL(FileUtil.getArchiveRoot(fos[i]), URLMapper.INTERNAL)));
                    }
                }
            }
            File home = BlueJSettings.getDefault().getHome();
            if (home != null) {
                FileObject fo = FileUtil.toFileObject(BluejProject.getUserLibPath(home));
                if (fo != null) {
                    FileObject[] fos = fo.getChildren();
                    for (int i = 0; i < fos.length; i++) {
                        if (FileUtil.isArchiveFile(fos[i])) {
                            resources.add(ClassPathSupport.createResource(URLMapper.findURL(FileUtil.getArchiveRoot(fos[i]), URLMapper.INTERNAL)));
                        }
                    }
                } 
                if (userLib != null && !userLib.equals(fo)) {
                    //remove
                    userLib.removeFileChangeListener(fileListener);
                }
                userLib = fo;
                //add
                userLib.addFileChangeListener(fileListener);
            } else if (userLib != null) {
                //remove listener
                userLib.removeFileChangeListener(fileListener);
                userLib = null;
            }
            String userPath = BlueJSettings.getDefault().getUserLibrariesAsClassPath();
            if (userPath.length() > 0) {
                StringTokenizer tokens = new StringTokenizer(userPath, ":", false);  // NOI18N
                while (tokens.hasMoreTokens()) {
                    File fil = new File(tokens.nextToken());
                    FileObject fo = FileUtil.toFileObject(fil);
                    if (fo != null && FileUtil.isArchiveFile(fo)) {
                        resources.add(ClassPathSupport.createResource(URLMapper.findURL(FileUtil.getArchiveRoot(fo), URLMapper.INTERNAL)));
                    }
                }
            }
        }
        return resources;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    public synchronized void fireChange() {
        resources = null;
        List lst = new ArrayList();
        lst.addAll(listeners);
        Iterator it = lst.iterator();
        PropertyChangeEvent evnt = new PropertyChangeEvent(this, ClassPathImplementation.PROP_RESOURCES, null, null);
        while (it.hasNext()) {
            PropertyChangeListener listener = (PropertyChangeListener)it.next();
            listener.propertyChange(evnt);
        }
    }
}
