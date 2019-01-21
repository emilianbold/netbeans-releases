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
package org.netbeans.modules.team.server.ui.picker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.team.server.ui.common.ErrorNode;
import org.netbeans.modules.team.server.ui.common.LinkButton;
import org.netbeans.modules.team.server.ui.common.EditInstanceAction;
import org.netbeans.modules.team.server.ui.common.OneProjectDashboard;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.api.TeamUIUtils;
import org.netbeans.modules.team.commons.treelist.ListNode;
import org.netbeans.modules.team.commons.treelist.SelectionList;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Mega-menu content for a sing TeamServer.
 * 
 * 
 */
class ServerPanel extends JPanel {

    private final TeamServer server;
    private final SelectionModel selModel;
    private final static RequestProcessor RP = new RequestProcessor( "ProjectsLoader" ); //NOI18N
    private final Object LOADER_LOCK = new Object();
    private Object loadingToken = null;
    private boolean justLoggedIn;

    private JLabel title;
    private JButton dropDownButton;
    private JPopupMenu popupMenu;
    private final JPanel panelProjects;
    private SelectionList currentProjects;

    private final PropertyChangeListener serverListener = new PropertyChangeListener() {
        @Override
        public void propertyChange( PropertyChangeEvent evt ) {
            switch (evt.getPropertyName()) {
                case TeamServer.PROP_NAME:
                    MegaMenu.showAgain(); // rebuild entire menu - the server name appears on various places (title, tooltip, accessibility context, ...)
                    break;
                case TeamServer.PROP_LOGIN:
                    rebuildAndPack();
                    break;
            }
        }
    };
    
    private final ListDataListener modelListener = new ListDataListener() {
        @Override
        public void intervalAdded(ListDataEvent e) {
            synchronized ( LOADER_LOCK ) {
                if(currentProjects == null) {
                    return;
                }
                if(currentProjects.getModel().getSize() > 0 && e.getIndex0() == 0) {
                    rebuildAndPack();
                } else {
                    // XXX maybe this should be handled direcly in SelectionList
                    pack();
                }
            }
        }
        @Override 
        public void intervalRemoved(ListDataEvent e) { 
            synchronized ( LOADER_LOCK ) {
                if(currentProjects == null) {
                    return;
                }            
                if(currentProjects.getModel().getSize() == 0) {
                    rebuildAndPack();
                } else {
                    // XXX maybe this should be handled direcly in SelectionList
                    pack();
                }
            }
        }
        @Override 
        public void contentsChanged(ListDataEvent e) { }
        
        private void pack() {
            runInAWT(new Runnable() {
                @Override
                public void run() {    
                    synchronized ( LOADER_LOCK ) {
                        if(currentProjects == null) {
                            return;
                        }                    
                        currentProjects.invalidate();
                        currentProjects.revalidate();
                        PopupWindow.pack();
                    }
                }
            });
        }
    };

    private ServerPanel( final TeamServer server, SelectionModel selModel ) {
        super( new BorderLayout(10,5) );
        setOpaque( false );

        this.server = server;
        this.selModel = selModel;

        add(createHeader(), BorderLayout.NORTH);

        panelProjects = new JPanel(new BorderLayout());
        panelProjects.setOpaque(false);
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(panelProjects, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        rebuild();
    }

    public static JComponent create( TeamServer server, SelectionModel selModel ) {
        return new ServerPanel( server, selModel );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        server.addPropertyChangeListener( serverListener );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        server.removePropertyChangeListener( serverListener );
        synchronized ( LOADER_LOCK ) {
            if( currentProjects != null ) {
                this.currentProjects.getModel().removeListDataListener(modelListener);
            }
        }
    }

    @NbBundle.Messages({"# {0} - name of team server",
                        "A11Y_Settings=Settings for server {0}"})
    private JComponent createHeader() {
        JPanel res = new JPanel( new BorderLayout(10,10) );
        res.setOpaque( false );

        JPanel panelTitle = new JPanel( new BorderLayout( 5, 5) );
        panelTitle.setOpaque( false );
        title = new JLabel( server.getDisplayName() );
        title.setToolTipText( getTooltipText(server) );
        title.setIcon( server.getIcon() );
        Font f = new JLabel().getFont();
        f = f.deriveFont( Font.BOLD, f.getSize2D()+3 );
        title.setFont( f );
        panelTitle.add( title, BorderLayout.CENTER );

        IconWithArrow icon = new IconWithArrow(ImageUtilities.loadImageIcon( "org/netbeans/modules/team/server/resources/gear.png", true));
        dropDownButton = new JButton(icon);
        dropDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupMenu.show(dropDownButton, 0, dropDownButton.getHeight());
            }
        });
        dropDownButton.getAccessibleContext().setAccessibleName(Bundle.A11Y_Settings(server.getDisplayName()));

        JToolBar toolbar = new ServerToolbar();

        toolbar.add( dropDownButton );
        panelTitle.add( toolbar, BorderLayout.EAST );

        res.add( panelTitle, BorderLayout.CENTER );
        return res;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu res = new JPopupMenu();
        
        boolean isOnline = isOnline();

        // login / logout 
        res.add( isOnline ? 
                    NbBundle.getMessage(ServerPanel.class, "Ctl_LOGOUT") : 
                    NbBundle.getMessage(ServerPanel.class, "Ctl_LOGIN") )
            .addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        if( isOnline() ) {
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    server.logout();
                                }
                            });
                        } else if (TeamUIUtils.showLogin(server)) {
                            justLoggedIn = true;
                        }
                    }
                });
        
        // refresh
        if( isOnline ) {
            res.add( NbBundle.getMessage(OneProjectDashboard.class, "LBL_Refresh") ).addActionListener( new ActionListener() {
                @Override
                public void actionPerformed( ActionEvent e ) {
                    startLoadingProjects( true );
                }
            } );
        }
        
        // sync mode
        res.add( new SyncMenu() );
        
        res.addSeparator();
        
        boolean newOrOpen = false;
        
        // new
        Action a = server.getNewProjectAction();
        if( a != null ) {
            newOrOpen = true;
            res.add( NbBundle.getMessage(ServerPanel.class, "Btn_NEWPROJECT") ).addActionListener(a);
        }
        
        // open
        Action openProjectAction = server.getOpenProjectAction();
        if( openProjectAction != null ) {
            newOrOpen = true;
            res.add( NbBundle.getMessage(ServerPanel.class, "Btn_OPENPROJECT") ).addActionListener(openProjectAction);
        }
        
        if( newOrOpen ) {
            res.addSeparator();
        }
        
        // edit
        res.add( NbBundle.getMessage(ServerPanel.class, "Ctl_EDIT") ).addActionListener( new EditInstanceAction(server));        
        
        // remove
        res.add( NbBundle.getMessage(ServerPanel.class, "Ctl_REMOVE") ).addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                server.getProvider().removeTeamServer( server );
            }
        });
        
        return res;
    }

    private void startLoadingProjects( boolean forceRefresh ) {
        synchronized( LOADER_LOCK ) {
            if( null != currentProjects )
                selModel.remove( currentProjects );
            currentProjects = null;
            panelProjects.removeAll();
            JLabel lblLoading = new JLabel( NbBundle.getMessage(ServerPanel.class, "Lbl_LOADING"));
            lblLoading.setHorizontalAlignment( JLabel.CENTER );
            lblLoading.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
            panelProjects.add( lblLoading, BorderLayout.CENTER );

            loadingToken = new Object();
            RP.post( new ProjectLoader( loadingToken, forceRefresh ) );
            invalidate();
            revalidate();
            doLayout();
        }
    }

    private void rebuild() {
        title.setText(server.getDisplayName());
        title.setToolTipText(getTooltipText(server));

        popupMenu = createPopupMenu();

        Action openProjectAction = server.getOpenProjectAction();
        if (isOnline() || openProjectAction != null) {
            startLoadingProjects(false);
        } else {
            rebuildButtonPanel();
        }
        invalidate();
        revalidate();
        doLayout();
    }

    private void rebuildAndPack() {
        runInAWT(new Runnable() {
            @Override
            public void run() {
                rebuild();
                PopupWindow.pack();
            }
        });
    }

    private void rebuildButtonPanel() throws MissingResourceException {
        panelProjects.removeAll();
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        
        if(server.getStatus() != TeamServer.Status.ONLINE) {
            JButton btnLogin = new LinkButton( NbBundle.getMessage(ServerPanel.class, "Btn_LOGIN"), new AbstractAction() {
                @Override
                public void actionPerformed( ActionEvent e ) {
                    if (TeamUIUtils.showLogin(server)) {
                        justLoggedIn = true;
                        dropDownButton.requestFocus(); // to have something focused when the login button goes away
                    }
                }
            });
            btnLogin.setFocusable( true );

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 8;
            gridBagConstraints.ipady = 8;
            buttonPanel.add( btnLogin, gridBagConstraints );
        }
        
        final Action openProjectAction = server.getOpenProjectAction();
        if(openProjectAction != null) {
            JButton btnOpenProject = new LinkButton( NbBundle.getMessage(ServerPanel.class, "Btn_OPENPROJECT"), new AbstractAction() {
                @Override
                public void actionPerformed( ActionEvent e ) {
                    openProjectAction.actionPerformed(null);
                }
            });
            GridBagConstraints  gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 1;
            gridBagConstraints.ipadx = 8;
            gridBagConstraints.ipady = 8;
            buttonPanel.add( btnOpenProject, gridBagConstraints );
        }
        
        panelProjects.add( buttonPanel, BorderLayout.CENTER );
    }

    private boolean isOnline() {
        return server.getStatus() == TeamServer.Status.ONLINE;
    }

    @NbBundle.Messages({"# {0} - the name of a team server", 
                        "CTL_ServerName=Team Server Name : {0}",
                        "# {0} - the url of a team server", 
                        "CTL_Url=URL : {0}",
                        "CTL_OnlineStatusLI=Online Status : logged in",
                        "CTL_OnlineStatusLO=Online Status : not logged in"})
    private String getTooltipText(TeamServer server) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>") // NOI18N
          .append(Bundle.CTL_ServerName(server.getDisplayName()))
          .append("<br/>")  // NOI18N    
          .append(Bundle.CTL_Url(server.getUrl().toString()))
          .append("<br/>")  // NOI18N    
          .append(isOnline() ? Bundle.CTL_OnlineStatusLI() : Bundle.CTL_OnlineStatusLO())
          .append("</html>");
        return sb.toString();
    }

    @NbBundle.Messages({"# {0} - name of a team server",
                        "A11Y_ServerProjectsList=List of member projects from {0}",
                        "A11Y_SelectProjectToOpen=Select project to open"})
    private final class ProjectLoader implements Runnable {
        
        private final Object loaderToken;
        private final boolean forceRefresh;
        
        public ProjectLoader( Object token, boolean forceRefresh ) {
            this.loaderToken = token;
            this.forceRefresh = forceRefresh;
        }

        @Override
        public void run() {
            boolean wasError = false;
            SelectionList projects = server.getProjects( forceRefresh );
            if( null == projects ) {
                wasError = true;
                projects = new SelectionList();
                ErrorNode node = new ErrorNode( NbBundle.getMessage(ServerPanel.class, "Err_LoadProjects"), new AbstractAction() {
                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        startLoadingProjects( true );
                    }
                });
                ArrayList<ListNode> content = new ArrayList<>( 1 );
                content.add( node );
                projects.setItems( content );
            }
            projects.getAccessibleContext().setAccessibleName(Bundle.A11Y_ServerProjectsList(server.getDisplayName()));
            projects.getAccessibleContext().setAccessibleDescription(Bundle.A11Y_SelectProjectToOpen());
            final SelectionList res = projects;
            final boolean error = wasError;
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    onProjectsLoaded( res, error, loaderToken );
                }
            });
        }
    };

    private void onProjectsLoaded( final SelectionList projects, boolean wasError, Object loaderToken ) {
        synchronized( LOADER_LOCK ) {
            if( loaderToken != loadingToken ) {
                return;
            }

            if( !wasError ) {
                currentProjects = projects;
            }

            ListModel<ListNode> model = projects.getModel();
            if(model.getSize() > 0) {
                panelProjects.removeAll();
                ScrollingContainer sc = new ScrollingContainer( projects, false );
                sc.setBorder( BorderFactory.createEmptyBorder(0,10,0,0) );
                panelProjects.add( sc, BorderLayout.CENTER );
                if( !wasError ) {
                    if (justLoggedIn && selModel.getInitialSelection() == null) {
                        ListNode project = MegaMenu.getPreSelectedProject(); // could be the restored selected project after login
                        if (project != null) {
                            selModel.setInitialSelection(project);
                        }
                    }
                    if (selModel.add(projects) || justLoggedIn) {
                        projects.requestFocus();
                        PopupWindow.setFocusedComponent(projects);
                    }
                }
            } else {
                rebuildButtonPanel();
            }
            justLoggedIn = false;

            this.currentProjects.getModel().removeListDataListener(modelListener);
            this.currentProjects.getModel().addListDataListener(modelListener);

            invalidate();
            revalidate();
            PopupWindow.pack();
        }
    }
    
    static class IconWithArrow implements Icon {

        private static final String ARROW_IMAGE_NAME = "org/netbeans/modules/team/server/resources/arrow.png"; //NOI18N

        private final Icon orig;
        private final Icon arrow = ImageUtilities.loadImageIcon(ARROW_IMAGE_NAME, false);

        private static final int GAP = 6;

        /** Creates a new instance of IconWithArrow */
        public IconWithArrow(  Icon orig ) {
            Parameters.notNull("original icon", orig); //NOI18N
            this.orig = orig;
        }

        @Override
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            int height = getIconHeight();
            orig.paintIcon( c, g, x, y+(height-orig.getIconHeight())/2 );
            arrow.paintIcon( c, g, x+GAP+orig.getIconWidth(), y+(height-arrow.getIconHeight())/2 );
        }

        @Override
        public int getIconWidth() {
            return orig.getIconWidth() + GAP + arrow.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return Math.max( orig.getIconHeight(), arrow.getIconHeight() );
        }
    }    
    
    private void runInAWT(Runnable r) {
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    private class SyncMenuItem extends JCheckBoxMenuItem {
        public SyncMenuItem(String name, ActionListener al) {
            super( name );
            addActionListener( al );
        }
    }
    
    @NbBundle.Messages({"CTL_SyncAll=All Projects",
                        "CTL_SyncOnlySelectedProject=Selected Project Only",
                        "CTL_SyncRecentlySelectedProjects=Recently Selected Projects"})
    private class SyncMenu extends JMenu {
        private final JMenuItem syncAll = new SyncMenuItem(Bundle.CTL_SyncAll(), new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                OneProjectDashboard.SyncMode mode = OneProjectDashboard.SyncMode.PREF_ALL;
                OneProjectDashboard.setSyncMode( server, mode );
                selectItem( mode );
            }
        } );
        private final JMenuItem syncRecentlySelected = new SyncMenuItem(Bundle.CTL_SyncRecentlySelectedProjects(), new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                OneProjectDashboard.SyncMode mode = OneProjectDashboard.SyncMode.PREF_ALL_SELECTED;
                OneProjectDashboard.setSyncMode( server, mode );
                selectItem( mode );
            }
        } );
        private final JMenuItem syncOnlySelected = new SyncMenuItem(Bundle.CTL_SyncOnlySelectedProject(), new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                OneProjectDashboard.SyncMode mode = OneProjectDashboard.SyncMode.PREF_SELECTED;
                OneProjectDashboard.setSyncMode( server, mode );
                selectItem( mode );
            }
        } );

        @NbBundle.Messages({"CTL_SyncMode=Auto Synchronize Services"})        
        public SyncMenu() {
            super(Bundle.CTL_SyncMode());
            add( syncOnlySelected );
            add( syncRecentlySelected );
            add( syncAll );
            selectItem( OneProjectDashboard.getSyncMode(server) );
        }
        
        private void selectItem(OneProjectDashboard.SyncMode mode) {
            switch( mode ) {
                case PREF_ALL:
                    syncAll.setSelected( true );
                    syncRecentlySelected.setSelected( false );
                    syncOnlySelected.setSelected( false );
                    break;
                case PREF_ALL_SELECTED:
                    syncAll.setSelected( false );
                    syncRecentlySelected.setSelected( true );
                    syncOnlySelected.setSelected( false );
                    break;
                case PREF_SELECTED:
                    syncAll.setSelected( false );
                    syncRecentlySelected.setSelected( false );
                    syncOnlySelected.setSelected( true );
                    break;
            }
        }
    }
    
}
