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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.ejbjarproject.ui.EjbJarLogicalViewProvider;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

public class EjbJarSources implements Sources, PropertyChangeListener, ChangeListener {

    private static final String BUILD_DIR_PROP = "${" + EjbJarProjectProperties.BUILD_DIR + "}";    //NOI18N
    private static final String DIST_DIR_PROP = "${" + EjbJarProjectProperties.DIST_DIR + "}";    //NOI18N

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private Sources delegate;
    /**
     * Flag to forbid multiple invocation of {@link SourcesHelper#registerExternalRoots} 
     **/
    private boolean externalRootsRegistered;    
    private final List/*<ChangeListener>*/ listeners = new ArrayList();

    EjbJarSources(AntProjectHelper helper, PropertyEvaluator evaluator,
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
     * {@link EjbJarSources#fireChange} method.
     */
    public SourceGroup[] getSourceGroups(final String type) {
        return (SourceGroup[]) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Sources _delegate;
                synchronized (EjbJarSources.this) {
                    if (delegate == null) {
                        delegate = initSources();
                        delegate.addChangeListener(EjbJarSources.this);
                    }
                    _delegate = delegate;
                }
                return _delegate.getSourceGroups(type);
            }
        });
    }

    private Sources initSources() {
        final SourcesHelper h = new SourcesHelper(helper, evaluator);
        File projectDir = FileUtil.toFile(this.helper.getProjectDirectory());
        String[] propNames = sourceRoots.getRootProperties();
        String[] rootNames = sourceRoots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String displayName = rootNames[i];
            String prop = "${" + propNames[i] + "}";
            if (displayName.length() ==0) {
                //If the prop is src.dir use the default name
                if ("src.dir".equals(propNames[i])) {   //NOI18N
                    displayName = SourceRoots.DEFAULT_SOURCE_LABEL;
                }
                else {
                    //If the name is not given, it should be either a relative path in the project dir
                    //or absolute path when the root is not under the project dir
                    File sourceRoot = helper.resolveFile(evaluator.evaluate(prop));
                    if (sourceRoot != null) {
                        String srPath = sourceRoot.getAbsolutePath();
                        String pdPath = projectDir.getAbsolutePath() + File.separatorChar;
                        if (srPath.startsWith(pdPath)) {
                            displayName = srPath.substring(pdPath.length());
                        }
                        else {
                            displayName = sourceRoot.getAbsolutePath();
                        }
                    }
                    else {
                        displayName = SourceRoots.DEFAULT_SOURCE_LABEL;
                    }
                }
            }
            h.addPrincipalSourceRoot(prop, displayName, /*XXX*/null, null);
            h.addTypedSourceRoot(prop, JavaProjectConstants.SOURCES_TYPE_JAVA, displayName, /*XXX*/null, null);
        }
        propNames = testRoots.getRootProperties();
        rootNames = testRoots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String displayName = rootNames[i];
            String prop = "${" + propNames[i] + "}";
            if (displayName.length() ==0) {
                //If the prop is test.src.dir use the default name
                if ("test.src.dir".equals(propNames[i])) {   //NOI18N
                    displayName = SourceRoots.DEFAULT_TEST_LABEL;
                }
                else {
                    //If the name is not given, it should be either a relative path in the project dir
                    //or absolute path when the root is not under the project dir
                    File sourceRoot = helper.resolveFile(evaluator.evaluate(prop));
                    if (sourceRoot != null) {
                        String srPath = sourceRoot.getAbsolutePath();
                        String pdPath = projectDir.getAbsolutePath() + File.separatorChar;
                        if (srPath.startsWith(pdPath)) {
                            displayName = srPath.substring(pdPath.length());
                        }
                        else {
                            displayName = sourceRoot.getAbsolutePath();
                        }
                    }
                    else {
                        displayName = SourceRoots.DEFAULT_TEST_LABEL;
                    }
                }
            }
            h.addPrincipalSourceRoot(prop, displayName, /*XXX*/null, null);
            String configFilesLabel = org.openide.util.NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Node_DocBase"); //NOI18N
            h.addPrincipalSourceRoot("${"+EjbJarProjectProperties.META_INF+"}", configFilesLabel, /*XXX*/null, null);
            h.addTypedSourceRoot(prop, JavaProjectConstants.SOURCES_TYPE_JAVA, displayName, /*XXX*/null, null);
        }
        
        h.addNonSourceRoot(BUILD_DIR_PROP);
        h.addNonSourceRoot(DIST_DIR_PROP);
        
        externalRootsRegistered = false;
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                if (!externalRootsRegistered) {
                    h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    externalRootsRegistered = true;
                }
            }
        });
        return h.createSources();
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
            _listeners = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(ev);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) || EjbJarProjectProperties.BUILD_DIR.equals(propName) || EjbJarProjectProperties.DIST_DIR.equals(propName))
            this.fireChange();
    }

    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}
