/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.Mutex;

import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.ChangeSupport;

/**
 * Implementation of {@link Sources} interface for WebProject.
 */
public class WebSources implements Sources, PropertyChangeListener, ChangeListener {

    private static final String BUILD_DIR_PROP = "${" + WebProjectProperties.BUILD_DIR + "}";    //NOI18N
    private static final String DIST_DIR_PROP = "${" + WebProjectProperties.DIST_DIR + "}";    //NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private Sources delegate;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean dirty;
    private volatile Map<String, SourceGroup[]> cachedGroups = new HashMap<String, SourceGroup[]>();
    private long eventId;

    WebSources(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
        this.evaluator.addPropertyChangeListener(this);
        delegate = initSources(); // have to register external build roots eagerly
    }

    /**
     * Returns an array of SourceGroup of given type. It delegates to {@link SourcesHelper}.
     * This method firstly acquire the {@link ProjectManager#mutex} in read mode then it enters
     * into the synchronized block to ensure that just one instance of the {@link SourcesHelper}
     * is created. These instance is cleared also in the synchronized block by the
     * {@link WebSources#fireChange} method.
     */
    public SourceGroup[] getSourceGroups(final String type) {
        final SourceGroup[] _cachedGroups = this.cachedGroups.get(type);
        if (_cachedGroups != null) {
            return _cachedGroups;
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                Sources _delegate;
                long myEventId;
                synchronized (WebSources.this) {
                    if (dirty) {
                        delegate.removeChangeListener(WebSources.this);
                        delegate = initSources();
                        delegate.addChangeListener(WebSources.this);
                        dirty = false;
                    }
                    _delegate = delegate;
                    myEventId = ++eventId;
                }
                SourceGroup[] groups = _delegate.getSourceGroups(type);
                synchronized (WebSources.this) {
                    if (myEventId == eventId) {
                        WebSources.this.cachedGroups.put(type, groups);
                    }
                }
    
                return groups;
            }
        });
    }

    SourceGroupModifierImplementation sgmi;

    SourceGroupModifierImplementation getSourceGroupModifierImplementation() {
        return sgmi;
    }

    private Sources initSources() {
        SourcesHelper sourcesHelper = new SourcesHelper(project, helper, evaluator);
        register(sourcesHelper, sourceRoots, JavaProjectConstants.SOURCES_HINT_MAIN);
        register(sourcesHelper, testRoots, JavaProjectConstants.SOURCES_HINT_TEST);
        
        //Web Pages
        String webModuleLabel = org.openide.util.NbBundle.getMessage(WebSources.class, "LBL_Node_WebModule"); //NOI18N
        String webPagesLabel = org.openide.util.NbBundle.getMessage(WebSources.class, "LBL_Node_DocBase"); //NOI18N
        String webInfLabel = org.openide.util.NbBundle.getMessage(WebSources.class, "LBL_Node_WebInf"); //NOI18N
        String includes = "${" + ProjectProperties.INCLUDES + "}"; // NOI18N
        String excludes = "${" + ProjectProperties.EXCLUDES + "}"; // NOI18N
        sourcesHelper.sourceRoot("${"+ WebProjectProperties.SOURCE_ROOT+"}").displayName(webModuleLabel).add(); //NOI18N
        sourcesHelper.sourceRoot("${"+ WebProjectProperties.WEB_DOCBASE_DIR+"}").includes(includes).excludes(excludes)
                .displayName(webPagesLabel).add()   // principal root
                .type(WebProjectConstants.TYPE_DOC_ROOT).add(); // typed root
//        sourcesHelper.addTypedSourceRoot("${"+ WebProjectProperties.WEB_DOCBASE_DIR+"}/WEB-INF", WebProjectConstants.TYPE_WEB_INF, /*XXX I18N*/ "WEB-INF", /*XXX*/null, null); //NOI18N
        sourcesHelper.sourceRoot("${" + WebProjectProperties.WEBINF_DIR + "}").type(WebProjectConstants.TYPE_WEB_INF)// NOI18N
                .displayName(webInfLabel).add();
        
        sourcesHelper.addNonSourceRoot(BUILD_DIR_PROP);
        sourcesHelper.addNonSourceRoot(DIST_DIR_PROP);
        
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT, false);
        sgmi = sourcesHelper.createSourceGroupModifierImplementation();
        return sourcesHelper.createSources();
    }

    private void register(SourcesHelper sourcesHelper, SourceRoots roots, String hint) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            String includes = "${" + ProjectProperties.INCLUDES + "}"; // NOI18N
            String excludes = "${" + ProjectProperties.EXCLUDES + "}"; // NOI18N
            sourcesHelper.sourceRoot(loc).includes(includes).excludes(excludes).hint(hint).displayName(displayName)
                    .add() // principal root
                    .type(JavaProjectConstants.SOURCES_TYPE_JAVA).add();    // typed root
        }
     }
    
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private void fireChange() {
        synchronized (this) {
            cachedGroups.clear();
            dirty = true;
        }
        changeSupport.fireChange();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        // was listening to PROP_ROOT_PROPERTIES, changed to PROP_ROOTS in #143633 as changes
        // from SourceGroupModifierImplementation need refresh too
        if (SourceRoots.PROP_ROOTS.equals(propName)  
                || WebProjectProperties.BUILD_DIR.equals(propName)
                || WebProjectProperties.DIST_DIR.equals(propName))
            this.fireChange();
    }

    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}
