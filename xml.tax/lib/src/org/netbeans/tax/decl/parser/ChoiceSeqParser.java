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

public class ChoiceSeqParser implements ModelParser {
    
    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {
        TreeElementDecl.ContentType first = new ContentParticleParser ().parseModel (s);
        
        //determine type of parser
        
        if (s.trim ().startsWith ("|")) { // NOI18N
            return new ChoiceParser (first).parseModel (s);
        } else if (s.startsWith (",")) { // NOI18N
            return new SequenceParser (first).parseModel (s);
        } else {
            //(element) is treated by this
            TreeElementDecl.ContentType tmp = new SequenceParser (first).parseModel (s);
            //                first.setMultiplicity(tmp.getMultiplicity());
            return first;
        }
    }
    
}
