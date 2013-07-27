package org.netbeans.modules.team.server.ui.common;

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



import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.QueryAccessor;
import org.netbeans.modules.team.server.ui.spi.QueryHandle;
import org.netbeans.modules.team.server.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.commons.treelist.AsynchronousNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;

/**
 * Node query results.
 *
 * @author S. Aubrecht
 */
public class QueryNode<P> extends AsynchronousNode<List<QueryResultHandle>> implements PropertyChangeListener {

    private final DashboardProvider<P> dashboard;
    private final QueryHandle query;
    
    private JPanel panel;
    private List<JLabel> labels = new ArrayList<JLabel>(15);
    private List<LinkButton> buttons = new ArrayList<LinkButton>(10);
    private final Object LOCK = new Object();

    public QueryNode( QueryHandle query, TreeListNode parent, DashboardProvider<P> dashboard ) {
        super(false, parent, query.getDisplayName());
        this.query = query;
        this.dashboard = dashboard;
        query.addPropertyChangeListener(this);
    }

    @Override
    protected void dispose() {
        super.dispose();
        query.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( QueryHandle.PROP_QUERY_RESULT.equals(evt.getPropertyName()) ) {
            refresh();
        }
    }

    @Override
    public Action getDefaultAction() {
        return dashboard.getQueryAccessor().getDefaultAction(query);
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized( LOCK ) {
            for( JLabel lbl : labels ) {
                lbl.setForeground(foreground);
            }
            for( LinkButton lb : buttons ) {
                lb.setForeground(foreground, isSelected);
            }
        }
    }

    @Override
    protected JComponent createComponent(List<QueryResultHandle> data) {
        QueryAccessor accessor = dashboard.getQueryAccessor();
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        synchronized( LOCK ) {
            labels.clear();
            buttons.clear();
            int col = 0;
            JLabel lbl = new TreeLabel(query.getDisplayName());
            labels.add(lbl);
            panel.add( lbl, new GridBagConstraints(col++,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));

            if (data.size() > 0) {
                lbl = new TreeLabel("("); //NOI18N
                labels.add(lbl);
                panel.add(lbl, new GridBagConstraints(col++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));

                for (int i = 0; i < data.size(); i++) {
                    QueryResultHandle qrh = data.get(i);
                    if (qrh.getResultType() == QueryResultHandle.ResultType.NAMED_RESULT) {
                        LinkButton btn = new LinkButton(qrh.getText(), accessor.getOpenQueryResultAction(qrh));
                        btn.setToolTipText(qrh.getToolTipText());
                        buttons.add(btn);
                        panel.add(btn, new GridBagConstraints(col++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                        if (i < data.size() - 1 && data.get(i+1).getResultType()==QueryResultHandle.ResultType .NAMED_RESULT) {
                            lbl = new TreeLabel("|"); //NOI18N
                            labels.add(lbl);
                            panel.add(lbl, new GridBagConstraints(col++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
                        }
                    }
                }

                lbl = new TreeLabel(")"); //NOI18N
                labels.add(lbl);
                panel.add(lbl, new GridBagConstraints(col++, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            }
        }
        return panel;
    }

    @Override
    protected List<QueryResultHandle> load() {
        return dashboard.getQueryAccessor().getQueryResults(query);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        return Collections.emptyList();
    }
}
