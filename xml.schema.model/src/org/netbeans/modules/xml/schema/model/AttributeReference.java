/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents a reference to a global attribute definition or 
 * local definition (cannot be referenced ).
 * @author Chris Webster
 */
public interface AttributeReference extends Attribute, SchemaComponent  {
        public static final String REF_PROPERTY         = "ref";
        public static final String FORM_PROPERTY        = "form";
        public static final String USE_PROPERTY         = "use";
        
	Form getForm();
	void setForm(Form form);
	Form getFormDefault();
	Form getFormEffective();
	
	GlobalReference<GlobalAttribute> getRef();
	void setRef(GlobalReference<GlobalAttribute> attribute);
	
	Use getUse();
	void setUse(Use use);
        Use getUseDefault();
        Use getUseEffective();
	
}
