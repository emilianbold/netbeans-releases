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

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;

import org.netbeans.spi.project.support.ant.PropertyUtils;

final class ProjectClassPathImplementation implements ClassPathImplementation, AntProjectListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private AntProjectHelper helper;
    private String propertyName;
    private List resources;

    public ProjectClassPathImplementation (AntProjectHelper helper, String propertyName) {
        assert helper != null && propertyName != null;
        this.helper = helper;
        this.propertyName = propertyName;
        this.helper.addAntProjectListener (this);
    }

    public synchronized List /*<PathResourceImplementation>*/ getResources() {
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


    public void configurationXmlChanged(AntProjectEvent ev) {
        //Not interesting
    }

    public void propertiesChanged(AntProjectEvent ev) {
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

    private List getPath() {
        List result = new ArrayList ();
        String prop = helper.getStandardPropertyEvaluator ().getProperty (propertyName);
        if (prop != null) {
            String[] pieces = PropertyUtils.tokenizePath(prop);
            for (int i = 0; i < pieces.length; i++) {
                File f = helper.resolveFile(pieces[i]);
                try {
                    URL entry;
                    // if file does not exist (e.g. build/classes folder
                    // was not created yet) then corresponding File will
                    // not be ended with slash. Fix that.
                    if (f.getPath().toLowerCase().endsWith(".jar")) {
                        entry = FileUtil.getArchiveRoot(f.toURI().toURL());
                    } else {
                        entry = f.toURI().toURL();
                        if (!f.exists()) {
                            assert !entry.toExternalForm().endsWith("/") : f;
                            entry = new URL(entry.toExternalForm() + "/");
                        }
                    }
                    if (entry != null) {
                        result.add(ClassPathSupport.createResource(entry));
                    }
                } catch (MalformedURLException mue) {
                    ErrorManager.getDefault().notify(mue);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

}
