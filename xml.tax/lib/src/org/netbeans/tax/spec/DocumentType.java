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
package org.netbeans.tax.spec;

import org.netbeans.tax.TreeDocumentType;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface DocumentType {
    
    //
    // Constraints
    //
    
    /**
     *
     */
    public static interface Constraints {
        
        public void checkDocumentTypeElementName (String elementName) throws InvalidArgumentException;
        
        public boolean isValidDocumentTypeElementName (String elementName);
        
        
        public void checkDocumentTypePublicId (String publicId) throws InvalidArgumentException;
        
        public boolean isValidDocumentTypePublicId (String publicId);
        
        
        public void checkDocumentTypeSystemId (String systemId) throws InvalidArgumentException;
        
        public boolean isValidDocumentTypeSystemId (String systemId);
        
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
        public TreeDocumentType createDocumentType (String elementName);
        
        /**
         * @throws InvalidArgumentException
         */
        public TreeDocumentType createDocumentType (String elementName, String publicId, String systemId);
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeDocumentType (TreeDocumentType documentType) throws TreeException;
        
    } // end: interface Writer
    
    
    //
    // Child
    //
    
    /**
     *
     */
    public static interface Child {
        
    } // end: intereface Child
    
}
