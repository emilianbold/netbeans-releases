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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.io.IOException;
import java.util.Collection;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.view.ImportSchemaCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 * NewType for importing a schema file.
 *
 * @author  Nathan Fiedler
 */
public class ImportSchemaNewType extends NewType {
    /** Component in which to create import. */
    private WSDLComponent component;

    /**
     * Creates a new instance of ImportWSDLNewType.
     *
     * @param  component  the WSDL component in which to create the import.
     */
    public ImportSchemaNewType(WSDLComponent component) {
        this.component = component;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportSchemaNewType.class,
                "LBL_NewType_ImportSchema");
    }

    @Override
    public void create() throws IOException {
        // Create the new import with empty attributes.
        WSDLModel model = component.getModel();
        Definitions def = model.getDefinitions();
        model.startTransaction();
        Types types = def.getTypes();
        if (types == null) {
            types = model.getFactory().createTypes();
        }
        Schema schema = null;
        String tns = def.getTargetNamespace();
        if (tns != null) {
            Collection<Schema> schemas = types.getSchemas();
            if (schemas != null) {
                for (Schema s : schemas) {
                    if (s.getTargetNamespace() != null && s.getTargetNamespace().equals(tns)) {
                        schema = s;
                        break;
                    }
                }
            }
        }
        WSDLSchema wsdlSchema = null;
        if (schema == null) {
            wsdlSchema = model.getFactory().createWSDLSchema();
            SchemaModel schemaModel = wsdlSchema.getSchemaModel();
            schema = schemaModel.getSchema();
            schema.setTargetNamespace(model.getDefinitions().getTargetNamespace());
        }

        // The customizer will create the new import(s).
        // Note this happens during the transaction, which is unforunate
        // but supposedly unavoidable.
        ImportSchemaCustomizer customizer = new ImportSchemaCustomizer(schema, model);
        DialogDescriptor descriptor = UIUtilities.getCreatorDialog(
                customizer, NbBundle.getMessage(ImportSchemaNewType.class,
                "LBL_NewType_ImportCustomizer"), true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        
        // If okay, add the import to the model.
        if (result == DialogDescriptor.OK_OPTION) {
            // The customizer has, by this time, already made its changes
            // to the model, so there is nothing for us to do here.
            if (def.getTypes() == null) {
                def.setTypes(types);
            }
            if (wsdlSchema != null) {
                types.addExtensibilityElement(wsdlSchema);
            }
        }
        // In either case, end the transaction.
        model.endTransaction();
    }
}
