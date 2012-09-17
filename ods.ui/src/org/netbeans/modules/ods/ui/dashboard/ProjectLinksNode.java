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

package org.netbeans.modules.ods.ui.dashboard;

import org.netbeans.modules.team.ui.common.LinkButton;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.team.ui.treelist.AsynchronousLeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;
import org.netbeans.modules.team.ui.spi.MessagingHandle;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.util.NbBundle;

/**
 * Node showing links to project's wiki, downloads and messages.
 *
 * @author S. Aubrecht
 * @author Jan Becicka
 */
public class ProjectLinksNode extends AsynchronousLeafNode<MessagingHandle> {

    private final ProjectHandle<ODSProject> project;
    private JPanel panel;
    private List<JLabel> labels = new ArrayList<JLabel>(5);
    private List<LinkButton> buttons = new ArrayList<LinkButton>(3);
    private final Object LOCK = new Object();
    private final DashboardProviderImpl dashboardProvider;

    public ProjectLinksNode( TreeListNode parent, ProjectHandle<ODSProject> project, DashboardProviderImpl dashboardProvider ) {
        super( parent, null );
        this.project = project;
        this.dashboardProvider = dashboardProvider;
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        if( panel == component ) {
            synchronized( LOCK ) {
                for( JLabel lbl : labels ) {
                    lbl.setForeground(foreground);
                }
                for( LinkButton lb : buttons ) {
                    lb.setForeground(foreground, isSelected);
                }
            }
        }
    }

    @Override
    protected JComponent createComponent( MessagingHandle data ) {
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        synchronized( LOCK ) {
            labels.clear();
            buttons.clear();
            LinkButton btn = new LinkButton(NbBundle.getMessage(ProjectLinksNode.class, "LBL_ProjectDashboard"), dashboardProvider.getProjectAccessor().getDetailsAction(project)); //NOI18N
            buttons.add( btn );
            panel.add( btn, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
            panel.add( new JLabel(), new GridBagConstraints(8,0,1,1,1.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
        }
        return panel;
    }

    @Override
    protected MessagingHandle load() {
        return dummyHandle; 
    }

    private MessagingHandle dummyHandle = new MessagingHandle() {
        @Override
        public int getOnlineCount() {
            return 0;
        }
        @Override
        public int getMessageCount() {
            return 0;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) { }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) { }
    };
}
