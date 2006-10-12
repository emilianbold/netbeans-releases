
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

package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 * The SchemaModelReference interface is implemented by classes which reference
 * other schema models (Import, Include, and Redefine). This interface provides
 * a uniform way of obtaining the referenced model.
 * @author Chris Webster
 */
public interface SchemaModelReference extends SchemaComponent {
	public static final String SCHEMA_LOCATION_PROPERTY = "schemaLocation";

        // TODO maybe use reference pattern Reference<SchemaModel> getModelReference()
        // issue is Reference.get() cannot throw CatalogModelException, but that fall
        // into the pattern Reference.isBroken(). 
        // Maybe the pattern need Reference.getProblemDescription.
        
        /**
	 * obtain the model for the referenced schema. 
	 * 
	 * @throws CatalogModelException if the referenced model cannot
	 * be created.
	 */
	SchemaModel resolveReferencedModel() throws CatalogModelException;

	
	String getSchemaLocation();
	void setSchemaLocation(String uri);
}
