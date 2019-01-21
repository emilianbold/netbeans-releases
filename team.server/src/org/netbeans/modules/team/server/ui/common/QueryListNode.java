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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.QueryAccessor;
import org.netbeans.modules.team.server.ui.spi.QueryHandle;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.LinkButton;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.NbBundle;

/**
 * Node for project's issues section.
 *
 * 
 */
public class QueryListNode<P> extends SectionNode {
    private final DashboardProvider<P> dashboard;

    public QueryListNode( TreeListNode parent,  ProjectHandle project, DashboardProvider<P> dashboard) {
        super( NbBundle.getMessage(QueryListNode.class, "LBL_Issues"), parent, project, ProjectHandle.PROP_QUERY_LIST ); //NOI18N
        this.dashboard = dashboard;
    }

    @Override
    protected List<TreeListNode> createChildren() {
        QueryAccessor accessor = dashboard.getQueryAccessor();
        if(!accessor.hasTasks(project)) {
            return Arrays.asList(new TreeListNode[] {new NANode(this)});
        }
        ArrayList<TreeListNode> res = new ArrayList<>(20);
        List<QueryHandle> queries = accessor.getQueries(project);
        for( QueryHandle q : queries ) {
            res.add( new QueryNode( q, this, dashboard ) );
        }
        res.add( new FindIssueNode(this) );
        res.add( new NewIssueNode(this) );
        return res;
    }

    private class FindIssueNode extends LeafNode {

        private JPanel panel;
        private LinkButton btn;
        private final Object LOCK = new Object();

        public FindIssueNode( QueryListNode parent ) {
            super( parent );
        }

        @Override
        protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
            synchronized (LOCK) {
                if (null == panel) {
                    panel = new JPanel(new GridBagLayout());
                    panel.setOpaque(false);
                    btn = new LinkButton(NbBundle.getMessage(QueryListNode.class, "LBL_FindIssue"), getDefaultAction(), false); //NOI18N
                    panel.add(btn, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    panel.add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                }
                if (btn!=null)
                    btn.setForeground(foreground, isSelected);
                return panel;
            }
        }

        @Override
        public Action getDefaultAction() {
            return dashboard.getQueryAccessor().getFindIssueAction(project);
        }
    }

    private class NewIssueNode extends LeafNode {

        private JPanel panel;
        private LinkButton btn;
        private final Object LOCK = new Object();
        public NewIssueNode( QueryListNode parent ) {
            super( parent );
        }

        @Override
        protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
            synchronized (LOCK) {
                if (null == panel) {
                    panel = new JPanel(new GridBagLayout());
                    panel.setOpaque(false);
                    btn = new LinkButton(NbBundle.getMessage(QueryListNode.class, "LBL_NewIssue"), getDefaultAction(), false); //NOI18N
                    panel.add(btn, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    panel.add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                }
                if (btn!=null)
                    btn.setForeground(foreground, isSelected);
                return panel;
            }
        }

        @Override
        public Action getDefaultAction() {
            return dashboard.getQueryAccessor().getCreateIssueAction(project);
        }
    }
}
