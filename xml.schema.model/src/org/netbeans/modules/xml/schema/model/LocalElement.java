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
 * This interface represents a local element.
 * @author Chris Webster
 */
public interface LocalElement extends CommonElement, SequenceDefinition,
	SchemaComponent, Named<SchemaComponent>, TypeContainer {
    public static final String MIN_OCCURS_PROPERTY = "minOccurs";
    public static final String MAX_OCCURS_PROPERTY = "maxOccurs";
    public static final String FORM_PROPERTY = "form"; //NOI18N
    
    Set<Block> getBlock();
    void setBlock(Set<Block> block);
    Set<Block> getBlockDefault();
    Set<Block> getBlockEffective();
    
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
    
}
