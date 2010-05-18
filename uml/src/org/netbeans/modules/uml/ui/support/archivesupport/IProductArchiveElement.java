/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
