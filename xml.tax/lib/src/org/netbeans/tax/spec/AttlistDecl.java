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

import org.netbeans.tax.TreeAttlistDecl;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.InvalidArgumentException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface AttlistDecl {

    //
    // Constraints
    //
    
    /**
     *
     */
    public static interface Constraints {

    	public void checkAttlistDeclElementName (String elementName) throws InvalidArgumentException;
    
    	public boolean isValidAttlistDeclElementName (String elementName);

    
    	public void checkAttlistDeclAttributeName (String attributeName) throws InvalidArgumentException;
    
    	public boolean isValidAttlistDeclAttributeName (String attributeName);

    
    	public void checkAttlistDeclAttributeType (short type) throws InvalidArgumentException;
    
    	public boolean isValidAttlistDeclAttributeType (short type);

    
    	public void checkAttlistDeclAttributeEnumeratedType (String[] enumeratedType) throws InvalidArgumentException;
    
    	public boolean isValidAttlistDeclAttributeEnumeratedType (String[] enumeratedType);

    
    	public void checkAttlistDeclAttributeDefaultType (short defaultType) throws InvalidArgumentException;
    
    	public boolean isValidAttlistDeclAttributeDefaultType (short defaultType);
    

    	public void checkAttlistDeclAttributeDefaultValue (String defaultValue) throws InvalidArgumentException;
    
    	public boolean isValidAttlistDeclAttributeDefaultValue (String defaultValue);
    
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
  	public TreeAttlistDecl createAttlistDecl (String elementName);

    } // end: interface Creator


    //
    // Writer
    //

    /**
     *
     */
    public static interface Writer {
	
	public void writeAttlistDecl (TreeAttlistDecl attlistDecl) throws TreeException;

    } // end: interface Writer

}
