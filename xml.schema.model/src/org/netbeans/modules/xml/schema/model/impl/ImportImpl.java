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

import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.EmbeddableRoot.ForeignParent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class ImportImpl extends SchemaComponentImpl implements Import {
	
        public ImportImpl(SchemaModelImpl model) {
            this(model,createNewComponent(SchemaElements.IMPORT,model));
        }
    
	/**
	 * Creates a new instance of ImportImpl
	 */
	public ImportImpl(SchemaModelImpl model, Element el) {
		super(model, el);
	}

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Import.class;
	}
	
	/**
	 *
	 */
	public void accept(SchemaVisitor visitor) {
		visitor.visit(this);
	}
	
	/**
	 *
	 */
	public void setSchemaLocation(String uri) {
		setAttribute(SCHEMA_LOCATION_PROPERTY, SchemaAttributes.SCHEMA_LOCATION, uri);
	}
	
	/**
	 *
	 */
	public void setNamespace(String uri) {
		setAttribute(NAMESPACE_PROPERTY, SchemaAttributes.NAMESPACE, uri);
	}
	
	/**
	 *
	 */
	public String getSchemaLocation() {
		 return getAttribute(SchemaAttributes.SCHEMA_LOCATION);
	}
	
	/**
	 *
	 */
	public String getNamespace() {
		   return getAttribute(SchemaAttributes.NAMESPACE);
	}

	public SchemaModel resolveReferencedModel() throws CatalogModelException {
            SchemaModel result = resolveEmbeddedReferencedModel();
            if (result == null) {
                ModelSource ms = resolveModel(getSchemaLocation());
		result = SchemaModelFactory.getDefault().getModel(ms);
            }
            return result;
	}
	
    protected SchemaModel resolveEmbeddedReferencedModel() {
        if (getNamespace() == null) {
            return null;
        }
        if (! (getModel().getSchema().getForeignParent() instanceof ForeignParent)) {
            return null;
        }
        ForeignParent fr = (ForeignParent) getModel().getSchema().getForeignParent();
        if (fr == null) return null;
        for (EmbeddableRoot embedded : fr.getAdoptedChildren()) {
            if (embedded instanceof Schema && embedded != getModel().getSchema()) {
                Schema es = (Schema) embedded;
                if (getNamespace().equals(es.getTargetNamespace())) {
                    return es.getModel();
                }
            }
        }
        return null;
    }
}
