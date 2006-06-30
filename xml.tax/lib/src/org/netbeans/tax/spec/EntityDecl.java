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

import org.netbeans.tax.TreeEntityDecl;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface EntityDecl {

    //
    // Constraints
    //

    /**
     *
     */
    public static interface Constraints {

        public void checkEntityDeclName (String name) throws InvalidArgumentException;

        public boolean isValidEntityDeclName (String name);


        public void checkEntityDeclInternalText (String internalText) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclInternalText (String internalText);
        
        
        public void checkEntityDeclPublicId (String publicId) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclPublicId (String publicId);
        
        
        public void checkEntityDeclSystemId (String systemId) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclSystemId (String systemId);
        
        
        public void checkEntityDeclNotationName (String notationName) throws InvalidArgumentException;
        
        public boolean isValidEntityDeclNotationName (String notationName);
        
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
        public TreeEntityDecl createEntityDecl (String name, String internalText);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (boolean parameter, String name, String internalText);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (String name, String publicId, String systemId);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (boolean parameter, String name, String publicId, String systemId);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeEntityDecl createEntityDecl (String name, String publicId, String systemId, String notationName);
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeEntityDecl (TreeEntityDecl entityDecl) throws TreeException;
        
    } // end: interface Writer
    
}
