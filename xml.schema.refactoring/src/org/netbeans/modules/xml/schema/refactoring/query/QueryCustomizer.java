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

/*
 * QueryCustomizer.java
 *
 * Created on January 15, 2006, 5:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query;

import javax.swing.JPanel;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;

/**
 *
 * @author Jeri Lockhart
 */
public interface QueryCustomizer {

	/**
	 * Setter for property schemaComponentReferences.
	 * @param schemaComponentReferences New value of property schemaComponentReferences.
	 */
	public void setSchemaComponentReferences(SchemaComponentReference[] schemaComponentReferences);
        
	/**
	 * Getter for property schemaComponentReferences.
	 * @return SchemaComponentReference[]
	 */
	public SchemaComponentReference[] getSchemaComponentReferences();

	/**
	 * Getter for property queryConstraints.
	 * @return Value of property queryConstraints.
	 */
	public CustomizerResults[] getQueryConstraints();

	/**
	 * Setter for property queryConstraints.
	 * @param queryConstraints New value of property queryConstraints.
	 */
	public void setQueryConstraints(CustomizerResults[] queryConstraints);
        
        
	/**
	 * Getter for property query customizer panel
	 */
        public JPanel getQueryCustomizerPanel() ;
    
}
