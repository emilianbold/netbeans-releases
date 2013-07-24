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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.server.ui.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.QueryAccessor;
import org.netbeans.modules.team.server.ui.spi.SourceAccessor;
import org.netbeans.modules.team.server.ui.spi.SourceHandle;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.NbBundle;

/**
 * Node for project's sources section.
 *
 * @author S. Aubrecht, Jan Becicka
 */
public class SourceListNode<P> extends SectionNode {
    private final DashboardProvider<P> dashboard;
    private final LeafNode[] nodes;

    public SourceListNode( TreeListNode parent,  ProjectHandle project, DashboardProvider<P> dashboard, LeafNode... nodes  ) {
        super( NbBundle.getMessage(SourceListNode.class, "LBL_Sources"), parent, project, ProjectHandle.PROP_SOURCE_LIST ); //NOI18N
        this.dashboard = dashboard;
        this.nodes = nodes;
    }

    @Override
    protected List<TreeListNode> createChildren() {
        SourceAccessor<P> accessor = dashboard.getSourceAccessor();
        if(!accessor.hasSources(project)) {
            return Arrays.asList(new TreeListNode[] {new NANode(this)});
        }        
        ArrayList<TreeListNode> res = new ArrayList<>(20);
        List<SourceHandle> sources = accessor.getSources(project);
        if(sources.isEmpty() && nodes != null) {
            res.addAll(Arrays.asList(nodes));
        }
        for (SourceHandle s : sources) {
            res.add(dashboard.createSourceNode(s, this));
            res.addAll(getRecentProjectsNodes(s));
            if (s.getWorkingDirectory() != null) {
                res.add(new OpenNbProjectNode(s, this, dashboard ));
                if (dashboard.getSourceAccessor().getOpenFavoritesAction(s) != null) {
                    res.add(new OpenFavoritesNode(s, this, dashboard ));
                }
            }
        }
        return res;
    }

    private List<TreeListNode> getRecentProjectsNodes(SourceHandle handle) {
        ArrayList<TreeListNode> res = new ArrayList<TreeListNode>();
        for (IDEProject p : handle.getRecentProjects()) {
            res.add(new NbProjectNode(p, this, dashboard));
        }
        return res;
    }
}
