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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.wsdleditorapi.generator;

import java.awt.Dialog;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;

import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
//import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

public class SchemaImportPasteType extends PasteType {

    private final WSDLModel currModel;
    private final SchemaModel schemaModel;

    public SchemaImportPasteType(WSDLModel currModel, SchemaModel schemaModel) {
        this.currModel = currModel;
        this.schemaModel = schemaModel;

    }

    @Override
    public Transferable paste() throws IOException {
        String errorMessage = validate(schemaModel, currModel);
        if (errorMessage != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(new Exception(errorMessage)));
            return null;
        }

        String prefix = NameGenerator.getInstance().generateNamespacePrefix(null, currModel);
        DataObject dObj = ActionHelper.getDataObject(schemaModel);
        Project project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        DnDImportPanel panel = new DnDImportPanel();
        panel.setNamespace(schemaModel.getSchema().getTargetNamespace());
        panel.setProject(project);
        panel.setPrefix(prefix);
        panel.setFileName(dObj.getPrimaryFile());
        
        Set prefixes = ((AbstractDocumentComponent)currModel.getDefinitions()).getPrefixes().keySet();
        panel.setPrefixes(prefixes);
        
        final DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(SchemaImportPasteType.class, "LBL_ConfirmSchemaDocDrop"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SchemaImportPasteType.class),
                null);
        panel.setDialogDescriptor(descriptor);


        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);

        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            prefix = panel.getPrefix();

            org.netbeans.modules.xml.schema.model.Import newImport = null;
            try {
                currModel.startTransaction();
                Utility.addNamespacePrefix(schemaModel.getSchema(), currModel, prefix);
                newImport = Utility.addSchemaImport(schemaModel, currModel);
            } finally {
                if (currModel.isIntransaction()) {
                    currModel.endTransaction();
                }
            }
            if (newImport != null) {
                ActionHelper.selectNode(newImport, currModel);
            }
        }
        return null;
    }

    private String validate(SchemaModel model, WSDLModel wsdlModel) {
        if (model.getState() != SchemaModel.State.VALID) {
            return NbBundle.getMessage(SchemaImportPasteType.class,
            "ERRMSG_SchemaIsNotValid");
        }
        
        if (!Utility.canImport(model, wsdlModel)) {
            return NbBundle.getMessage(SchemaImportPasteType.class, "ERRMSG_ProjectNotReferenceable");
        }
        //For wsdl, imported schema's should have a namespace.

        String tns = model.getSchema().getTargetNamespace();
        if (tns == null) {
            return NbBundle.getMessage(SchemaImportPasteType.class,
                    "ERRMSG_NoNamespace");
        }


        if (model.equals(wsdlModel)) {
            return NbBundle.getMessage(SchemaImportPasteType.class,
                    "ERRMSG_SameModel");
        }

        Definitions def = wsdlModel.getDefinitions();
        String namespace = def.getTargetNamespace();
        // This is an import, which must have no namespace, or a
        // different one than the customized component.
        // MALKIT: Masked below as catalog support not avaialbe
//        if (tns != null && !Utilities.NO_NAME_SPACE.equals(tns) &&
//                namespace.equals(tns)) {
//            return NbBundle.getMessage(SchemaImportPasteType.class,
//                    "ERRMSG_SameNamespace");
//        }
        
        Types types = def.getTypes();

        if (types == null) {
            return null;
        }

        Collection<WSDLSchema> wsdlSchemas = types.getExtensibilityElements(WSDLSchema.class);
        for (WSDLSchema wSchema : wsdlSchemas) {
            SchemaModel sm = wSchema.getSchemaModel();
            Collection<SchemaModelReference> references =
                    sm.getSchema().getSchemaReferences();
            // Ensure the selected document is not already among the
            // set that have been included.
            for (SchemaModelReference ref : references) {
                try {
                    SchemaModel otherModel = ref.resolveReferencedModel();
                    if (model.equals(otherModel)) {
                        return NbBundle.getMessage(SchemaImportPasteType.class,
                                "ERRMSG_AlreadyRefd");
                    }
                } catch (CatalogModelException cme) {
                // Ignore that one as it does not matter.
                }
            }
        }

        return null;
    }
}
