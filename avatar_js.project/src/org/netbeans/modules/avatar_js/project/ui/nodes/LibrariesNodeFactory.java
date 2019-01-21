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

package org.netbeans.modules.avatar_js.project.ui.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.avatar_js.project.AvatarJSProject;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 */
@NodeFactory.Registration(projectType=AvatarJSProject.ID, position=300)
public final class LibrariesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public LibrariesNodeFactory() {
    }

    @Override
    public NodeList<String> createNodes(Project p) {
        AvatarJSProject project = p.getLookup().lookup(AvatarJSProject.class);
        assert project != null;
        return new LibrariesNodeList(project);
    }

    private static class LibrariesNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String LIBRARIES = "Libs"; //NOI18N
        private static final String TEST_LIBRARIES = "TestLibs"; //NOI18N

        private final SourceRoots testSources;
        private final AvatarJSProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private final PropertyEvaluator evaluator;
        private final UpdateHelper helper;
        private final ReferenceHelper resolver;
        private final ClassPathSupport cs;
        
        LibrariesNodeList(@NonNull final AvatarJSProject proj) {
            Parameters.notNull("proj", proj);   //NOI18N
            project = proj;
            testSources = project.getTestSourceRoots();
            evaluator = project.evaluator();
            helper = project.getUpdateHelper();
            resolver = project.getReferenceHelper();
            cs = new ClassPathSupport(evaluator, resolver, helper.getAntProjectHelper(), helper, null);
        }
        
        @Override
        public List<String> keys() {
            List<String> result = new ArrayList<>();
            result.add(LIBRARIES);
            URL[] testRoots = testSources.getRootURLs();
            boolean addTestSources = false;
            for (URL testRoot : testRoots) {
                File f = Utilities.toFile(URI.create(testRoot.toExternalForm()));
                if (f.exists()) {
                    addTestSources = true;
                    break;
                }
            }
            if (addTestSources) {
                result.add(TEST_LIBRARIES);
            }
            return result;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @NbBundle.Messages({"CTL_LibrariesNode=Libraries",
                            "CTL_TestLibrariesNode=Test Libraries"})
        @Override
        public Node node(String key) {
            if (key == LIBRARIES) {
                //Libraries Node
                return  
                    new LibrariesNode(Bundle.CTL_LibrariesNode(),
                        project, evaluator, helper, resolver, ProjectProperties.RUN_CLASSPATH,
                        new String[] {ProjectProperties.BUILD_CLASSES_DIR},
                        "platform.active", // NOI18N
                        new Action[] {
                            LibrariesNode.createAddProjectAction(project, project.getSourceRoots()),
                            LibrariesNode.createAddLibraryAction(project.getReferenceHelper(), project.getSourceRoots(), null),
                            LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getSourceRoots()),
                            null,
                            ProjectUISupport.createPreselectPropertiesAction(project, "Libraries", "COMPILE"), // NOI18N
                        },
                        null,
                        cs,
                        null
                    );
            } else if (key == TEST_LIBRARIES) {
                return  
                    new LibrariesNode(Bundle.CTL_TestLibrariesNode(),
                        project, evaluator, helper, resolver, ProjectProperties.RUN_TEST_CLASSPATH,
                        new String[] {
                            ProjectProperties.BUILD_TEST_CLASSES_DIR,
                            ProjectProperties.JAVAC_CLASSPATH,
                            ProjectProperties.BUILD_CLASSES_DIR,
                        },
                        null,
                        new Action[] {
                            LibrariesNode.createAddProjectAction(project, project.getTestSourceRoots()),
                            LibrariesNode.createAddLibraryAction(project.getReferenceHelper(), project.getTestSourceRoots(), null),
                            LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getTestSourceRoots()),
                            null,
                            ProjectUISupport.createPreselectPropertiesAction(project, "Libraries", "COMPILE_TESTS"), // NOI18N
                        },
                        null,
                        cs,
                        null
                    );
            }
            assert false: "No node for key: " + key;
            return null;
            
        }

        @Override
        public void addNotify() {
            testSources.addPropertyChangeListener(this);
        }

        @Override
        public void removeNotify() {
            testSources.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
    }
    
}

