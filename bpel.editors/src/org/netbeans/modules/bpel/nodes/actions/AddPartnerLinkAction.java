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
import java.awt.Dialog;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.nodes.PartnerLinkNode;
import org.netbeans.modules.bpel.properties.editors.CorrelationSetMainPanel;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.editors.PartnerLinkMainPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 14 April 2006
 *
 */
public class AddPartnerLinkAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;


    public AddPartnerLinkAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddPartnerLinkAction")); // NOI18N
    }    
    
    protected String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_AddPartnerLinkAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_PARTNER_LINK;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(getBpelEntities(nodes))) {
            return;
        }
        //
        final Process bpelProcess;
        
        bpelProcess = (Process)((BpelNode)nodes[0]).getReference();
        if (bpelProcess == null) {
            return;
        }
        final BPELElementsBuilder elementBuilder = 
                bpelProcess.getBpelModel().getBuilder();
        final PartnerLink newPartnerLink = elementBuilder.createPartnerLink();
        //
        Lookup lookup = nodes[0].getLookup();
        PartnerLinkNode plNode =
                new PartnerLinkNode(newPartnerLink, lookup);
        //
        String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "DLG_AddPartnerLink"); // NOI18N
        SimpleCustomEditor customEditor = new SimpleCustomEditor<PartnerLink>(
                plNode, PartnerLinkMainPanel.class,
                EditingMode.CREATE_NEW_INSTANCE);
        //
        NodeEditorDescriptor descriptor =
                new NodeEditorDescriptor(customEditor, dialogTitle);
        descriptor.setOkButtonProcessor(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                PartnerLinkContainer container = bpelProcess.getPartnerLinkContainer();
                if (container == null) {
                    container = elementBuilder.createPartnerLinkContainer();
                    bpelProcess.setPartnerLinkContainer(container);
                    container = bpelProcess.getPartnerLinkContainer();
                }
                //
                container.insertPartnerLink(newPartnerLink, 0);
                return Boolean.TRUE;
            }
        });
        //
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        return (bpelEntity instanceof Process);
    }
}
