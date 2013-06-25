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
package org.netbeans.modules.team.ui.picker;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.ui.common.ErrorNode;
import org.netbeans.modules.team.ui.common.LinkButton;
import org.netbeans.modules.team.ui.common.EditInstanceAction;
import org.netbeans.modules.team.ui.common.OneProjectDashboard;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.netbeans.modules.team.ui.util.treelist.ListNode;
import org.netbeans.modules.team.ui.util.treelist.SelectionList;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Mega-menu content for a sing TeamServer.
 * 
 * @author S. Aubrecht
 */
class ServerPanel extends JPanel {

    private static final int MAX_COLUMN_WIDTH = 300;
    private final TeamServer server;
    private final SelectionModel selModel;
    private final static RequestProcessor RP = new RequestProcessor( "ProjectsLoader" ); //NOI18N
    private final Object LOADER_LOCK = new Object();
    private Object loadingToken = null;
    private final JPanel panelProjects;
    private SelectionList currentProjects;
    private final PropertyChangeListener loginListener = new PropertyChangeListener() {

        @Override
        public void propertyChange( PropertyChangeEvent evt ) {
            rebuild();
        }
    };
    private JLabel title;

    private ServerPanel( final TeamServer server, SelectionModel selModel ) {
        super( new BorderLayout(10,5) );
        setOpaque( false );

        this.server = server;
        this.selModel = selModel;

        panelProjects = new JPanel( new BorderLayout() );
        panelProjects.setOpaque( false );

        server.addPropertyChangeListener(new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( TeamServer.PROP_NAME.equals(evt.getPropertyName()) ) {
                    title.setText(server.getDisplayName());
                }
            }
        });
         
        rebuild();
    }

    public static JComponent create( TeamServer server, SelectionModel selModel ) {
        return new ServerPanel( server, selModel );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        server.addPropertyChangeListener( loginListener );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        server.removePropertyChangeListener( loginListener );
    }
    
    private JComponent createHeader() {
        JPanel res = new JPanel( new BorderLayout(10,10) );
        res.setOpaque( false );

        JPanel panelTitle = new JPanel( new BorderLayout( 5, 5) );
        panelTitle.setOpaque( false );
        title = new JLabel( server.getDisplayName() );
        title.setToolTipText( server.getUrl().toString() );
        title.setIcon( server.getIcon() );
        Font f = new JLabel().getFont();
        f = f.deriveFont( Font.BOLD, f.getSize2D()+3 );
        title.setFont( f );
        panelTitle.add( title, BorderLayout.CENTER );

        final JPopupMenu menu = createPopupMenu();
        final JButton dropDownButton = DropDownButtonFactory.createDropDownButton( ImageUtilities.loadImageIcon( "org/netbeans/modules/team/ui/resources/gear.png", true), menu );
        dropDownButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                menu.show( dropDownButton, 0, dropDownButton.getHeight() );
            }
        });
        
        JToolBar toolbar = new ServerToolbar();
        
        toolbar.add( dropDownButton );
        panelTitle.add( toolbar, BorderLayout.EAST );

        res.add( panelTitle, BorderLayout.CENTER );
        return res;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu res = new JPopupMenu();
        
        boolean isOnline = server.getStatus() == TeamServer.Status.ONLINE;

        // login / logout 
        res.add( isOnline ? 
                    NbBundle.getMessage(ServerPanel.class, "Ctl_LOGOUT") : 
                    NbBundle.getMessage(ServerPanel.class, "Ctl_LOGIN") )
            .addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        if( server.getStatus() == TeamServer.Status.ONLINE ) {
                            server.logout();
                            removeAll();
                            rebuild();
                            PopupWindow.pack();
                        } else {
                            doLogin();
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
        res.addSeparator();
        
        boolean newOrOpen = false;
        
        // new
        Action a = server.getNewProjectAction();
        if( a != null ) {
            newOrOpen = true;
            res.add( NbBundle.getMessage(ServerPanel.class, "Btn_NEWPROJECT") ).addActionListener(a);
        }
        
        // open
        a = server.getOpenProjectAction();
        if( a != null ) {
            newOrOpen = true;
            res.add( NbBundle.getMessage(ServerPanel.class, "Btn_OPENPROJECT") ).addActionListener(a);
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
                MegaMenu menu = MegaMenu.getCurrent();
                server.getProvider().removeTeamServer( server );
                menu.showAgain();
            }
        });
        
        return res;
    }

    private JComponent createProjects() {
        JPanel res = new JPanel( new BorderLayout( 5, 5 ) );
        res.setOpaque( false );

        res.add( panelProjects, BorderLayout.CENTER );

        startLoadingProjects( false );

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

    private void doLogin() {
        final MegaMenu menu = MegaMenu.getCurrent();
        if( TeamUIUtils.showLogin( server ) ) {
            SwingUtilities.invokeLater( new Runnable() {

                @Override
                public void run() {
                    if( null != menu )
                        menu.showAgain();
                }
            });
        }
    }

    private void rebuild() {
        removeAll();

        add( createHeader(), BorderLayout.NORTH );

        if( server.getStatus() == TeamServer.Status.ONLINE ) {
            add( createProjects(), BorderLayout.CENTER );
        } else {
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            buttonPanel.setOpaque(false);
            JButton btnLogin = new LinkButton( NbBundle.getMessage(ServerPanel.class, "Btn_LOGIN"), new AbstractAction() {
                @Override
                public void actionPerformed( ActionEvent e ) {
                    TeamUIUtils.showLogin( server );
                }
            });
            btnLogin.setFocusable( true );
            btnLogin.setFocusPainted( true );
            
            btnLogin.setFocusable( true );
            btnLogin.setFocusPainted( true );
            
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 8;
            gridBagConstraints.ipady = 8;
            buttonPanel.add( btnLogin, gridBagConstraints );
            
            final Action a = server.getOpenProjectAction();
            if(a != null) {
                JButton btnOpenProject = new LinkButton( NbBundle.getMessage(ServerPanel.class, "Btn_OPENPROJECT"), new AbstractAction() {
                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        a.actionPerformed(null);
                    }
                });
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridy = 1;
                gridBagConstraints.ipadx = 8;
                gridBagConstraints.ipady = 8;
                buttonPanel.add( btnOpenProject, gridBagConstraints );
            }
            
            
            add( buttonPanel, BorderLayout.CENTER );
        }
        invalidate();
        revalidate();
        doLayout();
    }

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
                ArrayList<ListNode> content = new ArrayList<ListNode>( 1 );
                content.add( node );
                projects.setItems( content );
            }
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

    private void onProjectsLoaded( SelectionList projects, boolean wasError, Object loaderToken ) {
        synchronized( LOADER_LOCK ) {
            if( loaderToken != loadingToken ) {
                return;
            }

            panelProjects.removeAll();
            ScrollingContainer sc = new ScrollingContainer( projects, false );
            sc.setBorder( BorderFactory.createEmptyBorder(0,10,0,0) );
            panelProjects.add( sc, BorderLayout.CENTER );
            if( !wasError ) {
                selModel.add( projects );
                currentProjects = projects;
            }
            invalidate();
            revalidate();
            PopupWindow.pack();
        }
    }
}
