/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.decl;

import org.netbeans.tax.*;

public class EMPTYType extends LeafType {

    //
    // init
    //

    public EMPTYType () {
        super ();
    }

    public EMPTYType (EMPTYType emptyType) {
        super (emptyType);
    }

    
    //
    // from TreeObject
    //

    /**
     */
    public Object clone () {
	return new EMPTYType (this);
    }

    //
    // itself
    //

    /**
     */
    public String getName () {
        return Util.getString ("NAME_EMPTY");
    }            

    /**
     */
    public String toString () {
        return "EMPTY"; // NOI18N
    }

}
