/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DesignListener;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author Karol Harezlak
 */
public final class InspectorManagerView implements DesignDocumentAwareness, ActiveDocumentSupport.Listener, DesignListener {

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
            ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
            document.getListenerManager().addDesignListener(this, new DesignEventFilter().setGlobal(true));
            IOUtils.runInAWTNoBlocking(new Runnable() {
                public void run() {
                    if (folderWrapperTree == null)
                        return;
                     folderWrapperTree.buildTree(null);
                     ui.getExplorerManager().setRootContext(folderWrapperTree.getRootWrapperFolder().getNode());
                }
            });         
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
                    Debug.warning("Access to the Navigator is locked"); //NOI18N
                }
                folderWrapperTree.buildTree(event);
                ui.setRootNode(folderWrapperTree.getRootWrapperFolder().getNode());
            }
        });
    }

    private void notifyUISelectionChanged() {
        final Runnable runnable = new Runnable() {
            public void run() {
                if (folderWrapperTree.isLocked()) {
                    Debug.warning("Access to the Navigator is locked"); //NOI18N
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
        };
        IOUtils.runInAWTNoBlocking( new Runnable(){
            public void run() {
                document.getTransactionManager().readAccess(runnable);
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
                    if (InspectorPanel.getInstance() == null)
                        return;
                    JComponent panel = InspectorPanel.getInstance().getComponent();
                    panel.removeAll();
                    if (ui != null) {
                        //Component was closed while still initializing
                        panel.add(ui, BorderLayout.CENTER);
                    } else {
                        panel.add (emptyPanel, BorderLayout.CENTER);
                    }
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
        InspectorRegistry.getInstance(document).cleanUpRegistry();
        this.ui.getExplorerManager();
    }
    
    ExplorerManager getUIExplorerManager() {
        if (ui != null) {
            return ui.getExplorerManager();
        }
        return null;
    }

    public static ExplorerManager getExplorerManager(DataObjectContext context) {
       for (DataObjectContext context_ : INSTANCES.keySet()) {
           if (context_.getDataObject() == context.getDataObject()) {
               return INSTANCES.get(context_).getUIExplorerManager();
           } 
       }
       
       return null;
    }
    
    
}