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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.buildplan.nodes;

import java.awt.Image;
import java.util.List;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.buildplan.BuildPlanView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Anuradha G
 */
public class NodeUtils {

    private NodeUtils() {
    }

    public static Node createLoadingNode() {
        AbstractNode nd = new AbstractNode(Children.LEAF) {

            @Override
            public Image getIcon(int arg0) {
                return Utilities.loadImage("org/netbeans/modules/maven/buildplan/nodes/wait.gif");
            }
        };
        nd.setName("Loading"); //NOI18N

        nd.setDisplayName(NbBundle.getMessage(NodeUtils.class, "Node_Loading"));
        return nd;
    }

    public static Children createBuildPlanChildren(final BuildPlanView view,
            final List<MavenProject> mps) {
        final Children.Array array = new Children.Array();

        try {
            ReactorManager rm = new ReactorManager(mps, ReactorManager.FAIL_FAST);
            List<MavenProject> sortedProjects = rm.getSortedProjects();
            for (MavenProject mp : sortedProjects) {
                array.add(new Node[]{new LifecycleNode(view, mp)
                        });
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return array;
    }
    
}
