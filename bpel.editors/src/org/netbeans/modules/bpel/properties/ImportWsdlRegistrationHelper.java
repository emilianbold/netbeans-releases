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
package org.netbeans.modules.bpel.properties;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.properties.Constants.StandardImportType;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
//import org.netbeans.modules.xml.schema.model.impl.EmbeddedSchemaModelImpl;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ImportWsdlRegistrationHelper {
    
    private WSDLModel myWsdlModel;
    
    public ImportWsdlRegistrationHelper(WSDLModel curWsdlModel) {
        myWsdlModel = curWsdlModel;
    }
    
    public void addImport(Model importedModel) {
        if (importedModel == null) {
            return;
        }
        
        if (importedModel instanceof SchemaModel) {
//            if (((SchemaModel)importedModel).getDocument().get ) {
//            }
            
            FileObject fo = (FileObject) importedModel.getModelSource().getLookup().lookup(FileObject.class);
            if (fo != null && fo.hasExt("wsdl")) { // NOI18N
                addImportWsdl(fo);
                return;
            }
            
            addImport((SchemaModel)importedModel);
            return;
        }
        
        if (importedModel instanceof WSDLModel) {
            addImport((WSDLModel)importedModel);
            return;
        }
    }
    
    private void addImportWsdl(FileObject fo) {
        if (fo == null) {
            return;
        }
        // check is it not self fo
        if (isSelfFo(fo)) {
            return;
        }
  
        WSDLModel foWsdlModel = getWsdlModelByFo(fo);
        if (foWsdlModel != null) {
            addImport(foWsdlModel);
        }
    }
    
    private WSDLModel getWsdlModelByFo(FileObject fo) {
        assert fo != null;
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        if (modelSource != null) {
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().
                    getModel(modelSource);
            if (wsdlModel.getState() != Model.State.NOT_WELL_FORMED) {
                return wsdlModel;
            }
        }
        return null;
    }
    
    private boolean isSelfFo(FileObject fo) {
        assert fo != null;
        FileObject modelFo = Util.getFileObjectByModel(myWsdlModel);
        return modelFo.equals(fo);
    }
    
    public void addImport(WSDLModel importedWsdlModel) {
        if (importedWsdlModel == null) {
            return;
        }
        // check if prop alias is in the same wsdl as correlation prop #87102
        if (importedWsdlModel.equals(myWsdlModel)) {
            return;
        }
        
        Definitions defs = myWsdlModel.getDefinitions();
        Collection<Import> imports = defs.getImports();
        for (Import elem : imports) {
            try {
                if (importedWsdlModel.equals(elem.getImportedWSDLModel())) {
                    return;
                }
            } catch (CatalogModelException ex) {
                // do nothing
                // just cannot resolve namespace or location of the imported wsdlModel
            }
        }
        createImport(importedWsdlModel);
    }
    
    public void createImport(WSDLModel importedWsdlModel) {
        if (importedWsdlModel == null) {
            return;
        }
        
        FileObject fo = Util.getFileObjectByModel(importedWsdlModel);
        Import new_imp = myWsdlModel.getFactory().createImport();
        
        String location = Util.getNewModelLocation(myWsdlModel ,fo );
        if (location != null) {
            new_imp.setLocation(location);
        }
        
        String namespace = Util.getNewModelNamespace(fo,StandardImportType.IMPORT_WSDL);
        if (namespace != null) {
            new_imp.setNamespace(namespace);
        }
        
        myWsdlModel.addChildComponent(myWsdlModel.getRootComponent(), new_imp, 0);
    }
    
//    private void addImport(EmbeddedSchemaModelImpl embeddedSchemaModel) {
//        ModelSource modelSource = embeddedSchemaModel.getModelSource();
//        System.out.println("modelSource: "+modelSource);
//        FileObject fo = (FileObject)modelSource.getLookup().lookup(FileObject.class);
//        if (fo != null) {
//            System.out.println("fo: "+fo.getPath());
//        } else {
//            System.out.println("fo is null ");
//        }
//    }
    
    public void addImport(SchemaModel importedSchemaModel) {
//        if (importedSchemaModel instanceof EmbeddedSchemaModelImpl) {
//                addImport(((EmbeddedSchemaModelImpl)importedSchemaModel));
//                return;
//        }
        
        Definitions definition = myWsdlModel.getDefinitions();
        if (definition == null) {
            return;
        }
        
//        myWsdlModel.startTransaction();
//
//        try {
        // create the new import with empty attributes
        Types types = definition.getTypes();
        if (types == null) {
            types = myWsdlModel.getFactory().createTypes();
            definition.setTypes(types);
        }
        
        Schema schema = null;
        String targetNamespace = definition.getTargetNamespace();
        if (targetNamespace != null) {
            Collection<Schema> schemas = types.getSchemas();
            if (schemas != null) {
                for (Schema elem : schemas) {
                    if (elem.getTargetNamespace() != null
                            && elem.getTargetNamespace().equals(targetNamespace)
                            ) {
                        schema = elem;
                        break;
                    }
                }
            }
        }
        
        WSDLSchema wsdlSchema = null;
        if (schema == null) {
            wsdlSchema = myWsdlModel.getFactory().createWSDLSchema();
            SchemaModel schemaModel = wsdlSchema.getSchemaModel();
            schema = schemaModel.getSchema();
            schema.setTargetNamespace(targetNamespace);
        }
        
        //
        
        
        org.netbeans.modules.xml.schema.model.Import schemaImport =
                importedSchemaModel.getFactory().createImport();
        try {
            
            // check if it's already exist schema import
            if (! isExistSchemaRef(schema, importedSchemaModel)) {
//            schemaImport.setSchemaLocation("sagjdsahgjhgasdjhg");
//            schemaImport.setNamespace("testnamespace ");
                FileObject fo = Util.getFileObjectByModel(importedSchemaModel);
                if (fo != null) {
                    String location = Util.getNewModelLocation(myWsdlModel ,fo );
                    if (location != null) {
                        schemaImport.setSchemaLocation(location);
                    }
                    
                    String namespace = Util.getNewModelNamespace(fo,StandardImportType.IMPORT_SCHEMA);
                    if (namespace != null) {
                        schemaImport.setNamespace(namespace);
                    }
                    
                    schema.addExternalReference(schemaImport);
                    if (wsdlSchema != null) {
                        types.addExtensibilityElement(wsdlSchema);
                    }
                }
                
            }
        } catch (CatalogModelException ex) {
            // do nothing
            // just cannot resolve schemaModel
        }
        
//        } finally {
//            // In either case, end the transaction.
//            myWsdlModel.endTransaction();
//        }
    }
    
    private boolean isExistSchemaRef(Schema mainSchema, SchemaModel importedSchema)
    throws CatalogModelException {
        if (mainSchema == null || importedSchema == null) {
            return false;
        }
        Collection<SchemaModelReference> schemaModelRef = mainSchema.getSchemaReferences();
        for (SchemaModelReference elem : schemaModelRef) {
            if (importedSchema.equals(elem.resolveReferencedModel())) {
                return true;
            }
            
        }
        return false;
    }
    
    public void addImport2BpelModel(Model importedModel, BpelModel bpelModel) {
        if (bpelModel == null || importedModel == null) {
            return;
        }
        
        if (importedModel.getClass().isAssignableFrom(SchemaModel.class)) {
            addImport2BpelModel(SchemaModel.class.cast(importedModel), bpelModel);
            return;
        }
        
        if (importedModel.getClass().isAssignableFrom(WSDLModel.class)) {
            addImport2BpelModel(WSDLModel.class.cast(importedModel), bpelModel);
            return;
        }
    }
    
    public void addImport2BpelModel(SchemaModel importedModel, BpelModel bpelModel) {
        throw new UnsupportedOperationException();
    }
    
    public void addImport2BpelModel(WSDLModel importedModel, BpelModel bpelModel) {
        throw new UnsupportedOperationException();
    }
}
