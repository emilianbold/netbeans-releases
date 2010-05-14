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

package org.netbeans.modules.xslt.model;

import java.util.List;


/**
 * "use-attribute-sets" attribute holder.
 *  Reference list could have more elements than 
 *  quantity of items in attribute value. 
 *  This is consequence of name resoving.  
 *  XSL can have many AttributeSet declarations with
 *  the same names. So each AttributeSet with the same names
 *  will appear in the list of references if they have appropriate
 *  name.
 * 
 * @author ads
 *
 */
public interface UseAttributesSetsSpec {

    String USE_ATTRIBUTE_SETS = "use-attribute-sets";           // NOI18N
    
    /**
     * @return unmodifiable collection references to attribute-set elements.
     */
    List<XslReference<AttributeSet>> getUseAttributeSets();
    
    /**
     * Sets new <code>collection</code> value for attribute use-attribute-sets.
     * @param collection new collection of references to attribute-set elements.
     */
    void setUseAttributeSets( List<XslReference<AttributeSet>> collection );
}
