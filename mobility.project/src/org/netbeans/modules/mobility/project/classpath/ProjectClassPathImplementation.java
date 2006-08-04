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

package org.netbeans.modules.mobility.project.classpath;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public abstract class ProjectClassPathImplementation implements ClassPathImplementation, AntProjectListener, Runnable {
    
    final private PropertyChangeSupport support = new PropertyChangeSupport(this);
    final private AntProjectHelper helper;
    private List<PathResourceImplementation> resources;
    private String path;
    
    
    public ProjectClassPathImplementation(AntProjectHelper helper) {
        assert helper != null;
        this.helper = helper;
        this.helper.addAntProjectListener(this);
    }
    
    public List<PathResourceImplementation> getResources() {
        if (this.resources == null) {
            final String newPath = evaluatePath();
            if (this.resources == null) {
                final List<PathResourceImplementation> newResources = createResources(newPath);
                synchronized (this) {
                    if (this.resources == null) {
                        this.path = newPath;
                        this.resources = newResources;
                    }
                }
            }
        }
        return this.resources;
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    public void addResource(@SuppressWarnings("unused")
	final PathResourceImplementation resource) {
        //TODO: Implement this
        throw new UnsupportedOperationException();
    }
    
    public void removeResource(final PathResourceImplementation resource) {
        final List<PathResourceImplementation> l = this.getResources();
        if (l.contains(resource)) {
            //TODO: Implement this
            throw new UnsupportedOperationException();
        }
    }
    
    public void reorder(@SuppressWarnings("unused")
	final PathResourceImplementation[] order) throws IllegalArgumentException {
        //TODO: Implement this
        throw new UnsupportedOperationException();
    }
    
    
    public void configurationXmlChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
        //Not interesting
    }
    
    public void propertiesChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        final String newPath = evaluatePath();
        boolean fire = false;
        if (this.resources == null || newPath == null || !newPath.equals(this.path)) {
            final List<PathResourceImplementation> newResources = createResources(newPath);
            synchronized (this) {
                if (this.resources == null || newPath == null || !newPath.equals(this.path)) {
                    this.path = newPath;
                    this.resources = newResources;
                    fire = true;
                }
            }
        }
        if (fire) {
            support.firePropertyChange(PROP_RESOURCES,null,null);
        }
    }
    
    private List<PathResourceImplementation> createResources(final String _path) {
        final List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        if (_path != null) {
            final String[] pieces = PropertyUtils.tokenizePath(_path);
            for (int i = 0; i < pieces.length; i++) {
                final File f = FileUtil.normalizeFile(helper.resolveFile(pieces[i]));
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
    
    abstract protected String evaluatePath();
}
