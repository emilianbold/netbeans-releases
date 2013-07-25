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
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.team.server.ui.spi.LoginHandle;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.ProgressLabel;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * The very first node in dashboard window showing logged-in user name.
 *
 * @author S. Aubrecht
 */
public class UserNode extends LeafNode {

    private JPanel panel;
    private JLabel lblUser;
    private ProgressLabel lblProgress;
    private LinkButton btnOpenProject;
    private LinkButton btnRefresh;
    private LinkButton btnLogin;
    private LinkButton btnNewProject;
    private LinkButton btnLogout;
    private String progressTitle;
    private JLabel lpar = new JLabel("("); // NOI18N
    private JLabel rpar = new JLabel(")"); // NOI18N
    private LoginHandle login;
    private boolean projectsAvailable = false;

    private final Object LOCK = new Object();
    private int loadingCounter = 0;
    private final Action loginAction;
    private final Action logoutAction;
    private final Action refreshAction;
    private final Action newProjectAction;
    private final Action openNonMemberProjectAction;

    // XXX maybe actions should be already initialized with name, icon & co.
    public UserNode( Action refreshAction, Action loginAction, Action logoutAction, Action newProjectAction, Action openNonMemberProjectAction ) {
        super( null );
        
        this.loginAction = loginAction;
        this.logoutAction = logoutAction;
        this.refreshAction = refreshAction;
        this.newProjectAction = newProjectAction;
        this.openNonMemberProjectAction = openNonMemberProjectAction;
    }

    @Override
    public JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        synchronized(this) { // ensure panel and it's components created before accessing them
            if( null == panel ) {
                panel = new JPanel( new GridBagLayout() );
                panel.setOpaque(false);

                btnLogin = new LinkButton(NbBundle.getMessage(UserNode.class, "LBL_Login"), loginAction); //NOI18N
                        
                btnLogout = new LinkButton(NbBundle.getMessage(UserNode.class, "LBL_Logout"), logoutAction); //NOI18N
                lblUser = new TreeLabel();
                lblProgress = createProgressLabel(progressTitle); //NOI18N
                if(openNonMemberProjectAction != null) {
                    btnOpenProject = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/open_team_project.png", true), openNonMemberProjectAction); //NOI18N
                    btnOpenProject.setToolTipText(NbBundle.getMessage(UserNode.class, "LBL_OpenProject")); //NOI18N
                }
                if(newProjectAction != null) {
                    btnNewProject = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/new_team_project.png", true), newProjectAction); //NOI18N
                    btnNewProject.setToolTipText(NbBundle.getMessage(UserNode.class, "LBL_NewProject")); //NOI18N
                } 
                btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/refresh.png", true), refreshAction); // NOI18N
                btnRefresh.setToolTipText(NbBundle.getMessage(UserNode.class, "LBL_Refresh")); //NOI18N

                panel.add( btnLogin, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0,0));
                panel.add( lblUser, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0,0));
                panel.add( lpar, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                panel.add( btnLogout, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                panel.add( rpar, new GridBagConstraints(4,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                panel.add( new JLabel(), new GridBagConstraints(5,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                panel.add( lblProgress, new GridBagConstraints(6,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                panel.add( new JLabel(), new GridBagConstraints(7,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                panel.add( btnRefresh, new GridBagConstraints(8,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
                if(btnNewProject != null) {
                    panel.add( btnNewProject, new GridBagConstraints(9,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
                }
                if(btnOpenProject != null) {
                    panel.add( btnOpenProject, new GridBagConstraints(10,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
                }
            }
        }

        lblProgress.setForeground(foreground);
        if( isSelected ) {
            lblUser.setForeground(foreground);
        } else {
            lblUser.setForeground(ColorManager.getDefault().getDisabledColor());
        }
        if(btnOpenProject != null) {
            btnOpenProject.setForeground(foreground, isSelected);
        }
        btnRefresh.setForeground(foreground, isSelected);
        btnLogin.setForeground(foreground, isSelected);
        synchronized( LOCK ) {
            lblProgress.setVisible(loadingCounter > 0);
            btnRefresh.setVisible(loadingCounter <= 0 && (projectsAvailable || null != login));
        }
        if( null != login )
            lblUser.setText( login.getUserName() );
        btnLogin.setVisible( null == login );
        btnLogout.setVisible(null!=login);
        rpar.setVisible(null!=login);
        lpar.setVisible(null!=login);
        lblUser.setVisible( null != login );
        return panel;
    }

    public void loadingStarted(String title) {
        synchronized( LOCK ) {
            progressTitle = title;
            if (lblProgress != null)
                lblProgress.setText(title);
            loadingCounter++;
            fireContentChanged();
        }
    }

    public void loadingFinished() {
        synchronized( LOCK ) {
            loadingCounter--;
            if( loadingCounter < 0 )
                loadingCounter = 0;
            if (loadingCounter == 0) {
                if (lblProgress!=null)
                    lblProgress.stop();
            }
            fireContentChanged();
        }
    }

    public void set( LoginHandle login, boolean projectsAvailable ) {
        this.projectsAvailable = projectsAvailable;
        this.login = login;
        fireContentChanged();
    }
}
