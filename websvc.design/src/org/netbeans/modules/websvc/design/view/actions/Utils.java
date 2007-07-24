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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author mkuchtiak
 */
public class Utils {
    
    private static final String[] primitiveTypes = new String[] {
        "string", //NOI18N
        "int", //NOI18N
        "decimal", //NOI18N
        "short", //NOI18N
        "unsignedShort", // NOI18N
        "long", //NOI18N
        "boolean", //NOI18N
        "float", //NOI18N
        "double", //NOI18N
        "duration", //NOI18N
        "base64Binary", //NOI18N
        "hexBinary", //NOI18N
        "date", //NOI18N
        "time", //NOI18N
        "dateTime", //NOI18N
        "anyURI", //NOI18N
        "QName" //NOI18N
    };
    
    public static String getDisplayName(ReferenceableSchemaComponent paramType) {
        if (paramType==null) return "null";
        else if (paramType instanceof GlobalSimpleType) return "xsd:"+paramType.getName(); //NOI18N
        else if (paramType instanceof GlobalElement) {
            return "schema element: {"+paramType.getModel().getEffectiveNamespace(paramType)+"}"+paramType.getName(); //NOI18N
        }
        else return "{"+paramType.getModel().getEffectiveNamespace(paramType)+"}"+paramType.getName(); //NOI18N
    }
    
    public static List<GlobalSimpleType> getPrimitiveTypes() {
        List<GlobalSimpleType> primitives = new ArrayList<GlobalSimpleType>();
        for (String primitiveType:primitiveTypes) {
            GlobalSimpleType type = getPrimitiveType(primitiveType);
            if (type!=null) primitives.add(type);
        }
        return primitives;
    }
    
    public static List<Schema> getImportedSchemas(WSDLModel wsdlModel){
        List<Schema> importedSchemas = new ArrayList<Schema>();
        Definitions definitions = wsdlModel.getDefinitions();
        Types types = definitions.getTypes();
        Collection<Schema> schemas = types.getSchemas();
        for(Schema schema : schemas){
            Collection<Import> imports = schema.getImports();
            for(Import imported : imports){
                importedSchemas.add(imported.getModel().getSchema());
            }
        }
        return importedSchemas;
    }
    
    public static List<ReferenceableSchemaComponent> getSchemaTypes(WSDLModel wsdlModel) throws CatalogModelException {
        Definitions definitions = wsdlModel.getDefinitions();
        Types types = definitions.getTypes();
        Collection<Schema> schemas = types.getSchemas();
        
        List<ReferenceableSchemaComponent> schemaTypes = new ArrayList<ReferenceableSchemaComponent>();
        schemaTypes.addAll(Utils.getPrimitiveTypes());
        
        for(Schema schema : schemas) {
            // populate with internal schema
            String schemaNamespace = schema.getTargetNamespace();
            if (schemaNamespace!=null) {
                populateWithElements(wsdlModel, schema.getModel(), schemaTypes);
            }
            // populate with imported schemas
            Collection<Import> importedSchemas = schema.getImports();
            for(Import importedSchema : importedSchemas){
                SchemaModel schemaModel = importedSchema.resolveReferencedModel();
                populateWithElements(wsdlModel, schemaModel, schemaTypes);
            }
        }
        
        return schemaTypes;
    }
    
    private static void populateWithElements(WSDLModel wsdlModel, SchemaModel schemaModel, List<ReferenceableSchemaComponent> schemaTypes) {
        
        Collection<GlobalElement> elements = schemaModel.getSchema().getElements();
        for(GlobalElement element : elements) {
            if (!isUsedInOperation(wsdlModel, element)) {
                schemaTypes.add(element);
            }
        }
        
        Collection<? extends GlobalType> complexTypes = schemaModel.getSchema().getComplexTypes();
        for(GlobalType type : complexTypes){
            schemaTypes.add(type);
        }
        
        Collection<? extends GlobalType> simpleTypes = schemaModel.getSchema().getSimpleTypes();
        for(GlobalType type : simpleTypes){
            schemaTypes.add(type);
        }
        
     }
    
    public static boolean isUsedInOperation(WSDLModel wsdlModel, GlobalElement element) {
        Collection<Message> messages = wsdlModel.getDefinitions().getMessages();
        for (Message message:messages) {
            Collection<Part> parts = message.getParts();
            for (Part part:parts) {
                NamedComponentReference<GlobalElement> partElement = part.getElement();
                if (partElement != null && element.equals(partElement.get())) {
                    return true;
                }
            } 
        }
        return false;
    }
    
    public static GlobalSimpleType getPrimitiveType(String typeName){
        SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
        for(GlobalSimpleType ptype: primitives) {
            if(ptype.getName().equals(typeName)){
                return ptype;
            }
        }
        return null;
    }
}
