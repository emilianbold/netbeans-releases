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
package org.netbeans.modules.j2ee.earproject.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class ProjectClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private AntProjectHelper helper;
    private String expression;
    private boolean isProperty;
    private String resolved;
    private List resources;
    private final PropertyEvaluator evaluator;

    /**
     * Construct the implementation.
     * @param helper an Ant project, used to resolve file paths
     * @param propertyName the name of an Ant property or an expression which will supply the classpath
     * @param evaluator a property evaluator used to find the value of the classpath
     */
    public ProjectClassPathImplementation(AntProjectHelper helper, String expression, PropertyEvaluator evaluator, boolean isProperty) {
        assert helper != null && expression != null;
        this.helper = helper;
        this.evaluator = evaluator;
        this.expression = expression;
        this.isProperty = isProperty;
        if (isProperty) {
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        } else {
            resolved = evaluator.evaluate (expression);
        }
    }
        
    /**
     * Construct the implementation.
     * @param helper an Ant project, used to resolve file paths
     * @param propertyName the name of an Ant property which will supply the classpath
     * @param evaluator a property evaluator used to find the value of the classpath
     */
    public ProjectClassPathImplementation(AntProjectHelper helper, String propertyName, PropertyEvaluator evaluator) {
        this (helper, propertyName, evaluator, true);
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


    public void propertyChange(PropertyChangeEvent evt) {
        if (isProperty && !evt.getPropertyName().equals(expression)) {
            // Not interesting to us.
            return;
        }
        if (!isProperty) {
            String eval = evaluator.evaluate (expression);
            if (eval == resolved) {
                return;
            } else {
                resolved = eval;
            }
        }
        
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
        String prop = isProperty ? evaluator.getProperty(expression) : resolved;
        if (prop != null) {
            String[] pieces = PropertyUtils.tokenizePath(prop);
            for (int i = 0; i < pieces.length; i++) {
                File f = helper.resolveFile(pieces[i]);
                try {
                    URL entry = f.toURI().toURL();
                    if (FileUtil.isArchiveFile(entry)) {
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
        return Collections.unmodifiableList(result);
    }

}
