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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
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
                    //createPasteTypes(transferable, new ArrayList());
                }
            });
        }
        ((InspectorChildren) getChildren()).setKeys(folderWrapper.getChildrenNodes());
    }
    
    protected void createPasteTypes(Transferable t, java.util.List s) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> CreatePasteType" + this);
        super.createPasteTypes(t, s);
        PasteType paste = getDropType(t, DnDConstants.ACTION_COPY, -1 );
        if( null != paste )
            s.add( paste );
    }
    
    public PasteType getDropType(final Transferable t, final int action, int index) {
        DesignComponent transComponent = null;
        if (t.isDataFlavorSupported(DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR) == false)
            return null;
        try {
            transComponent = (DesignComponent) t.getTransferData(DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (transComponent == null)
            return null;
        final DesignComponent ifnc = transComponent;
        ifnc.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                Map<AcceptPresenter, ComponentProducer> presentersMap = new HashMap<AcceptPresenter,ComponentProducer>();
                if (component == null)
                    return;
                for ( AcceptPresenter presenter : component.get().getPresenters(AcceptPresenter.class) ){
                    ComponentProducer producer = DocumentSupport.getComponentProducer(ifnc.getDocument(), ifnc.getType());
                    presentersMap.put(presenter, producer);
                }
                for (final AcceptPresenter presenter : presentersMap.keySet()) {
                    final Transferable trans = new NodeTransferable(ifnc);
                    System.out.println(presenter);
                    System.out.println(presenter.isAcceptable(trans, null));
                    if (presenter.getKind() == AcceptPresenter.Kind.TRANSFERABLE &&  presenter.isAcceptable(trans, null)) {
                        pasteType = new PasteType() {
                            public Transferable paste() throws IOException {
                                ifnc.getDocument().getTransactionManager().writeAccess(new Runnable() {
                                    public void run() {
                                        presenter.accept(trans, null);
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

    @Override
    public boolean canCopy() {
        return true;
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        return transferable;
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        return transferable;
    }
    
    
    
    public boolean canDestroy() {
        return true;
    }
    
    DesignComponent getComponent() {
        return component.get();
    }
    
    void terminate() {
        componentID = null;
        component = null;
        folder = null;
    }
    
    public Sheet createSheet() {
        if(component.get() == null)
            super.createSheet();
        DataEditorView view = ActiveViewSupport.getDefault().getActiveView();
        if (view != null && view.getKind() == DataEditorView.Kind.MODEL)
            return PropertiesSupport.getSheet(view, component.get());
        return super.createSheet();
    }
    
    private class NodeTransferable implements Transferable {
        
        private WeakReference<DesignComponent> component;
        
        public NodeTransferable(DesignComponent component) {
            assert (component != null);
            this.component = new WeakReference<DesignComponent>(component);
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR};
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor == DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR)
                return true;
            return false;
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return component.get();
        }
    };
    
}
