/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.kenai.ui.treelist.LeafNode;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.LoginHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.openide.util.NbBundle;

/**
 * The very first node in dashboard window showing logged-in user name.
 *
 * @author S. Aubrecht
 */
public class UserNode extends LeafNode {

    private final LoginHandle login;
    private final Dashboard dashboard;

    private JPanel panel;
    private JLabel lblUser;
    private JLabel lblStatus;
    private LinkButton btnOpenProject;
    private LinkButton btnRefresh;

    private String statusText;
    private String errorText;

    public UserNode( LoginHandle login, Dashboard dashboard ) {
        super( null );
        this.login = login;
        this.dashboard = dashboard;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        if( null == panel ) {
            panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);

            lblUser = new JLabel(login.getUserName());
            lblStatus = new JLabel();
            lblStatus.setHorizontalAlignment(JLabel.CENTER);
            btnOpenProject = new LinkButton(NbBundle.getMessage(UserNode.class, "LBL_OpenProject"), ProjectAccessor.getDefault().getOpenNonMemberProjectAction()); //NOI18N
            btnRefresh = new LinkButton(NbBundle.getMessage(UserNode.class, "LBL_Refresh"), new ActionListener() { //NOI18N
                public void actionPerformed(ActionEvent e) {
                    DashboardImpl.getInstance().refreshMemberProjects();
                }
            });

            panel.add( lblUser, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0,0));
            panel.add( new JLabel(), new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( btnRefresh, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( lblStatus, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( new JLabel(), new GridBagConstraints(4,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( btnOpenProject, new GridBagConstraints(5,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
        }

        if( null != errorText )
            lblStatus.setText(errorText);
        else if( null != statusText )
            lblStatus.setText(statusText);
        btnRefresh.setVisible(null == statusText && null == errorText);
        if( isSelected ) {
            lblUser.setForeground(foreground);
            lblStatus.setForeground(foreground);
        } else {
            lblUser.setForeground(ColorManager.disabledColor);
            if( null != errorText )
                lblStatus.setForeground(ColorManager.errorColor);
            else if( null != statusText )
                lblStatus.setForeground(ColorManager.disabledColor);
        }
        btnOpenProject.setForeground(foreground, isSelected);
        btnRefresh.setForeground(foreground, isSelected);
        return panel;
    }

    void setStatus( String text ) {
        if( null != lblStatus )
            lblStatus.setText(text);
        statusText = text;
        fireContentChanged();
    }

    void setError( String text ) {
        if( null != lblStatus )
        lblStatus.setText(text);
        errorText = text;
        fireContentChanged();
    }

    void clearMessage() {
        if( null != lblStatus )
            lblStatus.setText(null);
        statusText = null;
        errorText = null;
        fireContentChanged();
    }
}
