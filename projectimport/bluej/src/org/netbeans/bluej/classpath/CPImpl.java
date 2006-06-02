/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class CPImpl implements ClassPathImplementation {
    
    private List resources = null;
    private List listeners = new ArrayList();

    private BluejProject project;
    /** Creates a new instance of CPImpl */
    public CPImpl(BluejProject prj) {
        project = prj;
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
