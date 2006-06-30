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
        return Util.THIS.getString ("NAME_CHOICE") + getMultiplicity ();
    }
    
    /**
     */
    public String getSeparator () {
        return "|"; // NOI18N
    }
    
}
