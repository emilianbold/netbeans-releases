/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.ui.support.archivesupport;

import org.dom4j.Element;

public interface IProductArchiveElement
{
	/**
	 * The name of the element.
	*/
	public String getID();

	/**
	 * Creates a sub element.
	*/
	public IProductArchiveElement createElement( String sID );

	/**
	 * Removes an attribute.
	*/
	public void removeAttribute( String sID );

	/**
	 * Creates an attribute
	*/
	public IProductArchiveAttribute addAttribute( String sName, Object pVal );

	/**
	 * Creates an attribute
	*/
	public IProductArchiveAttribute addAttributeLong( String sName, long nVal );

	/**
	 * Creates an attribute
	*/
	public IProductArchiveAttribute addAttributeBool( String sName, boolean bVal );

	/**
	 * Creates an attribute
	*/
	public IProductArchiveAttribute addAttributeDouble( String sName, double fVal );

	/**
	 * Creates an attribute
	*/
	public IProductArchiveAttribute addAttributeString( String sName, String sVal );

	/**
	 * Gets all the sub elements of this element
	*/
	public IProductArchiveElement[] getElements();

	/**
	 * Gets all the attributes of this element
	*/
	public IProductArchiveAttribute[] getAttributes();

	/**
	 * Gets an attribute by name.
	*/
	public IProductArchiveAttribute getAttribute( String sName );

	/**
	 * Gets an element by name.
	*/
	public IProductArchiveElement getElement( String sID );

	/**
	 * Gets the IXMLDOMElement that matches this node.
	*/
	public Element getDOMElement();

	/**
	 * Gets the IXMLDOMElement that matches this node.
	*/
	public void setDOMElement( Element value );

	/**
	 * Gets an attribute value by name. 
	*/
	public long getAttributeLong( String sName);

	/**
	 * Gets an attribute value by name.
	*/
	public String getAttributeString( String sName );

   /**
    * Gets an attribute value by name.
   */
   public boolean getAttributeBool( String sName );

   /**
    * Gets an attribute value by name.
   */
   public boolean getAttributeBool( String sName, boolean defaultValue );

	/**
	 * Gets an attribute value by name. 
	*/
	public double getAttributeDouble( String sName );
   
   /**
    * Determines if the element has been deleted from the archive table.
    * 
    * @return true if the element has been deleted.
    */
   public boolean isDeleted();

}
