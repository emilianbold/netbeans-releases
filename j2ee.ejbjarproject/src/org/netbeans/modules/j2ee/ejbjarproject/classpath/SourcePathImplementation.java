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
package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;


/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List resources;
    private SourceRoots sourceRoots;

    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public SourcePathImplementation(SourceRoots sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
        this.sourceRoots.addPropertyChangeListener (this);
    }

    public List /*<PathResourceImplementation>*/ getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }        
        URL[] roots = this.sourceRoots.getRootURLs();                                
        synchronized (this) {
            if (this.resources == null) {
                List result = new ArrayList (roots.length);
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
