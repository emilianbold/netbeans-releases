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
