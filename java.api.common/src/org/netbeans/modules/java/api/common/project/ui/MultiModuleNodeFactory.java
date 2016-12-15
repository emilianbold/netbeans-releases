/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
public final class MultiModuleNodeFactory implements NodeFactory {
    private static final RequestProcessor RP = new RequestProcessor(MultiModuleNodeFactory.class);
    private final MultiModule sourceModules;
    private final MultiModule testModules;

    private MultiModuleNodeFactory(
            @NonNull final MultiModule sourceModules,
            @NonNull final MultiModule testModules) {
        Parameters.notNull("sourceModules", sourceModules); //NOI18N
        Parameters.notNull("testModules", testModules);     //NOI18N
        this.sourceModules = sourceModules;
        this.testModules = testModules;
    }

    @Override
    public NodeList<?> createNodes(@NonNull final Project project) {
        return new Nodes(project, sourceModules, testModules);
    }

    private static final class Nodes implements NodeList<ModuleKey>, PropertyChangeListener {
        private final Project project;
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final ChangeSupport listeners;

        Nodes(
                @NonNull final Project project,
                @NonNull final MultiModule sourceModules,
                @NonNull final MultiModule testModules) {
            Parameters.notNull("project", project);     //NOI18N
            Parameters.notNull("sourceModules", sourceModules); //NOI18N
            Parameters.notNull("testModules", testModules);     //NOI18N
            this.project = project;
            this.sourceModules = sourceModules;
            this.testModules = testModules;
            this.listeners = new ChangeSupport(this);
        }

        @Override
        public List<ModuleKey> keys() {
            return Stream.concat(
                    this.sourceModules.getModuleNames().stream(),
                    this.testModules.getModuleNames().stream())
                .sorted()
                .distinct()
                .map((name) -> new ModuleKey(project, name, sourceModules, testModules))
                .collect(Collectors.toList());
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            this.listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.listeners.removeChangeListener(l);
        }

        @Override
        public Node node(@NonNull final ModuleKey key) {
            return new ModuleNode(key);
        }

        @Override
        public void addNotify() {
            this.sourceModules.addPropertyChangeListener(this);
            this.testModules.addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            this.sourceModules.removePropertyChangeListener(this);
            this.testModules.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (MultiModule.PROP_MODULES.equals(evt.getPropertyName())) {
                this.listeners.fireChange();
            }
        }
    }

    private static final class ModuleKey {
        private final Project project;
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final String moduleName;

        ModuleKey(
                @NonNull final Project project,
                @NonNull final String moduleName,
                @NonNull final MultiModule sourceModules,
                @NonNull final MultiModule testModules) {
            Parameters.notNull("project", project);
            Parameters.notNull("moduleName", moduleName);       //NOI18N
            Parameters.notNull("sourceModules", sourceModules); //NOI18N
            Parameters.notNull("testModules", testModules);     //NOI18N
            this.project = project;
            this.moduleName = moduleName;
            this.sourceModules = sourceModules;
            this.testModules = testModules;
        }

        @NonNull
        String getModuleName() {
            return moduleName;
        }

        @NonNull
        MultiModule getSourceModules() {
            return sourceModules;
        }

        @NonNull
        MultiModule getTestModules() {
            return testModules;
        }

        @NonNull
        Project getProject() {
            return project;
        }

        @Override
        public int hashCode() {
            return moduleName.hashCode();
        }

        @Override
        public boolean equals(@NullAllowed final Object other) {
            if (other == this) {
                return true;
            }
            if (other.getClass() != ModuleKey.class) {
                return false;
            }
            return ((ModuleKey)other).moduleName.equals(this.moduleName);
        }
    }

    private static final class ModuleNode extends AbstractNode {
        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/api/common/project/ui/resources/module.png";
        private final MultiModule modules;
        private final MultiModule testModules;
        private final String moduleName;
        private volatile String shortDesc;

        ModuleNode(@NonNull final ModuleKey key) {
            super(ModuleChildren.create(key), Lookup.EMPTY);
            this.modules = key.getSourceModules();
            this.testModules = key.getTestModules();
            this.moduleName = key.getModuleName();
            setIconBaseWithExtension(ICON);
            setName(moduleName);
        }

        @Override
        public String getShortDescription() {
            String res = shortDesc;
            if (res == null) {
                Collection<FileObject> locs = new HashSet<>();
                locs.addAll(modules.getModulePath().findAllResources(moduleName));
                locs.addAll(testModules.getModulePath().findAllResources(moduleName));
                if (locs.size() > 1) {
                    locs = new ArrayList<>(locs);
                    Collections.sort(
                            (List<FileObject>)locs,
                            (a,b)->a.getPath().compareTo(b.getPath()));
                }
                final StringBuilder sb = new StringBuilder();
                boolean cadr = false;
                for (FileObject fo : locs) {
                    if (cadr) {
                        sb.append('\n');    //NOI18N
                    } else {
                        cadr = true;
                    }
                    sb.append(FileUtil.getFileDisplayName(fo));
                }
                res = shortDesc = sb.toString();
            }
            return res;
        }
    }


    @NonNull
    public static MultiModuleNodeFactory create(
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots srcRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule mods = MultiModule.getOrCreate(sourceModules, srcRoots);
        final MultiModule testMods = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleNodeFactory(mods, testMods);
    }

    private static final class ModuleChildren extends Children.Keys<Pair<SourceGroup,Boolean>> implements PropertyChangeListener {
        private final String moduleName;
        private final Sources sources;
        private final MultiModule srcModule;
        private final MultiModule testModule;
        private final RequestProcessor.Task refresh;
        private final AtomicReference<ClassPath> srcPath;
        private final AtomicReference<ClassPath> testPath;

        private ModuleChildren(
                @NonNull final String moduleName,
                @NonNull final Sources sources,
                @NonNull final MultiModule srcModule,
                @NonNull final MultiModule testModule) {
            Parameters.notNull("moduleName", moduleName);   //NOI18N
            Parameters.notNull("sources", sources);         //NOI18N
            Parameters.notNull("srcModule", srcModule);     //NOI18N
            Parameters.notNull("testModule", testModule);   //NOI18N
            this.moduleName = moduleName;
            this.sources = sources;
            this.srcModule = srcModule;
            this.testModule = testModule;
            this.srcPath = new AtomicReference<>();
            this.testPath = new AtomicReference<>();
            refresh = RP.create(()->setKeys(createKeys()));
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            ClassPath cp = srcModule.getModuleSources(moduleName);
            if (cp == null) {
                cp = ClassPath.EMPTY;
            }
            if (srcPath.compareAndSet(null, cp)) {
                cp.addPropertyChangeListener(this);
            }
            cp = testModule.getModuleSources(moduleName);
            if (cp == null) {
                cp = ClassPath.EMPTY;
            }
            if (testPath.compareAndSet(null, cp)) {
                cp.addPropertyChangeListener(this);
            }
            setKeys(createKeys());
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            ClassPath cp = srcPath.get();
            if (cp != null && srcPath.compareAndSet(cp, null)) {
                cp.removePropertyChangeListener(this);
            }
            cp = testPath.get();
            if (cp != null && testPath.compareAndSet(cp, null)) {
                cp.removePropertyChangeListener(this);
            }
            setKeys(Collections.emptySet());
        }

        @Override
        @NonNull
        protected Node[] createNodes(@NonNull final Pair<SourceGroup,Boolean> key) {
            Node n = PackageView.createPackageView(key.first());
            if (key.second()) {
                n = new TestRootNode(n);
            }
            return new Node[] {n};
        }

        @NonNull
        private Collection<? extends Pair<SourceGroup,Boolean>> createKeys() {
            final java.util.Map<FileObject,SourceGroup> grpsByRoot = new HashMap<>();
            for (SourceGroup g : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                grpsByRoot.put(g.getRootFolder(), g);
            }
            final Comparator<FileObject> foc = (a,b) -> a.getNameExt().compareTo(b.getNameExt());
            return Stream.concat(
                    Arrays.stream(srcPath.get().getRoots())
                        .sorted(foc)
                        .map((fo) -> Pair.of(fo,false)),
                    Arrays.stream(testPath.get().getRoots())
                        .sorted(foc)
                        .map((fo) -> Pair.of(fo,true)))
                    .map((p) -> {
                        final SourceGroup g = grpsByRoot.get(p.first());
                        return g == null ?
                                null :
                                Pair.of(g,p.second());
                     })
                    .filter((p) -> p != null)
                    .collect(Collectors.toList());
        }

        @NonNull
        static ModuleChildren create(@NonNull final ModuleKey key) {
            return new ModuleChildren(
                    key.getModuleName(),
                    key.getProject().getLookup().lookup(Sources.class),
                    key.getSourceModules(),
                    key.getTestModules());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                refresh.schedule(100);
            }
        }
    }

    private static final class TestRootNode extends FilterNode {
        @StaticResource
        private static final String TEST_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/test-badge.png";

        TestRootNode(@NonNull final Node original) {
            super(original);
        }

        @Override
        public Image getIcon(int type) {
            return computeIcon(false, type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Image computeIcon(boolean opened, int type) {
            Image image = opened ?
                    getDataFolderNodeDelegate().getOpenedIcon(type) :
                    getDataFolderNodeDelegate().getIcon(type);
            image = ImageUtilities.mergeImages(
                    image,
                    ImageUtilities.loadImage(TEST_BADGE),
                    4, 5);
            return image;
        }

        @NonNull
        private Node getDataFolderNodeDelegate() {
            final DataFolder df = getLookup().lookup(DataFolder.class);
            try {
                if (df.isValid()) {
                    return df.getNodeDelegate();
                }
            } catch (IllegalStateException e) {
                if (df.isValid()) {
                    throw e;
                }
            }
            return new AbstractNode(Children.LEAF);
        }
    }
}
