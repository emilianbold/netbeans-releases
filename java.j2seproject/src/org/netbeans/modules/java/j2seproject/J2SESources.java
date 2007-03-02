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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.openide.util.Mutex;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Implementation of {@link Sources} interface for J2SEProject.
 */
public class J2SESources implements Sources, PropertyChangeListener, ChangeListener  {
    
    private static final String BUILD_DIR_PROP = "${" + J2SEProjectProperties.BUILD_DIR + "}";    //NOI18N
    private static final String DIST_DIR_PROP = "${" + J2SEProjectProperties.DIST_DIR + "}";    //NOI18N

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private SourcesHelper sourcesHelper;
    private Sources delegate;
    /**
     * Flag to forbid multiple invocation of {@link SourcesHelper#registerExternalRoots} 
     **/
    private boolean externalRootsRegistered;    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    J2SESources(AntProjectHelper helper, PropertyEvaluator evaluator,
                SourceRoots sourceRoots, SourceRoots testRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);        
        this.evaluator.addPropertyChangeListener(this);
        initSources(); // have to register external build roots eagerly
    }

    /**
     * Returns an array of SourceGroup of given type. It delegates to {@link SourcesHelper}.
     * This method firstly acquire the {@link ProjectManager#mutex} in read mode then it enters
     * into the synchronized block to ensure that just one instance of the {@link SourcesHelper}
     * is created. These instance is cleared also in the synchronized block by the
     * {@link J2SESources#fireChange} method.
     */
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                Sources _delegate;
                synchronized (J2SESources.this) {
                    if (delegate == null) {                    
                        delegate = initSources();
                        delegate.addChangeListener(J2SESources.this);
                    }
                    _delegate = delegate;
                }
                return _delegate.getSourceGroups(type);
            }
        });
    }

    private Sources initSources() {        
        this.sourcesHelper = new SourcesHelper(helper, evaluator);   //Safe to pass APH        
        register(sourceRoots);
        register(testRoots);
        this.sourcesHelper.addNonSourceRoot (BUILD_DIR_PROP);
        this.sourcesHelper.addNonSourceRoot(DIST_DIR_PROP);
        externalRootsRegistered = false;
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {                
                if (!externalRootsRegistered) {
                    sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    externalRootsRegistered = true;
                }
            }
        });
        return this.sourcesHelper.createSources();
    }

    private void register(SourceRoots roots) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            String includes = "${" + J2SEProjectProperties.INCLUDES + "}"; // NOI18N
            String excludes = "${" + J2SEProjectProperties.EXCLUDES + "}"; // NOI18N
            sourcesHelper.addPrincipalSourceRoot(loc, includes, excludes, displayName, null, null); // NOI18N
            sourcesHelper.addTypedSourceRoot(loc, includes, excludes, JavaProjectConstants.SOURCES_TYPE_JAVA, displayName, null, null); // NOI18N
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.remove(changeListener);
        }
    }

    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (delegate != null) {
                delegate.removeChangeListener(this);
                delegate = null;
            }
        }
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : _listeners) {
            l.stateChanged(ev);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) ||
            J2SEProjectProperties.BUILD_DIR.equals(propName)  ||
            J2SEProjectProperties.DIST_DIR.equals(propName)) {
            this.fireChange();
        }
    }
    
    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}
