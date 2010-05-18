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
package org.netbeans.modules.xslt.tmap.nodes.actions;

import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.nodes.ImportsContainerNode;
import org.netbeans.modules.xslt.tmap.nodes.TransformMapNode;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xslt.tmap.ui.importchooser.ImportWSDLCreator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 */
public class AddWsdlImportAction extends TMapAbstractNodeAction {

    public AddWsdlImportAction() {
        super();
    }

    @Override
    protected String getBundleName() {
        return NbBundle.getMessage(AddWsdlImportAction.class,
                "CTL_AddWsdlImportAction"); // NOI18N
    }

    @Override
    public ActionType getType() {
        return ActionType.ADD_WSDL_IMPORT;
    }

    @Override
    protected boolean enable(TMapComponent[] tmapComponents) {
        return tmapComponents != null && tmapComponents.length > 0 &&
                super.enable(tmapComponents) && tmapComponents[0] instanceof TransformMap;
    }
    
    @Override
    protected void performAction(TMapComponent[] tmapComponents) {
    }

    @Override
    public void performAction(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return;
        }
        //
        final TransformMap transformMap;
        if (nodes[0] instanceof TransformMapNode) {
            transformMap = ((TransformMapNode)nodes[0]).getReference().getReference();
        } else if (nodes[0] instanceof ImportsContainerNode) {
            transformMap = ((ImportsContainerNode)nodes[0]).getReference().getReference();
        } else {
            transformMap = null;
        }
        //
        if (transformMap == null) {
            return;
        }
        //
        TMapModel model = transformMap.getModel();
        //
        String dialogTitle = NbBundle.getMessage(
                AddWsdlImportAction.class, "LBL_AddWsdlImport"); // NOI18N
        ImportWSDLCreator creator = new ImportWSDLCreator(transformMap);
        DialogDescriptor descriptor =
                UIUtilities.getCustomizerDialog(creator, dialogTitle, true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
    }
}
