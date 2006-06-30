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
package org.netbeans.tax.spec;

import org.netbeans.tax.TreeElementDecl;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface ElementDecl {

    //
    // Constraints
    //

    /**
     *
     */
    public static interface Constraints {

        public void checkElementDeclName (String name) throws InvalidArgumentException;

        public boolean isValidElementDeclName (String name);


        public void checkElementDeclContentType (TreeElementDecl.ContentType contentType) throws InvalidArgumentException;
        
        public boolean isValidElementDeclContentType (TreeElementDecl.ContentType contentType);
        
    } // end: interface Constraints
    
    
    //
    // Creator
    //
    
    /**
     *
     */
    public static interface Creator {
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeElementDecl createElementDecl (String name, TreeElementDecl.ContentType contentType);
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeElementDecl (TreeElementDecl elementDecl) throws TreeException;
        
    } // end: interface Writer
    
}
