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

import java.util.*;

import org.netbeans.tax.*;

public class ChoiceType extends ChildrenType {
    
    //
    // init
    //
    
    public ChoiceType (Collection types) {
        super (types);
    }
    
    public ChoiceType () {
        super ();
    }
    
    public ChoiceType (ChoiceType choiceType) {
        super (choiceType);
    }
    
    
    /**
     */
    public Object clone () {
        return new ChoiceType (this);
    }
    
    //
    // itself
    //
    
    /**
     */
    public String toString () {
        Iterator it = getTypes ().iterator ();
        StringBuffer sb = new StringBuffer ();
        while (it.hasNext ()) {
            TreeElementDecl.ContentType next = (TreeElementDecl.ContentType) it.next ();
            sb.append (" | ").append (next.toString ()); // NOI18N
        }
        if (sb.length () > 0) sb.delete (0,3);
        
        return "( " + sb.toString () + " )" + getMultiplicity (); // NOI18N
    }
    
    /**
     */
    public String getName () {
        return Util.getString ("NAME_CHOICE") + getMultiplicity ();
    }
    
    /**
     */
    public String getSeparator () {
        return "|"; // NOI18N
    }
    
}
