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

package org.netbeans.modules.uml.core.typemanagement;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
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
	 * Retrieves all the types visible from inside the "space" namespace that have a matching type specified in the filter. filter should be white-space delimited list of typenames, such as 'Class Interface Steroetype'. 
	*/
	public IStrings getTypeNamesWithStringFilterNamespaceVisible( String filter, boolean fullNames, INamespace space );

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
