/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Displays data directories.
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-apisupport-project", position=150)
public class TestDataDirsNodeFactory implements NodeFactory {

    /** public for layer */
    public TestDataDirsNodeFactory() {}

    public NodeList<?> createNodes(Project p) {
        NbModuleProject prj = p.getLookup().lookup(NbModuleProject.class);
        return new TestDataDirsNL(prj);
    }

    private static class TestDataDirsNL implements NodeList<SourceGroup> {

        private final NbModuleProject project;

        public TestDataDirsNL(NbModuleProject project) {
            this.project = project;
        }

        public List<SourceGroup> keys() {
            List<SourceGroup> keys = new ArrayList<SourceGroup>();
            for (String testType : project.supportedTestTypes()) {
                String dataDir = project.evaluator().getProperty("test." + testType + ".data.dir");
                if (dataDir != null) {
                    FileObject root = project.getHelper().resolveFileObject(dataDir);
                    if (root != null) {
                        String displayName = NbBundle.getMessage(TestDataDirsNodeFactory.class, "TestDataDirsNodeFactory." + testType + "_test_data");
                        keys.add(GenericSources.group(project, root, testType,displayName, null, null));
                    }
                }
            }
            return keys;
        }

        public Node node(final SourceGroup key) {
            try {
                return new FilterNode(DataObject.find(key.getRootFolder()).getNodeDelegate()) {
                    @Override
                    public String getName() {
                        return key.getName();
                    }
                    @Override
                    public String getDisplayName() {
                        return key.getDisplayName();
                    }
                };
            } catch (DataObjectNotFoundException ex) {
                throw new AssertionError(ex);
            }
        }

        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}

        public void addNotify() {}

        public void removeNotify() {}

    }

}
