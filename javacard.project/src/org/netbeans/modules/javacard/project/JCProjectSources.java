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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.util.Exceptions;

/**
 * Sources of Java Card project.
 */
public class JCProjectSources implements Sources, ChangeListener,
        PropertyChangeListener {

    private static final String BUILD_DIR_PROP = "${" + ProjectPropertyNames.PROJECT_PROP_BUILD_DIR + "}";    //NOI18N
    private static final String DIST_DIR_PROP = "${" + ProjectPropertyNames.PROJECT_PROP_DIST_DIR + "}";    //NOI18N

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private SourcesHelper sourcesHelper;
    private Sources delegate;
    /**
     * Flag to forbid multiple invocation of {@link SourcesHelper#registerExternalRoots}
     **/
    private boolean externalRootsRegistered;
    private final JCProject project;

    JCProjectSources(JCProject project, AntProjectHelper helper, PropertyEvaluator evaluator,
                SourceRoots sourceRoots) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.sourceRoots.addPropertyChangeListener(this);
        this.evaluator.addPropertyChangeListener(this);
        initSources(); // have to register external build roots eagerly
    }

    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {

            public SourceGroup[] run() {
                Sources _delegate;
                synchronized (JCProjectSources.this) {
                    if (delegate == null) {
                        delegate = initSources();
                        delegate.addChangeListener(JCProjectSources.this);
                    }
                    _delegate = delegate;
                }
                SourceGroup[] groups = _delegate.getSourceGroups(type);
                if (type.equals(Sources.TYPE_GENERIC)) {
                    FileObject libLoc = getSharedLibraryFolderLocation();
                    if (libLoc != null) {
                        SourceGroup[] grps = new SourceGroup[groups.length + 1];
                        System.arraycopy(groups, 0, grps, 0, groups.length);
                        grps[grps.length - 1] = GenericSources.group(null, libLoc,
                                "sharedlibraries", // NOI18N
                                NbBundle.getMessage(JCProjectSources.class,
                                "LibrarySourceGroup_DisplayName"), //NOI18N
                                null, null);
                        groups = grps;
                    }
                }
//                ProjectKind kind = ProjectKind.kindForProject(helper);
//                if (kind.isApplication()) {
//                    FileObject dir =
//                            helper.getProjectDirectory().getFileObject (kind == ProjectKind.WEB ?
//                                JCConstants.HTML_FILE_PATH : JCConstants.SCRIPTS_DIR_PATH);
//                    if (dir != null) {
//                        SourceGroup[] nue = new SourceGroup[groups.length + 1];
//                        System.arraycopy(groups, 0, nue, 0, groups.length);
//                        nue[nue.length - 1] = new ScriptsSourceGroup(kind, helper);
//                        groups = nue;
//                    }
//                }
                return groups;
            }
        });
    }

    private FileObject getSharedLibraryFolderLocation() {
        String libLoc = helper.getLibrariesLocation();
        if (libLoc != null) {
            String libLocEval = evaluator.evaluate(libLoc);
            File file = null;
            if (libLocEval != null) {
                file = helper.resolveFile(libLocEval);
            }
            FileObject libLocFO = FileUtil.toFileObject(file);
            if (libLocFO != null) {
                //#126366 this can happen when people checkout the project but not the libraries description
                //that is located outside the project
                FileObject libLocParent = libLocFO.getParent();
                return libLocParent;
            }
        }
        return null;
    }

    private Sources initSources() {
        this.sourcesHelper = new SourcesHelper(project, helper, evaluator);   //Safe to pass APH
        register(sourceRoots);
        this.sourcesHelper.addNonSourceRoot(BUILD_DIR_PROP);
        this.sourcesHelper.addNonSourceRoot(DIST_DIR_PROP);
        externalRootsRegistered = false;
        ProjectManager.mutex().postWriteRequest(new Runnable() {

            public void run() {
                if (!externalRootsRegistered) {
                    sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT, false);
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
            sourcesHelper.sourceRoot(loc).displayName(displayName).type(
                    JavaProjectConstants.SOURCES_TYPE_JAVA).add();
        }
    }

    private final Set<ChangeListener> listeners = Collections.synchronizedSet(new HashSet<ChangeListener>());
    public void addChangeListener(ChangeListener changeListener) {
        listeners.add(changeListener);
    }


    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChange() {
        synchronized (this) {
            if (delegate != null) {
                delegate.removeChangeListener(this);
                delegate = null;
            }
        }
        ChangeListener[] l = (ChangeListener[]) listeners.toArray(new ChangeListener[listeners.size()]);
        for (ChangeListener cl : l) {
            try {
                cl.stateChanged(new ChangeEvent(this));
            } catch (IllegalStateException e) {
                //http://netbeans.org/bugzilla/show_bug.cgi?id=182740
                //"Too many request processors" exception during project
                //metadata creation leaves metadata files locked and unreadable
                Exceptions.printStackTrace(e);
            }
        }
    }


    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) ||
            ProjectPropertyNames.PROJECT_PROP_BUILD_DIR.equals(propName)  ||
            ProjectPropertyNames.PROJECT_PROP_DIST_DIR.equals(propName)) {
            this.fireChange();
        }
    }


    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}
