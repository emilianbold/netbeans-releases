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
