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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 23 March 2006
 */
public class ShowPropertyEditorAction extends BpelNodeAction {

    protected String getBundleName() {
        return NbBundle.getMessage(ShowPropertyEditorAction.class, 
                "CTL_PropertyEditor"); // NOI18N
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        NodeUtils.showNodeCustomEditor((BpelNode)nodes[0], 
                CustomNodeEditor.EditingMode.EDIT_INSTANCE);
    }

    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    private NodeFactory getNodeFactory(){
        return NavigatorNodeFactory.getInstance();
    }

    
    public boolean enable(final Node[] nodes) {
        if (nodes == null) return false;
        if (nodes.length != 1) return false;
        if (!(nodes[0] instanceof BpelNode)) return false;

        return !((BpelNode) nodes[0]).isModelReadOnly();
    }
    
    
    public ActionType getType() {
        return ActionType.SHOW_POPERTY_EDITOR;
    }
}
