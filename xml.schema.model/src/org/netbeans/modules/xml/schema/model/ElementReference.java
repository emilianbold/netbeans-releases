/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.Set;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Named;

/**
 * This interface represents a local element using the ref attribute
 * @author Chris Webster
 */
public interface ElementReference extends CommonElement, SequenceDefinition,
	SchemaComponent {
    public static final String MIN_OCCURS_PROPERTY = "minOccurs";
    public static final String MAX_OCCURS_PROPERTY = "maxOccurs";
    public static final String FORM_PROPERTY = "form"; //NOI18N
    
    Form getForm();
    void setForm(Form form);
    Form getFormDefault();
    Form getFormEffective();
    
    String getMaxOccurs();
    void setMaxOccurs(String max);
    String getMaxOccursDefault();
    String getMaxOccursEffective();
    
    Integer getMinOccurs();
    void setMinOccurs(Integer min);
    int getMinOccursDefault();
    int getMinOccursEffective();
    
    GlobalReference<GlobalElement> getRef();
    void setRef(GlobalReference<GlobalElement> ref);
}
