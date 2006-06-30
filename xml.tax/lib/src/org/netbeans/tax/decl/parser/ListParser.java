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

/** Parse list of ContentParticles. */
public abstract class ListParser extends MultiplicityParser implements ModelParser {

    /** Parse model content.
     * @param model parserreader without starting delimiter.
     */
    public TreeElementDecl.ContentType parseModel (ParserReader s) {

        ChildrenType type = createType (s);

        //first element
        type.addType (new ContentParticleParser ().parseModel (s));
        
        while ( !!! s.trim ().startsWith (")")) { // NOI18N
            
            if (s.startsWith (getSeparator ()) ) {
                type.addType (new ContentParticleParser ().parseModel (s));
                
            } else {
                //should not occure
                new RuntimeException ("Error in " + this); // NOI18N
                break;
            }
        }
        
        return type; // may be empty (e.g. (#PCDATA))
    }
    
    /**
     */
    protected abstract ChildrenType createType (ParserReader model);
    
    /**
     */
    protected abstract String getSeparator ();
    
    /**
     */
    protected boolean isEndMark (int ch) {
        switch (ch) {
            case ')': case -1:
                return true;
            default:
                return false;
        }
    }
    
}
