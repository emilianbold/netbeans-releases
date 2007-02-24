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

package org.netbeans.modules.uml.core.typemanagement;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IPickListManager
{
	/**
	 * Retrieves all the types in the Project that have a matching type specified in the typeFilter. typeFilter is a collection of type names to match against, such as 'Class'.
	*/
	public IElement getTypesWithFilter( IStrings typeFilter );

	/**
	 * Retrieves all the types in the Project that match the type passed in via the type parameter. type should be a typename such as 'Class'.
	*/
	public ETList<IElement> getTypesOfType( String type );

	/**
	 * Retrieves all the types in the Project that have a matching type specified in the filter. filter should be white-space delimited list of typenames, such as 'Class Interface Steroetype'.
	*/
	public ETList<IElement> getTypesWithStringFilter( String filter );

	/**
	 * Retrieves all the type names in the Project that have a matching type specified in the typeFilter. typeFilter is a collection of type names to match against, such as 'Class'
	*/
	public IStrings getTypeNamesWithFilter( IStrings typeFilter );

	/**
	 * Retrieves all the types in the Project that match the type passed in via the type parameter. type should be a typename such as 'Class'.
	*/
	public IStrings getTypeNamesOfType( String type );

	/**
	 * Retrieves all the types in the Project that have a matching type specified in the filter. filter should be white-space delimited list of typenames, such as 'Class Interface Steroetype'.
	*/
	public IStrings getTypeNamesWithStringFilter( String filter );

	/**
	 * Retrieves all the type names in the Project that have a matching type specified in the typeFilter. typeFilter is a collection of type names to match against, such as 'Class'
	*/
	public IStrings getFullyQualifiedTypeNamesWithFilter( IStrings typeFilter );

	/**
	 * Retrieves all the types in the Project that match the type passed in via the type parameter. type should be a typename such as 'Class'.
	*/
	public IStrings getFullyQualifiedTypeNamesOfType( String type );

	/**
	 * Retrieves all the types in the Project that have a matching type specified in the filter. filter should be white-space delimited list of typenames, such as 'Class Interface Steroetype'.
	*/
	public IStrings getFullyQualifiedTypeNamesWithStringFilter( String filter );

	/**
	 * Deinitializes this manager. This manager should not be used once this is called.
	*/
	public void deinitialize();

	/**
	 * The TypeManager associated with this PickListManager.
	*/
	public ITypeManager getTypeManager();

	/**
	 * The TypeManager associated with this PickListManager.
	*/
	public void setTypeManager( ITypeManager value );

	/**
	 * Retrieves the ID of the element with the passed in name. If multiple elements have that name, the first is retrieved.
	*/
	public String getIDByName( String elementName );

	/**
	 * Retrieves the ID of all elements with the passed in name.
	*/
	public IStrings getIDsByName( String elementName );

	/**
	 * Adds the passed in type to the PickListManager's cache.
	*/
	public void addNamedType( INamedElement pNamedElement );

	/**
	 * Adds the passed in type to the PickListManager's cache.
	*/
	public void addExternalNamedType( INamedElement pNamedElement );

        /**
	 * Removes the passed in type to the PickListManager's cache.
	*/
	public void removeNamedType( INamedElement pNamedElement, String curName );

	/**
	 * Retrieves the element with the passed in name and element type. If multiple elements have that name, the first is retrieved. filter should be white-space delimited list of typenames, such as 'Class Interface Steroetype'.
	*/
	public IElement getElementByNameAndStringFilter( String elementName, String filter );

	/**
	 * Retrieves the elements with the passed in name and element type. filter should be white-space delimited list of typenames, such as 'Class Interface Steroetype'.
	*/
	public ETList<IElement> getElementsByNameAndStringFilter( String elementName, String filter );

	/**
	 * Retrieves the element with the passed in name and element type. If multiple elements have that name, the first is retrieved.
	*/
	public IElement getElementByNameAndType( String elementName, String sType );

	public IStrings getLocalIDsByName(String elementName);

}
