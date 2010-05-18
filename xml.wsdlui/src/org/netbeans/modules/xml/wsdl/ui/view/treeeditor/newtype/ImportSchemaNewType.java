/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.awt.Dialog;
import java.io.IOException;
import java.util.Collection;

import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
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

        Collection<Import> oldImports = schema.getImports();
        
        // The customizer will create the new import(s).
        // Note this happens during the transaction, which is unforunate
        // but supposedly unavoidable.
        ImportSchemaCustomizer customizer = new ImportSchemaCustomizer(schema, model);
        DialogDescriptor descriptor = UIUtilities.getCreatorDialog(
                customizer, NbBundle.getMessage(ImportSchemaNewType.class,
                "LBL_NewType_ImportCustomizer"), true);
        descriptor.setValid(false);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(descriptor.getTitle());
        dialog.setVisible(true);
        
        
        // If okay, add the import to the model.
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
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
        
        Collection<Import> newImports = schema.getImports();
        Import lastAdded = null;
        for (Import imp : newImports) {
            if (!oldImports.contains(imp)) {
                lastAdded = imp;
            }
        }
        
        ActionHelper.selectNode(lastAdded, model);
        
    }
}
