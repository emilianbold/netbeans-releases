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
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.netbeans.modules.bpel.nodes.ImportContainerNode;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.importchooser.ImportSchemaCreator;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 28 March 2006
 */
public class AddSchemaImportAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    protected String getBundleName() {
        return NbBundle.getMessage(AddSchemaImportAction.class,
                "CTL_AddSchemaImportAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_SCHEMA_IMPORT;
    }
    
    public void performAction(Node[] nodeArr) {
        if (nodeArr == null || nodeArr.length == 0) {
            return;
        }
        //
        final Process process;
        if (nodeArr[0] instanceof BpelProcessNode) {
            process = ((BpelProcessNode)nodeArr[0]).getReference();
        } else if (nodeArr[0] instanceof ImportContainerNode) {
            process = ((ImportContainerNode)nodeArr[0]).getReference();
        } else {
            process = null;
        }
        //
        if (process == null) {
            return;
        }
        //
        BpelModel model = process.getBpelModel();
        //
        String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "LBL_AddSchemaImport"); // NOI18N
        ImportSchemaCreator creator = new ImportSchemaCreator(process);
        DialogDescriptor descriptor =
                UIUtilities.getCustomizerDialog(creator, dialogTitle, true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
//        System.out.println("bpelEntities: "+(bpelEntities.length)+"; entity: "+bpelEntities[0]);
        return bpelEntities != null && bpelEntities.length > 0 &&
                super.enable(bpelEntities) && bpelEntities[0] instanceof Process;
    }
}
