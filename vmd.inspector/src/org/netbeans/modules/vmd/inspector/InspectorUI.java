/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    private transient DesignDocument document;

    InspectorUI(DesignDocument document) {
        lockSelectionSetting = false;
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        initComponents();
        lockSelectionSetting = false;
        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        this.document = document;
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
        if (document == null || document.getTransactionManager().isAccess()) {
            return;
        }
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                if (lockSelectionSetting) {
                    return;
                }
                try {
                    lockSelectionSetting = true;
                    Node[] selectedNodes = explorerManager.getSelectedNodes();
                    WeakSet<DesignComponent> selectedComponents = new WeakSet<DesignComponent>();
                    for (Node node : selectedNodes) {
                        if (node instanceof InspectorFolderNode) {
                            Long componentID = ((InspectorFolderNode) node).getComponentID();
                            DesignComponent component = componentID == null ? null : document.getComponentByUID(componentID);
                            if (component != null) {
                                selectedComponents.add(component);
                            }
                        }
                    }
                    document.setSelectedComponents(InspectorUI.INSPECTOR_UI_ID, selectedComponents);
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

    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }

    public Action[] getActions() {
        return new Action[0];
    }

    public JMenuItem getPopupPresenter() {
        return new JMenu("menu"); //NOI18N
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(explorerManager, true);
    }

    protected void componentDeactivated() {
        super.componentDeactivated();
        ExplorerUtils.activateActions(explorerManager, false);
    }
}
