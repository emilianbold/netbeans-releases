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
package org.netbeans.tax.decl.parser;

import org.netbeans.tax.TreeElementDecl;
import org.netbeans.tax.decl.*;

/** Root parser */
public class ContentSpecParser extends MultiplicityParser implements ModelParser {
    
    /**
     */
    public TreeElementDecl.ContentType parseModel (ParserReader model) {
        ParserReader s = model.trim ();
        if (s.startsWith ("EMPTY")) { // NOI18N
            return new EMPTYType ();
        } else if (s.startsWith ("ANY")) { // NOI18N
            return new ANYType ();
        } else if (s.startsWith ("(")) { // NOI18N
            if (s.trim ().startsWith ("#PCDATA")) { // NOI18N
                return new MixedParser ().parseModel (s);
            } else {
                return new ChildrenParser ().parseModel (s);
            }
        } else {
            //grammar does not allow it!!!
            //let others skip it
            return null;
        }
    }
    
}
