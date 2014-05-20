/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.api.common.Roots;
import org.openide.util.Mutex;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.RootsAccessor;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Implementation of {@link Sources} interface.
 */
final class SourcesImpl implements Sources, SourceGroupModifierImplementation, PropertyChangeListener, ChangeListener  {

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final List<? extends Roots> roots;
    private boolean dirty;
    private final Map<String,SourceGroup[]> cachedGroups = new ConcurrentHashMap<String,SourceGroup[]>();
    private long eventId;
    private Sources delegate;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private SourceGroupModifierImplementation sgmi;
    private final FireAction fireTask = new FireAction();

    @SuppressWarnings("LeakingThisInConstructor")
    SourcesImpl(Project project, AntProjectHelper helper, PropertyEvaluator evaluator,
                Roots... roots) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.roots = Collections.unmodifiableList(Arrays.asList(roots));
        for (Roots r : this.roots) {
            r.addPropertyChangeListener(WeakListeners.propertyChange(this, r));
        }
        final SourcesHelper sh = initSources();
        assert sh != null;
        sgmi = sh.createSourceGroupModifierImplementation();
        delegate = sh.createSources(); // have to register external build roots eagerly
    }

    /**
     * Returns an array of SourceGroup of given type. It delegates to {@link SourcesHelper}.
     * This method firstly acquire the {@link ProjectManager#mutex} in read mode then it enters
     * into the synchronized block to ensure that just one instance of the {@link SourcesHelper}
     * is created. These instance is cleared also in the synchronized block by the
     * {@link J2SESources#fireChange} method.
     */
    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        final SourceGroup[] _cachedGroups = this.cachedGroups.get(type);
        if (_cachedGroups != null) {
            return _cachedGroups;
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                Sources _delegate;
                long myEventId;
                synchronized (SourcesImpl.this) {
                    if (dirty) {
                        delegate.removeChangeListener(SourcesImpl.this);
                        SourcesHelper sh = initSources();
                        sgmi = sh.createSourceGroupModifierImplementation();
                        delegate = sh.createSources();
                        delegate.addChangeListener(SourcesImpl.this);
                        dirty = false;
                    }
                    _delegate = delegate;
                    myEventId = ++eventId;
                }
                SourceGroup[] groups = _delegate.getSourceGroups(type);
                if (type.equals(Sources.TYPE_GENERIC)) {
                    FileObject libLoc = getSharedLibraryFolderLocation();
                    if (libLoc != null) {
                        //#204232 only return as separate source group if not inside the default project one.
                        boolean isIncluded = false;
                        for (SourceGroup sg : groups) {
                            if (FileUtil.isParentOf(sg.getRootFolder(), libLoc)) {
                                isIncluded = true;
                                break;
                            }
                        }
                        if (!isIncluded) {
                        SourceGroup[] grps = new SourceGroup[groups.length + 1];
                        System.arraycopy(groups, 0, grps, 0, groups.length);
                        grps[grps.length - 1] = GenericSources.group(project, libLoc,
                                "sharedlibraries", // NOI18N
                                NbBundle.getMessage(SourcesImpl.class, "LibrarySourceGroup_DisplayName"),
                                null, null);
                        groups = grps;
                        }
                    }
                }
                synchronized (SourcesImpl.this) {
                    if (myEventId == eventId) {
                        SourcesImpl.this.cachedGroups.put(type, groups);
                    }
                }
                return groups;
            }
        });
    }

    @Override
    public SourceGroup createSourceGroup(String type, String hint) {
        return sgmi.createSourceGroup(type, hint);
    }

    @Override
    public boolean canCreateSourceGroup(String type, String hint) {
        return sgmi.canCreateSourceGroup(type, hint);
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private FileObject getSharedLibraryFolderLocation() {
        String libLoc = helper.getLibrariesLocation();
        if (libLoc != null) {
            String libLocEval = evaluator.evaluate(libLoc);
            if (libLocEval != null) {
                final File file = helper.resolveFile(libLocEval);
                FileObject libLocFO = FileUtil.toFileObject(file);
                if (libLocFO != null) {
                    //#126366 this can happen when people checkout the project but not the libraries description
                    //that is located outside the project
                    FileObject libLocParent = libLocFO.getParent();
                    return libLocParent;
                }
            }
        }
        return null;
    }

    private SourcesHelper initSources() {
        final SourcesHelper sourcesHelper = new SourcesHelper(project, helper, evaluator);   //Safe to pass APH
        for (Roots r : roots) {
            if (RootsAccessor.getInstance().isSourceRoot(r)) {
                registerSources(sourcesHelper, r);
            } else {
                registerNonSources(sourcesHelper, r);
            }
        }
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT, false);
        return sourcesHelper;
    }

    private void registerSources(SourcesHelper sourcesHelper, Roots roots) {
        String[] propNames = roots.getRootProperties();
        String[] displayNames = roots.getRootDisplayNames();
        for (int i = 0; i < propNames.length; i++) {
            final String prop = propNames[i];
            final String loc = "${" + prop + "}"; // NOI18N
            final SourcesHelper.SourceRootConfig cfg = sourcesHelper.sourceRoot(loc);
            cfg.displayName(displayNames[i]);
            if (RootsAccessor.getInstance().supportIncludes(roots)) {
                final String includes = "${" + ProjectProperties.INCLUDES + "}"; // NOI18N
                final String excludes = "${" + ProjectProperties.EXCLUDES + "}"; // NOI18N
                cfg.includes(includes);
                cfg.excludes(excludes);
            }
            final String hint = RootsAccessor.getInstance().getHint(roots);
            if (hint != null) {
                cfg.hint(hint);
            }
            cfg.add();  // principal root
            final String type = RootsAccessor.getInstance().getType(roots);
            if (type != null) {
                cfg.type(type).add();    // typed root
            }
        }
    }

    private void registerNonSources(final SourcesHelper sourcesHelper, final Roots nonSources) {
        for (String nonSourceRootProp : nonSources.getRootProperties()) {
            sourcesHelper.addNonSourceRoot(String.format("${%s}", nonSourceRootProp));
        }
    }

    private void fireChange() {
        synchronized (this) {
            cachedGroups.clear();   //threading: CHM.clear is not atomic, the getSourceGroup may return staled data which is not a problem in this case.
            dirty = true;
        }
        ProjectManager.mutex().postReadRequest(fireTask.activate());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        // was listening to PROP_ROOT_PROPERTIES, changed to PROP_ROOTS in #143633 as changes
        // from SourceGroupModifierImplementation need refresh too
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            this.fireChange();
        }
    }

    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }


    private class FireAction implements Runnable {

        private AtomicBoolean fire = new AtomicBoolean();

        public void run() {
            if (fire.getAndSet(false)) {
                changeSupport.fireChange();
            }
        }

        FireAction activate() {
            this.fire.set(true);
            return this;
        }
    };
}
