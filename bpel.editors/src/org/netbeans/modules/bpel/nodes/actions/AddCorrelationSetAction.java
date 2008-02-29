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
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.properties.editors.CorrelationSetMainPanel;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
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
public class AddCorrelationSetAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;

    public AddCorrelationSetAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddCorrelationSetAction")); // NOI18N
    }    
    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddCorrelationSetAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_CORRELATION_SET;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    @Override
    public void performAction(Node[] nodes) {
        //
        final BaseScope baseScope;
        
        baseScope = (BaseScope)((BpelNode)nodes[0]).getReference();
        if (baseScope == null) {
            return;
        }
        final BPELElementsBuilder elementBuilder = 
                baseScope.getBpelModel().getBuilder();
        final CorrelationSet newCorrSet = elementBuilder.createCorrelationSet();
        //
        Lookup lookup = nodes[0].getLookup();
        Children children = new CorrelationSetNode.MyChildren(newCorrSet, lookup);
        CorrelationSetNode corrSetNode =
                new CorrelationSetNode(newCorrSet, children, lookup);
        //
        String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "DLG_AddCorrelationSet"); // NOI18N
        SimpleCustomEditor customEditor = new SimpleCustomEditor<CorrelationSet>(
                corrSetNode, CorrelationSetMainPanel.class,
                EditingMode.CREATE_NEW_INSTANCE);
        //
        NodeEditorDescriptor descriptor =
                new NodeEditorDescriptor(customEditor, dialogTitle);
        descriptor.setOkButtonProcessor(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                CorrelationSetContainer container = baseScope.getCorrelationSetContainer();
                if (container == null) {
                    container = elementBuilder.createCorrelationSetContainer();
                    baseScope.setCorrelationSetContainer(container);
                    container = baseScope.getCorrelationSetContainer();
                }
                //
                container.insertCorrelationSet(newCorrSet, 0);
                return Boolean.TRUE;
            }
        });
        //
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
            getClass(), "A11_DESCRIPTOR_AddCorrelationSetDialog"));
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
            getClass(), "A11_NAME_AddCorrelationSetDialog"));
        dialog.setVisible(true);
    }
    
//    public void performAction(Node[] nodes) {
//        //
//        final BaseScope baseScope;
//
//        baseScope = (BaseScope)((BpelNode)nodes[0]).getReference();
//        if (baseScope == null) {
//            return;
//        }
//        BPELElementsBuilder elementBuilder = baseScope.getBpelModel().getBuilder();
//        //
//        boolean containerAdded = false;
//        CorrelationSetContainer container = baseScope.getCorrelationSetContainer();
//        if (container == null) {
//            container = elementBuilder.createCorrelationSetContainer();
//            baseScope.setCorrelationSetContainer(container);
//            container = baseScope.getCorrelationSetContainer();
//            containerAdded = true;
//        }
//        //
//        CorrelationSet newCorrSet = elementBuilder.createCorrelationSet();
//        container.insertCorrelationSet(newCorrSet, 0);
//        newCorrSet = container.getCorrelationSet(0);
//        //
//        Lookup lookup = nodes[0].getLookup();
//        Children children = new CorrelationSetNode.MyChildren(newCorrSet, lookup);
//        CorrelationSetNode corrSetNode =
//                new CorrelationSetNode(newCorrSet, children, lookup);
//        //
//        String dialogTitle = NbBundle.getMessage(
//                FormBundle.class, "DLG_AddCorrelationSet"); // NOI18N
//        SimpleCustomEditor customEditor = new SimpleCustomEditor<CorrelationSet>(
//                corrSetNode, CorrelationSetMainPanel.class,
//                EditingMode.EDIT_INSTANCE);
//        NodeEditorDescriptor descriptor =
//                new NodeEditorDescriptor(customEditor, dialogTitle);
//        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
//        dialog.setVisible(true);
//        //
//        if (!descriptor.isOkHasPressed()) {
//            container.removeCorrelationSet(0);
//            if (containerAdded) {
//                baseScope.removeCorrelationSetContainer();
//            }
//        }
//        //
////        BaseScope baseScope = (BaseScope) ((BpelNode)nodes[0]).getReference();
////        BPELElementsBuilder elementBuilder = baseScope.getBpelModel().getBuilder();
////        //
////        CorrelationSetContainer container = baseScope.getCorrelationSetContainer();
////        if (container == null) {
////            container = elementBuilder.createCorrelationSetContainer();
////            baseScope.setCorrelationSetContainer(container);
////            container = baseScope.getCorrelationSetContainer();
////        }
////        CorrelationSet newCorrSet = elementBuilder.createCorrelationSet();
////        container.addCorrelationSet(newCorrSet);
//    }
//

    @Override
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        return (bpelEntity instanceof BaseScope);
    }
}
