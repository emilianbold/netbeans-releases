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



