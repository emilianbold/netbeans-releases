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
package org.netbeans.tax.decl.parser;

import org.netbeans.tax.TreeElementDecl;
import org.netbeans.tax.decl.*;

public class MixedParser extends ChoiceParser {

    //
    // init
    //

    public MixedParser() {                
        super(null);
    }


    //
    // itself
    //

    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {

        if (s.trim().startsWith("|")) { // NOI18N
            // choice-of                        
            MixedType cht = (MixedType) super.parseModel(s);
            return cht;

        } else {
            // leaf #PCDATA
            return new MixedType();
        }
    }

    /**
     */
    protected ChildrenType createType (ParserReader s) {
        return new MixedType ();
    }

}   
