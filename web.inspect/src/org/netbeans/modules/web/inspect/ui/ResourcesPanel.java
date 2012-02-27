/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import javax.swing.*;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel that displays resources of the inspected page.
 *
 * @author Jan Stola
 */
public class ResourcesPanel extends JPanel implements ExplorerManager.Provider {
    /** Request processor used for interaction with {@code PageModel}. */
    private static final RequestProcessor RP = new RequestProcessor(ResourcesPanel.class.getName(), 5);
    /** Explorer manager provided by this panel. */
    private ExplorerManager manager = new ExplorerManager();
    /** Tree view that displays the resources of the inspected page. */
    private BeanTreeView treeView;
    /** Label used when no page is inspected. */
    private JLabel noResourcesLabel;
    /** Context actions (actions shown when the panel/view is right-clicked) of this panel. */
    private Action[] contextActions;
   /** Page model used by this panel. */
    private PageModel pageModel;

    /**
     * Creates a new {@code ResourcesPanel}.
     */
    public ResourcesPanel(PageModel pageModel) {
        this.pageModel = pageModel;
        setLayout(new BorderLayout());
        initTreeView();
        initResourcesLabel();
        add(noResourcesLabel);
        update(true);
    }

    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        treeView = new BeanTreeView();
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
    }

    /**
     * Initializes the "no resources available" label.
     */
    private void initResourcesLabel() {
        noResourcesLabel = new JLabel();
        noResourcesLabel.setText(NbBundle.getMessage(DomPanel.class, "ResourcesPanel.noResourcesLabel.text")); // NOI18N
        noResourcesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noResourcesLabel.setVerticalAlignment(SwingConstants.CENTER);
        noResourcesLabel.setEnabled(false);
        noResourcesLabel.setBackground(treeView.getViewport().getView().getBackground());
        noResourcesLabel.setOpaque(true);
    }

    /**
     * Updates the content of the panel. It fetches the current data
     * from the model and updates the view accordingly.
     * 
     * @param rebuild determines if the view should be rebuilt completely
     * or whether it should attempt to keep the current expand/collapse state
     * of the nodes displayed currently.
     */
    private void update(final boolean rebuild) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Collection<PageModel.ResourceInfo> resources;
                if (pageModel == null) {
                    resources = Collections.EMPTY_LIST;
                } else {
                    resources = pageModel.getResources();
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        update(rebuild, resources);
                    }
                });
            }
        });
    }

    /**
     * Updates the content of the panel.
     * 
     * @param rebuild determines if the view should be rebuilt completely
     * or whether it should attempt to keep the current expand/collapse state
     * of the nodes displayed currently.
     * @param resources new resources that should be shown in the panel.
     */
    private void update(boolean rebuild, Collection<PageModel.ResourceInfo> resources) {
        if (resources == null) {
            Node root = new AbstractNode(Children.LEAF);
            replace(treeView, noResourcesLabel);
            manager.setRootContext(root);
        } else {
            if (rebuild) {
                ResourcesNode root = new ResourcesNode(getContextActions());
                root.setResources(resources);
                manager.setRootContext(root);
            } else {
                // Trying to keep the selected nodes and expanded branches in the tree view
                ResourcesNode root = (ResourcesNode)manager.getRootContext();
                root.setResources(resources);
            }
            replace(noResourcesLabel, treeView);
        }
    }

    /**
     * Helper method that replaces one component by another (if it is necessary).
     * 
     * @param componentToHide component that should be hidden.
     * @param componentToShow component that should be shown.
     */
    private void replace(Component componentToHide, Component componentToShow) {
        if (componentToHide.getParent() != null) {
            remove(componentToHide);
            add(componentToShow);
            revalidate();
            repaint();
        }
    }

    /**
     * Returns context actions, i.e., the actions that are shown when
     * the tree view is right-clicked.
     * 
     * @return context actions.
     */
    private Action[] getContextActions() {
        if (contextActions == null) {
            contextActions = new Action[] {
                createRefreshAction()
            };
        }
        return contextActions;
    }

    /**
     * Creates an action that refreshes the content of this panel.
     * 
     * @return action that refreshes the content of this panel.
     */
    private Action createRefreshAction() {
        String name = NbBundle.getMessage(ResourcesPanel.class, "ResourcesPanel.refreshAction"); // NOI18N
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(false);
            }
        };
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}
