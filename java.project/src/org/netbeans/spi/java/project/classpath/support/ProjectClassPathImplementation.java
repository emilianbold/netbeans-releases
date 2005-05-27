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

package org.netbeans.spi.java.project.classpath.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/** 
 * Implementation of a single classpath that is derived from list of Ant properties.
 */
final class ProjectClassPathImplementation implements ClassPathImplementation, PropertyChangeListener, Runnable {
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final File projectFolder;
    private List/*<PathResourceImplementation>*/ resources;
    private final PropertyEvaluator evaluator;
    private boolean dirty = false;
    private final List/*<String>*/ propertyNames;

    /**
     * Construct the implementation.
     * @param projectFolder the folder containing the project, used to resolve relative paths
     * @param propertyNames the names of an Ant properties which will supply the classpath
     * @param evaluator a property evaluator used to find the value of the classpath
     */
    public ProjectClassPathImplementation(File projectFolder, String[] propertyNames, PropertyEvaluator evaluator) {
        assert projectFolder != null && propertyNames != null && evaluator != null;
        this.projectFolder = projectFolder;
        this.evaluator = evaluator;
        this.propertyNames = Arrays.asList(propertyNames);
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    public synchronized List/*<PathResourceImplementation>*/ getResources() {
        if (this.resources == null) {
            this.resources = this.getPath ();
        }
        return this.resources;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {        
        if (!propertyNames.contains(evt.getPropertyName())) {
            // Not interesting to us.
            return;
        }
        // Coalesce changes; can come in fast after huge CP changes (#47910):
        // XXX any synch needed on dirty flag?
        if (!dirty) {
            dirty = true;
            ProjectManager.mutex().postReadRequest(this);
        }
    }
    
    public void run() {
        dirty = false;
        List newRoots = getPath ();
        boolean fire = false;
        synchronized (this) {
            if (this.resources != null && !this.resources.equals(newRoots)) {
                this.resources = newRoots;
                fire = true;
            }
        }
        if (fire) {
            support.firePropertyChange (PROP_RESOURCES,null,null);
        }
    }
    
    private List/*<PathResourceImplementation>*/ getPath() {
        List/*<PathResourceImplementation>*/ result = new ArrayList();
        Iterator it = propertyNames.iterator();
        while (it.hasNext()) {
            String prop = evaluator.getProperty((String) it.next());
            if (prop != null) {
                String[] pieces = PropertyUtils.tokenizePath(prop);
                for (int i = 0; i < pieces.length; i++) {                    
                    File f = PropertyUtils.resolveFile(this.projectFolder, pieces[i]); 
                    try {
                        URL entry = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(entry) || (f.isFile() && f.length()<4)) {    //XXX: Not yet closed archive file
                            entry = FileUtil.getArchiveRoot(entry);
                        } else if (!f.exists()) {
                            // if file does not exist (e.g. build/classes folder
                            // was not created yet) then corresponding File will
                            // not be ended with slash. Fix that.
                            assert !entry.toExternalForm().endsWith("/") : f; // NOI18N
                            entry = new URL(entry.toExternalForm() + "/"); // NOI18N
                        }
                        result.add(ClassPathSupport.createResource(entry));
                    } catch (MalformedURLException mue) {
                        assert false : mue;
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

}
