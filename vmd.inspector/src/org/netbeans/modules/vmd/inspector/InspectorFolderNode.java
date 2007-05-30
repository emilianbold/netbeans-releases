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

import java.io.IOException;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.nodes.AbstractNode;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.common.AbstractAcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;


/**
 *
 * @author Karol Harezlak
 */
final class InspectorFolderNode extends AbstractNode {
    
    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];
    
    private Long componentID;
    private WeakReference<DesignComponent> component;
    private InspectorFolder folder;
    private Transferable transferable;
    private PasteType pasteType;
    
    InspectorFolderNode(DataObjectContext context) {
        super(new InspectorChildren(), context.getDataObject().getLookup());
    }
    
    InspectorFolderNode() {
        super(new InspectorChildren());
    }
    
    Long getComponentID() {
        return componentID;
    }
    
    public String getHtmlDisplayName() {
        return folder.getHtmlDisplayName();
    }
    
    public Image getIcon(int type) {
        if (folder == null)
            throw new IllegalStateException("Not resolved Folder- Broken tree structure. Check InspectorPosisitonPresenters and InspectorFolderPresenters"); //NOI18N
        return folder.getIcon();
    }
    
    public Image getOpenedIcon(int type) {
        return folder.getIcon();
    }
    
    public Action[] getActions(boolean context) {
        if (folder.getActions() == null)
            return EMPTY_ACTION_ARRAY;
        return folder.getActions();
    }
    
    public boolean canRename() {
        return folder.canRename();
    }
    
    public void setName(final String name) {
        if (name == null)
            throw new IllegalArgumentException("Argument name cant be null"); //NOI18N
        
        if(component == null || component.get() == null)
            return;
        component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                InfoPresenter presenter = component.get().getPresenter(InfoPresenter.class);
                if (presenter != null){
                    presenter.setEditableName(name);
                }
            }
        });
    }
    
    void resolveNode(final InspectorFolderWrapper folderWrapper, final DesignDocument document) {
        this.folder = folderWrapper.getFolder();
        super.setDisplayName(folder.getDisplayName());
        this.componentID = folder.getComponentID();
        if (folder.getName() == null)
            super.setName(folder.getDisplayName());
        else
            super.setName(folder.getName());
        if (componentID != null) {
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    component = new WeakReference<DesignComponent>(document.getComponentByUID(componentID));
                    transferable = new NodeTransferable(component.get());
                }
            });
        }
        ((InspectorChildren) getChildren()).setKeys(folderWrapper.getChildrenNodes());
    }
    
    protected void createPasteTypes(Transferable t, java.util.List s) {
        PasteType paste = getDropType(t, DnDConstants.ACTION_COPY, -1 );
        if( null != paste )
            s.add( paste );
    }
    
    public PasteType getDropType(final Transferable t, final int action, int index) {
        final Node dropNode = NodeTransfer.node( t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT );
        if (!(dropNode instanceof InspectorFolderNode))
            return null;
         
        final InspectorFolderNode ifn = ((InspectorFolderNode) dropNode);
        ifn.getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
         
                final DesignComponent ifnc = ifn.getComponent();
                Map<AbstractAcceptPresenter, ComponentProducer> presentersMap = new HashMap<AbstractAcceptPresenter,ComponentProducer>();
                if (component == null)
                    return;
                for ( AbstractAcceptPresenter presenter : component.get().getPresenters(AbstractAcceptPresenter.class) ){
                    ComponentProducer producer = DocumentSupport.getComponentProducer(ifnc.getDocument(), ifnc.getType());
                    presentersMap.put(presenter, producer);
                }
                for (final AbstractAcceptPresenter presenter : presentersMap.keySet()) {
                    final Transferable trans = new NodeTransferable(ifnc);
                    if (presenter.getKind() == AbstractAcceptPresenter.Kind.TRANSFERABLE &&  presenter.isAcceptable(trans)) {
                        pasteType = new PasteType() {
                            public Transferable paste() throws IOException {
                                ifnc.getDocument().getTransactionManager().writeAccess(new Runnable() {
                                    public void run() {
                                         presenter.accept(trans);
                                    }
                                });
                                return t;
                            }
                        };
                        return;
                    } else
                        pasteType = null;
                }
            }
        });

        return pasteType;
    }

    public Transferable drag() throws IOException {
        return transferable;
    }
    
    public boolean canCut() {
        return true;
    }

    public boolean canDestroy() {
        return false;
    }
    
    DesignComponent getComponent() {
        return component.get();
    }
    
    void terminate() {
        componentID = null;
        component = null;
        folder = null;
    }
    
    private class NodeTransferable implements Transferable {
        
        private DesignComponentDataFlavor dataFlavor;
        private WeakReference<DesignComponent> component;
        
        public NodeTransferable(DesignComponent component) {
            assert (component != null);
            dataFlavor = new DesignComponentDataFlavor(component);
            this.component = new WeakReference<DesignComponent>(component);
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{dataFlavor};
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return false;
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return component.get();
        }
    };
    
}
