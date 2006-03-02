/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TypeReferenceContainer.java
 *
 * Created on January 19, 2006, 6:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * A type container defines a schema element which can either
 * reference or include a local type definition. 
 * @author Chris Webster
 */
public interface TypeContainer {
    public static final String TYPE_PROPERTY = "type";
    public static final String INLINE_TYPE_PROPERTY = "inlineType";
    
    GlobalReference<? extends GlobalType> getType();
    void setType(GlobalReference<? extends GlobalType>t);
    
    LocalType getInlineType();
    void setInlineType(LocalType t);
}
