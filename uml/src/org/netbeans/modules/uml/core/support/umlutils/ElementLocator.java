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

package org.netbeans.modules.uml.core.support.umlutils;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ElementLocator implements IElementLocator
{
   
   /**
    *
    * Finds the elements that match the passed-in name. The entire Project
    * is searched.
    *
    * @param project[in] The Project to search through
    * @param name[in] The name to match against
    * @param foundElements[out] The found elements, 0 on error
    *
    * @return HRESULTs
    */
   public ETList<INamedElement> findByName(IProject proj, String name)
   {
      ETList<INamedElement> elems = null;
      Document doc = proj.getDocument();
      if (doc != null)
         elems = UMLXMLManip.findByNameInDocument(doc, name);
      return elems;
   }
   
   /**
    *
    * Find the element in the Namespace passed. Only the immediate namespace is searched.
    * No imported package or parent Namespaces are searched.
    *
    * @param space[in] The Namespace to search through
    * @param name[in] The name to match against
    * @param foundElements[out] The found elements, else 0 on error
    *
    * @return HRESULTs
    */
   public ETList<INamedElement> findByName(INamespace space, String name)
   {
      return space.getOwnedElementsByName(name);
   }
   
   /**
    *
    * Searches the immediate namespace and any imported packages in order to find
    * the element that matches the passed-in name.
    *
    * @param space[in] The Namespace to search
    * @param name[in] The name to match against
    * @param foundElements[out] The found elements, else 0 on error
    *
    * @return HRESULTs
    */
   public ETList<INamedElement> findByNameInMembersAndImports(INamespace space, String name)
   {
      return UMLXMLManip.findByNameInMembersAndImports(space, name);
   }
   
   public IElement findByID(INamespace space, String idToFind)
   {
      IElement elem = null;
      if (space instanceof IVersionableElement)
      {
         IVersionableElement vElem = (IVersionableElement)space;
         elem = findByID(vElem, idToFind);
      }
      return elem;
   }
   
   public IElement findByID(IProject proj, String idToFind)
   {
      IElement elem = null;
      if (proj instanceof IVersionableElement)
      {
         IVersionableElement vElem = (IVersionableElement)proj;
         elem = findByID(vElem, idToFind);
      }
      return elem;
   }
   
   /**
    *
    * Retrieves the single element that matches the passed-in query.
    *
    * @param space[in] The element used just for context
    * @param query[in] An appropriate XPath query
    * @param foundElement[out] The found element, else 0
    *
    * @return HRESULTs
    */
   public IElement findSingleElementByQuery(IVersionableElement context, String query)
   {
      IElement elem = null;
      if (context != null)
      {
         Node node = context.getNode();
         
         Node n = UMLXMLManip.selectSingleNode(node, query);
         if (n != null)
         {
            Object obj = populateElement(n);
            if (obj instanceof IElement)
               elem = (IElement)obj;
         }
      }
      return elem;
   }
   
   /**
    *
    * Retrieves the elements that match the passed-in query.
    *
    * @param ver[in] The element to provide a context for the search
    * @param query[in] An appropriate XPath query
    * @param foundElements[out] The found elements
    *
    * @return HRESULTs
    */
   public ETList<IElement> findElementsByQuery(IVersionableElement context, String query)
   {
      Node node = null;
      if (context != null)
      {
         node = context.getNode();
      }
      IElement dummy = null;
      return UMLXMLManip.retrieveElementCollection(node, dummy, query, IElement.class);
   }
   
   /**
    *
    * Retrieves the element that has the passed in id.
    *
    * @param context[in] The element that provides a context in which to search. The XML node of the element
    *							 is retrieved in order to get the owning document.
    * @param idToFind[in] The id of the element to retrieve
    * @param element[out] The found element, else 0.
    *
    * @return HRESULTs
    */
   public IElement findByID(IVersionableElement context, String idToFind)
   {
      IElement retEle = null;
      Node n = context.getNode();
      if (n != null)
      {
         Document doc = n.getDocument();
         if (doc != null)
         {
            //Node foundNode = XMLManip.findElementByID(doc, idToFind);
            Node foundNode = UMLXMLManip.findElementByID(doc, idToFind);
            if (foundNode != null)
            {
               Object obj = populateElement(foundNode);
               if (obj instanceof IElement)
                  retEle = (IElement)obj;
            }
         }
      }
      return retEle;
   }
   
   public IElement findElementByID(IProject proj, String idToFind)
   {
      IElement elem = null;
      if (proj != null && proj instanceof IVersionableElement)
      {
         IVersionableElement vElem = (IVersionableElement)proj;
         elem = findByID(vElem, idToFind);
      }
      return elem;
   }
   
   public IElement findElementByID(IVersionableElement context, String idToFind)
   {
      return findByID(context, idToFind);
   }
   
   /**
    * Reloads an element given only the XMI id.  Given the fact that this
    * routine has an xmiid context it must loop over all open projects and
    * find the project with the given xmiid.
    *
    * @param sTopLevelXMIID [in] The project xmiid.
    * @param sElementXMIID [in] The element xmiid.
    * @param pElement [out,retval] The discovered element.
    */
   public IElement findElementByID(String sTopLevelXMIID, String sElementXMIID)
   {
      IElement retEle = null;
      if (sTopLevelXMIID != null && sTopLevelXMIID.length() > 0)
      {
         ICoreProduct prod = ProductRetriever.retrieveProduct();
         if (prod != null && sElementXMIID != null && sElementXMIID.length() > 0)
         {
            IApplication pApp = prod.getApplication();
            if (pApp != null)
            {
               IProject proj = pApp.getProjectByID(sTopLevelXMIID);
               if (proj != null)
               {
                  // Use the element locator to find the model element
                  retEle = findElementByID(proj, sElementXMIID);
               }
            }
         }
      }
      else
      {
         retEle = findElementByID(sElementXMIID);
      }
      return retEle;
   }
   
   /**
    * Reloads an element given only the XMI id.  Given the fact that this
    * routine has no context it must loop over all open projects and search
    * each one.
    *
    * @param sElementXMIID [in] The element xmiid.
    * @param pElement [out,retval] The discovered element.
    */
   public IElement findElementByID(String sElementXMIID)
   {
      IElement retEle = null;
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null && sElementXMIID != null && sElementXMIID.length() > 0)
      {
         IApplication pApp = prod.getApplication();
         if (pApp != null)
         {
            ETList<IProject> projs = pApp.getProjects();
            if (projs != null)
            {
               int count = projs.size();
               
               for (int i=0; i<count; i++)
               {
                  IProject proj = projs.get(i);
                  
                  // Use the element locator to find the model element
                  retEle = findElementByID(proj, sElementXMIID);
                  if (retEle != null)
                  {
                     break;
                  }
               }
            }
         }
      }
      return retEle;
   }
   
   /**
    *
    * Attempts to retrieve the ModelElement at the given scope.
    *
    * @param context[in] The element to provide the context for the search.
    * @param fullyScopedName[in] The fully scoped name of the element to retrieve. Must be in the UML
    *                            scoping for ( e.g., "Package::InnerPackage" )
    * @param elements[out] The collection of elements matching the passed-in fully scoped name
    *
    * @return HRESULT
    * @note If the collection returned contains more than one element, the model is considered
    *       "malformed" in the strict UML sense.
    */
   public ETList<IElement> findScopedElements(IVersionableElement context, String fullyScopedName)
   {
      String query = buildFullyScopedQuery(fullyScopedName);
      
      //return findElementsByQuery(context, query);
      ETList<IElement> retVal = findElementsByQuery(context, query);
      if((retVal == null) || (retVal.size() <= 0))
      {
         if(context instanceof IElement)
         {
            IProject proj = ((IElement)context).getProject();
//            retVal = findScopedElements(context, proj.getName() + fullyScopedName);
            
            if(proj != null)
            {
               query = buildFullyScopedQuery(proj.getName() + "::" + fullyScopedName);
               retVal = findElementsByQuery(context, query);
            }
         }
      }
      
      return retVal;
   }
   
   protected String buildFullyScopedQuery(String fullyScopedName)
   {      
      String curStr = fullyScopedName;
      String query = "/";
      int pos = fullyScopedName.indexOf("::");
      boolean topPackage = true;
      while (pos > 0 || curStr.length()>0)
      {
         String name = "";
         if (pos > -1)
         {
            name = curStr.substring(0, pos);
         }
         else
         {
            name = curStr;
         }
         if (name.length() >0)
         {
            if (topPackage)
            {
               query =  "/XMI/XMI.content/UML:Project[@name=\""  + name +  "\"]";
               topPackage = false;
            }
            else
            {
               //query +=  "//UML:Element.ownedElement/*" ;
               query +=  "/UML:Element.ownedElement/*" ;
               query +=  "[@name=\""  + name +  "\"]" ;
            }
         }
         if (pos > 0)
         {
            curStr = curStr.substring( pos + 2 );
            pos = curStr.indexOf( "::" );
         }
         else
         {
            curStr = "";
         }
      }
      
      return query;
   }
   
   /**
    * Performs the passed-in query within the passed-in Namespace. If the query fails, (i.e.,
    * no elements are found), and the Namespace has imported elements (i.e., the Namespace is
    * QI'd to see if it supported the IPackage interface), the same query is run
    * on the imported elements (i.e., the imported elements are loaded and the query performed).
    * If the query fails here as well, the Namespace of the passed-in space is queried, and the
    * process starts all over. This ends once the query successfully finds elements that match, else
    * all imported elements and namespaces have been queried to no avail.
    *
    * @param space[in] The Namespace to begin the query in
    * @param query[in] An XPath query to perform
    * @param elements[out] Elements that matched the query, if any
    *
    * @return HRESULT
    * @note This can be an expensive call. For instance, if a query is performed on a Namespace that
    *       has numerous and large imported elements, those elements will be brought in to perform the
    *       search.
    */
   public ETList<IElement> findElementsByDeepQuery(INamespace space, String query)
   {
      ETList<IElement> retElems = null;
      ETList<INamedElement> elems = null;
      if (space instanceof IVersionableElement)
      {
         IVersionableElement vEle = (IVersionableElement)space;
         retElems = findElementsByQuery(vEle, query);
         if (retElems == null || retElems.size() == 0)
         {
            if (space instanceof IPackage)
            {
               IPackage pack = (IPackage)space;
               elems = pack.findTypeByNameInImports(query);
               
               CollectionTranslator<INamedElement,IElement> translator = new
               CollectionTranslator<INamedElement,IElement>();
               retElems = translator.copyCollection(elems);
            }
         }
      }
      
      return retElems;
   }
   
   public IElement resolveScopedElement(IVersionableElement context, String fullyScopedName)
   {
      IElement retEle = null;
      if (context instanceof INamespace)
      {
         INamespace proj = getProject(context);
         if (proj != null)
         {
            IElement curEle = proj;
            String[] strs = fullyScopedName.split("::");
            if (strs != null && strs.length > 0)
            {
               for (int i=0; i<strs.length; i++)
               {
                  String query = "";
                  if(i == 0)
                  {
                     query = "./*[@name=\"" + strs[i] + "\"]" ;
                  }
                  else
                  {
                     query += "UML:Element.ownedElement/*";
                     query += "[@name=\"" + strs[i] +  "\"]" ;
                  }
                  ETList<IElement> elems = findElementsByQuery(curEle, query);
                  IElement firstElement = null;
                  if (elems != null && elems.size() > 0)
                  {
                     firstElement = elems.get(0);
                  }
                  else
                  {
                     ETList<INamedElement> nElems = findByNameInMembersAndImports(proj, strs[i]);
                     if (nElems != null && nElems.size() > 0)
                     {
                        firstElement = (IElement)nElems.get(0);
                     }
                  }
                  if (firstElement != null)
                  {
                     curEle = firstElement;
                  }
                  else
                  {
                     INamedElement newEle = null;
                     if (i < strs.length-1)
                     {
                        newEle = createPackage(curEle, strs[i]);
                     }
                     else
                     {
                        ETList<INamedElement> els =
                        ((INamespace) curEle).getOwnedElementsByName(strs[i]);
                        newEle = els.size() > 0?
                        els.get(0)
                        : createUnknownType(curEle, strs[i]);
                     }
                     curEle = newEle;
                  }
               }
               if (curEle != null)
                  retEle = curEle;
            }
         }
      }
      return retEle;
   }
   
   /**
    *
    * Retrieves the Project interface this Element is a part of.
    *
    * @param pProj[out] The dispatch interface that really holds the IProject
    *
    * @return HRESULT
    *
    */
   private INamespace getProject(IVersionableElement vEle)
   {
      INamespace proj = null;
      Node n = vEle.getNode();
      if (n != null)
      {
         Document doc = n.getDocument();
         if (doc != null)
         {
            proj = UMLXMLManip.getProject(doc);
         }
      }
      return proj;
   }
   
   /**
    * Creates a new package element and adds it to a namespace.
    *
    * @param *nSpace [in] The element that will contains the new package.  The
    *                     element must be a namespace element.
    * @param name [in] The name of the package
    * @param pPackage [out] The new package element
    */
   private INamedElement createPackage(IElement space, String name)
   {
      INamedElement nEle = null;
      if (space != null && space instanceof INamespace)
      {
         INamespace nSpace = (INamespace)space;
         TypedFactoryRetriever < IPackage > fact = new TypedFactoryRetriever < IPackage >();
         IPackage pack = fact.createType("Package");
         
         if (pack != null)
         {
            pack.setName(name);
            nSpace.addOwnedElement(pack);
            nEle = (INamedElement)pack;
         }
      }
      return nEle;
   }
   
   /**
    * Creates an unknown data type element and adds it to a namespace.  The creation of
    * the unknown data types uses the user preferences to determine if an unknown data
    * type is to be created, and what type to create.
    *
    * @param *nSpace [in] The element that will contains the new package.  The
    *                     element must be a namespace element
    * @param name [in] The name of the package
    * @param pType [out] The new package element
    */
   private INamedElement createUnknownType(IElement space, String name)
   {
      INamedElement nEle = null;
      if (space != null && space instanceof INamespace)
      {
         INamespace nSpace = (INamespace)space;
         Node pNode = nSpace.getNode();
         if (pNode != null)
         {
            Document doc = pNode.getDocument();
            if (doc != null)
            {
               nEle = UMLXMLManip.resolveUnknownType(doc, name);
               if (nEle != null)
               {
                  nSpace.addOwnedElement(nEle);
               }
            }
         }
      }
      return nEle;
   }
   
   /**
    *
    * Retrieves the fully qualified name of the element specified by the passed in ID.
    *
    * @param node[in]            The node of the element to use to determine the name for.
    * @param qualifiedName[out]  The elment's fully qualified name in the form "parent::element name"
    *
    * @return HRESULT
    *
    */
   public String retrieveFullyQualifiedName(Node node)
   {
      return retrieveFullyQualifiedName(node, "::");
   }
   
   /**
    *
    * Retrieves the fully qualified name of the element specified by the passed in ID.
    *
    * @param node[in]            The node of the element to use to determine the name for.
    * @param delimiter[in]       The delimiter to use between namespaces.
    * @param qualifiedName[out]  The elment's fully qualified name in the form "parent::element name"
    *
    * @return
    *
    */
   public String retrieveFullyQualifiedName(Node node, String delimiter)
   {
      String retStr = "";
      TypedFactoryRetriever < INamedElement > fact = new TypedFactoryRetriever < INamedElement >();
      Object nEle = fact.createTypeAndFill(node);
      if (nEle != null)
      {
         if (nEle instanceof INamedElement)
         {
            INamedElement elem = (INamedElement)nEle;
            retStr = retrieveFullyQualifiedName(elem, delimiter);
         }
      }
      return retStr;
   }
   
   /**
    *
    * Retrieves the passed in Element's fully qualified name, delimited by the passed in string.
    *
    * @param pElement[in]        The element whose qualified name is needed
    * @param delimiter[in]       The delimiter to use between namespaces.
    * @param qualifiedName[out]  The fully qualified name
    *
    * @return HRESULT
    *
    */
   public String retrieveFullyQualifiedName(INamedElement elem, String delimiter)
   {
      String retStr = elem.getQualifiedName();
      if (retStr.length() > 0)
      {
         String delim = delimiter;
         if (!delim.equalsIgnoreCase("::"))
         {
            //retStr.replaceAll("::", delim);
            retStr = StringUtilities.replaceAllSubstrings(retStr, "::", delim);
         }
      }
      return retStr;
   }
   
   /**
    *
    * Given the XML node, creates the encapsulating ModelElement and passes it back.
    *
    * @param node[in] The XML node to wrap
    * @param element[out] The resultant IModelElement interface, else 0.
    *
    * @return HRESULTs
    */
   private Object populateElement(Node pNode)
   {
      Object obj = null;
      FactoryRetriever fact = FactoryRetriever.instance();
      if (fact != null)
      {
         obj = fact.createTypeAndFill(pNode.getName(), pNode);
      }
      return obj;
   }
}
