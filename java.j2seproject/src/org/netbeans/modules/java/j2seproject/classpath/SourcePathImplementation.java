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
package org.netbeans.modules.java.j2seproject.classpath;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Utilities;


/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List resources;
    private WeakReference/*<SourceRoots>*/ sourceRoots;
    private WeakReference/*<AntProjectHelper>*/ projectHelper;
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public SourcePathImplementation(SourceRoots sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = new CleanableWeakReference (sourceRoots);
        sourceRoots.addPropertyChangeListener (this);
    }
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     * @param projectHelper used to obtain the project root
     */
    public SourcePathImplementation(SourceRoots sourceRoots, AntProjectHelper projectHelper) {
        assert sourceRoots != null && projectHelper != null;
        this.sourceRoots = new CleanableWeakReference (sourceRoots);
        sourceRoots.addPropertyChangeListener (this);
        this.projectHelper= new CleanableWeakReference (projectHelper);
    }

    public List /*<PathResourceImplementation>*/ getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }        
        
        SourceRoots sourceRoots = (SourceRoots) this.sourceRoots.get();        
        if (sourceRoots == null) {
            return Collections.EMPTY_LIST;
        }
        AntProjectHelper projectHelper = this.projectHelper == null ? null : (AntProjectHelper) this.projectHelper.get ();
        URL[] roots = sourceRoots.getRootURLs();                                
        synchronized (this) {
            if (this.resources == null) {
                List result = new ArrayList (roots.length);
                for (int i = 0; i < roots.length; i++) {
                    PathResourceImplementation res = ClassPathSupport.createResource(roots[i]);
                    result.add (res);
                }
                // adds build/generated/wsclient to resources to be available for code completion
                if (projectHelper!=null) {
                    try {
                        String rootURL = projectHelper.getProjectDirectory().getURL().toString();
                        URL url = new URL(rootURL+"build/generated/wsclient/"); //NOI18N
                        if (url!=null) result.add(ClassPathSupport.createResource(url));
                    } catch (MalformedURLException ex) {
                    } catch (FileStateInvalidException ex){}
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
    
    
    private class CleanableWeakReference extends WeakReference implements Runnable {
        
        public CleanableWeakReference (Object obj) {
            super (obj, Utilities.activeReferenceQueue());
        }
        
        public void run () {
            synchronized (SourcePathImplementation.this) {
                SourcePathImplementation.this.resources = null;
            }
            SourcePathImplementation.this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
        
    }

}
