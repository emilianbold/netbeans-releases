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
