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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

/**
 * @author sumitabhk
 *
 */
public class MetaLayerRelationFactory {

	private static MetaLayerRelationFactory m_Instance = null;

	/**
	 *
	 */
	private MetaLayerRelationFactory() {
		super();
	}
	
	public static MetaLayerRelationFactory instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new MetaLayerRelationFactory();
		}
		return m_Instance;
	}

	/**
	 *
	 * Creates either a PackageImport or ElementImport in the Package namespace of the importingElement.
	 * If elementToImport is a Package, then a PackageImport is created, else and ElementImport.
	 *
	 * @param importingElement[in]   The element importing the other.
	 * @param elementToImport[in]    The element being imported
	 * @param pImport[out]           The import element
	 *
	 * @return HRESULT
	 *
	 */
	public IDirectedRelationship createImport(IElement importingElement,
											  IAutonomousElement elementToImport)
	{
		IDirectedRelationship pImport = null;

		// Check to see if importingElement is already a package. If he isn't, retrieve the first
		// owning namespace that is a Package
		IPackage importingPackage = null;
		if (importingElement instanceof IPackage)
		{
			importingPackage = (IPackage)importingElement;
		}
		else
		{
			importingPackage = importingElement.getOwningPackage();
		}
		
		if (importingPackage != null)
		{
			pImport = createPackageImport(importingPackage, elementToImport);
		}
		return pImport;
	}

	/**
	 *
	 * Creates either a PackageImport or ElementImport in the Package namespace of the importingElement.
	 * If elementToImport is a Package, then a PackageImport is created, else and ElementImport.
	 *
	 * @param importingPackage[in]   The Package importing elementToImport.
	 * @param elementToImport[in]    The element being imported
	 * @param pImport[out]           The import element
	 *
	 * @return HRESULT
	 *
	 */
	private IDirectedRelationship createPackageImport(IPackage importingPackage, IAutonomousElement elementToImport) {
	
		IDirectedRelationship pImport = null;

		// Now check to see if the elmentToImport is a Package. If it is, we'll create a PackageImport, 
		// otherwise, we'll create the ElementImport
		UMLURILocator loc = new UMLURILocator(importingPackage);
		String uri = loc.getVersionedURI(elementToImport);
		
		if (elementToImport instanceof IPackage)
		{
			//create a pacakge import
			IPackage packageToImport = (IPackage)elementToImport;
			pImport = importingPackage.importPackage(packageToImport, uri, true);
		}
		else
		{
			//create an element import
			pImport = importingPackage.importElement(elementToImport, uri, true);
		}
		return pImport;
	}

	/**
	 *
	 * Determines whether or not an import is needed. A true result will be returned if the elements 
	 * passed in belong to different Projects
	 *
	 * @param importingElement[in]   The element potentially doing the import.
	 * @param elementToImport[in]    The element to potentially import.
	 *
	 * @return true if an import is needed, else false otherwise.
	 *
	 */
	public boolean isImportNeeded(IElement importingElement, IElement elementToImport)
	{
		boolean isNeeded = false;
		// Make sure that elementToImport supports the IAutonmousElement. That is 
		// required in order to import
		if (elementToImport instanceof IAutonomousElement)
		{
			boolean sameProject = true;
			sameProject = importingElement.inSameProject(elementToImport);
			if (!sameProject)
			{
				isNeeded = true;
			}
		}
		return isNeeded;
	}

	/**
	 *
	 * Creates the appropriate import relationship between the passed in elements, if needed.
	 *
	 * @param importingElement[in]   The element potentially importing elementToImport
	 * @param elementToImport[in]    The element to import
	 * @param pNewImport[out]        The new relationship
	 *
	 * @return true if the import was needed and successfully created, else false
	 *
	 */
	public IDirectedRelationship establishImportIfNeeded(IElement importingElement, IElement elementToImport)
	{
      IDirectedRelationship pNewRel = null;
      if(isImportNeeded(importingElement, elementToImport) == true)
      {   		
   		if (elementToImport instanceof IAutonomousElement)
   		{
   			IAutonomousElement autoElement = (IAutonomousElement)elementToImport;
   			pNewRel = createImport(importingElement, autoElement);
   		}
      }
		return pNewRel;
	}

}



