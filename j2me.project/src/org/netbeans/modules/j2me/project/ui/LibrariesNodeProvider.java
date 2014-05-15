/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.j2me.project.ui.customizer.J2MELibrariesPanel;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@NodeFactory.Registration(projectType = "org-netbeans-modules-j2me-project", position=300)
public class LibrariesNodeProvider implements NodeFactory {

    public LibrariesNodeProvider() {
    }

    @Override
    public NodeList<?> createNodes(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        final J2MEProject j2mePrj = project.getLookup().lookup(J2MEProject.class);
        if (j2mePrj != null) {
            return new Libs(j2mePrj);
        } else {
            return NodeFactorySupport.fixedNodeList();
        }
    }

    private static final class Libs implements NodeList<Libs.NodeType>, PropertyChangeListener {

        private final J2MEProject prj;
        private final SourceRoots sources;
        private final SourceRoots tests;
        private final ChangeSupport changeSupport;
        private final ClassPathSupport cs;

        Libs(@NonNull final J2MEProject prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
            this.sources = this.prj.getSourceRoots();
            this.tests = this.prj.getTestRoots();
            this.changeSupport = new ChangeSupport(this);
            this.cs = new ClassPathSupport(
                prj.evaluator(),
                prj.getReferenceHelper(),
                prj.getHelper(),
                prj.getUpdateHelper(),
                null);
        }

        @Override
        @NonNull
        public List<NodeType> keys() {
            final List<NodeType> res = new ArrayList<>(2);
            res.add(NodeType.SOURCES);  //Always show libraries for sources
            if (nonEmpty(tests)) {      //Show test libraries only when they are some tests
                res.add(NodeType.TESTS);
            }
            return res;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            Parameters.notNull("l", l); //NOI18N
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            Parameters.notNull("l", l); //NOI18N
            changeSupport.removeChangeListener(l);
        }

        @Override
        @NonNull
        public Node node(@NonNull final NodeType key) {
            Parameters.notNull("key", key); //NOI18N
            switch (key) {
                case SOURCES:
                    ClassPath bootCp;
                    final FileObject[] roots = sources.getRoots();
                    if (roots.length > 0) {
                        bootCp = prj.getClassPathProvider().findClassPath(roots[0], ClassPath.BOOT);
                    } else {
                        bootCp = prj.getClassPathProvider().getProjectClassPaths(ClassPath.BOOT)[0];
                    }
                    return new LibrariesNode.Builder(
                            prj,
                            prj.evaluator(),
                            prj.getUpdateHelper(),
                            prj.getReferenceHelper(),
                            cs).
                        setName(NbBundle.getMessage(LibrariesNodeProvider.class,"CTL_LibrariesNode")).
                        addClassPathProperties(ProjectProperties.RUN_CLASSPATH).
                        addClassPathIgnoreRefs(ProjectProperties.BUILD_CLASSES_DIR).
                        setPlatformProperty(ProjectProperties.PLATFORM_ACTIVE).
                        setPlatformType(J2MEProjectProperties.PLATFORM_TYPE_J2ME).
                        setBootPath(bootCp).
                        addLibrariesNodeActions(
                            LibrariesNode.createAddProjectAction(prj, sources),
                            LibrariesNode.createAddLibraryAction(prj.getReferenceHelper(), sources, null),
                            LibrariesNode.createAddFolderAction(prj.getHelper(), sources),
                            null,
                            ProjectUISupport.createPreselectPropertiesAction(prj, "Libraries", J2MELibrariesPanel.COMPILE)). // NOI18N)
                        build();
                case TESTS:
                    return new LibrariesNode.Builder(
                            prj,
                            prj.evaluator(),
                            prj.getUpdateHelper(),
                            prj.getReferenceHelper(),
                            cs).
                        setName(NbBundle.getMessage(LibrariesNodeProvider.class,"CTL_TestLibrariesNode")).
                        addClassPathProperties(ProjectProperties.RUN_TEST_CLASSPATH).
                        addClassPathIgnoreRefs(
                                ProjectProperties.BUILD_TEST_CLASSES_DIR,
                                ProjectProperties.JAVAC_CLASSPATH,
                                ProjectProperties.BUILD_CLASSES_DIR).
                        addLibrariesNodeActions(
                            LibrariesNode.createAddProjectAction(prj, tests),
                            LibrariesNode.createAddLibraryAction(prj.getReferenceHelper(), tests, null),
                            LibrariesNode.createAddFolderAction(prj.getHelper(), tests),
                            null,
                            ProjectUISupport.createPreselectPropertiesAction(prj, "Libraries", null)). // NOI18N
                        build();
                default:
                    throw new IllegalArgumentException(key.toString());
            }
        }

        @Override
        public void addNotify() {
            this.sources.addPropertyChangeListener(this);
            this.tests.addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            this.sources.removePropertyChangeListener(this);
            this.tests.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                changeSupport.fireChange();
            }
        }

        private static boolean nonEmpty(@NonNull final SourceRoots sr) {
            return sr.getRoots().length > 0;
        }

        private static enum NodeType {
            SOURCES,
            TESTS;
        }
    }


}
