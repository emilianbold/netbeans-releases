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
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;
import org.openide.nodes.Sheet;
import org.openide.util.datatransfer.PasteType;


/**
 *
 * @author Karol Harezlak
 */
final class InspectorFolderNode extends AbstractNode {
    
    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];
    private  static DataFlavor INSPECTOR_NODE_DATA_FLAVOR = new DataFlavor(NodeTransferable.class, "Nodetransferable"); //NOI18N
    
    private Long componentID;
    private WeakReference<DesignComponent> component;
    private InspectorFolder folder;
    private Transferable transferable;
    
    InspectorFolderNode(DataObjectContext context) {
        super(new InspectorChildren(), context.getDataObject().getLookup());
    }
    
    InspectorFolderNode() {
        super(new InspectorChildren());
    }
    
    Long getComponentID() {
        return componentID;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (component == null)
            return ""; //NOI18N
        final String[] componentTypeName = new String[1];
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                InfoPresenter presenter = getComponent().getPresenter(InfoPresenter.class);
                componentTypeName[0] = presenter.getDisplayName (InfoPresenter.NameType.SECONDARY);
            }
        });
        String name = getName();
        if (getName() != null && getName().contains("<")) { //NOI18N
            name = getName().replace("<", "&lt;").replace(">", "&gt;"); //NOI18N
        }
        return componentTypeName[0] != null ? name + " <font color=\"#808080\">[" + componentTypeName[0] + "]" : name; //NOI18N
        
    }
    
    @Override
    public Image getIcon(int type) {
        if (folder == null)
            throw new IllegalStateException("Not resolved Folder- Broken tree structure. Check InspectorPosisitonPresenters and InspectorFolderPresenters"); //NOI18N
        return folder.getIcon();
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return folder.getIcon();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        if (folder.getActions() == null)
            return EMPTY_ACTION_ARRAY;
        return folder.getActions();
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        return folder.createSuggestion(transferable);
    }
    
    @Override
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
        
        if (folder.getName() == null) {
            super.setName(folder.getDisplayName());
        } else {
            super.setName(folder.getName());
        }
        if (componentID != null) {
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    component = new WeakReference<DesignComponent>(document.getComponentByUID(componentID));
                    transferable = new NodeTransferable(component.get());
                }
            });
        }
        IOUtils.runInAWTNoBlocking(new Runnable() {
            public void run() {
                ((InspectorChildren) getChildren()).setKeys(folderWrapper.getChildrenNodes());
            }
        });
    }
    
    @Override
    protected void createPasteTypes(Transferable t, java.util.List s) {
        super.createPasteTypes(t, s);
        if (!t.isDataFlavorSupported(INSPECTOR_NODE_DATA_FLAVOR)) 
            return;
        
        PasteType paste = getDropType(t, DnDConstants.ACTION_COPY_OR_MOVE, -1);
        if( paste != null)
            s.add(paste);
    }
    
    @Override
    public PasteType getDropType(final Transferable t, final int action, final int index) {
        final PasteType[] pasteType = new PasteType[1];
        if (!t.isDataFlavorSupported(INSPECTOR_NODE_DATA_FLAVOR))
            return null;
        if (component == null || component.get() == null)
            return null;
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                final AcceptSuggestion suggestion = createSuggestion(t);
                if (component.get() != null && AcceptSupport.isAcceptable(component.get(), t, suggestion)) {
                    pasteType[0] = new PasteType() {
                        public Transferable paste() throws IOException {
                            component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {
                                public void run() {
                                    if (component.get() != null && AcceptSupport.isAcceptable(component.get(), t, suggestion)) {
                                        AcceptSupport.accept(component.get(), t, null);
                                    }
                                }
                            });
                            return t;
                        }
                    };
                }
            }
        });
        return pasteType[0];
    }
    
    @Override
    public Transferable drag() throws IOException {
        return transferable;
    }
    
    @Override
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
    
    @Override
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
    
    @Override
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
            return new DataFlavor[]{DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR, INSPECTOR_NODE_DATA_FLAVOR};
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor == DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR)
                return true;
            if (flavor == INSPECTOR_NODE_DATA_FLAVOR)
                return true;
            return false;
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return component.get();
        }
    };
    
}
