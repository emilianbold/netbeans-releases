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
package org.netbeans.modules.j2ee.clientproject.classpath;

import java.beans.PropertyChangeEvent;
import java.io.File;
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
import org.netbeans.modules.j2ee.clientproject.SourceRoots;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Utilities;


/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final String PROP_BUILD_DIR = "build.dir";   //NOI18N
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List resources;
    private final SourceRoots sourceRoots;
    private final AntProjectHelper projectHelper;
    private final PropertyEvaluator evaluator;
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public SourcePathImplementation(SourceRoots sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
        this.projectHelper = null;
        this.evaluator = null;
        sourceRoots.addPropertyChangeListener (this);
    }
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     * @param projectHelper used to obtain the project root
     */
    public SourcePathImplementation(SourceRoots sourceRoots, AntProjectHelper projectHelper, PropertyEvaluator evaluator) {
        assert sourceRoots != null && projectHelper != null && evaluator != null;
        this.sourceRoots = sourceRoots;
        sourceRoots.addPropertyChangeListener (this);
        this.projectHelper = projectHelper;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener (this);
    }

    public List /*<PathResourceImplementation>*/ getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }                                
        URL[] roots = sourceRoots.getRootURLs();                                
        String buildDir = null;
        if (projectHelper != null)
            buildDir = projectHelper.getStandardPropertyEvaluator().getProperty(PROP_BUILD_DIR);
        synchronized (this) {
            if (this.resources == null) {
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(roots.length);
                for (int i = 0; i < roots.length; i++) {
                    PathResourceImplementation res = ClassPathSupport.createResource(roots[i]);
                    result.add (res);
                }
                // adds build/generated/wsclient and build/generated/wsimport/client to resources to be available for code completion
                if (projectHelper!=null) {
                    try {
                        if (buildDir != null) {
                            // generated/wsclient
                            File f =  new File (this.projectHelper.resolveFile (buildDir),"generated/wsclient"); //NOI18N
                            URL url = f.toURI().toURL();
                            if (!f.exists()) {  //NOI18N
                                assert !url.toExternalForm().endsWith("/");  //NOI18N
                                url = new URL (url.toExternalForm()+'/');   //NOI18N
                            }
                            result.add(ClassPathSupport.createResource(url));
                            // generated/wsimport/client
                            f = new File (projectHelper.resolveFile(buildDir),"generated/wsimport/client"); //NOI18N
                            url = f.toURI().toURL();
                            if (!f.exists()) {  //NOI18N
                                assert !url.toExternalForm().endsWith("/");  //NOI18N
                                url = new URL (url.toExternalForm()+'/');   //NOI18N
                            }
                            result.add(ClassPathSupport.createResource(url));
                        }
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault ().notify (ex);
                    }
                }
                this.resources = Collections.unmodifiableList(result);
            }
            return this.resources;
        }
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
        else if (this.evaluator != null && evt.getSource() == this.evaluator && 
            (evt.getPropertyName() == null || PROP_BUILD_DIR.equals(evt.getPropertyName()))) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
    }

}
