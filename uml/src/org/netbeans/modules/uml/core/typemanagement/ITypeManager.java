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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ITypeManager
{
	/**
	 * The IProject this TypeManager is monitoring.
	*/
	public IProject getProject();

	/**
	 * The IProject this TypeManager is monitoring.
	*/
	public void setProject( IProject value );

	/**
	 * Retrieves the element with the passed in name. If more than one element has the name, the first one found is returned.
	*/
	public INamedElement getElementByName( String name );

	/**
	 * Retrieves the collection of elements with the passed in name.
	*/
	public ETList<INamedElement> getElementsByName( String name );

	/**
	 * Retrieves the collection of elements with the passed in name.
	*/
	public IVersionableElement getElementByID( String xmiID );

	/**
	 * method GetRawElementByID
	*/
	public Node getRawElementByID( String elementID );

	/**
	 * Saves the contents of this manager.
	*/
	public void save( String Location );

	/**
	 * Adds an entry to the Type file with information found in the passed in element.
	*/
	public void addType( IVersionableElement element );

	/**
	 * Adds an entry to the Type file with information found in the passed in element. The element is assumed to be an import element from another Project.
	*/
	public void addExternalType( IVersionableElement element );

	/**
	 * Removes the entry that matches the passed in element from the type file.
	*/
	public void removeType( IVersionableElement element );

	/**
	 * Retrieves the absolute file locations of externally loaded elements.
	*/
	public IStrings gatherExternalFileLocations();

	/**
	 * Loads the external elements found in fileLocation into the Project this TypeManager is a part of.
	*/
	public void loadExternalFile( String fileLocation );
    
	public void loadExternalElements();
    
	/**
	 * Retrieves the owner of childElement that is under version control.
	*/
	public IVersionableElement getVersionedOwner( IVersionableElement childElement );

	/**
	 * Removes the passed in element from the type resolution portions of the .ettm file, cleaning any references to element throughout the IProject this TypeManager is associated with.
	*/
	public void removeFromTypeLookup( IVersionableElement element );

	/**
	 * Retrieves all the elements in the Project this TypeManager is associated with that are directly under version control.
	*/
	public ETList<IVersionableElement> getAllVersionedElements();

	/**
	 * Ensures that the element passed in is properly parented, with immediate parent loaded in memory.
	*/
	public boolean verifyInMemoryStatus( IVersionableElement element);

	/**
	 * The PickListManager associated with this TypeManager.
	*/
	public IPickListManager getPickListManager();

	/**
	 * The PickListManager associated with this TypeManager.
	*/
	public void setPickListManager( IPickListManager value );

	/**
	 * Retrieves the element with the passed in name by searching the Project's list of referenced libraries. If more than one element has the name, the first one found is returned.
	*/
	public INamedElement getElementFromLibrariesByName( String name );

	/**
	 * Retrieves the collection of elements with the passed in name by searching the Project's list of referenced libraries.
	*/
	public ETList<INamedElement> getElementsFromLibrariesByName( String name );

	/**
	 * Adds the passed in elements to the type file. 
	*/
	public void addTypes( ETList<IElement> Elements, boolean addOnlyIfVersioned );

	/**
	 * Adds an entry to the Type file with information found in the passed in element.
	*/
	public void addType( IVersionableElement element, boolean addOnlyIfVersioned, boolean loadAllExternalElements );

	/**
	 * Retrieves the referenced library with the passed in name.
	*/
	public IProject getReferencedLibraryProjectByLocation( String refLibLoc );

	/**
	 * Retrieves all the referenced libraries associated with the Project this TypeManager manages.
	*/
	public ETList<IProject> getReferencedLibraryProjects();

	/**
	 * Retrieves all the types that are generally managed by the PickListManager of a particular name.
	*/
	public ETList<INamedElement> getCachedTypesByName( String nameOfType );

	/**
	 * Retrieves the first type that is generally managed by the PickListManager of a particular name.
	*/
	public INamedElement getCachedTypeByName( String nameOfType );

	/**
	 * Retrieves the element with the passed in name by searching the Project's list of referenced libraries. If more than one element has the name, the first one found is returned. filter should be white-space delimited list of typenames, such as 'Class Interface St?Ô?
	*/
	public INamedElement getElementFromLibrariesByNameAndType( String name, String filter );

	/**
	 * Adds the passed in ID to the cache of IDs that have been deleted. This prevents type resolution for IDs that have been deleted.
	*/
	public void addToDeletedIds(String id);

	/**
	 * Clears the cache of deleted IDs.
	*/
	public void clearDeletedIDs();

	/**
	 * Retrieves the collection of types that are resident in the Project this TypeManager is associated with. Searches will not be done across projects..
	*/
	public ETList<INamedElement> getLocalCachedTypesByName( String nameOfType );

}
