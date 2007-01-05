/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.SyncUnit;

/**
 *
 * @author Vidhya Narayanan
 */
public class SchemaModelImpl extends AbstractDocumentModel<SchemaComponent> implements SchemaModel {
    
    private SchemaImpl schema;
    private SchemaComponentFactory csef;
    
    public SchemaModelImpl(ModelSource modelSource) {
        super(modelSource);	
        csef = new SchemaComponentFactoryImpl(this);
        //getAccess().setAutoSync(true);
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
        SchemaImpl newSchema = (SchemaImpl)csef.create(root, null);
        if (newSchema != null) {
            schema = newSchema;
        } else {
            return null;
        }
        return getSchema();
    }


    public SchemaComponent getRootComponent() {
        return schema;
    }

    public <T extends NamedReferenceable>
            T resolve(String namespace, String localName, Class<T> type) 
    {
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)){
            SchemaModel sm = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            return sm.findByNameAndType(localName, type);
        }
        
        return resolve(namespace, localName, type, null, new ArrayList<SchemaModel>());
    }
    
    <T extends NamedReferenceable>
            T resolve(String namespace, String localName, Class<T> type, SchemaModelReference refToMe, Collection<SchemaModel> checked) 
    {
        if (getState() != State.VALID) {
            return null;
        }
        
        T found = null;
        String targetNamespace = getSchema().getTargetNamespace();
        if (targetNamespace != null && targetNamespace.equals(namespace) ||
            targetNamespace == null && namespace == null) {
            found = findByNameAndType(localName, type);
        }
        
        if (found == null && ! (refToMe instanceof Import)) {
            checked.add(this);
            
            Collection<SchemaModelReference> modelRefs = getSchemaModelReferences();
            for (SchemaModelReference r : modelRefs) {
                // import should not have null namespace
                if (r instanceof Import) {
                    if (namespace == null || ! namespace.equals(((Import)r).getNamespace())) {
                        continue;
                    }
                }
                
                SchemaModelImpl sm = resolve(r);
                if (sm != null && ! checked.contains(sm)) {
                    found = sm.resolve(namespace, localName, type, r, checked);
                }
                if (found != null) {
                    break;
                }
            }
        }
        
        return found;
    }
    
    public SchemaModelImpl resolve(SchemaModelReference ref) {
        try {
            return (SchemaModelImpl) ref.resolveReferencedModel();
        } catch (CatalogModelException ex) {
            return null;
        }
    }

    public Collection<SchemaModelReference> getSchemaModelReferences() {
        Collection<SchemaModelReference> refs = new ArrayList<SchemaModelReference>();
        refs.addAll(getSchema().getRedefines());
        refs.addAll(getSchema().getIncludes());
        refs.addAll(getSchema().getImports());
        return refs;
    }
            
    public <T extends NamedReferenceable> T findByNameAndType(String localName, Class<T> type) {
        return new FindGlobalReferenceVisitor<T>().find(type, localName, getSchema());
    }
    
    public Set<Schema> findSchemas(String namespace) {
        Set<Schema> result = new HashSet<Schema>();
        
        // build-in XSD schema is always visible
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)){
            SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            result.add(primitiveModel.getSchema());
            return result;
        } 
        
        return _findSchemas(namespace, result, null);
    }
    
    protected enum ReferenceType { IMPORT, INCLUDE, REDEFINE }
    
    Set<Schema> _findSchemas(String namespace, Set<Schema> result, ReferenceType refType) {
        SchemaImpl schema = getSchema();
        // schema could be null, if last sync throwed exception
        if (schema == null) {
            return result;
        }
        
        String targetNamespace = schema.getTargetNamespace();
        if (targetNamespace != null && targetNamespace.equals(namespace) ||
            targetNamespace == null && namespace == null) {
            result.add(schema);
        }
        
        if (refType != ReferenceType.IMPORT) {
            checkIncludeSchemas(namespace, result);
            checkRedefineSchemas(namespace, result);
            checkImportedSchemas(namespace, result);
        }
        
        return result;    
    }
    
    private void checkIncludeSchemas(String namespace, Set<Schema> result) {
        Collection<Include> includes = getSchema().getIncludes();
        for (Include include : includes) {
            try {
                SchemaModel model = include.resolveReferencedModel();
                if (model.getState() == Model.State.NOT_WELL_FORMED) {
                    continue;            
                }
                
                if (! result.contains(model.getSchema())) {
                    result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.INCLUDE));
                }
            } catch (CatalogModelException ex) {
                // ignore this exception to proceed with search
            }
        }
    }
    
    private void checkRedefineSchemas(String namespace, Set<Schema> result) {
        Collection<Redefine> redefines = getSchema().getRedefines();
        for (Redefine redefine : redefines) {
	       try {
		   SchemaModel model = redefine.resolveReferencedModel();
           if (model.getState() == Model.State.NOT_WELL_FORMED)
                continue;
            
		   if (! result.contains(model.getSchema())) {
		       result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.REDEFINE));
		   }
	       } catch (CatalogModelException ex) {
		   // ignore this exception to proceed with search
	       }
	   }
    }
    
    private void checkImportedSchemas(String namespace, Set<Schema> result) {
        Collection<Import> imports = getSchema().getImports();
        for (Import imp : imports) {
		try {
		    SchemaModel model = imp.resolveReferencedModel();
            if (model.getState() == Model.State.NOT_WELL_FORMED)
                continue;
            
		   if (! result.contains(model.getSchema())) {
		       result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.IMPORT));
		   }
		} catch (CatalogModelException ex) {
		    // ignore this exception to proceed with search
		}
	    }
    }
    
    /**
	 * This api returns the effective namespace for a given component. 
	 * If given component has a targetNamespace different than the 
	 * this schema, that namespace is returned. The special case is that if
	 * the targetNamespace of the component is null, there is no target
	 * namespace defined, then the import statements for this file are 
	 * examined to determine if this component is directly or indirectly 
	 * imported. If the component is imported, then null if returned 
	 * otherwise the component is assumed to be included or redefined and
	 * the namespace of this schema is returned. 
     */
    public String getEffectiveNamespace(SchemaComponent component) {
	SchemaModel componentModel = component.getModel();
	Schema schema = getSchema();
        Schema componentSchema = componentModel.getSchema();
        String tns = schema.getTargetNamespace();
        String componentTNS = componentSchema.getTargetNamespace();
	if (this == componentModel) {
	    return tns;
        } else if (componentTNS == null && tns != null) {
            // only include/redefine model can assum host model targetNamespace
            // so check if is from imported to just return null
	    Collection<Import> imports = schema.getImports();
	    for (Import imp: imports) {
		SchemaModel m = null;
		try {
		    m = imp.resolveReferencedModel();
		} catch (CatalogModelException ex) {
		    // the import cannot be resolved 
		}
		if(componentModel.equals(m)) {
		    return null;
		}
                if (m == null || m.getState() == Model.State.NOT_WELL_FORMED) {
                    continue;
                }
                String importedTNS = m.getSchema().getTargetNamespace();
                if (importedTNS == null) continue;
                Set<Schema> visibleSchemas = findSchemas(importedTNS);
                for (Schema visible : visibleSchemas) {
                    if (componentModel.equals(visible.getModel()))  {
                        return null;
                    }
                }
	    }
            return tns;
    	} else {
            return componentTNS;
        }
    }

    public SchemaComponent createComponent(SchemaComponent parent, org.w3c.dom.Element element) {
       return csef.create(element, parent);
    }

    protected ComponentUpdater<SchemaComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    public Set<QName> getQNames() {
        return SchemaElements.allQNames();
    }
    
    public SyncUnit prepareSyncUnit(ChangeInfo changes, SyncUnit unit) {
        unit = super.prepareSyncUnit(changes, unit);
        if (unit != null) {
            return new SyncUnitReviewVisitor().review(unit);
        }
        return null;
    }
    
    public DocumentModelAccess getAccess() {
        if (access == null) {
            super.getAccess().setAutoSync(true);  // default autosync true
        }
        return super.getAccess();
    }
    
    public Map<QName,List<QName>> getQNameValuedAttributes() {
        return SchemaAttributes.getQNameValuedAttributes();
    }
}
