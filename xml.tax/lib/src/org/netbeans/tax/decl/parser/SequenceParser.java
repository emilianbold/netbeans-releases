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

import org.netbeans.tax.*;
import org.netbeans.tax.decl.*;

public class SequenceParser extends ListParser {

    /** */
    TreeElementDecl.ContentType first;

    /** */
    SequenceType seqType;

    //
    // init
    //

    /** */
    public SequenceParser (TreeElementDecl.ContentType first) {
        this.first = first;
    }

    
    //
    // itself
    //

    /**
     */ 
    public TreeElementDecl.ContentType parseModel (ParserReader s) {

        if (first != null) //prepend first el of seq
            getSequenceType ().addType(first);

        return super.parseModel(s);
    }

    /**
     */ 
    protected ChildrenType createType (ParserReader s) {            
        return getSequenceType ();
    }

    /**
     */ 
    private synchronized SequenceType getSequenceType () {
        if (seqType == null)
            seqType = new SequenceType ();

        return seqType;
    }            

    /**
     */ 
    public String getSeparator () {
        return ","; // NOI18N
    }

}    
