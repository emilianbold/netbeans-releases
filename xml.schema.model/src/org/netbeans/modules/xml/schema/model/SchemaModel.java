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


package org.netbeans.modules.xml.schema.model;
import java.util.Collection;
import org.netbeans.modules.xml.xam.DocumentModel;
import org.w3c.dom.Document;

/**
 * This interface represents an instance of a schema model. A schema model is 
 * bound to a single file. 
 * @author Chris Webster
 */
public interface SchemaModel extends DocumentModel<SchemaComponent> {
	
	/**
	 * @return the schema represented by this model. The returned schema 
	 * instance will be valid and well formed, thus attempting to update 
	 * from a document which is not well formed will not result in any changes
	 * to the schema model. 
	 */
	Schema getSchema();
        
	/**
	 * This api returns the effective namespace for a given component.
	 * If component is owned by this schemamodel, the target namespace
	 * of this model is returned.
	 * If component is owned by imported schema model, the target namespace
	 * of imported model is returned.
	 * If component is owned by included schemamodel, the target namespace
	 * of this model is returned.
	 * If component is owned by redefined schemamodel, the target namespace
	 * of this model is returned.
	 * @param component The component which namespace to find
	 * @return The effective target namespace
	 */
	String getEffectiveNamespace(SchemaComponent component);
	
	/**
	 * @return common schema element factory valid for this instance
	 */
	SchemaComponentFactory getFactory();
	
        /**
         * Returns all visible schemas matching the given namespace.  
         * Note visibility rule is defined as following:
         * (1) Include or redefine are transitive, i.e., includer can see all schemas 
         *     included or redefined by the included
         * (2) Import is not transitive, i.e., importing schema can only see the 
         *     imported schema, but not those included, redefined or imported by the imported.
         * (3) Imported schemas are not visible to includer or redefiner.
         */
        Collection<Schema> findSchemas(String namespaceURI);
}
