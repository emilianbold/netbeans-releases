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

package org.netbeans.modules.uml.core.support.umlutils;


import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IElementLocator 
{

//   Finds the elements that match the passed in name. The entire Project is searched.
//HRESULT FindByName([in] IProject* project, [in] BSTR name, [out, retval] INamedElements** foundElements);
  public ETList<INamedElement> findByName(IProject proj, String name);

//   Find the element in the Namespace passed. Only the immediate namespace is searched. No imported package or parent Namespaces are searched.
//HRESULT FindByName2([in] INamespace* space, [in] BSTR name, [out,retval] INamedElements** foundElements);
  public ETList<INamedElement> findByName(INamespace space, String name);

//   Searches the immediate namespace and any imported packages in order to find the element that matches the passed in name.
//HRESULT FindByNameInMembersAndImports([in] INamespace* space, [in] BSTR name, [out, retval ] INamedElements** foundElements);
  public ETList<INamedElement> findByNameInMembersAndImports(INamespace space, String name);

//   Retrieves the element that has the passed in id.
//HRESULT FindByID([in] INamespace* space, [in] BSTR idToFind, [out, retval ] IElement** foundElement );
  public IElement findByID(INamespace space, String idToFind);

//   Retrieves the element that has the passed in id.
//HRESULT FindByID2([in] IProject* project, [in] BSTR idToFind, [ out, retval ] IElement** foundElement);
  public IElement findByID(IProject proj, String idToFind);

//   Retrieves the single element that matches the passed in query.
//HRESULT FindSingleElementByQuery([in] IVersionableElement* context, [in] BSTR query, [out, retval ] IElement** foundElement );
  public IElement findSingleElementByQuery(IVersionableElement context, String query);

//   Retrieves the elements that match the passed in query.
//HRESULT FindElementsByQuery([in] IVersionableElement* context, [in] BSTR query, [out, retval ] IElements** foundElements);
  public ETList<IElement> findElementsByQuery(IVersionableElement context, String query);

//   Retrieves the element that has the passed in id.
//HRESULT FindByID3([in] IVersionableElement* context, [in] BSTR idToFind, [out,retval] IElement** element);
  public IElement findByID(IVersionableElement context, String idToFind);

//   Retrieves the element that has the passed in id.
//HRESULT FindElementByID([in] IProject* project, [in] BSTR idToFind, [ out, retval ] IElement** foundElement);
  public IElement findElementByID(IProject proj, String idToFind);

// Retrieves the element that has the passed in id.
//HRESULT FindElementByID2([in] IVersionableElement* context, [in] BSTR idToFind, [out,retval] IElement** element);
  public IElement findElementByID(IVersionableElement context, String idToFind);

//   Reloads an element given only the XMI id.  Given the fact that this routine has an xmiid context it must loop over all open projects and find the project with the given xmiid. 
  public IElement findElementByID(String sTopLevelXMIID, String sElementXMIID);

//   Reloads an element given only the XMI id.  Given the fact that this routine has no context it must loop over all open projects and search each one. 
	public IElement findElementByID(String sElementXMIID);

//   Retrieves all elements with the passed in fully scoped name. The name should be in UML format ( e.g., Package::InnerPackage ).
//HRESULT FindScopedElements( [in] IVersionableElement* context, [in] BSTR fullyScopedName, [out, retval ] IElements** elements);
  public ETList<IElement> findScopedElements(IVersionableElement context, String fullyScopedName);

//   Performs a query within the passed in Namespace. If the query fails, attempts same query on imported elements, as well as the parent namespaces up the tree.
//HRESULT FindElementsByDeepQuery([in] INamespace* space, [in] BSTR query, [out,retval] IElements** elements);
  public ETList<IElement> findElementsByDeepQuery(INamespace space, String query);

//   Retrieves the specified element.  If the element does not exist an element is created and added to the correct namespace.  The creation of the element is driven by user preferences.
//HRESULT ResolveScopedElement(IVersionableElement* context, BSTR fullyScopedName, IElement **pType);
  public IElement resolveScopedElement(IVersionableElement context, String fullyScopedName);

//   Retrieves the fully qualified name of the element with the passed in node. Form is \"outerNamespace::innerNamespace::elementName\"
//HRESULT RetrieveFullyQualifiedName([in] IXMLDOMNode* node, [out, retval] BSTR* qualifiedName);
  public String retrieveFullyQualifiedName(Node node);

//   Retrieves the fully qualified name of the element with the passed in node.
//HRESULT RetrieveFullyQualifiedName2([in] IXMLDOMNode* node, [in] BSTR delimiter, [out, retval] BSTR* qualifiedName);
  public String retrieveFullyQualifiedName(Node node, String delimiter);

//   Retrieves the fully qualified name of the passed in element.
//HRESULT RetrieveFullyQualifiedName3([in] INamedElement* pElement, [in] BSTR delimiter, [out,retval] BSTR* qualifiedName);
  public String retrieveFullyQualifiedName(INamedElement elem, String delimiter);

}
