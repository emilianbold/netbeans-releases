/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.ods.ui.dashboard;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.team.c2c.api.CloudServer;
import org.netbeans.modules.team.ui.common.ColorManager;
import org.netbeans.modules.team.ui.common.LinkButton;
import org.netbeans.modules.team.ui.treelist.TreeLabel;
import org.netbeans.modules.team.ui.treelist.TreeList;
import org.netbeans.modules.team.ui.treelist.TreeListModel;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static org.netbeans.modules.team.ods.ui.dashboard.Bundle.*;
import org.netbeans.modules.team.ui.common.CategoryNode;
import org.netbeans.modules.team.ui.common.EmptyNode;
import org.netbeans.modules.team.ui.common.ErrorNode;
import org.netbeans.modules.team.ui.common.UserNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jan Becicka, Ondrej Vrabec
 */
public final class DashboardImpl {
    
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        @Override
        public int getSize() {
            return 0;
        }
        @Override
        public Object getElementAt(int index) {
            return null;
        }
    };
    private final TreeList treeList = new TreeList(model);
    private final JScrollPane dashboardComponent;
    private boolean opened = false;

    private final Object LOCK = new Object();

    private CloudServer server;
    private final CategoryNode openProjectsNode;
    private final CategoryNode myProjectsNode;

    private final EmptyNode noOpenProjects = new EmptyNode(NbBundle.getMessage(DashboardImpl.class, "NO_PROJECTS_OPEN"),NbBundle.getMessage(DashboardImpl.class, "LBL_OpeningProjects"));
    private final EmptyNode noMyProjects = new EmptyNode(NbBundle.getMessage(DashboardImpl.class, "NO_MY_PROJECTS"), NbBundle.getMessage(DashboardImpl.class, "LBL_OpeningMyProjects"));
    private final ErrorNode otherProjectsError;
    
    private final PropertyChangeListener userListener;
    private final UserNode userNode;
    
//  XXX  private LoginHandle login;
    
    private DashboardImpl() {
        dashboardComponent = new JScrollPane() {
            @Override
            public void requestFocus() {
                Component view = getViewport().getView();
                if (view != null) {
                    view.requestFocus();
                } else {
                    super.requestFocus();
                }
            }
            @Override
            public boolean requestFocusInWindow() {
                Component view = getViewport().getView();
                return view != null ? view.requestFocusInWindow() : super.requestFocusInWindow();
            }
        };
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());

        userListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
//                XXX
//                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
//                    refreshMemberProjects(true);
//                }
            }
        };

        Action dummy = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        userNode = new UserNode(dummy, dummy, dummy, null, null);
        model.addRoot(-1, userNode);
        
        openProjectsNode = new CategoryNode(org.openide.util.NbBundle.getMessage(DashboardImpl.class, "LBL_OpenProjects"), null); // NOI18N
        model.addRoot(-1, openProjectsNode);
        model.addRoot(-1, noOpenProjects);

        myProjectsNode = new CategoryNode(org.openide.util.NbBundle.getMessage(DashboardImpl.class, "LBL_MyProjects"), // NOI18N
                ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/bookmark.png", true)); // NOI18N
        
//        if (login!=null) {
//            if (!model.getRootNodes().contains(myProjectsNode)) {
//                model.addRoot(-1, myProjectsNode);
//            }
//            if (!model.getRootNodes().contains(noMyProjects)) {
//                model.addRoot(-1, noMyProjects);
//            }
//        }
        
        otherProjectsError = new ErrorNode(NbBundle.getMessage(DashboardImpl.class, "ERR_OpenProjects"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // XXX
//                clearError(otherProjectsError);
//                refreshProjects();
            }
        });
        
        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DashboardImpl.class, "A11Y_TeamProjects"); //NOI18N
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        setServer(server);
    }
    
    private static class Holder {
        private static final DashboardImpl theInstance = new DashboardImpl();
    }
    
    public static DashboardImpl getInstance() {
        return Holder.theInstance;
    }
    
    
    public void setServer (CloudServer server) {
        this.server = server;
    }
    
    public JComponent getComponent() {
        synchronized( LOCK ) {
            if (!opened) {
                switchContent();
                opened = true;
            }
        }
        return dashboardComponent;
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean isEmpty = true;

                synchronized( LOCK ) {
                    isEmpty = true;
                }

                boolean isTreeListShowing = dashboardComponent.getViewport().getView() == treeList;
                if( isEmpty ) {
                    if( isTreeListShowing || dashboardComponent.getViewport().getView() == null ) {
                        dashboardComponent.setViewportView(createEmptyContent());
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                    }
                } else {
                    
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    @Messages({"LBL_No_Project_Open=No Team Project Open", "LBL_WhatIsTeamServer=What is Team Server?"})
    private JComponent createEmptyContent() {
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque(false);
        
        JLabel lbl = new TreeLabel(LBL_No_Project_Open());
        lbl.setForeground(ColorManager.getDefault().getDisabledColor());
        lbl.setHorizontalAlignment(JLabel.CENTER);
        LinkButton btnWhatIs = new LinkButton(LBL_WhatIsTeamServer(), createWhatIsCloudServerAction() );

        model.removeRoot(userNode);
        model.removeRoot(myProjectsNode);
        model.removeRoot(openProjectsNode);
        userNode.set(null, false);
        res.add( userNode.getComponent(UIManager.getColor("List.foreground"), ColorManager.getDefault().getDefaultBackground(), false, false), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 4, 3, 4), 0, 0) ); //NOI18N
        res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
        res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
        res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        return res;
    }
    
    // XXX obsolete? 
    private Action createWhatIsCloudServerAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(
                            new URL("http://netbeans.org/kb/docs/ide/team-servers.html")); //NOI18N
                } catch( MalformedURLException ex ) {
                    //shouldn't happen
                    Exceptions.printStackTrace(ex);
                }
            }
        };
    }
}
