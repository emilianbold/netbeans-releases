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

public class ChoiceParser extends ListParser {
    
    /** */
    TreeElementDecl.ContentType first;
    
    
    //
    // init
    //
    
    public ChoiceParser (TreeElementDecl.ContentType first) {
        this.first = first;
    }
    
    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {
        ChoiceType cht = (ChoiceType) super.parseModel (s);
        if (first != null)
            cht.addType (first);
        return cht;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected ChildrenType createType (ParserReader model) {
        return new ChoiceType ();
    }
    
    /**
     */
    public String getSeparator () {
        return "|"; // NOI18N
    }
    
}
