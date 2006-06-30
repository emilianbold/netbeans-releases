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

/**
 * This interface represents a reference to a group.
 * @author Chris Webster
 */
public interface GroupReference extends ComplexExtensionDefinition,
        SequenceDefinition, ComplexTypeDefinition, SchemaComponent  {
    public static final String REF_PROPERTY         = "ref";
    public static final String MAX_OCCURS_PROPERTY  = "maxOccurs";
    public static final String MIN_OCCURS_PROPERTY   = "minOccurs";
    
    String getMaxOccurs();
    void setMaxOccurs(String max);
    String getMaxOccursDefault();
    String getMaxOccursEffective();
    
    Integer getMinOccurs();
    void setMinOccurs(Integer min);
    int getMinOccursDefault();
    int getMinOccursEffective();
    
    GlobalReference<GlobalGroup> getRef();
    void setRef(GlobalReference<GlobalGroup> def);
}
