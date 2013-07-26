/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.server.ui.common;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ToolBarUI;
import org.netbeans.modules.team.server.TeamView;
import org.netbeans.modules.team.server.ui.picker.MegaMenu;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.commons.treelist.ListNode;
import org.netbeans.modules.team.commons.treelist.ProgressLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 * @param <P> project type
 */
@NbBundle.Messages({"LBL_Switch=Select project", 
                    "LBL_NewServer=New Connection"})
public final class OneProjectDashboardPicker<P> extends JPanel {

    private ProjectHandle<P> currentProject;
    private ListNode currentProjectNode;

    private final JLabel lbl;
    private final LinkButton btnPick;

    private final LinkButton btnNewServer;
    private final LinkButton btnBookmark;
    private final ProgressLabel lblBookmarkingProgress;
    private final LinkButton btnClose;
    private final JToolBar.Separator separator;

    private final AbstractAction bookmarkAction;

    private final MouseOverListener mListener;
    private final JLabel placeholder;
    private boolean bookmarking;
    private TeamServer server;

    private final Object LOCK = new Object();
    
    public OneProjectDashboardPicker() {
        setLayout( new GridBagLayout() );
        setOpaque(false);
        
        setBackground(ColorManager.getDefault().getDefaultBackground());
        
        JSeparator jSeparator = new javax.swing.JSeparator();
        add(jSeparator, new GridBagConstraints(0,1,5,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,3,0,0), 0,0));            

        lbl = new JLabel();
        lbl.setBorder(new EmptyBorder(0, 0, 0, 10));
        lbl.setFont( lbl.getFont().deriveFont( Font.BOLD, lbl.getFont().getSize2D() + 1 ) );
        add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8,8,8,0), 0,0) );
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchProject();
            }
            @Override
            public void mousePressed(MouseEvent e) { }
        };
        lbl.addMouseListener(mouseAdapter);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon arrowDown = ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/arrow-down.png", true); //NOI18N
        btnPick = new LinkButton(arrowDown, new AbstractAction(Bundle.LBL_Switch()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchProject();
            }
        }); 
        btnPick.setToolTipText(Bundle.LBL_Switch());
        btnPick.setRolloverEnabled(true);
        add( btnPick, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,0,0,0), 0,0) );            

        placeholder = new JLabel();
        add( placeholder, new GridBagConstraints(2,0,1,1,1.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0,0) );

        JToolBar toolbar = new ProjectToolbar();
        add( toolbar, new GridBagConstraints(3,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0,0) );            

        bookmarkAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert currentProject != null;
                DashboardProvider<P> provider = getDashboard(server).getDashboardProvider();
                ProjectAccessor<P> paccessor = provider.getProjectAccessor();
                paccessor.bookmark(currentProject);
            }
        };

        btnBookmark = new LinkButton( (ImageIcon) null, bookmarkAction ); //NOI18N
        btnBookmark.setRolloverEnabled(true);
        btnBookmark.setVisible(false);
        toolbar.add(btnBookmark);
        lblBookmarkingProgress = new ProgressLabel("", this);
        lblBookmarkingProgress.setVisible(false);
        toolbar.add(lblBookmarkingProgress);

        Action closeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert currentProject != null;
                if(currentProject != null) {
                    getDashboard(server).getCloseProjectAction(currentProject).actionPerformed(e);
                }
            }
        };
        btnClose = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/close.png", true), closeAction); //NOI18N
        btnClose.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, "LBL_Close"));
        btnClose.setRolloverEnabled(true);
        btnClose.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/close_over.png", true)); // NOI18N
        btnClose.setVisible(false);
        btnClose.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, "LBL_Close"));
        toolbar.add(btnClose);

        separator = new JToolBar.Separator();
        toolbar.add(separator);

        btnNewServer = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/new_team_project.png", true), new AddInstanceAction()); 
        btnNewServer.setToolTipText(Bundle.LBL_NewServer());
        btnNewServer.setRolloverEnabled(true);
        btnNewServer.setVisible(false);
        toolbar.add(btnNewServer);

        mListener = new MouseOverListener();
        addMouseListener(mListener);
        addMouseMotionListener(mListener);
        lbl.addMouseListener(mListener);
        lbl.addMouseMotionListener(mListener);
        btnPick.addMouseListener(mListener);
        btnPick.addMouseMotionListener(mListener);
        placeholder.addMouseListener(mListener);
        placeholder.addMouseMotionListener(mListener);
        btnNewServer.addMouseListener(mListener);
        btnNewServer.addMouseMotionListener(mListener);
        btnClose.addMouseListener(mListener);
        btnClose.addMouseMotionListener(mListener);
        btnBookmark.addMouseListener(mListener);
        btnBookmark.addMouseMotionListener(mListener);
        separator.addMouseListener(mListener);
        separator.addMouseMotionListener(mListener);
        lblBookmarkingProgress.addMouseListener(mListener);
        lblBookmarkingProgress.addMouseMotionListener(mListener);

        setNoProject();
    }

    void setButtons() {
        btnNewServer.setVisible(mListener.mouseOver);

        if(currentProject != null) {
            boolean isMemberProject = getDashboard(server).isMemberProject(currentProject);
            btnClose.setVisible(mListener.mouseOver && !isMemberProject);
            
            btnBookmark.setVisible(mListener.mouseOver && !bookmarking);
            lblBookmarkingProgress.setVisible(mListener.mouseOver && bookmarking);
            separator.setVisible(mListener.mouseOver);
            
            btnBookmark.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, isMemberProject?"LBL_LeaveProject":"LBL_Bookmark"));
            btnBookmark.setIcon(
                ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/team/server/resources/"  + (isMemberProject ? "bookmark.png" : "unbookmark.png"), true));
            btnBookmark.setRolloverIcon(
                ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/team/server/resources/" + (isMemberProject ? "bookmark_over.png" : "unbookmark_over.png"), true)); // NOI18N                                
        } else {
            btnClose.setVisible(false);
            btnBookmark.setVisible(false);
            lblBookmarkingProgress.setVisible(false);
            separator.setVisible(false);                
        }
    }

    void setCurrentProject(final TeamServer server, ProjectHandle<P> project, ListNode node) {
        synchronized ( LOCK ) {
            this.server = server;
            if (project != null) {
                this.currentProject = project;
                this.currentProjectNode = node;
                setProjectLabel(project.getDisplayName());
            } else {
                setNoProject();
            }
            TeamView.getInstance().setSelectedServer(server);
        }
        setButtons();
        hideMenu();
    }

    void setNoProject() {
        synchronized ( LOCK ) {
            this.currentProject = null;
            this.currentProjectNode = null;
        }
        setProjectLabel(NbBundle.getMessage(DashboardSupport.class, "CLICK_TO_SELECT"));
        setButtons();
    }

    boolean removed(TeamServer server, ProjectHandle<P> project) {
        if(isCurrentProject(server, project)) {
            setNoProject();
            return true;
        } else {
            return false;
        }
    }
    
    void bookmarkingStarted(TeamServer server, ProjectHandle<P> project) {
        if(isCurrentProject(server, project)) {
            bookmarking = true;
            setButtons();
        }
    }

    void bookmarkingFinished(TeamServer server, ProjectHandle<P> project) {
        if(isCurrentProject(server, project)) {
            bookmarking = false;
            setButtons();
        }
    }

    boolean isNoProject() {
        synchronized ( LOCK ) {
            return currentProject == null;
        }
    }
    
    private void switchProject() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(!OneProjectDashboardPicker.this.isShowing()) {
                    return;
                }
                final MegaMenu mm = MegaMenu.create();
                if(server != null) {
                    mm.setInitialSelection(server, currentProjectNode);
                }
                mm.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        ListNode node = mm.getSelectedItem();
                        if(node != null && node instanceof MyProjectNode) {
                            MyProjectNode mpn = (MyProjectNode) node;
                            ProjectHandle<P> ph = mpn.getProject();
                            
                            ListNode curNode;
                            synchronized ( LOCK ) {
                                curNode = currentProjectNode;
                            }
                            if(curNode == null || !node.equals(curNode)) {
                                setCurrentProject(mpn.getTeamServer(), ph, node);
                                getDashboard(mpn.getTeamServer()).switchProject(ph);
                            }
                        }
                    }
                });
                mm.show(OneProjectDashboardPicker.this);
            }
        });
    }

    private void hideMenu() {
        final MegaMenu mm = MegaMenu.getCurrent();
        if(mm != null) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mm.hide();
                }
            };
            if(SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        }
    }

    private boolean isCurrentProject(TeamServer server, ProjectHandle<P> project) {
        synchronized ( LOCK ) {
            return currentProject != null && project.getId().equals(currentProject.getId()) && 
                   server.getUrl().equals(this.server.getUrl());
        }
    }

    private OneProjectDashboard<P> getDashboard(TeamServer server) {
        return OneProjectDashboard.forServer(server);
//        return server != null ? OneProjectDashboard.forServer(server) : null;
    }

    private void setProjectLabel(final String name) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lbl.setText(name);
                if(server != null) {
                    lbl.setIcon(server.getIcon());
                }
            }
        });
    }

    private class MouseOverListener extends MouseAdapter {
        boolean mouseOver = false;
        @Override
        public void mouseEntered( MouseEvent e ) {
            mouseMoved( e );
        }
        @Override
        public void mouseExited( MouseEvent e ) {
            mouseOver = false;
            setButtons();
        }
        @Override
        public void mouseMoved( MouseEvent e ) {
            mouseOver = true;
            setButtons();
        }
    };
    
    private class ProjectToolbar extends JToolBar {
        @Override
        public String getUIClassID() {
            if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
                return "Nb.Toolbar.ui"; //NOI18N
            } else {
                return super.getUIClassID();
            }
        }
        @Override
        public void setUI( ToolBarUI ui ) {
            super.setUI( ui );
            configure();
        }

        private void configure() {
            setOpaque( false );
            setFloatable( false );
            setBorderPainted( false );
            setBorder( BorderFactory.createEmptyBorder() );
        }
    }    
}
