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
package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.web.project.SourceRoots;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;


/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class JspSourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List resources;
    private SourceRoots sourceRoots;
    private FileObject documentBaseDir;

    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public JspSourcePathImplementation(FileObject documentBaseDir, SourceRoots sourceRoots) {
        assert sourceRoots != null;
        // assert documentBaseDir != null;
        this.documentBaseDir = documentBaseDir;
        this.sourceRoots = sourceRoots;
        this.sourceRoots.addPropertyChangeListener (this);
    }

    public List /*<PathResourceImplementation>*/ getResources() {
        synchronized (this) {
            if (this.resources != null)
                return resources;
        }
        URL[] roots = this.sourceRoots.getRootURLs();
        PathResourceImplementation documentBaseDirRes = null;
        if (documentBaseDir != null) {
            try {
                documentBaseDirRes = ClassPathSupport.createResource(documentBaseDir.getURL());
            }
            catch (FileStateInvalidException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        synchronized (this) {
            if (this.resources == null) {
                List result = new ArrayList ();
                if (documentBaseDirRes != null) {
                    result.add(documentBaseDirRes);
                }
                for (int i = 0; i < roots.length; i++) {
                    PathResourceImplementation res = ClassPathSupport.createResource(roots[i]);
                    result.add (res);
                }
                this.resources = Collections.unmodifiableList(result);
            }
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
        if (SourceRoots.PROP_ROOTS.equals (evt.getPropertyName())) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
    }

}
