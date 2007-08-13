/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.inspector;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import org.netbeans.modules.vmd.api.inspector.InspectorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import java.util.Collection;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DesignListener;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Controls view of Navigator
 * @author Karol Harezlak
 */
final class InspectorManagerView implements DesignDocumentAwareness, ActiveDocumentSupport.Listener, DesignListener {

    private static WeakHashMap<DataObjectContext, InspectorManagerView> INSTANCES = new WeakHashMap<DataObjectContext, InspectorManagerView>();
    private static final JLabel emptyPanel = new JLabel(NbBundle.getMessage(InspectorManagerView.class, "LBL_emptyPanel"), JLabel.CENTER); //NOI18N
    private DesignDocument document;
    private InspectorWrapperTree folderWrapperTree;
    private InspectorUI ui;
    private DataObjectContext context;

    public static void register(DataObjectContext context) {
        assert context != null;
        synchronized (InspectorManagerView.class) {
            if (INSTANCES.get(context) == null) {
                INSTANCES.put(context, new InspectorManagerView(context));
            }
        }
    }

    private InspectorManagerView(DataObjectContext context) {
        this.context = context;
        context.addDesignDocumentAwareness(this);
    }

    public void setDesignDocument(DesignDocument document) {
        if (document != null) {
            this.document = document;
            ui = new InspectorUI(document);
            folderWrapperTree = new InspectorWrapperTree(document, ui);
            ui.getExplorerManager().setRootContext(folderWrapperTree.getRootWrapperFolder().getNode());
            ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
            document.getListenerManager().addDesignListener(this, new DesignEventFilter().setGlobal(true));
            folderWrapperTree.buildTree(null);
        } else if (this.document != null && document == null) {
            ActiveDocumentSupport.getDefault().removeActiveDocumentListener(this);
            this.document.getListenerManager().removeDesignListener(this);
            folderWrapperTree.terminate();
            ui = null;
            folderWrapperTree = null;
            this.document = null;
            INSTANCES.remove(context);
            context = null;
        }
    }

    private void notifyUIContentChanged(final DesignEvent event) {
        IOUtils.runInAWTNoBlocking(new Runnable() {
            public void run() {
                if (folderWrapperTree.isLocked()) {
                    throw new IllegalStateException("Access to the Navigator is locked"); //NOI18N
                }
                folderWrapperTree.buildTree(event);
                ui.setRootNode(folderWrapperTree.getRootWrapperFolder().getNode());
            }
        });
    }

    private void notifyUISelectionChanged() {
        IOUtils.runInAWTNoBlocking(new Runnable() {

            public void run() {
                if (folderWrapperTree.isLocked()) {
                    throw new IllegalStateException("Access to the Navigator is locked"); //NOI18N
                }
                if (document == null || document.getListenerManager() == null) {
                    return;
                }
                if (folderWrapperTree.getRootWrapperFolder().getNode() != null) {
                    final Collection<Node> selectedNodes = folderWrapperTree.getSelectedNodes();
                    if (selectedNodes != null) {
                        try {
                            if (!selectedNodes.isEmpty() && !folderWrapperTree.isLocked()) {
                                ui.getExplorerManager().setSelectedNodes(selectedNodes.toArray(new Node[selectedNodes.size()]));
                            }
                        } catch (PropertyVetoException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (folderWrapperTree.getRootWrapperFolder().getNode() == null) {
                    ui.setRootNode(Node.EMPTY);
                }
            }
        });
    }

    public synchronized void activeDocumentChanged(DesignDocument deactivatedDocument, final DesignDocument activatedDocument) {
        if (deactivatedDocument == document && activatedDocument == null) {
            IOUtils.runInAWTNoBlocking(new Runnable() {
                public void run() {
                    JComponent panel = InspectorPanel.getInstance().getComponent();
                    panel.removeAll();
                    panel.add(emptyPanel, BorderLayout.CENTER);
                    panel.revalidate();
                    panel.repaint();
                }
            });
            return;
        } else if (activatedDocument == document) {
            IOUtils.runInAWTNoBlocking(new Runnable() {
                public void run() {
                    JComponent panel = InspectorPanel.getInstance().getComponent();
                    panel.removeAll();
                    panel.add(ui, BorderLayout.CENTER);
                    panel.revalidate();
                    panel.repaint();
                }
            });
        }
    }

    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }

    public void designChanged(DesignEvent event) {
        if (document.getRootComponent() == null) {
            return;
        }
        if (event.isStructureChanged()) {
            notifyUIContentChanged(event);
        }
        if (event.isSelectionChanged()) {
            notifyUISelectionChanged();
        }
        InspectorRegistry.getDefault().clearRegistry();
    }
}