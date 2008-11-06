/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mobility.project.ui;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.ProjectRootNodeChildren.ChildKind;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class ProjectRootNodeChildren extends ChildFactory<ChildKind> {

    private final J2MEProject project;

    public static enum ChildKind {
        Sources, 
        Resources,
        Configurations
    }

    ProjectRootNodeChildren(J2MEProject project) {
        this.project = project;
    }

    protected boolean createKeys(List<ChildKind> toPopulate) {
        toPopulate.addAll(Arrays.asList(ChildKind.values()));
        return true;
    }

    @Override
    protected Node[] createNodesForKey(ChildKind key) {
        switch (key) {
            case Configurations:
                return new Node[]{createConfigurationsNode()};
            case Resources:
                return new Node[]{createResourcesNode()};
            case Sources:
                return createSourcesNodes();
            default:
                throw new AssertionError();
        }
    }

    private Node createConfigurationsNode() {
        return new ConfigurationsNode (project);
    }

    private Node createResourcesNode() {
        return new ResourcesNode(project, null);
    }

    private Node[] createSourcesNodes() {
        Node[] result = new Node[0];
        final Sources src = ProjectUtils.getSources(project);
        if (src != null) {
            final SourceGroup sg[] = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            result = new Node[sg.length];
            int ix = 0;
            //in preparation for multiple source roots
            for (SourceGroup group : sg) {
                result[ix++] = PackageView.createPackageView(sg[0]);
            }
        }
        return result.length == 0 ? new Node[] { Node.EMPTY } : result;
    }
}
