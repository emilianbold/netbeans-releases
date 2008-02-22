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
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.nodes.ImportWsdlNode;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.CorrelationPropertyMainPanel;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class AddPropertyToWsdlAction extends BpelNodeAction {
    
    protected String getBundleName() {
        return NbBundle.getMessage(AddPropertyToWsdlAction.class,
                "CTL_AddPropertyToWsdlAction"); // NOI18N
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        Import imprt = ((ImportWsdlNode)nodes[0]).getReference();
        if (imprt == null) {
            return;
        }
        final WSDLModel wsdlModel = ImportHelper.getWsdlModel(imprt, true);
        if (wsdlModel == null) {
            return;
        }
        ModelSource modelSource = wsdlModel.getModelSource();
        if (modelSource == null || !modelSource.isEditable()) {
            return;
        }
        FileObject fo = modelSource.getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid() || !fo.canWrite()) {
            return;
        }
        //
        Lookup lookup = nodes[0].getLookup();
        // 
        final CorrelationProperty property = (CorrelationProperty)wsdlModel.
                getFactory().create(
                    wsdlModel.getDefinitions(),
                    BPELQName.PROPERTY.getQName());
        // 
        CorrelationPropertyNode corrPropertyNode =
                new CorrelationPropertyNode(property, lookup); 
        
        //
        final String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "DLG_AddCorrelationProperty");
        
        SimpleCustomEditor customEditor = new SimpleCustomEditor<CorrelationProperty>(
                corrPropertyNode, CorrelationPropertyMainPanel.class, 
                EditingMode.CREATE_NEW_INSTANCE);
        //
        NodeEditorDescriptor descriptor =
                new NodeEditorDescriptor(customEditor, dialogTitle);
        descriptor.setOkButtonProcessor(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                wsdlModel.addChildComponent(wsdlModel.getRootComponent(),property,0);
                return Boolean.TRUE;
            }
        });
        descriptor.setHelpCtx(null);
        //
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }
    
    public boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1 || !(nodes[0] instanceof ImportWsdlNode)) {
            return false;
        }
        Import imprt = ((ImportWsdlNode)nodes[0]).getReference();
        if (imprt == null) {
            return false;
        }
        final WSDLModel wsdlModel = ImportHelper.getWsdlModel(imprt, true);
        if (wsdlModel == null) {
            return false;
        }
        ModelSource modelSource = wsdlModel.getModelSource();
        if (modelSource == null || !modelSource.isEditable()) {
            return false;
        }
        FileObject fo = modelSource.getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid() || !fo.canWrite()) {
            return false;
        }
        //
        return true;
    }
    
    public ActionType getType() {
        return ActionType.ADD_PROPERTY_TO_WSDL;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
}
