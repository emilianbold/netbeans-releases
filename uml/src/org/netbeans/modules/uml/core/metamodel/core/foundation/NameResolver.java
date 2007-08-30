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

import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class NameResolver
{

	private static boolean m_ExternalResolution = false;

	/**
	 * 
	 */
	public NameResolver()
	{
		super();
	}
	
	public static boolean typesResolvedExternally()
	{
		return m_ExternalResolution;
	}

	/**
	 *
	 * Determines whether or not the passed in name is fully qualified
	 * in UML syntax ( "A::B::C" ). If it is, the type will be located.
	 *
	 * @param contextElement[in]  The element to query against. 
	 * @param name[in]            The name to check. If the name is not
	 *                            qualified, nothing occurs
	 * @param foundElement[out]   The found element, else 0
	 *
	 * @return true if the name coming in is qualified, else false
	 *
	 */
	public static INamedElement resolveFullyQualifiedName(IElement element, String name)
	{
		ETList<INamedElement> elems = null;
		elems = resolveFullyQualifiedNames(true, element, name, "");
		if (elems != null)
		{
			int count = elems.size();
			if (count > 0)
			{
				return elems.get(0);
			}
		}
		return null;
	}

	/**
	 *
	 * Determines whether or not the passed in name is fully qualified
	 * in UML syntax ( "A::B::C" ). If it is, any type at that location
	 * by that name will be retrieved
	 *
	 * @param baseElement[in]     The element to query against. 
	 * @param name[in]            The name to check. If the name is not
	 *                            qualified, nothing occurs
	 * @param foundElement[out]   The found element, else 0
	 *
	 * @return true if the name coming in is qualified, else false
	 *
	 */
	public static ETList <INamedElement> resolveFullyQualifiedNames(IElement element, String name)
	{
		return resolveFullyQualifiedNames(false, element, name, "");
	}
	/**
	 * Internal method used by all the public methods. Finds and resolves types
	 * by name. If the fully qualified type does not exist, it is created ( 
	 * depending on the Unknown classifier preference )
	 * 
	 * @param resolveOnlyFirstFound[in] Handles only the first element found if true
	 * @param contextElement[in]        The element whose project is retrieved for 
	 *                                  location purposes
	 * @param name[in]                  Fully qualified name to handle. If not fully 
	 *                                  qualifed, nothing occurs
	 * @param foundElements[out]        All elements found
	 * @param typeName[in]              The type to filter against, such as "Class" or
	 *                                  "DataType"
	 * 
	 * @return true if "name" was determined to be fully qualified, else false if not
	 */	
	private static ETList <INamedElement> resolveFullyQualifiedNames(boolean resolveOnlyFirstFound, 
								IElement element, String name, String type)
	{
            ETList <INamedElement> elems = null;
            if (name.length() > 0)
            {
                int pos = name.indexOf("::");
                if (pos >= 0)
                {
                    // We have a qualified name
                    IProject proj = element.getProject();
                    
                    if (proj == null)
                    {
                        // Check to see if we have a transition element
                        if (element instanceof ITransitionElement)
                        {
                            IElement futureOwner = ((ITransitionElement)element).getFutureOwner();
                            if (futureOwner != null)
                            {
                                proj = futureOwner.getProject();
                                if (proj == null)
                                {
                                    IPackage opkg = futureOwner.getOwningPackage();
                                    if (opkg != null)
                                        proj = opkg.getProject();
                                }
                                
                            }
                        }
                    }
                    
                    if (proj != null)
                    {
                        ITypeManager tMan = proj.getTypeManager();
                        if (tMan != null)
                        {
                            IPickListManager pMan = tMan.getPickListManager();
                            if (pMan != null)
                            {
                                String lookupName = name;
                                if(pos == 0)
                                {
                                    // In case of a primitive we add "::"
                                    // in front of the name, so we are know that
                                    // the name is fully qualified.  However,
                                    // before we lookup the name in the typemanager
                                    // we have to remove the "::" characters.
                                   lookupName = name.substring(2);    
                                }
                                
                                IStrings strs = pMan.getIDsByName(lookupName);
                                //need to find a way to do this.
/*							if( hr == PL_S_TYPES_FROM_EXTERNAL_PROJECT )
                                                        {
                                                           // Types were retrieved from an external
                                                           // project
 
                                                           m_ExternalResolution = true;
                                                        } */
                                
                                if (strs != null)
                                {
                                    int num = strs.getCount();
                                    if (num > 0)
                                    {
                                        if (resolveOnlyFirstFound)
                                        {
                                            num = 1;
                                        }
                                        for (int i=0; i<num; i++)
                                        {
                                            String xmiid = strs.item(i);
                                            if (xmiid.length() > 0)
                                            {
                                                IVersionableElement ver = tMan.getElementByID(xmiid);
                                                if (ver instanceof INamedElement)
                                                {
                                                    INamedElement named = (INamedElement)ver;
                                                    if(elems == null)
                                                        elems = new ETArrayList <INamedElement>();
                                                    if (addToCollection(named, type))
                                                    {
                                                        elems.add(named);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (num == 0 || elems == null || elems.size() == 0)
                                    {
                                        // We have a situation where the user is passing in a fully qualified name that does not exist,
                                        // so we need to create the package structure, and then create the type as dictate by
                                        // the unknown classifier preference
                                        INamedElement newEle = establishPackagedType(proj, name, type);
                                        if(elems == null)
                                            elems = new ETArrayList <INamedElement>();
                                        if (addToCollection(newEle, type))
                                        {
                                            elems.add(newEle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return elems;
        }

	/**
	 * Makes sure that when name comes in fully qualified, that the package structure is created and the unknown
	 * classifier preference is checked to create the new type
	 * 
	 * @param project[in]      The project where the package structure will be created
	 * @param name[in]         The fully qualified name to handle, such as A::B::myType
	 * @param newElement[out]  The newly created element
	 * 
	 * @return HRESULT 
	 * @note If the unknown classifier preference is not set to "Yes", then no package structure will be 
	 *       created.
	 */
	private static INamedElement establishPackagedType( IProject project, String name, String filterType)
        {
            INamedElement retEle = null;
            if (name != null && name.length() > 0)
            {
                // Need to check our preference to see if we should
                // just create a node 'cause we couldn't find a classifier
                // with that name.
                IPreferenceAccessor pPref = PreferenceAccessor.instance();
                boolean create = pPref.getUnknownClassifierCreate();
                if (create)
                {
                    // Trim the last name off the fully qualified name, as that will be the
                    // new type to create. But we need to ensure the package structure exists.
                    String fullName = name;
                    int pos = fullName.lastIndexOf("::");
                    if (pos >= 0)
                    {
                        // Add 2 to get passed the '::'
                        String typeName = fullName.substring(pos + 2);
                        String packageStructure = fullName.substring(0, pos);
                        
                        IPackage outerPack = project.createPackageStructure(packageStructure);
                        if (outerPack != null) {
                            if (filterType != null && filterType.length() > 0) {
                                //kris richards - removed access to expunged prefs.
                                retEle = UMLXMLManip.createAndAddUnknownType(outerPack, typeName);
                                
                                // THis is a hack.  Since createAndAddUnknowType will always add
                                // the type to the pacakge.
//                                outerPack.addOwnedElement(retEle);
//                                
//                            } else {
////                                retEle = UMLXMLManip.createAndAddUnknownType(outerPack, typeName);
                            }
                        }
                    }
                }
            }
            return retEle;
        }

	/**
	 *
	 * Retrieves the simple name of a fully qualified name. So if 
	 * you pass in "A::B::C", "C" is returned. If you pass in just
	 * "C", "C" is returned
	 *
	 * @param fullyQualified[in] The name to simplify
	 *
	 * @return The name
	 *
	 */
        public static String getSimpleName(String fullyQualified)
        {
            String retStr = fullyQualified;
            if (fullyQualified != null && fullyQualified.length() > 0)
            {
                int pos = retStr.lastIndexOf(":");

                if (pos >= 0 && ((pos + 1) != fullyQualified.length()) )
                    retStr = fullyQualified.substring(pos + 1);
            }
            return retStr;
        }
        
	public static INamedElement resolveFullyQualifiedNameByType(IElement element, String name, String typeName)
	{
		INamedElement foundElement = null;
		if (element != null)
		{
			ETList<INamedElement> elements = resolveFullyQualifiedNamesByType(element, name, typeName);
			if (elements != null)
			{
				int count = elements.size();
				if (count > 0)
				{
					foundElement = elements.get(0);
				}
			}
		}
		return foundElement;
	}
	public static ETList<INamedElement> resolveFullyQualifiedNamesByType(IElement element, String name, String typeName)
	{
		return resolveFullyQualifiedNames(false, element, name, typeName);
	}
	/**
	 * Adds the passed in element to the collection. If the elements collection has not
	 * been established, it will be created and returned.
	 * 
	 * @param elements[in,out] The collection to add to
	 * @param element[in]      The element to add. If 0, elements will be unchanged
	 * @param typeName[in]     Type to filter on. Can be "", in which case it is ignored.
	 * 
	 * @return HRESULT 
	 */

	private static boolean addToCollection( INamedElement element, String typeName )
	{
		boolean addIt = true;
		if (element != null)
		{
			if( typeName != null && typeName.length() > 0 )
			{
				Node node = element.getNode();
				if( node != null)
				{
				   String name = XMLManip.retrieveSimpleName(node);
				   if (name != null && (!(typeName.equals(name))))
				   {
					  // Only add if the type matches the type passed in
					  addIt = false;
				   }
				}
			}
		}
		return addIt;
	}

}



