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
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.pastetype;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;
import java.awt.Dialog;

import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

public class WSDLImportPasteType extends PasteType {

    private final WSDLModel newModel;
    private final WSDLModel currModel;

    public WSDLImportPasteType(WSDLModel currModel, WSDLModel newModel) {
        this.currModel = currModel;
        this.newModel = newModel;

    }

    @Override
    public Transferable paste() throws IOException {
        if (newModel.equals(currModel)) {
            return null;
        }
        String errorMessage = validate();
        if (errorMessage != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(new Exception(errorMessage)));
            return null;
        }
        Set prefixes = ((AbstractDocumentComponent) currModel.getDefinitions()).getPrefixes().keySet();

        String prefix = NameGenerator.getInstance().generateNamespacePrefix(null, currModel);
        DataObject dObj = ActionHelper.getDataObject(newModel);
        Project project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        DnDImportPanel panel = new DnDImportPanel();
        panel.setNamespace(newModel.getDefinitions().getTargetNamespace());
        panel.setProject(project);
        panel.setPrefix(prefix);
        panel.setFileName(dObj.getPrimaryFile());
        panel.setPrefixes(prefixes);
        final DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(WSDLImportPasteType.class, "LBL_ConfirmWSDLDocDrop"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(WSDLImportPasteType.class),
                null);
        panel.setDialogDescriptor(descriptor);


        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);

        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            prefix = panel.getPrefix();
            Import newImport = null;
            try {
                currModel.startTransaction();
                Utility.addNamespacePrefix(newModel, currModel, prefix);
                newImport = Utility.addWSDLImport(newModel, currModel);
            } finally {
                if (currModel.isIntransaction()) {
                    currModel.endTransaction();
                }
            }
            if (newImport != null) {
                ActionHelper.selectNode(newImport);
            }
        }

        return null;
    }

    String validate() {
        DataObject dObj = ActionHelper.getDataObject(newModel);
        
        if (newModel.getState() != WSDLModel.State.VALID) {
            return NbBundle.getMessage(WSDLImportPasteType.class, "ERRMSG_ImportedWSDLIsNotValid", dObj.getPrimaryFile().getNameExt());
        }
        
        if (!Utility.canImport(newModel, currModel)) {
            return NbBundle.getMessage(WSDLImportPasteType.class, "ERRMSG_ProjectNotReferenceable");
        }
        
        String impNamespace = newModel.getDefinitions().getTargetNamespace();
        if (impNamespace == null) {
            return NbBundle.getMessage(WSDLImportPasteType.class, "ERRMSG_ImportedWSDLWithNoTargetNamespace", dObj.getPrimaryFile().getNameExt());
        }
        Collection<Import> references =
                currModel.getDefinitions().getImports();
        // Ensure the selected document is not already among the
        // set that have been included.
        for (Import ref : references) {
            try {
                WSDLModel otherModel = ref.getImportedWSDLModel();
                if (newModel.equals(otherModel)) {
                    return NbBundle.getMessage(WSDLImportPasteType.class, "ERRMSG_AlreadyRefd");
                }
            } catch (CatalogModelException cme) {
            // Ignore that one as it does not matter.
            }
        }
        return null;
    }
}
