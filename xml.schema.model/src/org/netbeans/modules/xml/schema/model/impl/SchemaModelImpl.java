/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.xdm.AbstractXDMModel;
import org.netbeans.modules.xml.xam.xdm.ChangedNodes;
import org.netbeans.modules.xml.xam.xdm.ComponentFinder;
import org.netbeans.modules.xml.xam.xdm.ComponentUpdater;
import org.netbeans.modules.xml.xam.xdm.SyncUnit;

/**
 *
 * @author Vidhya Narayanan
 */
public class SchemaModelImpl extends AbstractXDMModel<SchemaComponent> implements SchemaModel {
    
    private SchemaImpl schema;
    private SchemaComponentFactory csef;
    
    public SchemaModelImpl(Document doc) {
        super(doc);
        csef = new SchemaComponentFactoryImpl(this);
    }

    public SchemaModelImpl(ModelSource modelSource) {
        super(modelSource);
        csef = new SchemaComponentFactoryImpl(this);
    }

    /**
     *
     *
     * @return the schema represented by this model. The returned schema
     * instance will be valid and well formed, thus attempting to update
     * from a document which is not well formed will not result in any changes
     * to the schema model.
     */

    public SchemaImpl getSchema() {
        return (SchemaImpl)getRootComponent();
    }
    
    /**
     *
     *
     * @return common schema element factory valid for this instance
     */
    public SchemaComponentFactory getFactory() {
        return csef;
    }
    
    public SchemaComponent createRootComponent(org.w3c.dom.Element root) {
        schema = (SchemaImpl)csef.create(root, null);
        return schema;
    }


    public SchemaComponent getRootComponent() {
        return schema;
    }

    public Collection<Schema> findSchemas(String namespace) {
        Set<Schema> resultSchemas = new HashSet<Schema>();
        return _findSchemas(namespace, resultSchemas);
    }
    
    Collection<Schema> _findSchemas(String namespace, Set<Schema> result) {
        SchemaImpl schema = getSchema();

        // schema could be null, if last sync throwed exception
        if (schema == null) {
            return result;
        }

        // handle schema without target namespace and null lookup namespace
        String targetNamespace = schema.getTargetNamespace();
        
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)){
            SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            result.add(primitiveModel.getSchema());
        } else if ( targetNamespace == null && namespace == null ||
                    targetNamespace != null && namespace == null ||
                    namespace.equals(schema.getTargetNamespace())) {
            result.add(schema);
            checkIncludeSchemas(namespace, result);
            checkRedefineSchemas(namespace, result);
        } else if ( targetNamespace == null && namespace != null) {
            result.add(schema);
            checkIncludeSchemas(namespace, result);
            checkRedefineSchemas(namespace, result);
            checkImportedSchemas(namespace, result);
        } else { // namespace != null && ! targetNamespace.equals(namespace)
            checkImportedSchemas(namespace, result);
        }

        return result;
    }

    private void checkIncludeSchemas(String namespace, Set<Schema> result) {
            Collection<SchemaModel> includedModels = getSchema().getIncludedSchemas();
            for (SchemaModel model : includedModels) {
                if (! result.contains(model.getSchema())) {
                    result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result));
                }
            }
    }
    
    private void checkRedefineSchemas(String namespace, Set<Schema> result) {
            Collection<SchemaModel> redefinedModels = getSchema().getRedefinedSchemas();
            for (SchemaModel model : redefinedModels) {
                if (! result.contains(model.getSchema())) {
                    result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result));
                }
            }
    }
    
    private void checkImportedSchemas(String namespace, Set<Schema> result) {
            Collection<SchemaModel> importedModels = getSchema().getImportedSchemas();
            for (SchemaModel model : importedModels) {
                String targetNS = model.getSchema().getTargetNamespace();
                if (namespace == null && targetNS == null ||
                    namespace != null && namespace.equals(targetNS)) {
                    result.add(model.getSchema());
                }
            }
    }
    
    public String getEffectiveNamespace(SchemaComponent component) {
        SchemaModel componentModel = component.getSchemaModel();
        SchemaImpl schema = getSchema();
        if(this == componentModel) {
            return schema.getTargetNamespace();
        } else if (componentModel == SchemaModelFactory.getDefault().getPrimitiveTypesModel()) {
            return XMLConstants.W3C_XML_SCHEMA_NS_URI;
        } else {
            Collection<SchemaModel> importedSchemaModels = 
                    schema.getImportedSchemas();
            if(importedSchemaModels != null && importedSchemaModels.contains(componentModel)) {
                return componentModel.getSchema().getTargetNamespace();
            }
            Collection<SchemaModel> includedSchemaModels = 
                    schema.getIncludedSchemas();
            if(includedSchemaModels != null && includedSchemaModels.contains(componentModel)) {
                return schema.getTargetNamespace();
            }
            Collection<SchemaModel> redefinedSchemaModels = 
                    schema.getRedefinedSchemas();
            if(redefinedSchemaModels != null && redefinedSchemaModels.contains(componentModel)) {
                return schema.getTargetNamespace();
            }
        }
        return null;
    }

    public SchemaComponent createComponent(SchemaComponent parent, org.w3c.dom.Element element) {
       return csef.create(element, parent);
    }

    protected ComponentUpdater<SchemaComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    protected ComponentFinder<SchemaComponent> getComponentFinder() {
        return new FindSchemaComponentFromDOM();
    }
    
    public Set<QName> getQNames() {
        return SchemaElements.allQNames();
    }
    
    protected SyncUnit fillSyncOrder(ChangedNodes changes, SyncUnit unit) {
        unit = super.fillSyncOrder(changes, unit);
        return new SyncUnitReviewVisitor().review(unit);
    }
}
