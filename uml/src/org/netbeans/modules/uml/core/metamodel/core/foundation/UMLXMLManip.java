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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Vector;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.w3c.dom.NamedNodeMap;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocumentFactory;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class UMLXMLManip
{
   public static final String PROJECT_DTD_FILE = "UML_2.0EMBT.dtd";
   private static boolean resolvingFullyQualifiedName = false;

   public static ETList < INamedElement > findByNameInDocument(Document doc, String name)
   {
      ETList < INamedElement > elems = null;
      if (doc != null)
      {
         List list = doc.selectNodes(name);
         if (list != null)
         {
            int size = list.size();
            elems = new ETArrayList < INamedElement > ();
            for (int i = 0; i < size; i++)
            {
               Node node = (Node)list.get(i);
               //Sumitabh set the named element using this node.
            }
         }
      }
      return elems;
   }

   /**
    *
    * Attempts to find a type by the passed in name in either the direct members of the
    * passed in namespace object, or in the imported elements contained by that namespace,
    * if that namespace happens to also be a package.
    *
    * @param space[in] The Namespace to search.
    * @param name[in] The name to match types against.
    * @param foundNodes[out] The found elements that match the passed in name, else the
    *								  collection is emtpy.
    *
    * @return HRESULTs
    */
   public static ETList < INamedElement > findByNameInMembersAndImports(INamespace space, String name)
   {
      ETList < INamedElement > elems = space.getOwnedElementsByName(name);
      if (elems == null || elems.size() == 0)
      {
         if (space instanceof IPackage)
         {
            IPackage pack = (IPackage)space;
            elems = pack.findTypeByNameInImports(name);
         }
      }
      return elems;
   }

   /**
    *
    * Makes sure that any nodes returning from the query that reference
    * external nodes are resolved. This wraps the DOM selectNodes() call.
    *
    * @param node[in] the node we are querying on. Needs to support the
    *              selectNodes() method.
    * @param query[in] the query to perform
    * @param nodeList[out] the resultant list.
    *
    * @return HRESULTs
    *
    */
   public static ETList < Node > selectNodes(Node node, String query)
   {
      ETList < Node > retVal = new ETArrayList < Node > ();

      // Ideally, I would like to wait before creating this collection,
      // but the code that was being used here before the refactor was
      // always returning a valid collection even if nothing was in it,
      // so that behavior must continue, at least for now

      List result = node.selectNodes(query);

      if (result != null)
      {
         FactoryRetriever fact = FactoryRetriever.instance();
         boolean isCloned = fact.isCloned(node);

         for (int index = 0; index < result.size(); index++)
         {
            Node nodeToResolve = null;
            if(result.get(index) instanceof Node)
            nodeToResolve = (Node)result.get(index);
            if (nodeToResolve != null)
            {
               Node resolved = null;
               if (isCloned == true)
               {
                  resolved = nodeToResolve;
               }
               else
               {
                  resolved = resolveNode(node, nodeToResolve);
               }

               if (resolved != null)
               {
                  retVal.add(resolved);
               }
            }
         }
      }

      return retVal;
   }

   /**
    * @param node
    * @param nodeToResolve
    * @return
    */
   protected static Node resolveNode(Node node, Node nodeToResolve)
   {
      Node retVal = null;

      FactoryRetriever fact = FactoryRetriever.instance();
      assert fact != null : "Failed to retrieve the factory retriever.";

      boolean isCloned = false;

      if (fact != null)
      {
         isCloned = fact.isCloned(nodeToResolve);
      }

      if (isCloned == true)
      {
         retVal = nodeToResolve;
      }
      else
      {
         String xmiID = getAttributeValue(nodeToResolve, "xmi.id");
         if (xmiID != null && xmiID.length() > 0)
         {
            Document doc = node.getDocument();
            boolean nodeInDocument = false;

            if ((doc == null) && (node instanceof Document))
            {
               doc = (Document)node;
               nodeInDocument = true;
            }

//            assert doc != null : "Failed to retrieve the Document.";

            if (doc != null)
            {
               // Now make sure that the node passed in is actually
               // part of a Project document. If not, we'll simply
               // go through the basic resolution mechanism as we
               // always have. The only time this should occur
               // is element / package imports, and the etup mechanism

               Node projNode = null;
               try // using ancestor in the XPath query can sometimes throw
                  {
                  projNode = node.selectSingleNode("ancestor::UML:Project");
               }
               catch (Exception e)
               {
               }
               if (projNode != null)
               {
                  retVal = findInProject(doc, xmiID);
               }
               else
               {
                  ExternalFileManager man = new ExternalFileManager();

                  // In the C++ code if the call to resolveExternalNode returns somethng
                  // other than S_OK the found node is released.  However, since we are
                  // not returning HRESULT in java we should return a NULL pointer if we
                  // failed to resolve the node.  Therefore, no need for the code to 
                  // release the found node.
                  retVal = ExternalFileManager.resolveExternalNode(nodeToResolve);

                  if ((retVal == null) && (nodeInDocument == false))
                  {
                     // Before trying to find in the Project, CreateTypeAndFill to 
                     // see if this is a TransitionElement   
                     TypedFactoryRetriever < IElement > ret = new TypedFactoryRetriever < IElement > ();
                     IElement element = ret.createTypeAndFill(node);
                     if (element != null)
                     {
                        if (element instanceof ITransitionElement)
                        {
                           retVal = nodeToResolve;
                        }
                        else
                        {
                           retVal = findInProject(doc, xmiID);
                        }
                     }
                  }
                  else if(retVal == null)
                  {
                     retVal = nodeToResolve;
                  }
               }
            }
         }
         else
         {
            retVal = nodeToResolve;
         }

      }

      return retVal;
   }

   /**
    *
    * This should be used instead of direct calls to the DOM method
    * selectSingleNode(). This method makes sure that any external references
    * are resolved.
    *
    * @param node the node to perform the selectSingleNode() on. Needs to
    *             support the selectSingeNode() method.
    * @param query the query string to pass to selectSingleNode();
    * @return the fully resolved node.
    *
    */
   public static Node selectSingleNode(Node pNode, String query)
   {
      Node n = null;

      try
      {
         n = selectSingleNodeByNode(pNode, query);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return n;
   }

   /**
   *
   * Retrieves a particular node based on a query that is passed in.
   * One large problem with how this code was before imported elements
   * was supported is that it wasn't making sure that imported elements
   * were getting properly parented. Now it is.
   *
   * @param node[in]         The node to perform the query on.
   * @param query[in]        The query
   * @param foundNode[out]   The found node, else 0
   *
   * @return HRESULT
   *
   */
   private static Node selectSingleNodeByNode(Node pNode, String query)
   {
      Node retNode = null;
      Node unresolved = XMLManip.selectSingleNode(pNode, query);
      if (unresolved != null)
      {
         retNode = resolveNode(pNode, unresolved);

         // A little hack: if pNode belongs to a transition element,
         // resolveNode() will not be able to resolve it, so until we
         // can figure out what's really supposed to be done in such cases,
         // this hack ought to hold things together.
         if (retNode == null)
            retNode = unresolved;
      }
      return retNode;
   }

   /**
    *
    * Retrieves all the specified nodes / children of the passed in node
    * element.
    *
    * @param node the actual XML node that we are querying. It is the
    *             children of this node that we are gathering.
    * @param nodeName actual names of the elements that will make up the
    *                 collection. This should be a simple XPath expression.
    * @param collClass the CLSID of the collection class to create and
    *                  return
    * @return The actual collection object that will house the
    *         individual elements.
    * @warning See NamedElementImpl::get_TaggedValue() for an example of how to use
    *    this method.
    *
    */
   public static < Type > ETList < Type > retrieveElementCollection(Node pNode, Type dummy, String query, Class c)
   {
      //      Type[] elems = null;
      //      try 
      //      {
      //         List list = XMLManip.selectNodeList(pNode, query);
      //         if (list != null && list.size() >0 )
      //         {
      //            int size = list.size();
      //            elems = new Type[size];
      //            for (int i=0; i<size; i++)
      //            {
      //               Node n = (Node)list.get(i);
      //               //Sumitabh create an IElement from this node and add to elems.
      //            }
      //         }
      //      } 
      //      catch (Exception e)
      //      {
      //      }
      //      return elems;
      ElementCollector < Type > collector = new ElementCollector < Type > ();
      return collector.retrieveElementCollection(pNode, query, c);
   }

   /**
    *
    * Given an XML document, retrieves the IProject element from it.
    *
    * @param doc[in] The XML document to search
    * @param proj[out] The IProject. Actually, and INamespace, which is the IProject
    *
    * @return HRESULT
    *
    */
   public static INamespace getProject(Node doc)
   {
      INamespace retEle = null;
      try
      {
         String str = "xmi.id";
         org.dom4j.Node n = getProjectNode(doc);
         if (n == null)
         {
            // Most likely, we have a versioned element. Get the VersionedElement element, which should have
            // the projectID attribute on it.
            n = XMLManip.selectSingleNode(doc, "VersionedElement");

            // It is still possible to have a NULL node in this case. This is most likely
            // due to a PackageImport not yet connected to the main Project.
            if (n != null)
            {
               str = "projectID";
            }
         }
         if (n != null)
         {
            String xmiID = XMLManip.getAttributeValue(n, str);
            if (xmiID.length() > 0)
            {
               // For some reason, we didn't find a project, so
               // just create a new element, and pass it back

               FactoryRetriever fact = FactoryRetriever.instance();
               if (fact != null)
               {
                  retEle = (INamespace)fact.retrieveObject(xmiID);
               }
            }
         }
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
      return retEle;
   }

   /**
    *
    * Retrieves the value of the XML attribute specified, converting the string
    * value into an integer.
    *
    * @param element[in] the element to pull the attribute off of.
    * @param attrName[in] the name of that attribute to retrieve, such as "name"
    * @param pVal[out] the value of the attribute if found. If the attribute does not exist,
    *                  the value returned will be false.
    *
    *  @return HRESULT
    *
    */
   public static String getAttributeValue(Node pNode, String str)
   {
      String retStr = "";
      if (pNode instanceof org.dom4j.Element)
      {
         org.dom4j.Element ele = (org.dom4j.Element)pNode;
         retStr = ele.attributeValue(str);
      }
      return retStr;
   }

   /**
    *
    * Checks to see if the user has asked us to automatically create
    * a specified type when we cannot resolve a type of the passed in
    * kind. If we are to create one, it is returned in foundNode
    *
    * @param doc[in] The document who will create the new node if
    *					   necessary.
    * @param curSpace[in] The namespace where the unknown type will be created, if necessary
    * @param typeName[in] The name of the type that was not found in
    *							  this project. If the preference file tells
    *						     us to create a new type, this will be that
    *							  types name.
    * @param foundNode[out] The new type, else 0.
    *
    * @return Various HRESULTs. This call will return HR_E_ELEMENT_NOT_ESTABLISHED
    *         if the document passed in is actually a fragment.
    *
    */
   public static INamedElement resolveUnknownType(org.dom4j.Document doc, INamespace space, String typeName)
   {
      INamedElement foundNode = null;
      boolean valid = validate(doc);
      if (valid)
      {
         // Now be sure to add the newly created type to the current
         // namespace. If there isn't a current namespace, then it
         // will be added to the Project. If we aren't actually
         // creating a new type ( 'cause we found a match in a 
         // reference library ) curSpace will own the ElementImport
         if (space == null)
         {
            space = getProject(doc);
         }

         if (space != null)
         {
            // First, have the TypeManager check the Project's reference
            // libraries. Maybe we'll find a match there
            ITypeManager typeMan = getTypeManager(doc);
            if (typeMan != null)
            {
               foundNode = typeMan.getElementFromLibrariesByName(typeName);
            }

            if (foundNode == null)
            {
               //foundNode = createUnknownType(space, typeName);
					foundNode = createAndAddUnknownType(space, typeName);
            }
            else
            {
               // We found an element from a reference library. Be sure to create
               // the ElementImport
               if (foundNode instanceof IAutonomousElement)
               {
                  IAutonomousElement aEle = (IAutonomousElement)foundNode;
                  MetaLayerRelationFactory fact = MetaLayerRelationFactory.instance();
                  IDirectedRelationship impRel = fact.createImport(space, aEle);
               }
            }
         }
      }
      return foundNode;
   }

   private static IPackage getPackage(INamespace space, String packageName)
   {
      if (space == null || packageName == null || packageName.length() == 0)
         return null;

      // First see if we already have the package.
      ETList < INamedElement > els = space.getOwnedElementsByName(packageName);
      if (els != null)
      {
         for (int i = 0; i < els.size(); ++i)
         {
            INamedElement e = els.get(i);
            if (e instanceof IPackage)
               return (IPackage)e;
         }
      }

      FactoryRetriever ret = FactoryRetriever.instance();
      Object obj = ret.createType("Package", null);
      if (obj instanceof IPackage)
      {
         IPackage p = (IPackage)obj;
         space.addOwnedElement(p);
         p.setName(packageName);

         return p;
      }
      return null;
   }

   private static INamedElement createUnknownType(INamespace space, String typeName)
   {
      INamedElement foundNode = null;
      // Need to check our preference to see if we should 
      // just create a node 'cause we couldn't find a classifier
      // with that name.
      IPreferenceAccessor acc = PreferenceAccessor.instance();
      boolean create = acc.getUnknownClassifierCreate();
      if (create)
      {
         if (typeName.indexOf("::") != -1)
         {
            // Assemble package structure
            ETList < String > packages = StringUtilities.splitOnDelimiter(typeName, "::");
            typeName = packages.get(packages.size() - 1);

            if (space instanceof IPackage)
            {
               for (int i = 0; i < packages.size() - 1; ++i)
                  space = getPackage(space, packages.get(i));
            }
         }

         ETList < INamedElement > nels = space.getOwnedElementsByName(typeName);
         if (nels.size() > 0)
            foundNode = nels.get(0);

         if (foundNode == null)
         {
            String createType = acc.getUnknownClassifierType();
            if (firePreUnknownCreate(createType))
            {
               FactoryRetriever ret = FactoryRetriever.instance();
               Object obj = ret.createType(createType, null);
               if (obj != null && obj instanceof INamedElement)
               {
                  INamedElement nEle = (INamedElement)obj;
                  // Add the element to its owned namespace here because the
                  // put_Name() below will fire events.
                  space.addOwnedElement(nEle);

                  // Set the name of the type to the name passed in
                  nEle.setName(typeName);
                  foundNode = nEle;
                  fireUnknownCreated(nEle);
               }
            }
         }
      }
      //			}
      //			else
      //			{
      //				// We found an element from a reference library. Be sure to create
      //				// the ElementImport
      //				if (foundNode instanceof IAutonomousElement)
      //				{
      //					IAutonomousElement aEle = (IAutonomousElement)foundNode;
      //					MetaLayerRelationFactory fact = MetaLayerRelationFactory.instance();
      //					IDirectedRelationship impRel = fact.createImport(space, aEle);
      //				}
      //			}
      //		}
      //	}
      return foundNode;
   }

   /**
    *
    * Attempts to find a single NamedElement in the context elements's namespace or above namespaces. If
    * more than one element is found with the same name, only the first one is used.
    *
    * @param contextElement[in]  The contextElement from which to search
    * @param typeName[in]        The name to match against
    * @param foundElement[out]   The found NamedElement, else 0.
    *
    * @return HRESULTs
    */
   public static INamedElement resolveSingleTypeFromString(IElement context, String typeName)
   {
      return resolveSingleTypeFromString(context, typeName, false);
   }
   
   public static INamedElement resolveSingleTypeFromString(IElement context, String typeName, boolean inOwner)
   {
      if (context == null || typeName == null || typeName.length() == 0)
         return null;

        // workaround for #6294177 - primitive types are being created with package specification
        // this workaround is Java language dependent, it should be removed and the problem should be handled in a more approprite way
        if (typeName.equals("int") || typeName.equals("char") || typeName.equals("byte") || 
            typeName.equals("short") || typeName.equals("long") || typeName.equals("float") ||
            typeName.equals("double") || typeName.equals("void") || typeName.equals("boolean")) {
                typeName = "::" + typeName;
        }
        // end of workaround
      
      resolvingFullyQualifiedName = true;
      INamedElement nel = null;
      try
      {
        nel = NameResolver.resolveFullyQualifiedName(context, typeName);
      }
      finally
      {
         resolvingFullyQualifiedName = false;
      }

      if (nel == null)
      {
         resolvingFullyQualifiedName = inOwner;
         try
         {
             ETList < INamedElement > foundEls = resolveTypeFromString(context, typeName);
             if (foundEls != null && foundEls.size() > 0)
             {
                // Currently, if there are more than one type found
                // ( i.e., malformed model ), we'll only grab the first
                // type
                nel = foundEls.get(0);
             }
         }
         finally
         {
            resolvingFullyQualifiedName = false;
         }
      }
      else if (NameResolver.typesResolvedExternally() && nel != null)
      {
         // The types were retrieved from an imported Project. Need to 
         // add those types to the type file
         // TODO:
      }
      return nel;
   }

   public static ETList < INamedElement > resolveTypeFromString(IElement context, String typeName)
   {
      if (context == null || typeName == null || typeName.length() == 0)
         return null;

      // Find the Classifier that makes up the type
      Document doc = getDocument(context);
      if (doc != null)
      {
         // We don't want to alter the namespace of the
         // incoming contextElement, if indeed that contextElement
         // is already a namespace, 'cause this will cause us 
         // to create unknown types in the wrong
         INamespace space = null;
         if (context instanceof INamespace)
         {
            space = (INamespace)context;
         }
         else
         {
            space = OwnerRetriever.getOwnerByType(context, INamespace.class);
            //            if(space == null)   
            //            {
            //               if (context instanceof INamespace)
            //               {
            //                  space = (INamespace)context;               
            //               }
            //            }
         }

         return resolveTypeFromString(context, doc, typeName, space);

      }
      return null;
   }

   private static Document getDocument(IElement el)
   {
      if (el == null)
         return null;

      Node n = el.getNode();
      if (n == null)
         return null;

      Document d = n.getDocument();
      if (d == null)
      {
         if (el instanceof ITransitionElement)
            return getDocument(((ITransitionElement)el).getFutureOwner());

         // If we're not a transition element, perhaps we're a child of a 
         // transition element?
         return getDocument(el.getOwner());
      }
      return d;
   }

   public static ETList < INamedElement > resolveTypeFromString(IElement context, Document doc, String typeName, INamespace space)
   {
      if (context == null || doc == null)
         return null;

      // Resolve the type of the passed in type in string
      // form. findByName will search the current namespace
      // and any imported packages, and then walk up the
      // owning namespaces until a match is found.

      // Added the false parameter at the end to prevent the creation of the 
      // unknown type if needed. If false is not passed and we find that the unknown 
      // type needs to be created, it would be created at the project level. We
      // don't want that in this case. We want to create in the namespace of the
      // contextElement
      boolean elementEstablished = true;
      ETList < INamedElement > nels = null;
      try
      {
         nels = UMLXMLManip.findByName(doc, typeName, space, true, false);
         // Since we are now creating datatypes under the project, we 
         // need to search from the project.
         if (nels == null || nels.size() == 0)
         {
             nels = UMLXMLManip.findByName(doc, typeName, space.getProject(), true, false);
         }
         
         if (nels == null || nels.size() == 0)
         {
            // Fix J1732:  We want to make sure elements are only created in packages.
            //             This fixes the issue we had with the lifeline representing
            //             classifiers being created under the interaction.
            IPackage creationSpace = ( space instanceof IPackage )
               ? (IPackage) space
               : (IPackage) OwnerRetriever.getOwnerByType(context, IPackage.class);
            
            if(creationSpace != null)
            {
               INamedElement namedElement = resolveUnknownType(doc, creationSpace, typeName);
               if(namedElement != null)
               {
                  if(nels == null)
                  {
                     nels = new ETArrayList < INamedElement >();                     
                  }
                  nels.add(namedElement);
               }
            }
         }
      }
      catch (ProjectMissingException e)
      {
         // In the C++ version findByName returns GR_S_ELEMENT_NOT_ESTABLISHED if
         // the project is not found in the document.  Since the Java version
         // returns the named elements (if they where found) we can not return 
         // the HRESULT GR_S_ELEMENT_NOT_ESTABLISHED if the project is not
         // set up.  So, the ProjectMissingException is thrown instead.  So the 
         // implementation that occurs when GR_S_ELEMENT_NOT_ESTABLISHED is return
         // is implemented inside of this catch statement.

         // This element has not been established. Let's see if this 
         // elemetn implements the ITransitionElement interface. If it
         // does, we can resolve the type with the element's doc on that
         // interface
         if (context instanceof ITransitionElement)
         {
            IElement futureOwner = ((ITransitionElement)context).getFutureOwner();

            if (futureOwner != null && futureOwner instanceof INamedElement)
            {
               Node n = futureOwner.getNode();
               Document d = n != null ? n.getDocument() : null;
               nels = resolveTypeFromString(futureOwner, d, typeName, ((INamedElement)futureOwner).getNamespace());
            }
         }
      }

      return nels;
   }

   public static INamedElement resolveUnknownType(Document doc, String typeName)
   {
      INamedElement nEle = null;
      if (doc != null)
      {
         if (validate(doc))
         {
            // Need to check our preference to see if we should 
            // just create a node 'cause we couldn't find a classifier
            // with that name.
            IPreferenceAccessor pPref = PreferenceAccessor.instance();
            boolean create = pPref.getUnknownClassifierCreate();
            if (create)
            {
               String createType = pPref.getUnknownClassifierType();
               if (firePreUnknownCreate(createType))
               {
                  FactoryRetriever fact = FactoryRetriever.instance();
                  Object obj = fact.createType(createType, null);
                  if (obj != null && obj instanceof INamedElement)
                  {
                     nEle = (INamedElement)obj;
                     nEle.setName(typeName);
                     fireUnknownCreated(nEle);
                  }
               }
            }
         }
      }
      return nEle;
   }

   private static void fireUnknownCreated(INamedElement nEle)
   {
      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementLifeTimeEventDispatcher disp = (IElementLifeTimeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("UnknownCreated");
         disp.fireUnknownCreated(nEle, payload);
      }
   }

   private static boolean firePreUnknownCreate(String createType)
   {
      boolean proceed = true;
      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementLifeTimeEventDispatcher disp = (IElementLifeTimeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("PreUnknownCreate");
         proceed = disp.firePreUnknownCreate(createType, payload);
      }
      return proceed;
   }


   private static org.dom4j.XPath projectNodeXPath;
   private static Object pnLock = new Object();

   public static Node getProjectNode(Node doc) 
   {
       if (doc == null) 
       {
	   return null;
       }
       synchronized(pnLock) 
       {
	   if (projectNodeXPath == null) 
	   {
	       DocumentFactory fact = DOMDocumentFactory.getInstance();
	       projectNodeXPath = fact.createXPath("/XMI/XMI.content/UML:Project");
	   }
       }	
       return projectNodeXPath.selectSingleNode(doc);
   }

   public static boolean validate(org.dom4j.Document doc)
   {
      boolean valid = false;
      if (doc != null)
      {
         Node n = getProjectNode(doc);
         if (n != null)
         {
            valid = true;
         }
      }
      return valid;
   }

   /**
    *
    * Sets the xml attribute passed in with the value passed in. Causes on element pre and
    * modified event to fire.
    *
    * @param element[in] The element being changed
    * @param attrName[in] The name of the xml attribute to set.
    * @param value[in] The value to use
    *
    * @return HRESULT
    *
    */
   public static void setAttributeValue(IVersionableElement elem, String name, String value)
   {
      Node node = elem.getNode();
      if (node != null)
      {
         boolean proceed = true;

         String curVal = getAttributeValue(node, name);
         IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();

         // Don't fire the event if the values are the same
         if ((curVal == null && value != null) || (curVal != null && !curVal.equals(value)))
         {
            proceed = fireElementPreModified(elem, helper);
         }

         if (proceed)
         {
            // We need to retrieve the node again here, as it is possible
            // that the XML node has been changed on the COM object sent in.
            // This can occur due to version control
            org.dom4j.Element currNode = elem.getElementNode();
            boolean wasModified = elem.verifyInMemoryStatus();
            XMLManip.setAttributeValue(currNode, name, value);

            if (helper != null)
            {
               helper.dispatchElementModified(elem);
            }
         }
      }
      else
      {
         throw new IllegalStateException("Element " + elem + " cannot have null " + "XML node (trying to set @" + name + " = '" + value + "'");
      }
   }

   /**
    *
    * Fires the ElementPreModified event
    *
    * @param element[in] The element about to be modified
    * @param help[out] The helper that can be used to dispatch the final modified
    *                  event.
    *
    * @return - true if it is ok to continue with the pending modification, else
    *         - false to cancel the change.
    *
    */
   public static boolean fireElementPreModified(IVersionableElement elem, IElementChangeDispatchHelper helper)
   {
      boolean proceed = true;
      //helper = new ElementChangeDispatchHelper();
      boolean fireEvents = helper.dispatchElementPreModified(elem);
      proceed = fireEvents ? true : false;
      return proceed;
   }

   /**
    *
    * Retrieves all the XML attribute elements that refer in some way to the passed in XMI ID
    *
    * @param context[in]   The node that contains the elements to retrieve
    * @param xmiID[in]     The id of the element to match against
    * @param elements[out] All XML attribute elements that reference the passed in ID
    *
    * @return HRESULT
    * @note The elements collection returned is filled with IXMLDOMAttribute objects
    *
    */
   public static List getAllAffectedElements(Node doc, String xmiid)
   {
      List retList = null;
      // This query checks every XML attribute to see if
      // the value of that XML attribute contains a string
      // that matches xmiID
      String query = "//@*[contains( ., '";
      query += xmiid;
      query += "')]";
      retList = XMLManip.selectNodeList(doc, query);
      return retList;
   }

   /**
    *
    * Removes all references to the passed in ID from any element referring to it
    *
    * @param context[in]   The node to fully query
    * @param xmiID[in]     The id to remove references to.
    *
    * @return HRESULT
    *
    */
   public static void cleanReferences(Node doc, String xmiid)
   {
      replaceReferences(doc, xmiid, "");
   }

   /**
    *
    * Replaces xmiID with replacementXMIID in any element referring to it
    *
    * @param context[in]            The node to fully query
    * @param xmiID[in]              The id to remove references to.
    * @param replacementXMIID[in]   The id to replace xmiID with
    *
    * @return HRESULT
    *
    */
   public static void replaceReferences(Node n, String xmiid, String replacementXMIID)
   {
      List list = getAllAffectedElements(n, xmiid);
      if (list != null)
      {
         for (Iterator iter = list.iterator(); iter.hasNext();)
         {
            Object obj = (Object)iter.next();
            if (obj instanceof org.dom4j.Attribute)
            {
               org.dom4j.Attribute attr = (org.dom4j.Attribute)obj;
               String strValue = attr.getValue();
               strValue = StringUtilities.replaceSubString(strValue, xmiid, replacementXMIID);
               attr.setValue(strValue);
            }
         }
      }
   }

   /**
    *
    * Generates the appropriate id to be used on any XML element.
    *
    * @param useUUID[in] generates a GUID to be used as the id, else
    *						a simple id in the form of "S.X" is created.
    *
    * @return HRESULTs
    *
    */
   public static String generateId(boolean useUUID)
   {
      String id = "";
      if (useUUID)
      {
         id = XMLManip.retrieveDCEID();
      }
      else
      {
         // For now, this is just a static count. This, needless to say,
         // needs to be enhanced.

         int iID = 0;

         String buffer = "";
         id = "S.";
         id += iID;

         iID++;
      }
      return id;
   }

   /**
    *
    * Retrieves the number of xml elements found as a result of the passed in query
    *
    * @param node[in] The node to query against
    * @param query[in] The actual query to perform
    * @param byID[in] true if the query is against an xml attribute, else false. If
    *                 true, then query is assumed to be the name of an XML attribute
    *                 whose value is a list of IDREFS
    * @param count[out] The number of elements returned from the query
    *
    * @return HRESULT
    *
    */
   public static long queryCount(Node node, String query, boolean byID)
   {
      long count = 0;
      if (byID)
      {
         if (node.getNodeType() == Node.ELEMENT_NODE)
         {
            org.dom4j.Element elem = (org.dom4j.Element)node;

            ElementCollector < Object > col = new ElementCollector < Object > ();
            count = col.numIDRefs(elem, query);
         }
      }
      else
      {
         ElementCollector < Object > col = new ElementCollector < Object > ();
         ETList < Node > list = col.selectNodes(node, query);
         count = list.size();
      }
      return count;
   }

   /**
    *
    * Adds the xmi.id of inElement the the IDREFS attribute passed in of element
    *
    * @param element[in] the element to whose XML attribute value we are
    *                   adding to
    * @param newElement[in] the element whose id we are adding.
    * @param attrName[in] that name of the XML attribute we are adding to.
    *
    * @return - S_OK
    *			  - GR_E_VERSIONABLEELEMENT_NOT_SUPPORTED: owner doesn't support
    *							the IVersionableElement interface.
    *
    */
   public static void addElementByID(IVersionableElement element, Object elem, String attrName)
   {
      if (elem instanceof IVersionableElement)
      {
         IVersionableElement ver = (IVersionableElement)elem;
         String id = ver.getVersionedURI();
         if (id != null && id.length() > 0)
         {
            // We have to make sure that the id itself contains
            // no spaces, as the IDREFS is a white space delimited
            // list
            Node elementNode = element.getNode();
            String tempVal = XMLManip.getAttributeValue(elementNode, attrName);
            id = URILocator.encodeURI(id);
            String ids = "";
            if (tempVal != null && tempVal.length() > 0)
            {
               // Make sure that the element isn't already listed here before adding. If
               // it is already present, do nothing
               int pos = tempVal.indexOf(id);
               if (pos < 0)
               {
                  ids = tempVal.trim() + " " + id;
               }
            }
            else
            {
               ids = id;
            }
            if (ids.length() > 0)
            {
               setAttributeValue(element, attrName, ids);
            }
         }
      }
   }

   /**
    *
    * Removes the id of the elementToRemove from the attribute value on element
    * that matches the attribute name passed in. This method does not
    * remove the element to be removed from element's attribute value.
    *
    * @param element[in] the element we are changing
    * @param elementToRemove[in] the id of this element is being removed
    * @param attrName[in] name of the XML attribute that contains the
    *                         IDREFS attribute value we are channging.
    *
    * @return HRESULTs
    *
    */
   public static void removeElementByID(IVersionableElement element, IVersionableElement elem, String attrName)
   {
      if (elem != null)
      {
         String id = elem.getXMIID();
         removeElementByID(id, element, attrName);
      }
   }

   /**
    *
    * Removes the element that has the passed in id from that attribute
    * of the passed in element.
    *
    * @param idToMatch[in] The ID to remove from the XML attribute value
    * @param element[in] The element whose XML attribute value will
    *							 be modified if this routine is successful.
    * @param attrName[in] The name of the XML attribute to affect.
    *
    * @return HRESULTs
    */
   public static void removeElementByID(String idToMatch, IVersionableElement element, String attrName)
   {
      Node node = element.getNode();
      String ids = getAttributeValue(node, attrName);
      if (ids != null && ids.length() > 0)
      {
         // All ids that have been added have already been encode,
         // so make sure the ID coming in is encoded
         idToMatch = URILocator.encodeURI(idToMatch);

         String str = removeElementFromString(idToMatch, ids);
         setAttributeValue(element, attrName, str.trim());

         //   		int pos = ids.indexOf(idToMatch);
         //   		if (pos >= 0 )
         //   		{
         //   			int start = pos + idToMatch.length();
         //   			int end = ids.length();
         //   			//remove idToMatch from ids and then setAttributeValue
         //   			String str = ids.substring(0, pos) + ids.substring(start, end);
         //   			setAttributeValue(element, attrName, str.trim());
         //   		}
      }
   }

   /**
    *
    * Removes the child from curNode that matches the id of the passed in
    * element.
    *
    * @param curNode[in] the node whose child we are removing.
    * @param elementToRemove[in] the element we are removing.
    *
    * @return HRESULTs
    *
    */
   public static void removeChild(Node node, IVersionableElement elem)
   {
      if (elem != null)
      {
         // Get the id of the passed in element
         String id = elem.getXMIID();
         String query = ".//*[@xmi.id=\"";
         query += id + "\"]";

         removeSingleChild(node, query);
      }
   }

   /**
    *
    * Removes the node that is a child of curNode that matches the passed
    * in query. If that node is associated with an external file ( versioning ),
    * that file will be removed as well as the node.
    *
    * @param curNode[in] the node whose child is to be removed
    * @param query[in] the query to match against.
    *
    * @return HRESULTs
    *
    */
   protected static void removeSingleChild(Node node, String query)
   {
      try
      {
         org.dom4j.Node n = XMLManip.selectSingleNode(node, query);
         removeNode(node, n, null);
      }
      catch (Exception e)
      {
      }
   }

   /**
    *
    * Checks to see if node is referencing an external file. If it is, that
    * file is also cleaned up.
    *
    * @param parent[in] the parent of the node that is being removed
    * @param node[in] the node to remove
    * @param ignored
    *
    * @return HRESULTs
    *
    */
   //private static void removeNode(Node parent, Node node, Node ignored)
   //{
   //	if (node != null)
   //	{
   //		// Check to see if this node has an external reference.
   //		String value = getAttributeValue(node, "isVersioned");
   //		if (value.length() > 0)
   //		{
   //			Node parentNode = node.getParentNode();
   //			ExternalFileManager.removeExternalNode(parentNode, node);
   //		}
   //		else
   //		{
   //			Node parentNode = node.getParentNode();
   //			if (parentNode != null)
   //			{
   //				parentNode.removeChild(node);
   //			}
   //		}
   //	}
   //}

   protected static void removeNode(Node parent, org.dom4j.Node node, Node ignored)
   {
      if (node != null)
      {
         // Check to see if this node has an external reference.
         String value = XMLManip.getAttributeValue(node, "isVersioned");
         if (value != null && value.length() > 0)
         {
            org.dom4j.Element parentNode = node.getParent();
            ExternalFileManager.removeExternalNode(parentNode, node);
         }
         else
         {
            org.dom4j.Element parentNode = node.getParent();
            if (parentNode != null)
            {
               node.detach();
               //parentNode.removeChild(node);
            }
         }
      }
   }

   /**
    *
    * Attempts to retrieve a node that matches the xmi.id attribute
    * value passed in. 
    *
    * @param node The document to search through
    * @param idToFind the value of the id to find.
    * @return The node that is found.
    *
    * @return HRESULTs
    *
    */
   public static Node findElementByID(Node node, String idToFind)
   {
      Node retNode = null;

      Document doc = null;
      if (node instanceof Document)
      {
         doc = (Document)node;
         validate(doc);
      }
      else
      {
         doc = node.getDocument();
      }

      //assert doc != null : "Unable to retrieve the document.";

      if (doc != null)
      {
         retNode = findInProject(doc, idToFind);
         if (retNode == null)
         {
            retNode = XMLManip.findElementByID(doc, idToFind);

            Node resolved = ExternalFileManager.resolveExternalNode(retNode);
            if (resolved != null)
            {
               retNode = null;
               retNode = resolved;
            }
         }

      }
      return retNode;
   }

   /**
    *
    * Retrieves the element by going through the owning Project's TypeManager. If the document
    * passed in doesn't house an IProject, false is returned.
    *
    * @param doc[in] The document to retrieve the IProject from.
    * @param id[in] The xmi id we are looking for.
    * @param foundNode[out] The found node, else 0.
    *
    * @return true if the document houses an IProject, else false
    *
    */
   protected static Node findInProject(Document doc, String idToFind)
   {
      Node retVal = null;

      ITypeManager tMan = getTypeManager(doc);
      if (tMan != null)
      {
         retVal = tMan.getRawElementByID(idToFind);
      }

      return retVal;
   }

   /**
    *
    * Retrieves the TypeManager if the passed in document actually houses
    * an IProject
    *
    * @param doc[in] The document
    * @param typeMan[out] The ITypeManager, disguised as an IDispatch
    *
    * @return HRESULT
    *
    */
   protected static ITypeManager getTypeManager(org.dom4j.Document doc)
   {
      ITypeManager retMan = null;
      INamespace space = getProject(doc);
      if (space != null && space instanceof IProject)
      {
         IProject proj = (IProject)space;
         retMan = proj.getTypeManager();
      }
      return retMan;
   }

   /**
    *
    * Retrieves the TypeManager associated with the passed in element.
    *
    * @param element[in]   The element to use as context
    * @param manager[out]  The found Manager
    *
    * @return HRESULT
    *
    */
   public static ITypeManager getTypeManager(IVersionableElement ver)
   {
      ITypeManager retMan = null;
      if (ver != null)
      {
         Node n = ver.getNode();
         if (n != null)
         {
            Document doc = n.getDocument();
            retMan = getTypeManager(doc);
         }
      }
      return retMan;
   }

   /**
    * 
    * Retrieves the element identified via xmi.id in the XML attribute
    * passed in.
    *
    * @param element[in] The element to search. This is the element
    *							 whose indicated XML attribute has the value
    *							 who contains the xmi.id of the element we need.
    * @param attrName[in] The XML attribute to retrieve the xmi.id of
    *							  the element we need.
    * @foundElement[out] The found element, else 0.
    *
    * @return HRESULTs
    *
    */
   public static < Type > Type retrieveSingleElementWithAttrID(org.dom4j.Element element, Type dummy, // Used only for type resolutions
   String attrName, Class c)
   {
      ElementCollector < Type > collector = new ElementCollector < Type > ();
      return collector.retrieveSingleElementWithAttrID(element, attrName, c);
   }

   /**
    *
    * Retrieves a collection of elements based on the value of that passed in
    * attribute. It is assumed that the attribute is defined as containing an
    * IDREFS value.
    *
    * @param element[in] the element whose XML attribute we are querying.
    * @param attrName[in] the name of the attribute
    * @param collClass[in] CLSID of the collection object
    * @param dummy[in] used just for type resolution.
    * @return the actual collection of elements.
    */
   public static < Type > ETList < Type > retrieveElementCollectionWithAttrIDs(org.dom4j.Element element, Type dummy, // Used only for type resolutions
   String attrName, Class c)
   {
      ElementCollector < Type > collector = new ElementCollector < Type > ();
      return collector.retrieveElementCollectionWithAttrIDs(element, attrName, c);
   }

   /**
    *
    * Retrieves a single child element of node.
    *
    * @param node[in] The node to search child elements from.
    * @param query[in] The query string to use. selectSingleNode()
    *						  is used internally, so use appropriately.
    * @param foundElement[out] The found element, else 0.
    *
    * @return HRESULTs
    *
    */
   public static < Type > Type retrieveSingleElement(Node node, Type dummy, // Used only for type resolutions
   String query, Class c)
   {
      ElementCollector < Type > collector = new ElementCollector < Type > ();
      return collector.retrieveSingleElement(node, query, c);
   }

   /**
    *
    * Finds an XML node based on the xmi.id that is passed in and then
    * creates the appropriate COM object that wraps that node.
    *
    * @param doc[in] the XML document to search
    * @param id[in]  the id of the element to find.
    * @param found[out] the found element, if any.
    *
    * @return HRESULTs.
    *
    */
   public static Object findAndFill(Document doc, String id)
   {
      Object retObj = null;

      // Find the element in the DOM that matches the
      // id of the presentation element
      Node node = findElementByID(doc, id);
      if (node != null)
      {
         // Found the node that is the element. Now
         // We need to create the correct COM object to wrap
         // this node. We just need to call the FactoryRetriever
         // CreateTypeAndFill
         FactoryRetriever ret = FactoryRetriever.instance();
         String name = XMLManip.retrieveSimpleName(node);
         retObj = ret.createTypeAndFill(name, node);
      }

      return retObj;
   }

   public static void setNodeTextValue(Node curNode, String query, String newVal, boolean useCData)
   {
      XMLManip.setNodeTextValue(curNode, query, newVal, useCData);
   }

   public static void setNodeTextValue(IVersionableElement elem, String query, String newVal, boolean useCData)
   {
      ElementChangeDispatchHelper help = new ElementChangeDispatchHelper();
      boolean proceed = true;
      proceed = help.dispatchElementPreModified(elem);

      if (proceed)
      {
         Node node = elem.getNode();
         setNodeTextValue(node, query, newVal, useCData);
         help.dispatchElementModified(elem);
      }
   }

   /**
    *
    * This routine returns the URI of the passed in node, if the node has been extracted
    * ( most likely due to version control ) from the Project's DOM. This routine
    * is smart in that if this node is encapsulated by an element that was extracted,
    * that element's URI is used to build this ones.
    *
    * @param node[in] The node to query.
    * @param isID[out] Will be set to true if the result returned is the XML id of the node
    *                  passed in. Otherwise, isID is set to false. When false, it can be
    *                  assumed that the result of this method call is a full blown URI.
    *
    * @return The URI of the passed in node. If it is determineds that this node
    *                 or any of its parents has not been extracted, then the XMI id of 
    *                 this node is returned.
    *
    */
   public static ETPairT < String, Boolean > getVersionedURI(Node node)
   {
      boolean isId = false;
      String versionedURI = "";
      if (node != null)
      {
         String href = XMLManip.getAttributeValue(node, "href");
         // Fix J554:  The bug found a case where the etd file had a bogus href value.
         //            By checking the size > 40 the issue is fixed.
         if (href != null && href.length() > 40)
         {
            versionedURI = makeRelativeURI(node, href, "");
         }
         else
         {
            // Check to see if we belong to an element that has been extracted.
            // The query below grabs the first parent that has an href attribute of any length.
            org.dom4j.Node parent = null;
            String xmiid = XMLManip.getAttributeValue(node, "xmi.id");

            // TODO: This will fail if node is not attached to a Document 
            //       proper. I'm not sure, though, that ignoring the XPath
            //       failure is the right approach here.
            try
            {
               parent = XMLManip.selectSingleNode(node, "ancestor::*[string-length( @href ) > 0][1]");
            }
            catch (Exception ignored)
            {
            }

            if (parent != null)
            {
               href = XMLManip.getAttributeValue(parent, "href");

               // Make sure the href doesn't have a '%' character in it. This 
               // indicates that we have an expansion variable in there. Currently,
               // and for the forseeable future, we don't want to use URI's for 
               // expansion variables as these usually indicate that we are 
               // referencing a type in a default library import
               if (href.length() > 0)
               {
                  int pos = href.indexOf("%");
                  if (pos >= 0)
                  {
                     // If there is an expansion variable, just return the 
                     // XMI.id
                     versionedURI = xmiid;
                     isId = true;
                  }
                  else
                  {
                     versionedURI = makeRelativeURI(node, href, xmiid);
                  }
               }
            }
            else
            {
               versionedURI = xmiid;
               isId = true;
            }
         }
      }
      return new ETPairT < String, Boolean > (versionedURI, new Boolean(isId));
   }

   /**
    *
    * Creates a relative URI string, based on this element's href attribute value
    * and the file location of this element's project.
    *
    * @param node[in] The node whose uri is getting built
    * @param href[in] The href of this element.
    * @param xmiID[in] The default is "". If this has length, a URI is built using
    *                  the document part of the passed in href and the passed in ID.
    *                  Otherwise, the href is just made relative and returned.
    *
    * @return The relative URI string
    *
    */
   protected static String makeRelativeURI(Node node, String href, String xmiid)
   {
      String uri = href;
      if (href.length() > 0)
      {
         ETPairT < String, String > obj = URILocator.uriparts(href);
         String nodeLoc = obj.getParamTwo();
         String docLoc = obj.getParamOne();
         String finalURI = retrieveRelativePath(node, docLoc);
         finalURI += "#";

         if (xmiid.length() > 0)
         {
            finalURI += "//*[@xmi.id=\"";
            finalURI += xmiid;
            finalURI += "\"]";
         }
         else
         {
            finalURI += nodeLoc;
         }
         uri = URILocator.decorateURI(finalURI);
      }
      return uri;
   }

   /**
    *
    * Retrieves a relative path from the Project that this element is in
    * and the path passed in.
    *
    * @param path[in] The path to make relative.
    *
    * @return HRESULT
    *
    */
   protected static String retrieveRelativePath(Node node, String path)
   {
      String finalPath = path;
      if (node != null && path.length() > 0)
      {
         Document doc = node.getDocument();
         if (doc != null)
         {
            INamespace space = getProject(node);
            if (space instanceof IProject)
            {
               IProject proj = (IProject)space;
               String projFileName = proj.getFileName();
               if (projFileName.length() > 0)
               {
                  finalPath = PathManip.retrieveRelativePath(path, projFileName);
               }
            }
         }
      }
      return finalPath;
   }

   /**
    *
    * This routine returns the URI of the passed in node, if the node has been extracted
    * ( most likely due to version control ) from the Project's DOM. This routine
    * is smart in that if this node is encapsulated by an element that was extracted,
    * that element's URI is used to build this ones.
    *
    * @param node[in] The node to query.
   
    * @return The URI of the passed in node. If it is determineds that this node
    *                 or any of its parents has not been extracted, then the XMI id of 
    *                 this node is returned.
    *
    */
   //	public static String getVersionedURI( Node node )
   //	{
   //	   boolean isID = false;
   //	   return getVersionedURI( node, isID );
   //	}

   /**
   * Retrieves a node by the "name" XML attribute.  A new datatype will be
   * created if the type is not found.
   *
   * @param doc[in] The document to search through
   * @param name[in] The name to match against
   * @param curSpace[in] The namespace making the search for the element
   * @param foundNode[out] The found node, else 0 if not found
   * @param typeResolution[in] true if this FindByName() call should only return
   *                           elements that support the IClassifier interface
   *
   * @return HRESULTs
   * @warning This method will validate the passed in document, looking for a Model
    *          element. Generally this is fine and desired, but if you know that
    *          you have a document that is a fragment or simply a document that
    *          does not have a model in it, call SelectNodes() manually.
   * @todo Log the case where FindByName() returns more than one
   *       element.
   */
   public static ETList < INamedElement > findByName(Document doc, 
                                                     String typeName, 
                                                     INamespace space, 
                                                     boolean typeResolution)
   throws ProjectMissingException
   {
      return findByName(doc, typeName, space, typeResolution, true);
   }

   /**
    * Retrieves a node by the "name" XML attribute.
    *
    * @param doc[in] The document to search through
    * @param name[in] The name to match against
    * @param curSpace[in] The namespace making the search for the element
    * @param foundNode[out] The found node, else 0 if not found
    * @param typeResolution[in] true if this FindByName() call should only return
    *                           elements that support the IClassifier interface
    * @param createUnknown Determins if a datatype will be created if the type
    *                      is not found.
    *
    * @throws ProjectMissingException When the project is missing from the document.
    * @warning This method will validate the passed in document, looking for a Model
    *          element. Generally this is fine and desired, but if you know that
    *          you have a document that is a fragment or simply a document that
    *          does not have a model in it, call SelectNodes() manually.
    * @todo Log the case where FindByName() returns more than one
    *       element.
    */
   public static ETList < INamedElement > findByName(Document doc, 
                                                     String typeName, 
                                                     INamespace space, 
                                                     boolean typeResolution, 
                                                     boolean createUnknown) 
      throws ProjectMissingException
   {
      ETList < INamedElement > retElems = null;
      boolean valid = validate(doc);
      if (valid)
      {
         if (space != null)
         {
            retElems = findByNameInMembersAndImports(space, typeName);
         }
         else if (typeResolution)
         {
            ITypeManager manager = getTypeManager(doc);
            if (manager != null)
               retElems = manager.getLocalCachedTypesByName(typeName);
         }
         else
         {
            retElems = findByNameInDocument(doc, typeName);
         }

         // Even if we get a collection back, we need to make sure that
         // the collection actually contains an element.		
         if (typeResolution)
         {
            // We only want IClassifier types
            if (retElems != null && retElems.size() > 0)
            {
               ETList < INamedElement > newElements = null;
               // We only want IClassifier types
               int count = retElems.size();
               for (int i = 0; i < count; i++)
               {
                  INamedElement elem = retElems.get(i);
                  if (elem instanceof IClassifier)
                  {
                     if (newElements == null)
                        newElements = new ETArrayList < INamedElement > ();
                     newElements.add(elem);
                  }
               }
               retElems = newElements;
            }
         }

         if (retElems == null || retElems.size() == 0)
         {
            // First check to see if we need to crawl up in the
            // namespaces and check for the element in that namespace.
            // If we are at the top, then we need to check the 
            // PreferenceManager
            if (space != null)
            {
               INamespace parSpace = space.getNamespace();
               retElems = findByName(doc, typeName, parSpace, typeResolution, createUnknown);
            }
            else if (createUnknown == true)
            {
               INamedElement unkType = resolveUnknownType(doc, space, typeName);
               if (unkType != null && (retElems == null || retElems.size() == 0))
               {
                  retElems = new ETArrayList < INamedElement > ();
                  retElems.add(unkType);
               }
            }
         }
      }
      else
      {
         throw new ProjectMissingException();
      }
      return retElems;
   }

   /**
    *
    * Transforms the passed in element into a type specified in typeName
    *
    * @param element[in] The element to transform
    * @param typeName[in] The name of the type to transform to.
    * @param newObject[out] The new object
    *
    * @return HRESULT
    * @warning element should not be used it has been passed into this method
    *
    */
   public static Object transformElement(IVersionableElement element, String typeName)
   {
      Object retObj = null;
      String xmiid = element.getXMIID();
      FactoryRetriever ret = FactoryRetriever.instance();

      ret.removeObject(xmiid);

      // Now set the name of the node and create a new COM object that
      // wraps the node
      String nodeName = "UML:" + typeName;
      Node node = element.getNode();
      if (node != null)
      {
         Document doc = node.getDocument();
         if (doc != null)
         {
            Node newNode = doc.getRootElement().addElement(nodeName);
            copyAttributes(node, newNode);
            copyChildren(node, newNode);
            retObj = ret.createTypeAndFill(typeName, newNode);
            if (retObj != null)
            {
               // Remove m_Node from the document, and add newNode
               // in its place
               org.dom4j.Element parent = node.getParent();
               if (parent != null)
               {
                  node.detach();
                  newNode.detach();
                  parent.add(newNode);
               }

               // Now replace the node of the old element with the new node.
               // The client of this call should really discard the old ( incoming /
               // object that was just transformed ) COM object and use the returned
               // object, but in case they haven't, at least the data is correct.
               element.setNode(newNode);

            }
         }
      }

      // Notify all the presentation elements of the transformation
      if (retObj instanceof IElement)
      {
         IElement elem = (IElement)retObj;
         ETList < IPresentationElement > pElems = elem.getPresentationElements();
         if (pElems != null)
         {
            int count = pElems.size();
            for (int i = 0; i < count; i++)
            {
               IPresentationElement pEle = pElems.get(i);
               IPresentationElement pNewPE = pEle.transform(typeName);
               // 87522
               IGraphPresentation pres = TypeConversions.getETElement(pNewPE);
               if (pres != null)
                   pres.setModelElement(elem);
            }
         }
      }

      return retObj;
   }

   /**
    *
    * Removes the children from curNode to destNode
    *
    * @param curNode The node providing the child nodes
    * @param destNode The node receiving the child nodes
    *
    * @return HRESULT
    *
    */
   protected static void copyChildren(Node node, Node destNode)
   {
      if ((node instanceof Branch) && (destNode instanceof Branch))
      {
         Branch fromBranch = (Branch)node;
         Branch toBranch = (Branch)destNode;

         toBranch.appendContent(fromBranch);
         fromBranch.clearContent();
      }
   }

   /**
    *
    * Removes the attributes from curNode and puts them on destNode.
    *
    * @param curNode The node providing the attributes
    * @param destNode The node receiving the attributes
    */
   protected static void copyAttributes(Node curNode, Node destNode)
   {
      if ((curNode instanceof org.dom4j.Element) && (destNode instanceof org.dom4j.Element))
      {
         org.dom4j.Element fromElement = (org.dom4j.Element)curNode;
         org.dom4j.Element toElement = (org.dom4j.Element)destNode;

         toElement.appendAttributes(fromElement);

         // Remove the attributes from the current node.
         int max = fromElement.attributeCount();
         for (int index = max - 1; index >= 0; index--)
         {
            fromElement.remove(fromElement.attribute(index));
         }
      }

   }

   //	/////////////////////////////////////////////////////////////////////////////
   //
   //	 HRESULT EnsureElementExists( IXMLDOMNode* curNode,
   //											   const xstring& name, 
   //											   const xstring& query, 
   //											   IXMLDOMNode** node )
   //
   //	 Makes sure that the node with the passed in name is present
   //	 under curNode. If it isn't, one is created.
   //
   //	 INPUT:
   //		curNode  - the node to append to.
   //		name     - name of the node to check for existence for. 
   //		query    - the query string to used to check for existence
   //
   //	 OUTPUT:
   //		node  - the node representing the element
   //
   //	 RETURN:
   //		HRESULTs
   //
   //	 CAVEAT:
   //		None.
   //
   //	/////////////////////////////////////////////////////////////////////////////
   public static Node ensureElementExists(Node node, String name, String query)
   {
      Node n = null;
      if (node != null)
      {
         n = node.selectSingleNode(query);
         if (n == null)
         {
            // Node doesn't exist, so we need to create it.
            Document doc = node.getDocument();
            try
            {
               if (doc != null)
               {
                  n = XMLManip.createElement((Element)node, name, "");
               }
            }
            catch (Exception exc)
            {
               exc.printStackTrace();
            }
            //			else if (node != null && node instanceof Element)
            //			{
            //				//we might be adding a child to a child as in case of adding Region node to
            //				//statemachine node, while statemachine node is still not added to parent document.
            //				n = ((Element)node).addElement(name);
            //				((Element) node).addText("\n"); //$NON-NLS-1$
            //				n.detach();
            //				((Element) node).add(n);
            //			}
         }
      }
      return n;
   }

   /**
    * @param element
    * @param name
    * @param val
    */
   public static void setAttributeValue(IVersionableElement element, String name, int val)
   {
      String value = Integer.toString(val);
      setAttributeValue(element, name, value);
   }

   /**
    *
    * Replaces all occurrences of curElement's xmi id with a URI version of
    * the ID the passed in element. If curElement is not versioned,
    * nothing is done.
    *
    * @param curElement[in] The element who owns internalElement.
    * @param internalElement[in] The element being change.
    *
    * @return HRESULT
    *
    */
   public static boolean resolveInternalNode(IVersionableElement curElement, IVersionableElement internalEle)
   {
      boolean versioned = false;
      if (curElement != null && internalEle != null)
      {
         boolean isVersioned = false;
         isVersioned = curElement.isVersioned();
         String uri = curElement.getVersionedURI();
         String xmiid = curElement.getXMIID();

         if (!isVersioned)
         {
            // Need to check to see if curElement is owned by any element
            // that is versioned. We can easily tell this if the URI retrieved
            // from the get_VersionedURI matches the xmiID, the element is
            // not contained by a versioned element.
            if (!xmiid.equals(uri))
            {
               isVersioned = true;
            }
         }

         if (isVersioned && !xmiid.equals(uri))
         {
            versioned = true;
            Node internalNode = internalEle.getNode();

            if (internalNode != null)
            {
               if (xmiid.length() > 0)
               {
                  String localQuery = ".//@*[ not( name() = 'xmi.id' ) and not( name() = 'href' ) and contains( ., '";
                  localQuery += xmiid;
                  localQuery += "')]";

                  List elements = internalNode.selectNodes(localQuery);
                  if (elements != null)
                  {
                     resolveNodes(elements, xmiid, uri);
                  }
               }
            }
         }
      }
      return versioned;
   }

   /**
    *
    * Performs the actual modification of nodes refering to xmiID
    *
    * @param elements[in] The collection of nodes that need to be modified
    * @param xmiID[in] The XMI id that is being decorated with a URI
    * @param relPath[in] The relative path of the parent node
    *
    * @return HRESULT
    *
    */
   private static void resolveNodes(List elements, String xmiid, String uri)
   {
      int count = elements.size();
      if (count > 0)
      {
         String strId = xmiid;
         String strUri = uri;
         strUri += "#//*[@xmi.id=\"";
         strUri += strId;
         strUri += "\"]";

         String actualUri = URILocator.decodeURI(strUri);
         resolveNodes2(elements, strId, strUri);
      }
   }

   /**
    *
    * For all of the XML Attributes passed in elements, every value that contains xmiID
    * is replaced with uri
    *
    * @param elements[in] The collection of IXMLDOMAttributes
    * @param xmiID[in] The XMI ID to match against.
    * @param uri[in] The uri to replace xmiID matches with.
    *
    * @return HRESULT
    *
    */
   private static void resolveNodes2(List elements, String xmiId, String uri)
   {
      int count = elements.size();
      if (count > 0)
      {
         for (int i = 0; i < count; i++)
         {
            Node temp = (Node)elements.get(i);
            if (temp instanceof org.dom4j.Attribute)
            {
               org.dom4j.Attribute attr = (org.dom4j.Attribute)temp;
               String value = attr.getValue();

               boolean proceed = isResolutionNeeded(value, xmiId);
               if (proceed)
               {
                  int pos = value.indexOf(xmiId);
                  if (pos >= 0)
                  {
                     //value.replaceFirst(xmiId, uri);
                     StringBuffer buffer = new StringBuffer(value);
                     buffer.replace(pos, pos + uri.length(), uri);
                     value = buffer.toString();

                     attr.setValue(value);
                  }
               }
            }
         }
      }
   }

   /**
    *
    * Used to figure out whether or not an xml attribute value that contains the xmiID
    * value in it actually needs to be decorated in a uri or not. You don't want to 
    * transform the xmiID if it is already part of an href like format.
    *
    * @param strValue[in] The value to check
    * @param xmiID[in] The xmiID we are matching against
    *
    * @return true if the resolution needs to take place, else false.
    *
    */
   private static boolean isResolutionNeeded(String value, String xmiId)
   {
      boolean needed = true;
      if (!value.equals(xmiId))
      {
         // First check to see if we have an IDREFS field ( which is white space delimited ).
         // If we do, we need to check each token
         StringTokenizer tokenizer = new StringTokenizer(value, " ");
         needed = false;
         while (tokenizer.hasMoreTokens())
         {
            String token = tokenizer.nextToken();
            if (token.equals(xmiId))
            {
               needed = true;
               break;
            }
         }
      }
      return needed;
   }

   /**
    *
    * Appends the child element to the parent.
    *
    * @param parent[in] the XML node we are appending to.
    * @param element[in] an element that implements the IVersionableElement
    *                interface.
    *
    * @return HRESULTs
    *
    */
   public static void appendChild(Node parent, Object element)
   {
      if (element instanceof IVersionableElement)
      {
         IVersionableElement ver = (IVersionableElement)element;
         Node node = ver.getNode();
         if (node != null)
         {
            XMLManip.appendNewLineElement(parent, null);

            // If the node already belonged to somebody else, detach it.
            node.detach();
            ((org.dom4j.Element)parent).add(node);
            //				node.setParent((org.dom4j.Element)parent);
         }
      }
   }

   /**
    * Inserts a child node to the left of the specified node or at the end of the list.
    *
    * @param pParent [in] the XML node we are inserting into.
    * @param pNewElement [in] an element that implements the IVersionableElement interface.
    * @param pRefElement [in] the element to insert the new element before.  If NULL the new element is appended
    *
    * @return HRESULT
    */
   public static void insertBefore(Node parent, Object newEle, IVersionableElement refEle)
   {
      if (newEle instanceof IVersionableElement)
      {
         IVersionableElement newVer = (IVersionableElement)newEle;
         Node newNode = newVer.getNode();

         if (newNode == null)
            return;

         Node refNode = refEle != null ? refEle.getNode() : null;
         Element parentEl = (Element)parent;

         newNode.detach();

         List list = parentEl.elements();
         // dom4j guarantees that changes to 'List' will be reflected in the
         // underlying element.
         int pos;
         if (refNode == null || (pos = list.indexOf(refNode)) == -1)
            list.add(newNode);
         else
            list.add(pos, newNode);
      }
   }

   /**
    *
    * Determines if childToTest is an immediate child node of parent
    *
    * @param parent[in]          The parent node
    * @param childToTest[in]     The node to see if it is a child of parent
    *
    * @return - true if childToTest is a child of parent, else 
    *         - false
    *
    */
   public static boolean isDirectChild(Node parent, Node childTestNode)
   {
      return isChild(parent, childTestNode, true);
   }

   /**
    *
    * Determines if childToTest is an immediate child of parent
    *
    * @param parent[in]          The parent
    * @param childToTest[in]     The element to see if it is a child of parent
    *
    * @return - true if childToTest is a child of parent, else 
    *         - false
    *
    */
   public static boolean isDirectChild(IVersionableElement parent, IVersionableElement childToTest)
   {
      boolean isChild = false;
      if (parent != null && childToTest != null)
      {
         Node parNode = parent.getNode();
         Node childNode = childToTest.getNode();
         isChild = isDirectChild(parNode, childNode);
      }
      return isChild;
   }

   /**
    *
    * Determines whether or not childToTest is a child of parent.
    *
    * @param parent[in]          The parent to test against
    * @param childToTest[in]     The node to see if parent is its parent
    *
    * @return  - true if childToTest is indeed a child, else
    *          - false if childToTest is not a child of parent.
    *
    */
   public static boolean isChild(IVersionableElement parent, IVersionableElement childToTest)
   {
      boolean isChild = false;
      if (parent != null && childToTest != null)
      {
         Node parNode = parent.getNode();
         Node childNode = childToTest.getNode();
         isChild = isChild(parNode, childNode, false);
      }
      return isChild;
   }

   /**
    *
    * Determines whether or not childToTest is a child of parent.
    *
    * @param parent[in]          The parent to test against
    * @param childToTest[in]     The node to see if parent is its parent
    * @param checkForDirect[in]  - true if only interested if childToTest is an immediate child, else
    *                            - false if we don't care at what level childToTest is a child, just that
    *                              it is or isn't a child.
    *
    * @return  - true if childToTest is indeed a child, else
    *          - false if childToTest is not a child of parent.
    *
    */
   public static boolean isChild(Node parent, Node childToTest, boolean checkForDirect)
   {
      boolean isChild = false;
      if (parent != null && childToTest != null)
      {
         Node parNode = childToTest.getParent();
         String xmiid = getAttributeValue(parent, "xmi.id");

         boolean done = false;
         while (parNode != null && !done)
         {
            String parNodeXmiid = getAttributeValue(parNode, "xmi.id");

            // Need to look for an element that has the xmi.id attribute. If it has one,
            // check to see if we have the same parentNode.
            if (parNodeXmiid != null && parNodeXmiid.length() > 0)
            {
               if (checkForDirect)
               {
                  done = true;
               }
               else
               {
                  parNode = parNode.getParent();
               }

               if (xmiid.equals(parNodeXmiid))
               {
                  isChild = true;
                  done = true;
               }
            }
            else
            {
               parNode = parNode.getParent();
            }
         }
      }
      return isChild;
   }

   /**
    *
    * Retrieves the actual XMI ID, even if decorated within a URI.
    *
    * @param id[in] The id coming in that may or may not 
    *
    * @return 
    *
    */
   public static String retrieveRawID(String uri)
   {
      return URILocator.retrieveRawID(uri);
   }

   /**
    * Adds the xmi.id of inElement the the IDREFS attribute passed in of element
    *
    * @param element[in] the element to whose XML attribute value we are
    *                   adding to
    * @param attrName[in] that name of the XML attribute we are adding to.
    * @param newElement[in] the element whose id we are adding.
    * @param refElement[in] the element whose id we add the new id before.
    *
    * @return - S_OK
    *			  - GR_E_VERSIONABLEELEMENT_NOT_SUPPORTED: element doesn't support
    *							the IVersionableElement interface.
    */
   public static void insertElementBeforeByID(IVersionableElement curElement, String attrName, IVersionableElement newElement, Object refElement)
   {
      if (newElement != null)
      {
         // Get the id of the passed in element
         String newId = newElement.getVersionedURI();
         String refId = null;
         if (refElement instanceof IVersionableElement)
         {
            refId = ((IVersionableElement)refElement).getVersionedURI();
         }

         if (newId != null && newId.length() > 0)
         {
            // We have to make sure that the id itself contains
            // no spaces, as the IDREFS is a white space delimited
            // list
            newId = URILocator.encodeURI(newId);
            Node eleNode = curElement.getNode();
            String origIds = getAttributeValue(eleNode, attrName);

            String bsNewIds = null;
            if (origIds != null && origIds.length() > 0)
            {
               refId = URILocator.encodeURI(refId);
               if (refId != null && refId.length() > 0)
               {
                  String[] slTokens = null;
                  StringTokenizer tokenizer = new StringTokenizer(origIds, " ");
                  if (tokenizer.hasMoreTokens())
                  {
                     boolean needToInsert = true;
                     int numTokens = tokenizer.countTokens();
                     slTokens = new String[numTokens];
                     int i = 0;
                     while (tokenizer.hasMoreElements())
                     {
                        String token = tokenizer.nextToken();
                        if (token.equals(refId))
                        {
                           slTokens[i] = token;
                           i++;
                           bsNewIds = StringUtilities.join(slTokens, " ");
                           needToInsert = false;
                        }
                     }

                     if (needToInsert)
                     {
                        bsNewIds = origIds.trim();
                        bsNewIds += " ";
                        bsNewIds += newId;
                     }
                  }
                  else
                  {
                     bsNewIds = newId;
                  }
               }
               else
               {
                  bsNewIds = origIds.trim();
                  bsNewIds += " ";
                  bsNewIds += newId;
               }
            }
            else
            {
               bsNewIds = newId;
            }
            setAttributeValue(curElement, attrName, bsNewIds);
         }
      }
   }

   /**
    *
    * Cruises through the entire document, first finding nodes that have the
    * "isVersioned" XML attribute. This denotes a node that references an external
    * document, used for versioning. This element must have another attribute,
    * "href", which references the external file. That file will be persisted
    * relative to the location of the main document. For easy resolution later,
    * this method replaces that relative path to an absolute path based
    * on fileName. This is a temporary change and is not persisted.
    *
    * @param fileName[in] name of the file that contains the main document
    * @param node[in]     node to change versioned elements that are children
    *
    * @return HRESULTs
    *
    */
   public static String convertRelativeHrefs(String fileName, IVersionableElement ver)
   {
      if (ver != null)
      {
         Node node = ver.getNode();
         if (node != null)
         {
            String name = node.getName();
            List nodeList = node.selectNodes(".//*[@isVersioned]");
            if (nodeList != null)
            {
               int count = nodeList.size();
               if (count == 0)
               {
                  // Let's check to see if the node passed in has the isVersioned
                  // attribute. If it does, convert
                  String value = XMLManip.getAttributeValue(node, "isVersioned");
                  if (value != null && value.length() > 0)
                  {
                     convertRelativeHrefsOnNode(fileName, node);
                  }
               }
               for (int i = 0; i < count; i++)
               {
                  Node innerNode = (Node)nodeList.get(i);
                  convertRelativeHrefsOnNode(fileName, innerNode);
               }
            }
         }
      }
      return null;
   }

   /**
    *
    * Converts the relative HREF value on this not to an absolute
    * path.
    *
    * @param fileName[in] name of the file that contains the main document
    * @param node[in] node to change versioned elements that are children
    *
    * @return HRESULTs
    */
   private static void convertRelativeHrefsOnNode(String fileName, Node node)
   {
      if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
      {
         Element element = (Element)node;
         String curVal = getAttributeValue(element, "href");
         if (curVal != null && curVal.length() > 0)
         {
            curVal = FileSysManip.resolveVariableExpansion(curVal);
            ETPairT < String, String > result = URILocator.uriparts(curVal);
            String docLoc = result.getParamOne();
            String nodeLoc = result.getParamTwo();

            // If the href is a URI decorated href, then we don't want to 
            // convert it, as that is handled by the ExternalFileManager
            if (docLoc == null || docLoc.length() == 0 || nodeLoc == null || nodeLoc.length() == 0)
            {
               String path = FileSysManip.retrieveAbsolutePath(docLoc, fileName);
               XMLManip.setAttributeValue(element, "href", path);
            }
         }
      }
   }

   /**
    *
    * Cruises through the entire document, first finding nodes that have the
    * "isVersioned" XML attribute. This denotes a node that references an external
    * document, used for versioning. This element must have another attribute,
    * "href", which references the external file. That file is absolute if dealing
    * with a project currently in memory. This method makes sure to make the
    * external reference relative once again.
    *
    * @param fileName[in] name of the file that contains the main document
    * @param node[in] node to change versioned elements that are children
    *
    * @return HRESULTs
    *
    */
   public static void convertAbsoluteHrefs(org.dom4j.Element element, String fileName)
   {
      List nodeList = element.selectNodes("//*[@href]");
      if (nodeList != null)
      {
         int count = nodeList.size();
         for (int i = 0; i < count; i++)
         {
            Node node = (Node)nodeList.get(i);
            if (node instanceof org.dom4j.Element)
            {
               org.dom4j.Element ele = (org.dom4j.Element)node;
               String value = getAttributeValue(ele, "href");
               if (value != null && value.length() > 0)
               {
                  // In C++ the PathManip.retrieveRelativePath() seems to handle the path
                  // when there is a # extension in the path name.  However Java does not.
                  // So, we remove the # and its associated text then add it back when done.
                  
                  String strPoundName = null;
                  int pos = value.indexOf( "#" );
                  if( pos >= 0 )
                  {
                     strPoundName = value.substring( pos );
                     value = value.substring( 0, pos );
                  }
                  
                  String file = PathManip.retrieveRelativePath(value, fileName);
                  
                  if( strPoundName != null )
                  {
                     file += strPoundName;
                  }
                  
                  XMLManip.setAttributeValue(ele, "href", file);
               }
            }
         }
      }
   }

   /**
    * Makes sure that the required DTD file exists in the location of the 
    * project file. If the DTD file does not exist at that location, it will
    * be created.
    *
    * @author aztec
    * @param fileName The absolute file name of the project file.
    * @return true if the DTD file exists, else false if the file could
    *         not be created for some reason.
    */
   public static boolean verifyDTDExistence(String fileName)
   {
      String curFile = StringUtilities.getPath(fileName);
      curFile += PROJECT_DTD_FILE;

      boolean exists = new File(curFile).exists();
      if (!exists)
      {
         // Load the DTD from our resource file
         ICoreProduct prod = ProductRetriever.retrieveProduct();
         if (prod != null)
         {
            IConfigManager configManager = prod.getConfigManager();
            if (configManager != null)
            {
               String homeLocation = configManager.getDefaultConfigLocation();
               if (homeLocation != null && homeLocation.length() > 0)
               {
                  File dtdLocation = new File(homeLocation, PROJECT_DTD_FILE);
                  exists = copyFile(dtdLocation.toString(), curFile);
               }
            }
         }
      }
      return exists;
   }

   /**
    * Copies file from source to target.
    * 
    * @param source The source file name.
    * @param target The target file name.
    * @return <code>true</code> if the file was successfully copied.
    */
   public static boolean copyFile(String source, String target)
   {
      boolean bCopy = false;
      int bytesRead = 0;
      int bufferSize = 8000;

      try
      {
         File sourceFile = new File(source);
         File targetFile = new File(target);
         if (sourceFile != null && targetFile != null)
         {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));

            if (in.available() < bufferSize)
               bufferSize = in.available();

            byte[] buf = new byte[bufferSize];
            while (in.available() != 0)
            {
               bytesRead = in.read(buf, 0, bufferSize);
               out.write(buf, 0, bytesRead);
            }
            out.flush();

            in.close();
            out.close();
            bCopy = true;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return bCopy;
   }

   /**
    * @param resHandle
    * @param id
    * @return
    */
   public static String retrieveXMLFragmentFromResource(int resHandle, String id)
   {
      // TODO: Implement
      return null;
   }

   /**
    * Retrieves the contents of a resource specified by the given Class and 
    * relative resource path. Do <em>not</em> use for large resources.
    * 
    * @param resourceRoot A Class instance that can be used to source the
    *                     resource.
    * @param resourcePath The path of the resource, relative to the package
    *                     in which resourceRoot exists.
    * @return A <code>String</code> with the contents of the resource.
    */
   public static String retrieveXMLFragmentFromResource(Class resourceRoot, String resourcePath) throws IOException
   {
      InputStream is = resourceRoot.getResourceAsStream(resourcePath);
      BufferedInputStream bufis = new BufferedInputStream(is);
      int bufsize = 4000;
      if (bufsize > bufis.available())
         bufsize = bufis.available();
      byte[] buf = new byte[bufsize];
      StringBuffer out = new StringBuffer();
      if (bufis.available() > 0)
      {
         int read = bufis.read(buf, 0, bufsize);

         // Argh! Messy, but what else to do?
         out.append(new String(buf));
      }
      is.close();

      return out.toString();
   }

   /**
    * Retrieves an XML fragment (or anything else, for that matter) from the
    * foundation resource bundle, given the resource key. Just a fancy wrapper
    * around <code>FoundationMessages.getString()</code>.
    * 
    * @param resourceKey The resource key for the XML fragment to be retrieved.
    * @return A <code>String</code> containing the XML fragment.
    */
   public static String retrieveXMLFragmentFromResource(String resourceKey)
   {
      return FoundationMessages.getString(resourceKey);
   }

   /**
    *
    * Removes a string from within another
    *
    * @param strToMatch[in] The string to search
    * @param strToRemoveFrom[in] The string to remove from strToMatch
    *
    * @return the changed string
    * 
    */
   public static String removeElementFromString(String strToMatch, String strToRemoveFrom)
   {
      // The "presentation" attribute is an IDREFS, which is 
      // an XML attribute that is white-space delimited.
      String finalStr = "";
      String tempStr = null;
      ETList < String > tokens = StringUtilities.splitOnDelimiter(strToRemoveFrom, " ");
      for (int i = 0; i < tokens.size(); i++)
      {
         tempStr = tokens.get(i);
         if (tempStr != null)
         {
            if (tempStr.indexOf(strToMatch) != -1)
            {
               // We just skip over this
            }
            else
            {
               if (finalStr.length() > 0)
               {
                  // only append space if finalStr already has some text in it.
                  finalStr += " ";
               }
               finalStr += tempStr;
            }
         }
      }
      return finalStr;
   }

   /**
    * Creates a new type of the given name, dependent on the "Unknown Classifier" preference. If created, the
    * type is added to the passed in namespace
    * 
    * @param curSpace[in]  The namespace to add the new type to
    * @param typeName[in]  The name of the new type
    * @param newType[out]  The newly created type, else 0
    * 
    * @return S_FALSE if the preference is set to NOT create unknown types, else the typical HRESULTs
    */
   public static INamedElement createAndAddUnknownType(INamespace curSpace, String typeName)
   {
      INamedElement retEle = null;

      ETList < INamedElement > nels = curSpace.getOwnedElementsByName(typeName);
      if (nels.size() > 0)
         retEle = nels.get(0);

      // Need to check our preference to see if we should 
      // just create a node 'cause we couldn't find a classifier
      // with that name.
      IPreferenceAccessor pPref = PreferenceAccessor.instance();
      boolean create = pPref.getUnknownClassifierCreate();
      if (retEle == null && create)
      {
         String createType = pPref.getUnknownClassifierType();
         if (firePreUnknownCreate(createType))
         {
            FactoryRetriever ret = FactoryRetriever.instance();
            Object unk = ret.createType(createType, null);
            if (unk != null && unk instanceof INamedElement)
            {
               // Add the element to its owned namespace here because the
               // put_Name() below will fire events.
               INamedElement element = (INamedElement)unk;
               // this logic is flawed, totally ignores namespace specified, 
               // commented out as part of fix for 85895
//               if((resolvingFullyQualifiedName == false) &&
//                  (element instanceof IDataType))
//               {
//                   IProject project = curSpace.getProject();
//                   curSpace = project;
//               }
               curSpace.addOwnedElement(element);               
               //element.setOwner(curSpace);

               // Set the name of the type to the name passed in
               element.setName(typeName);
               
//               if (typeName.equals(element.getName())) {
//				   // <Sheryl> #6320801, we have already set the owner to current
//				   // workspace, why do we need to set to null here? Setting the 
//				   // owner to null will place the classifer to default package
////				   element.setOwner(null);
//                   curSpace.addOwnedElement(element);
//                   retEle = element;
//                   fireUnknownCreated(element);
//               }
               retEle = element;
               fireUnknownCreated(element);
            }
         }
      }

      return retEle;
   }
   
//   /**
//    * Searches under the given element for an element with the xmi.id supplied.
//    * 
//    * @param branch The element to search under (Document or Element).
//    * @param id     The xmi.id of the element to look for.
//    * @return The Element if found, or null.
//    */
//   public static org.dom4j.Element findElementByID(Branch parent, String id)
//   {
//      org.dom4j.Element retVal = null;
//      
////       if (branch == null || id == null)
////       	return null;
////		Element pElement = branch.elementByID(id);
////// For performance, move the chec to DefaultDocument
//////		if (pElement != null && pElement.getDocument() == null)
//////		{
//////       	String pattern = ".//*[@xmi.id=\"" + id + "\"]";
//////			pElement = (Element)selectSingleNode(branch, pattern);
//////		}
//      if(parent != null)
//      {
//         Document doc = parent.getDocument();
//         if((doc == null) && (doc instanceof Document))
//         {
//            doc = (Document)parent;
//         }
//         
//         if(doc != null)
//         {
//            retVal = findInProject(doc, id);
//            if(retVal == null)
//            {
//               retVal = doc.elementByID(id);
//               
//               ExternalFileManager man = new ExternalFileManager();
//               Node foundNode = man.resolveExternalNode(retVal);
//               if(foundNode instanceof Element)
//               {
//                  retVal = (Element)foundNode;
//               }
//            }
//         }
//      }
//		
//		return pElement;
//   }
}
