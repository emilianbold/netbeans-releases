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
package org.netbeans.tax.spec;

import org.netbeans.tax.TreeDocumentFragment;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface DocumentFragment {
    
    //
    // Constraints
    //
    
    /**
     *
     */
    public static interface Constraints {
        
        public void checkDocumentFragmentVersion (String version) throws InvalidArgumentException;
        
        public boolean isValidDocumentFragmentVersion (String version);
        
        
        public void checkDocumentFragmentEncoding (String encoding) throws InvalidArgumentException;
        
        public boolean isValidDocumentFragmentEncoding (String encoding);
        
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
        public TreeDocumentFragment createDocumentFragment (String version, String encoding);
        
    } // end: interface Creator
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static interface Writer {
        
        public void writeDocumentFragment (TreeDocumentFragment documentFragment) throws TreeException;
        
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
