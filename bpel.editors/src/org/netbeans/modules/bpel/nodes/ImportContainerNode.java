/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.bpel.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import org.netbeans.modules.bpel.design.nodes.DiagramExtInfo;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.design.nodes.image.FolderIcon;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.AddImportAction;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vitaly Bychkov
 */
public class ImportContainerNode extends DiagramBpelNode<Process, DiagramExtInfo> {
    public ImportContainerNode(Process reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public ImportContainerNode(Process reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public Image getIcon(int type) {
        return FolderIcon.getClosedIcon();
    }

    public Image getOpenedIcon(int type) {
        return FolderIcon.getOpenedIcon();
    }
    
    public NodeType getNodeType() {
        return NodeType.IMPORT_CONTAINER;
    }

    public String getDisplayName() {
        return getNodeType().getDisplayName();
    }

    protected String getImplHtmlDisplayName() {
        return getDisplayName();
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_IMPORT
        };
    }

    public Action createAction(ActionType actionType) {
        Action action = null;
        switch (actionType) {
            case ADD_IMPORT : 
                action = SystemAction.get(AddImportAction.class);
                break;    
            default 
                : action = super.createAction(actionType);
        }
        
        return action;
    }
}
