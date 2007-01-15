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

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Karol Harezlak
 */
final class InspectorFolderNode extends AbstractNode {

    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];
    
    private Long componentID;
    private DesignComponent component;
    private InspectorFolder folder;
    
    InspectorFolderNode() {
        super(new InspectorChildren());
    }
    
    Long getComponentID() {
        return componentID;
    }

    public String getHtmlDisplayName () {
        return getDisplayName ();
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
    
    public void setName(final String name){
        if (name == null)
            throw new IllegalArgumentException("Argument name cant be null");//NOI18N
        
        if(component == null)
            return;
        component.getDocument().getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                InfoPresenter presenter = component.getPresenter(InfoPresenter.class);
                if (presenter != null){
                    presenter.setEditableName (name);
                }
            }
        });
    }
    
    void resolveNode(final InspectorFolderWrapper folderWrapper, final DesignDocument document) {
        warmUp(this);
        this.folder = folderWrapper.getFolder();
        setDisplayName(folder.getDisplayName());
        this.componentID = folder.getComponentID();
        if (folder.getName() == null)
            super.setName(folder.getDisplayName());
        else 
            super.setName(folder.getName());
        
        if (componentID != null) {
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    component = document.getComponentByUID(componentID);
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ((InspectorChildren) getChildren()).setKeys(folderWrapper.getChildrenNodes());
            }
        });
    }
    
    private void warmUp(Node node) {
        for (Node child : node.getChildren().getNodes())
            warmUp(child);
    }
    
     void terminate() {
        componentID = null;
        component = null;
        folder = null;
    }
}
