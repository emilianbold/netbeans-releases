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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

public class WebSources implements Sources, PropertyChangeListener, ChangeListener {

    private static final String BUILD_DIR_PROP = "${" + WebProjectProperties.BUILD_DIR + "}";    //NOI18N
    private static final String DIST_DIR_PROP = "${" + WebProjectProperties.DIST_DIR + "}";    //NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private Sources delegate;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();

    WebSources(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
        this.evaluator.addPropertyChangeListener(this);
        initSources(); // have to register external build roots eagerly
    }

    public SourceGroup[] getSourceGroups(final String type) {
        return (SourceGroup[]) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Sources _delegate;
                synchronized (WebSources.this) {
                    if (delegate == null) {
                        delegate = initSources();
                        delegate.addChangeListener(WebSources.this);
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
            h.addTypedSourceRoot(prop, JavaProjectConstants.SOURCES_TYPE_JAVA, displayName, /*XXX*/null, null);
        }
        
        //Web Pages
        String webModuleLabel = org.openide.util.NbBundle.getMessage(WebProjectProperties.class, "LBL_Node_WebModule"); //NOI18N
        String webPagesLabel = org.openide.util.NbBundle.getMessage(WebProjectProperties.class, "LBL_Node_DocBase"); //NOI18N
        h.addPrincipalSourceRoot("${"+ WebProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null); //NOI18N
        h.addPrincipalSourceRoot("${"+ WebProjectProperties.WEB_DOCBASE_DIR+"}", webPagesLabel, /*XXX*/null, null); //NOI18N
        h.addTypedSourceRoot("${"+ WebProjectProperties.WEB_DOCBASE_DIR+"}", WebProjectConstants.TYPE_DOC_ROOT, webPagesLabel, /*XXX*/null, null); //NOI18N
        h.addTypedSourceRoot("${"+ WebProjectProperties.WEB_DOCBASE_DIR+"}/WEB-INF", WebProjectConstants.TYPE_WEB_INF, /*XXX I18N*/ "WEB-INF", /*XXX*/null, null); //NOI18N
        
        h.addNonSourceRoot(BUILD_DIR_PROP);
        h.addNonSourceRoot(DIST_DIR_PROP);

        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
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
        synchronized (listeners) {
            delegate = null;
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
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) || WebProjectProperties.BUILD_DIR.equals(propName) || WebProjectProperties.DIST_DIR.equals(propName))
            this.fireChange();
    }

    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}
