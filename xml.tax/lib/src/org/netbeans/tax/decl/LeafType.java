/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.decl;

import org.netbeans.tax.*;

/** Name, ANY, EMPTY all these are of leaf type.  */    //??? (#PCDATA)
public abstract class LeafType extends TreeElementDecl.ContentType {
    
    //
    // init
    //
    
    public LeafType () {
        super ();
    }
    
    public LeafType (LeafType leafType) {
        super (leafType);
    }
    
    
    //
    // itself
    //
    
    /** @return name of declared element or null  */
    public abstract String getName ();
    
    /**
     */
    public boolean allowElements () {
        return false;
    }
    
    /**
     */
    public boolean allowText () {
        return false;
    }
    
}
