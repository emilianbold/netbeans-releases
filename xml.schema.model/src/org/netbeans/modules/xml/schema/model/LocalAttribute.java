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

import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Named;

/**
 * This interface represents a reference to a global attribute definition or 
 * local definition (cannot be referenced ).
 * @author Chris Webster
 */
public interface LocalAttribute extends Attribute, SchemaComponent, Named<SchemaComponent>  {
        public static final String REF_PROPERTY         = "ref";
        public static final String FORM_PROPERTY        = "form";
        public static final String USE_PROPERTY         = "use";
        
	Form getForm();
	void setForm(Form form);
	Form getFormDefault();
	Form getFormEffective();
	
	GlobalReference<GlobalSimpleType> getType();
	void setType(GlobalReference<GlobalSimpleType> type);
	
	LocalSimpleType getInlineType();
	void setInlineType(LocalSimpleType type);
	
	Use getUse();
	void setUse(Use use);
        Use getUseDefault();
        Use getUseEffective();
	
}
