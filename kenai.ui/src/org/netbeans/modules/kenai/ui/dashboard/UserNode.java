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
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.treelist.LeafNode;
import org.netbeans.modules.kenai.ui.spi.LoginHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.kenai.ui.treelist.TreeLabel;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The very first node in dashboard window showing logged-in user name.
 *
 * @author S. Aubrecht
 */
public class UserNode extends LeafNode {

    private final DashboardImpl dashboard;

    private JPanel panel;
    private JLabel lblUser;
    private JLabel lblProgress;
    private LinkButton btnOpenProject;
    private LinkButton btnRefresh;
    private LinkButton btnLogin;
    private LinkButton btnNewProject;
    private LinkButton btnChangeInstance;

    private LoginHandle login;
    private boolean projectsAvailable = false;

    private final Object LOCK = new Object();
    private int loadingCounter = 0;

    public UserNode( DashboardImpl dashboard ) {
        super( null );
        this.dashboard = dashboard;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        if( null == panel ) {
            panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);

            btnLogin = new LinkButton(NbBundle.getMessage(UserNode.class, "LBL_LoginToKenai"), //NOI18N
                    dashboard.createLoginAction());
            lblUser = new TreeLabel();
            lblProgress = createProgressLabel(""); //NOI18N
            btnOpenProject = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/open_kenai_project.png", true), ProjectAccessor.getDefault().getOpenNonMemberProjectAction()); //NOI18N
            btnOpenProject.setToolTipText(NbBundle.getMessage(UserNode.class, "LBL_OpenProject"));
            btnNewProject = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/new_kenai_project.png", true), ProjectAccessor.getDefault().getNewKenaiProjectAction()); //NOI18N
            btnNewProject.setToolTipText(NbBundle.getMessage(UserNode.class, "LBL_NewKenaiProject"));
            btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/refresh.png", true), new AbstractAction() { //NOI18N
                public void actionPerformed(ActionEvent e) {
                    DashboardImpl.getInstance().refreshProjects();
                }
            });
            btnRefresh.setToolTipText(NbBundle.getMessage(UserNode.class, "LBL_Refresh"));
            btnChangeInstance = new LinkButton("[i]", new ChangeInstanceAction());

            panel.add( btnLogin, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0,0));
            panel.add( lblUser, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0,0));
            panel.add( new JLabel(), new GridBagConstraints(2,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( lblProgress, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( new JLabel(), new GridBagConstraints(4,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            panel.add( btnRefresh, new GridBagConstraints(5,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
            if (Boolean.parseBoolean(System.getProperty("kenai.instance.switcher", "false")))
                panel.add( btnChangeInstance, new GridBagConstraints(6,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
            panel.add( btnNewProject, new GridBagConstraints(7,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
            panel.add( btnOpenProject, new GridBagConstraints(8,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
        }

        lblProgress.setForeground(foreground);
        if( isSelected ) {
            lblUser.setForeground(foreground);
        } else {
            lblUser.setForeground(ColorManager.getDefault().getDisabledColor());
        }
        btnOpenProject.setForeground(foreground, isSelected);
        btnRefresh.setForeground(foreground, isSelected);
        btnLogin.setForeground(foreground, isSelected);
        synchronized( LOCK ) {
            lblProgress.setVisible(loadingCounter > 0);
            btnRefresh.setVisible(loadingCounter <= 0 && (projectsAvailable || null != login));
        }
        if( null != login )
            lblUser.setText( login.getUserName() );
        btnLogin.setVisible( null == login );
        lblUser.setVisible( null != login );
        return panel;
    }

    void loadingStarted(String title) {
        synchronized( LOCK ) {
            lblProgress.setText(title);
            loadingCounter++;
            fireContentChanged();
        }
    }

    void loadingFinished() {
        synchronized( LOCK ) {
            loadingCounter--;
            if( loadingCounter < 0 )
                loadingCounter = 0;
            fireContentChanged();
        }
    }

    void set( LoginHandle login, boolean projectsAvailable ) {
        this.projectsAvailable = projectsAvailable;
        this.login = login;
        fireContentChanged();
    }

    private class ChangeInstanceAction extends AbstractAction {
        final JRadioButtonMenuItem ke = new JRadioButtonMenuItem("kenai.com");
        final JRadioButtonMenuItem nb = new JRadioButtonMenuItem("netbeans.org (stg)");
        final JRadioButtonMenuItem nb2 = new JRadioButtonMenuItem("netbeans.org (test)");
        final JRadioButtonMenuItem oo = new JRadioButtonMenuItem("odftoolkit.org");
        final JRadioButtonMenuItem tk = new JRadioButtonMenuItem("testkenai.com");
        final ButtonGroup gr = new ButtonGroup();
        JPopupMenu menu = new JPopupMenu();

        public ChangeInstanceAction() {
            gr.add(ke);
            gr.add(nb);
            gr.add(oo);
            gr.add(tk);
            gr.add(nb2);
            menu.add(ke);
            menu.add(nb);
            menu.add(nb2);
            menu.add(oo);
            menu.add(tk);
            gr.setSelected(ke.getModel(), true);
            ke.addActionListener(new ActionL(ke.getModel(), "https://kenai.com"));
            nb.addActionListener(new ActionL(nb.getModel(), "https://stg.network.org"));
            nb2.addActionListener(new ActionL(nb2.getModel(), "https://test.netbeans.org"));
            oo.addActionListener(new ActionL(oo.getModel(), "https://odftoolkit.org"));
            tk.addActionListener(new ActionL(tk.getModel(), "https://testkenai.com"));
        }
        private class ActionL implements ActionListener {
            private ButtonModel m;
            private URL url;
            private ActionL (ButtonModel m, String url) {
                try {
                    this.url = new URL(url);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
                this.m=m;
            }

            public void actionPerformed(ActionEvent e) {
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        Kenai.getDefault().setUrl(url);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                gr.setSelected(m, true);
                                UIUtils.tryLogin(false);
                                DashboardImpl.getInstance().refreshNonMemberProjects();
                            }
                        });
                    }
                });
            }

        }

        public void actionPerformed(ActionEvent e) {
            menu.show(dashboard.dashboardComponent, dashboard.dashboardComponent.getSize().width-45, 8);
        }

    }
}
