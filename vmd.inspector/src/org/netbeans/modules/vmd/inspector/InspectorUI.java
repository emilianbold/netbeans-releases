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

package org.netbeans.modules.vmd.inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.util.actions.Presenter;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

/**
 *
 * @author Karol Harezlak
 */

final class InspectorUI extends TopComponent implements ExplorerManager.Provider, PropertyChangeListener, Presenter.Popup {

    static final String INSPECTOR_UI_ID = "InspectorUI"; //NOI18N
    private transient ExplorerManager explorerManager;
    private volatile transient boolean lockSelectionSetting;
    private transient BeanTreeView inspectorBeanTreeView;
    private transient WeakReference<DesignDocument> document;

    InspectorUI(DesignDocument document) {
        lockSelectionSetting = false;
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        initComponents();
        lockSelectionSetting = false;
        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        this.document = new WeakReference<DesignDocument>(document);
    }

    private void initComponents() {
        inspectorBeanTreeView = new InspectorBeanTreeView(explorerManager);
        inspectorBeanTreeView.setRootVisible(false);
        setLayout(new BorderLayout());
        add(inspectorBeanTreeView, BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            return;
        }
        if (explorerManager.getSelectedNodes().length < 1) {
            return;
        }
        if (document == null || document.get() == null) {
            return;
        }
        final DesignDocument d = document.get();
        if (d.getTransactionManager().isAccess())
            return;
        d.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                if (lockSelectionSetting) {
                    return;
                }
                try {
                    lockSelectionSetting = true;
                    Node[] selectedNodes = explorerManager.getSelectedNodes();
                    Collection<DesignComponent> selectedComponents = new WeakSet<DesignComponent>();
                    for (Node node : selectedNodes) {
                        if (node instanceof InspectorFolderNode) {
                            Long componentID = ((InspectorFolderNode) node).getComponentID();
                            DesignComponent component = componentID == null ? null : d.getComponentByUID(componentID);
                            if (component != null) {
                                selectedComponents.add(component);
                            }
                        }
                    }
                    d.setSelectedComponents(InspectorUI.INSPECTOR_UI_ID, selectedComponents);
                } finally {
                    lockSelectionSetting = false;
                }
            }
        });
    }

    void expandNodes(final Collection<InspectorFolderWrapper> foldersToUpdate) {
        if (foldersToUpdate == null) {
            return;
        }
        for (InspectorFolderWrapper wrapper : foldersToUpdate) {
            InspectorFolderNode node = wrapper.getNode();
            if (node != null) {
                inspectorBeanTreeView.expandNode(node);
            }
        }
    }

    synchronized void setRootNode(final Node rootNode) {
        getExplorerManager().setRootContext(rootNode);
        revalidate();
        repaint();
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    public JMenuItem getPopupPresenter() {
        return new JMenu("menu"); //NOI18N
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(explorerManager, true);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        ExplorerUtils.activateActions(explorerManager, false);
    }
}
