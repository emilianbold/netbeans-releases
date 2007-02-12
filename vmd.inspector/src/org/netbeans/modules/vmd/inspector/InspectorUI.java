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
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.util.actions.Presenter;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;

/**
 *
 * @author Karol Harezlak
 */
final class InspectorUI  extends JPanel implements ExplorerManager.Provider,
                                                   PropertyChangeListener,
                                                   InspectorAccessController.InspectorACListener,
                                                   ActiveDocumentSupport.Listener,
                                                   Presenter.Popup {
    
    private ExplorerManager explorerManager;
    private DesignDocument document;
    private volatile boolean lockSelectionSetting;
    private BeanTreeView inspectorBeanTreeView;
    private InspectorWrapperTree folderWrapperTree;
    
    InspectorUI() {
        lockSelectionSetting = false;
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        setLayout(new BorderLayout());
        folderWrapperTree = new InspectorWrapperTree();
        explorerManager = new ExplorerManager();
        lockSelectionSetting = false;
        explorerManager.addPropertyChangeListener(this);
        ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
        initComponents();
        activeDocumentChanged(null, ActiveDocumentSupport.getDefault().getActiveDocument());
    }
    
    
    void initComponents(){
        inspectorBeanTreeView = new InspectorBeanTreeView(explorerManager);
        setLayout(new BorderLayout());
        inspectorBeanTreeView.setRootVisible(false);
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (! ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
            return;
        
        final DesignDocument localDocument = this.document;
        if (localDocument == null || localDocument.getTransactionManager().isAccess())
            return;
        localDocument.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                if (lockSelectionSetting)
                    return;
                try {
                    lockSelectionSetting = true;
                    Node[] selectedNodes = explorerManager.getSelectedNodes();
                    ArrayList<DesignComponent> selectedComponents = new ArrayList<DesignComponent> ();
                    for (Node node : selectedNodes) {
                        if (node instanceof InspectorFolderNode) {
                            Long componentID = ((InspectorFolderNode) node).getComponentID();
                            DesignComponent component = componentID == null ? null : localDocument.getComponentByUID(componentID);
                            if (component != null)
                                selectedComponents.add(component);
                        }
                    }
                    localDocument.setSelectedComponents(InspectorAccessController.INSPECTOR_ID, selectedComponents);
                } finally {
                    lockSelectionSetting = false;
                }
            }
        });
    }
    
    public void notifyUIContentChanged(final Collection<DesignComponent> createdComponents,final Collection<DesignComponent> affectedComponents) {
        if (document == null)
            return;
        
        folderWrapperTree.buildTree(createdComponents, affectedComponents);
        expandNodes(folderWrapperTree.getFolderToUpdate());
    }
    
    public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
        if (this.document != null) {
            InspectorAccessController accessController = this.document.getListenerManager().getAccessController(InspectorAccessController.class);
            accessController.removeListener(this);
        }
        document = activatedDocument;
        
        if (document != null && document.getListenerManager().getAccessController(InspectorAccessController.class) != null) {
            InspectorAccessController accessController = this.document.getListenerManager().getAccessController(InspectorAccessController.class);
            accessController.addListener(this);
            
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    if (document.getRootComponent() == null)
                        return;
                    folderWrapperTree.setCurrentDesignDocument(document);
                    initView();
                    notifyUISelectionChanged();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setLayout(new BorderLayout());
                            add(inspectorBeanTreeView, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        }
                    });
                }
            });
            if (folderWrapperTree.getRootWrapperFolder() != null)
                expandNodes(folderWrapperTree.getRootWrapperFolder().getChildren());
        } else {
            document = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    remove(inspectorBeanTreeView);
                    setLayout(new GridBagLayout());
                    repaint();
                }
            });
        }
    }
    
    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }
    
    public void notifyUISelectionChanged() {
        if (document == null || document.getListenerManager() == null)
            return;
        final InspectorAccessController accessController = document.getListenerManager().getAccessController(InspectorAccessController.class);
        
        if (accessController != null) {
            if (folderWrapperTree.getRootWrapperFolder().getNode() != null) {
                final Collection<Node> selectedNodes = getSelectedNodes(folderWrapperTree.getRootWrapperFolder());
                if (selectedNodes != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                if (! selectedNodes.isEmpty())
                                    explorerManager.setSelectedNodes(selectedNodes.toArray(new Node[selectedNodes.size()]));
                            } catch(PropertyVetoException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
        if (folderWrapperTree.getRootWrapperFolder().getNode() == null)
            explorerManager.setRootContext(Node.EMPTY);
    }
    
    private void expandNodes(final Collection<InspectorFolderWrapper> foldersToUpdate) {
        if (foldersToUpdate == null)
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (InspectorFolderWrapper wrapper : foldersToUpdate) {
                    InspectorFolderNode node = wrapper.getNode();
                    if (node != null)
                        inspectorBeanTreeView.expandNode(node);
                }
            }
        });
    }
    
    private Collection<Node> getSelectedNodes(final InspectorFolderWrapper parentFolder) {
        final Collection<Node> selectedNodes = new HashSet<Node>();
        
        if (document.getSelectedComponents().isEmpty())
            return selectedNodes;
        for (InspectorFolderWrapper folder : parentFolder.getChildren()) {
            if (folder.getChildren() != null)
                selectedNodes.addAll(getSelectedNodes(folder));
            //TODO Selection based on the ComponentID and Display Name could be not enough it better to create smarter algorithm
            for (DesignComponent component : document.getSelectedComponents()) {
                Long  componentID = folder.getFolder().getComponentID();
                if (componentID == null || componentID  != component.getComponentID())
                    continue;
                if (folder.getFolder().getDisplayName().equals(InfoPresenter.getHtmlDisplayName(component)) )
                    selectedNodes.add(folder.getNode());
            }
        }
        
        return selectedNodes;
    }
    
    private void initView() {
        if (document == null || document.getListenerManager() == null)
            return;
        InspectorAccessController accessController = document.getListenerManager().getAccessController(InspectorAccessController.class);
        if (accessController != null) {
            if (folderWrapperTree.getRootWrapperFolder().getNode() != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (folderWrapperTree.getRootWrapperFolder().getNode() != null)
                            explorerManager.setRootContext(folderWrapperTree.getRootWrapperFolder().getNode());
                    }
                });
            }
        }
    }
    
    public Action[] getActions() {
        return new Action[0];
    }
    
    public JMenuItem getPopupPresenter() {
        return new JMenu("menu");
    }
}
