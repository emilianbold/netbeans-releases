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
