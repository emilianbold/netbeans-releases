/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.util.ArrayList;
import java.util.MissingResourceException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
import org.netbeans.modules.uml.core.metamodel.profiles.ProfileManager;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.CollectionType;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.openide.util.NbBundle;


public class Element extends BaseElement implements IElement
{

   /**
   * Retrieves the type of this element. For example, 'Class'.
   *
   * @param pVal The type of this element in string form
   *
   * @return HRESULTs
   */
   public String getElementType()
   {
      return retrieveSimpleName(getNode());
   }

   /**
    * Retrieves the name of the element typically used for creating icons.
    * It is composed of the element type and any other information needed to
    * make this type unique, such as 'Class' or 'PseudoState_Interface'
    *
    * The default implementation of this routine just returns the element type.
    *
    * @param pVal The icon type of this element in string form
    *
    * @return HRESULTs
    */
   public String getExpandedElementType()
   {
      return getElementType();
   }
   
   /**
    * Retrieves a element type name that can be displayed to the user.  The 
    * default implementation will try to turn the expanded element type into
    * a name that can be displayed.
    */
   public String getDisplayElementType()
   {
       String retVal = getExpandedElementType();
       try
       {
           retVal = NbBundle.getMessage(Element.class, retVal);
       }
       catch (MissingResourceException e)
       {
           // Since the goal of using a resource file is not to allow
           // the model element name to be translated (they have
           // been marked with NOI18N) but to put space in the element
           // names that are really to words.  For example 
           // CombinedFragment.  Therefore there will be some that
           // are missing.  
           //
           // So simply use the model elements name.
       }
       
       return retVal;
   }

   /**
    *
    * Adds the passed-in element to this namespace.
    *
    * @param newMember[in] The element to add
    *
    * @return S_OK, else NR_E_INVALID_MEMBER if a parent is being added
    *         as a member of a child.
    *
    */
   public IElement addElement(IElement element)
   {
      final IElement elem = element;
      if (validateMember(elem))
      {
         addChildAndConnect(false, "UML:Element.ownedElement", "UML:Element.ownedElement", elem, new IBackPointer < IElement > ()
         {
            public void execute(IElement obj)
            {
               elem.setOwner(obj);
            }
         });
      }

      //		//Sometimes we had to create a transition element to prevent events going through roundtrip
      //		//if we are in one of those elements, we should at this point remove the transition element
      //		//from the in-Memory, so that next time anyone asks for this element a proper model element
      //		//can be created and returned. This will happen for Attribute, Operation and Parameter.
      //		if (element instanceof ITransitionElement)
      //		{
      //			String xmiid = element.getXMIID();
      //			if (xmiid != null && xmiid.length() > 0)
      //			{
      //				FactoryRetriever ret = FactoryRetriever.instance();
      //				ret.removeObject(xmiid);
      //			}
      //		}
      return elem;
   }

   /**
    *
    * Makes sure that the element that is about to be added to this Element
    * is not an owner of this element, i.e., we will not allow the parent
    * or owning element to be placed into the owned elements list of a child
    * or owned element.
    *
    * @param newMember[in] The element to check
    *
    * @return true when it is valid to add the input element as an owned element.
    *
    */
   private boolean validateMember(IElement elem)
   {
      // Sun issue 5082597:  On the C++ side this code would throw ER_E_INVALID_MEMBER
      // when elem is an ancestor of this element.  This exception was caught in
      // NestedLinkPresentation.reconnectLink().  There was an additional error here,
      // because the isSame() check was not being made, which would cause the DOM
      // to throw.
      // So, we fix these two problems here by just making all the checks below.
      
      return (!isSame(elem) && !isOwnedElement( elem ) && !isParent(elem));
   }

   /**
    *
    * Checks to see if the passed in element is a parent
    *
    * @param element[in] The elemnet to check
    *
    * @return true if the element is a parent namespace, somewhere in the
    *         owning chain. false if it is not
    *
    */
   private boolean isParent(IElement elem)
   {
      boolean isParent = false;
      if (elem != null)
      {
         IElement parent = getOwner();
         while (parent != null)
         {
            if (parent != null)
            {
               if (parent.isSame(elem))
               {
                  isParent = true;
                  break;
               }
               else
               {
                  parent = parent.getOwner();
               }
            }
         }
      }
      return isParent;
   }

   /**
     *
     * Retrieves the element(s) that has the name that matches elementName.
     *
     * @param elementName[in] The name to match against.
     * @param elements[out] The elements found.
     *
     * @return HRESULTs
     *
     */
   public ETList < INamedElement > getOwnedElementsByName(String elemName)
   {
      String query = "./UML:Element.ownedElement/*[@name=\"";
      query += elemName;
      query += "\"]";
      INamedElement dummy = null;
      //use this query to retrieve the collection
      return retrieveElementCollection(dummy, query, INamedElement.class);
   }

   /**
    *
    * Removes the passed-in Element from this Element.
    *
    * @param element[in] The Element to remove
    *
    * @return HRESULTs
    */
   public IElement removeElement(IElement elem)
   {
      String query = "UML:Element.ownedElement/*[@xmi.id=\"";
      query += elem.getXMIID() + "\"]";
      return removeElement(elem, query);
   }

   /**
    *
    * Retrieves the elements owned by this Element.
    *
    * @param pVal[out] The collection of found elements
    *
    * @return HRESULTs
    *
    */
   public ETList < IElement > getElements()
   {
      //IElement dummy = null;
      String query = "UML:Element.ownedElement/*";
      return retrieveElementCollection(null, query, IElement.class);
   }

   /**
    *
    * Retrieves the Element that owns this element. This method
    * is smart in that it will determine the owner of an element that doesn't
    * have a owner attribute directly set. For instance, an Operation on a
    * Classifier does not have a owner attribute on it, but the Classifier
    * that it is in does. It is that Namespace that is returned in that case.
    *
    * @param owner[out]  the element, else 0.
    *
    * @return HRESULTs
    *
    */
   public IElement getOwner()
   {
      IElement owner = null;

      // First, let's see if the node has the "owner" attribute. If
      // it does, we'll use that to find the node, as that is the most
      // exact way to do it. If not, we'll crawl up the DOM tree.
      org.dom4j.Element elem = getElementNode();

      if (elem != null)
      {
         String ownerID = UMLXMLManip.getAttributeValue(elem, "owner");
         if (ownerID != null && ownerID.length() > 0)
         {
            owner = queryForOwner(elem, ownerID);
         }
         else
         {
            owner = walkTreeForOwner(elem);
         }
      }
      if (owner == null)
      {
         // Let's check to see if we are a Transition element
         if (getAggregator() instanceof ITransitionElement)
         {
            ITransitionElement transEle = (ITransitionElement) getAggregator();
            IElement futureOwner = transEle.getFutureOwner();
            if (futureOwner != null)
            {
               owner = futureOwner;
            }
         }
      }
      return owner;
   }

   /**
    * Traverses up the DOM tree, looking for the element that owns
    * element.
    *
    * @param element[in] the element whose owner we are trying to find.
    * @param space[out] the found element, else 0.
    *
    * @return HRESULTs
    *
    */
   private IElement walkTreeForOwner(org.dom4j.Element elem)
   {
      IElement owner = null;
      boolean done = false;
      String query = "UML:Element.ownedElement";
      Node temp = elem;
      while (!done && temp != null)
      {
         Node parent = temp.getParent();
         if (parent != null)
         {
            // Check for the UML:Element.ownedElement node
            Node node = UMLXMLManip.selectSingleNode(parent, query);
            if (node != null)
            {
               owner = buildOwner(parent);

               // OK, we found the node. Now verify that the
               // element's id is part of the ownedElements
               if (owner != null)
               {
						boolean isMember = owner.isOwnedElement(this);
                  if (isMember)
                  {
                     done = true;
                  }
               }
            }
            temp = parent;
         }
         else
         {
            done = true;
         }
      }
      return owner;
   }

   /**
      *
      * Retrieves the owner of this element that is also a Package element.
      * Retrieves the first element going up the owning hierarchy. For example,
      * if the owning hierarchy looks like A::B::C::D, with D being this element,
      * C being a Class element, and B being a Package ( or a package derived )
      * element, B would be returned.
      *
      * @param pVal[out] The found Package owner
      *
      * @return HRESULT
      * @see OwnerRetriever::GetOwnerByType()
      *
      */
   private IElement queryForOwner(org.dom4j.Element elem, String ownerID)
   {
      IElement retEle = null;
      Document doc = elem.getDocument();
      if (doc != null)
      {
         if (ownerID != null && ownerID.length() > 0)
         {
            // FindElementByID will return GR_S_ELEMENT_NOT_ESTABLISHED when the element
            // in question has not been added to the DOM tree that holds the rest of the
            // project elements. It is currently in "limbo" until it is added. We
            // don't want to propogate this out in this call as an error.
            Node node = UMLXMLManip.findElementByID(doc, ownerID);
            if (node != null)
            {
               retEle = buildOwner(node);
            }
         }
      }
      return retEle;
   }

   /**
    * Creates an Element object that encapsulates the node passed in.
    *
    * @param node[in] the DOM node to wrap in a Element object.
    * @param space[out] the IElement interface holding node.
    *
    * @return HRESULTs
    *
    */
   private IElement buildOwner(Node node)
   {
      Object obj = FactoryRetriever.instance().createTypeAndFill(retrieveSimpleName(node), node);
      return obj instanceof IElement ? (IElement) obj : null;
   }

   /**
      *
      * Sets the owner of this Element. Will remove this element from
      * its current owner if connected.
      *
      * @param owner[in] The new owner of this Element
      *
      * @return HRESULT
      *
      */
   public void setOwner(IElement owner)
   {
      setSingleElementAndConnect(owner, "owner", new IBackPointer < IElement > ()
      {
         public void execute(IElement own)
         {
            own.addElement(Element.this);
         }
      }, new IBackPointer < IElement > ()
      {
         public void execute(IElement own)
         {
            own.removeElement(Element.this);
         }
      });
   }

   /**
    *
    * Retrieves the owner of this element that is also a Package element.
    * Retrieves the first element going up the owning hierarchy. For example,
    * if the owning hierarchy looks like A::B::C::D, with D being this element,
    * C being a Class element, and B being a Package ( or a package derived )
    * element, B would be returned.
    *
    * @param pVal[out] The found Package owner
    *
    * @return HRESULT
    * @see OwnerRetriever::GetOwnerByType()
    *
    */
   public IPackage getOwningPackage()
   {
      return OwnerRetriever.getOwnerByType(this, IPackage.class);
   }

   /**
    *
    * Determines whether or not an element with the passed-in ID is owned
    * by this Element.
    *
    * @param elementID[in] The unique identifier of the element we are trying to
    *                  determine if is owned by this element
    * @param found[out] - true if the element is owned by this element, else
    *                   - false
    *
    * @return HRESULTs
    *
    */
   public boolean isOwnedElement(String id)
   {
      Node node = UMLXMLManip.findElementByID(m_Node, id);
		// We found a node in the doc by the id(), but is his owner us?
     return node != null && getXMIID().equals(UMLXMLManip.getAttributeValue(node, "owner"));
   }

   public boolean isOwnedElement(IElement elem)
   {
       // First check to see if the doc is in the same document first. If it isn't in the same
      // doc, then it certainly isn't owned by this element
      return sameDocument(elem) && isOwnedElement(elem.getXMIID());
   }

   /**
    *
    * Checks to see if the passed in element is connected to the
    * same XML document that this Element is.
    *
    * @param element[in] The element to check
    *
    * @return true if the document is the same, else false if it is not.
    *
    */
   private boolean sameDocument(IVersionableElement elem)
   {
      if (elem instanceof IElement)
      {
         IElement element = (IElement) elem;
         if (element.getProject() != null)
         {
            // InSameProject will return true if any of the two
            // elements have not been established in a project
            // document yet. That's why we check to see if element
            // has a Project before even proceeding. If the element
            // is NOT currently part of a Project, then we want
            // SameDocument to return false.
            return this.inSameProject(element);
         }
      }
      return false;
   }

   /**
    *
    * Adds a source Flow to this Element.
    *
    * @param flow[in] The new Flow to connect this element to
    *
    * @return HRESULT
    *
    */
   public void addSourceFlow(IFlow flow)
   {
      final IFlow curFlow = flow;
      addChildAndConnect(true, "sourceFlow", "sourceFlow", flow, new IBackPointer < IDirectedRelationship > ()
      {
         public void execute(IDirectedRelationship obj)
         {
            curFlow.addSource(obj);
         }
      });
   }

   /**
    *
    * Removes the passed-in Flow from this Element.
    *
    * @param flow[in] The Flow to remove
    *
    * @return HRESULT
    *
    */
   public void removeSourceFlow(IFlow flow)
   {
      removeElementByID(flow, "sourceFlow");
   }
   /**
    *  Retrieves the collection of source Flow relationships.
    *      HRESULT SourceFlows([out, retval] IFlows* *pVal);
    */
   public ETList < IFlow > getSourceFlows()
   {
      IFlow dummy = null;
      return retrieveElementCollectionWithAttrIDs(dummy, "sourceFlow", IFlow.class);
   }

   /**
    *
    * Adds a target Flow to this Element.
    *
    * @param flow[in] The target Flow
    *
    * @return HRESULT
    *
    */
   public void addTargetFlow(IFlow flow)
   {
      final IFlow curFlow = flow;
      addChildAndConnect(true, "targetFlow", "targetFlow", flow, new IBackPointer < IDirectedRelationship > ()
      {
         public void execute(IDirectedRelationship obj)
         {
            curFlow.addTarget(obj);
         }
      });
   }

   /**
    *
    * Removes the passed-in Flow from this Element.
    *
    * @param flow[in] The Flow to remove
    *
    * @return HRESULT
    *
    */
   public void removeTargetFlow(IFlow flow)
   {
      removeElementByID(flow, "targetFlow");
   }

   /**
    *
    * Retrieves the collection of target Flows this element is associated with.
    *
    * @param pVal[out] The collection of Flow objects
    *
    * @return HRESULT
    *
    */
   public ETList < IFlow > getTargetFlows()
   {
      IFlow dummy = null;
      return retrieveElementCollectionWithAttrIDs(dummy, "targetFlow", IFlow.class);
   }

   /**
    *  The set of TaggedValues that are associated with this Element. Standard tags are not included ( e.g., documentation )
    *      HRESULT TaggedValues([out, retval] ITaggedValues** pVal);
    */
   public ETList < ITaggedValue > getTaggedValues()
   {
      ITaggedValue dummy = null;
      return retrieveElementCollection(dummy, "UML:Element.ownedElement/UML:TaggedValue[not(@name='documentation') and ( not(@hidden = 'true' ))]", ITaggedValue.class);
   }

   public String getTaggedValuesAsString()
   {
       StringBuffer retVal = new StringBuffer();
       ETList < ITaggedValue > values = getTaggedValues();
       
       for(ITaggedValue value : values)
       {
           if(retVal.length() > 0)
           {
               retVal.append(",");
           }
           retVal.append(value.getNameWithAlias());
           retVal.append("=");
           retVal.append(value.getDataValue());
       }
       
       return retVal.toString();
   }
   
   public List<String> getTaggedValuesAsList()
   {
       ETList < ITaggedValue > values = getTaggedValues();
       List<String> retList = new ArrayList<String>();
       
       // create a list of Strings of "name=value" 
       for(ITaggedValue value : values)
       {
           String newVal = value.getNameWithAlias();
           newVal += ("=");
           newVal += value.getDataValue();
           retList.add(newVal);
       }
       return retList;
   }
   
   /**
    *
    * Adds a new TaggedValue to this element based on the name of the tag
    * and the value passed in.
    *
    * @param tagName[in] The name of the tag to add
    * @param value[in] The value of the new tag to add
    * @param pVal[out] The newly created TaggedValue
    *
    * @return HRESULT
    *
    */
   public ITaggedValue addTaggedValue(String tagName, String value)
   {
      ITaggedValue retVal = null;
      FactoryRetriever ret = FactoryRetriever.instance();

      // Create the actual COM tagged value through
      // the creation factory
      Object obj = ret.createType("TaggedValue", null);
      if (obj instanceof ITaggedValue)
      {
         ITaggedValue tag = (ITaggedValue) obj;
         addElement(tag);
         tag.populate(tagName, value);
         retVal = tag;
      }
      return retVal;
   }

   /**
    *
    * Removes the passed-in tag from this Element.
    *
    * @param tag[in] The TaggedValue to remove
    *
    * @return HRESULT
    *
    */
   public void removeTaggedValue(ITaggedValue tag)
   {
      removeElement(tag);
   }

   /**
    *
    * Retrieves the TaggedValue associated with this Element that matches the passed
    * in name.
    *
    * @param tagName[in] Name of the tag to find
    * @param tag[out] The tagged value if found
    *
    * @return HRESULT
    *
    * @warning If there are multiple tags with the same name, the first one will be
    *          returned.
    * @see ElementImpl::TaggedValuesByName()
    *
    */
   public ITaggedValue getTaggedValueByName(String tagName)
   {
      ETList < ITaggedValue > vals = getTaggedValuesByName(tagName);
      return vals != null && vals.size() > 0 ? vals.get(0) : null;
   }

   /**
    *
    * Retrieves all the tagged values of the passed in name.
    *
    * @param tagName[in] The name to match against
    * @param tags[out] The collection of found elements
    *
    * @return HRESULT
    *
    */
   public ETList < ITaggedValue > getTaggedValuesByName(String tagName)
   {
      String query = "UML:Element.ownedElement/UML:TaggedValue[@name=\"";
      if (tagName != null)
      {
         query += tagName;
      }
      query += "\"]";

      ITaggedValue dummy = null;
		ETList < ITaggedValue > values = UMLXMLManip.retrieveElementCollection(m_Node, dummy, query, ITaggedValue.class);

      ETList < ITaggedValue > vals = null;
      if (values != null)
      {
         vals = new ETArrayList < ITaggedValue > ();
         for (int i = 0; i < values.size(); i++)
         {
            vals.add((ITaggedValue) values.get(i));
         }
      }
      return vals;
   }

   /**
    *
    * Retrieves the documentation property of this Element. The Documentation
    * property is really just a TaggedValue with the name of "documentation".
    *
    * @param docs[out] The value of the documentation tag
    *
    * @return HRESULT
    *
    */
   public String getDocumentation()
   {
      String docs = null;
      ITaggedValue tag = getTaggedValueByName("documentation");
      if (tag != null)
      {
         // The documentation tag should always just be a string value
         docs = tag.getDataValue();
      }
      return (docs != null) ? docs : "";
   }

   /**
    *
    * Sets the documentation property of this Element.
    *
    * @param docs[in] The new documentation value
    *
    * @return HRESULT
    * @see get_Documentation()
    *
    */
   public void setDocumentation(String doc)
   {
      ITaggedValue tag = getTaggedValueByName("documentation");

      // Dispatch the Documentation modified events only if there is actually a
      // change to report
      
      boolean proceed = true;
      boolean fireEvents = true;
      if (tag != null)
      {
         String curDocs = tag.getDataValue();
         if (curDocs.equals(doc))
         {
            fireEvents = false;
         }
      }
      else
      {
         // If the docs coming in is 0 or has not length AND
         // we don't currently have a documentation tagged value,
         // then there is no need to send out a documentation modified event
         if (doc == null || doc.length() == 0)
         {
            fireEvents = false;
         }
         else
         	{
			doc = doc.replaceAll("       "," ");
			doc = doc.replaceAll("     "," ");
         	}
      }

      IElementChangeDispatchHelper helper = null;
      if (fireEvents)
      {
         helper = new ElementChangeDispatchHelper();
         
         proceed = false;
         if(helper.dispatchElementPreModified(this));
         {
         proceed = helper.dispatchDocPreModified(this, doc);
      }
      }

      if (proceed)
      {
         // Retrieve the node again since VCS may have changed the node on me.
         tag = getTaggedValueByName("documentation");
         if (tag != null)
         {
            tag.populate("documentation", doc);
         }
         else
         {
            // Only create the new tagged value if there is actually length
            // to the documentation value
            if (doc != null && doc.length() > 0 && !doc.equals("\n"))
            {
               tag = addTaggedValue("documentation", doc);
            }
         }

         if (fireEvents)
         {
            helper.dispatchDocModified(this);
            helper.dispatchElementModified(this);
         }
      }
      else
      {
         if (fireEvents)
         {
            //cancel events
         }
      }

   }

   /**
    * Adds a PresentationElement to this element's list of presentation
    * elements.
    *
    * @param newVal[in] The new PresentationElement
    *
    * @return HRESULTs
    *
    * @warning
    *    The passed-in element must exist in the tree already. This method
    *    just pulls the xmi.id from the passed-in presentation element and adds
    *    it to the "presentation" attribute of the model element.
    *
    */
   public IPresentationElement addPresentationElement(IPresentationElement element)
   {
      final IPresentationElement elem = element;
      if (elem != null)
      {
         Node node = elem.getNode();
         if (node != null)
         {
            // Create an event context that indicates that we are adding
            // a presentation element to a meta element. This is important
            // as we are currently pulling presentation elements out
            // of the data file on save. Because of this, there are certain
            // services available, such as Version Control, that execute
            // there functions upon an element modify, that shouldn't in
            // this particular case. For instance, we did not want
            // version control to query the user to check out a meta-type
            // every time they drag that type onto a diagram. So to prevent
            // this, the ISCMIntegrator listens to element modified events,
            // but checks for this context first before querying the user.
            EventContextManager conMan = new EventContextManager();
            
            //presContext = conMan.getNoEffectContext(this, EventDispatchNameKeeper.modifiedName(), "PresentationAdded", disp);
            ETPairT < IEventContext, IEventDispatcher > contextInfo = conMan.getNoEffectContext(this, 
                                                                                                EventDispatchNameKeeper.modifiedName(), 
                                                                                                "PresentationAdded");
            
            IEventDispatcher disp = contextInfo.getParamTwo();
            IEventContext presContext = contextInfo.getParamOne();
            
            EventState state = new EventState(disp, presContext);

            addChildAndConnect(false, "UML:Element.presentation", "UML:Element.presentation", elem, new IBackPointer < IElement > ()
            {
               public void execute(IElement obj)
               {
                  elem.addSubject(obj);
               }
            });
            elem.addSubject(this);
            state.existState();
         }
      }
      return elem;
   }

   /**
    * Removes the passed-in PresentationElement from this model element.
    *
    * @param pVal[in] The PresentationElement to remove from this element
    *
    * @return HRESULTs
    *
    */
   public void removePresentationElement(IPresentationElement elem)
   {

      // See ElementImpl::AddPresentationElement for an explanation
      // of why we are pushing this event context...
		EventContextManager conMan = new EventContextManager();
//		IEventDispatcher disp = null;
//		IEventContext presContext = null;
//		presContext = conMan.getNoEffectContext(this, EventDispatchNameKeeper.modifiedName(),
//											"PresentationRemoved", disp);
      ETPairT < IEventContext, IEventDispatcher > contextInfo = conMan.getNoEffectContext(this, 
                                                                                          EventDispatchNameKeeper.modifiedName(),
		                                                         									"PresentationRemoved");
            
      IEventDispatcher disp = contextInfo.getParamTwo();
      IEventContext presContext = contextInfo.getParamOne();
            
		EventState state = new EventState(disp, presContext);
		UMLXMLManip.removeChild(m_Node, elem);
		state.existState();

   }

   /**
    *  Retrieves all the PresentationElements representing this element.
    *      HRESULT PresentationElements([out, retval] IPresentationElements** pVal );
    */
   public ETList < IPresentationElement > getPresentationElements()
   {
      IPresentationElement dummy = null;
      return retrieveElementCollection(dummy, "./UML:Element.presentation/*", IPresentationElement.class);
   }

   /**
    * Determines whether or not the passed-in PresentationElement is
    * already associated with the NamedElement.
    *
    * @param pVal[in] The PresentationElement to check against
    * @param isPresent[out] - true indicates that the element
    *                         is already associated with this model element,
    *                         else,
    *                       - false indicates that it is not
    *
    * @return HRESULTs
    */
   public boolean isPresent(IPresentationElement elem)
   {
       boolean retVal = false;
       if(getBooleanAttributeValue("isDeleted", false) == false)
       {
           retVal = isElementPresent(elem, "./UML:Element.presentation/*", false);
       }
      return retVal;
   }

   /**
    * Removes any references to any PresentationElements this element may be
    * pointing to.
    *
    * @return HRESULTs
    */
   public void removeAllPresentationElements()
   {
      XMLManip.removeChild(m_Node, "UML:Element.presentation");
   }

   public void removePresentationElements()
   {
   }

   /**
    * Retrieves a PresentationElement from our internal collection by matching the passed-
    * in XMI id.
    *
    * @param id[in] The XMI id
    * @param element[out] The returned element. 0 if not found
    *
    * @return HRESULT
    */
   public IPresentationElement getPresentationElementById(String id)
   {
      ETList < IPresentationElement > elems = getPresentationElements();
      if (elems != null)
      {
         Iterator<IPresentationElement> iter = elems.iterator();
         while (iter.hasNext())
         {
            IPresentationElement elem = iter.next();
            if (elem.getXMIID().equals(id))
            {
               return elem;
            }
         }
      }
      return null;
   }

   /**
    *
    * Retrieves the ID of the top most Namespace object that this
    * element is a member of.
    *
    * @param topID[out] The id of the top level Namespace
    *
    * @return
    * @see Application::GetProjectByID()
    *
    */
   public String getTopLevelId()
   {
       // Check to see if we are a Transition element.
      // If we are, get the TopLevelID from the FutureOwner
      if (getAggregator() instanceof ITransitionElement)
      {
         ITransitionElement trans = (ITransitionElement) getAggregator();
         IElement futureOwner = trans.getFutureOwner();
         return  futureOwner != null ? futureOwner.getTopLevelId() : null;
      }
      else
      {
         IProject proj = getProject();
			return proj != null ?  proj.getXMIID() : getXMIID();
      }
   }

   public String topLevelId()
   {
      return getTopLevelId();
   }

   /**
    *
    * Retrieves a collection of IArtifacts that this element owns.
    *
    * @param artifacts[out] The collection of IArtifact interfaces, else 0 if none found
    *
    * @return HRESULT
    *
    */
   public ETList < IElement > getAssociatedArtifacts()


   {
      IArtifact dummy = null;
      ETList < IArtifact > values = retrieveElementCollection(dummy, "UML:Element.ownedElement/UML:Artifact", IArtifact.class);

      ISourceFileArtifact sourceDummy = null;
      ETList < ISourceFileArtifact > vals = retrieveElementCollection(sourceDummy, "UML:Element.ownedElement/UML:SourceFileArtifact", ISourceFileArtifact.class);

      //combine the two arrays and return.
      CollectionTranslator < IArtifact, ISourceFileArtifact > translator = new CollectionTranslator < IArtifact, ISourceFileArtifact > ();
      translator.addToCollection(values, vals);
      CollectionTranslator < ISourceFileArtifact, IElement > trans = new CollectionTranslator < ISourceFileArtifact, IElement > ();
      return trans.copyCollection(vals);
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
   public IProject getProject()
   {
      Document doc = getDocument();
      if (doc != null)
      {
         INamespace space = UMLXMLManip.getProject(doc);
         return space instanceof IProject ? (IProject) space : null;
      }
      return null;
   }

   /**
    *
    * Sets this element on the Referencing side of the passed-in Reference. The Reference
    * then becomes part of this element Elements collection (i.e., this element owns
    * the Reference).
    *
    * @param pRef[in] The incoming Reference
    *
    * @return HRESULT
    *
    */
   public IReference addReferencingReference(IReference ref)
   {
      final IReference reference = ref;
      new ElementConnector < IElement > ().addChildAndConnect(this, true, "referencingReference", "referencingReference", reference, new IBackPointer < IElement > ()
      {
         public void execute(IElement obj)
         {
            reference.setReferencingElement(obj);
         }
      });

      // Now take ownership of the reference
      addElement(reference);
      return reference;
   }

   /**
    *
    * Removes this element from the Referencing side of the passed-in Reference,
    * and then removes the Reference from this Element's collection of owned elements.
    *
    * @param pRef[in] The Reference
    *
    * @return HRESULT
    *
    */
   public void removeReferencingReference(IReference reference)
   {
      final IReference ref = reference;

      // There is a bug in c++ where the other half of the reference does not get its
      // xml attribute updated when removing the reference.  This is causing the property editor
      // to display incorrect information in jUML, so this code is removing the xmiid of the reference
      // from the referredElement
      IElement element = reference.getReferredElement();
      if (element != null)
      {
         String id = reference.getXMIID();
         String ids = UMLXMLManip.getAttributeValue(element.getNode(), "referredReference");
         ids = StringUtilities.replaceAllSubstrings(ids, id, "");
         UMLXMLManip.setAttributeValue(element, "referredReference", ids);
      }

      new ElementConnector < IElement > ().removeByID(this, ref, "referencingReference", new IBackPointer < IElement > ()
      {
         public void execute(IElement obj)
         {
            ref.setReferencingElement(obj);
         }
      });
      // Now remove the reference from our list of owned elements
      removeElement(ref);
   }

   /**
    *  Retrieves the collection of referencing Reference relationships.
    *      HRESULT ReferencingReferences([out, retval] IReferences* *pVal);
    */
   public ETList < IReference > getReferencingReferences()
   {
      ElementCollector < IReference > collector = new ElementCollector < IReference > ();
      return collector.retrieveElementCollectionWithAttrIDs(this, "referencingReference", IReference.class);
   }

   /**
    * Adds this element to the ReferredElement role of the passed-in Reference.
    *
    * @param pRef[in] The reference
    *
    * @return HRESULT
    *
    */
   public IReference addReferredReference(IReference reference)
   {
      final IReference ref = reference;
      new ElementConnector().addChildAndConnect(this, true, "referredReference", "referredReference", ref, new IBackPointer < IElement > ()
      {
         public void execute(IElement obj)
         {
            ref.setReferredElement(obj);
         }
      });
      return ref;
   }

   /**
    *
    * Removes this element from the ReferredElement role of the passed-in reference.
    *
    * @param pRef[in] The reference
    *
    * @return HRESULT
    *
    */
   public void removeReferredReference(IReference reference)
   {
      final IReference ref = reference;

      // There is a bug in c++ where the other half of the reference does not get its
      // xml attribute updated when removing the reference.  This is causing the property editor
      // to display incorrect information in jUML, so this code is removing the xmiid of the reference
      // from the referencingElement
      IElement element = reference.getReferencingElement();
      if (element != null)
      {
         String id = reference.getXMIID();
         String ids = UMLXMLManip.getAttributeValue(element.getNode(), "referencingReference");
         ids = StringUtilities.replaceAllSubstrings(ids, id, "");
         UMLXMLManip.setAttributeValue(element, "referencingReference", ids);
      }

      new ElementConnector < IElement > ().removeByID(this, ref, "referredReference", new IBackPointer < IElement > ()
      {
         public void execute(IElement obj)
         {
            ref.setReferredElement(obj);
         }
      });
   }

   /**
    *
    * Retrieves the collection of References that this element is playing the ReferredElement role.
    *
    * @param pVal[out] The collection
    *
    * @return HRESULT
    *
    */
   public ETList < IReference > getReferredReferences()
   {
      ElementCollector < IReference > collector = new ElementCollector < IReference > ();
      return collector.retrieveElementCollectionWithAttrIDs(this, "referredReference", IReference.class);
   }

   /**
    *
    * Description.
    *
    * @param pVal[out]
    *
    * @result HRESULT
    */
   public long getElementCount()
   {
      return UMLXMLManip.queryCount(m_Node, "UML:Element.ownedElement/*", false);
   }

   /**
    *
    * Description
    *
    * @param pVal[out]
    *
    * @result HRESULT
    */
   public long getSourceFlowCount()
   {
      return UMLXMLManip.queryCount(m_Node, "sourceFlow", true);
   }

   public long getTargetFlowCount()
   {
      return UMLXMLManip.queryCount(m_Node, "targetFlow", true);
   }

   public long getTaggedValueCount()
   {
      return UMLXMLManip.queryCount(m_Node, "UML:Element.ownedElement/UML:TaggedValue", false);
   }

   public long getPresentationElementCount()
   {
      return UMLXMLManip.queryCount(m_Node, "./UML:Element.presentation/*", false);
   }

   public long getAssociatedArtifactCount()
   {
      return UMLXMLManip.queryCount(m_Node, "UML:Element.ownedElement/UML:Artifact", false);
   }

   public long getReferencingReferenceCount()
   {
      return UMLXMLManip.queryCount(m_Node, "referencingReference", true);
   }

   public long getReferredReferenceCount()
   {
      return UMLXMLManip.queryCount(m_Node, "referredReference", true);
   }

   /**
    *  Retrieves all tagged values, including standard tags..
    *      HRESULT AllTaggedValues([out, retval] ITaggedValues** pVal);
    */
   public ETList < ITaggedValue > getAllTaggedValues()
   {
      ITaggedValue dummy = null;
      return retrieveElementCollection(dummy, "UML:Element.ownedElement/UML:TaggedValue", ITaggedValue.class);
   }

   /**
    *
    * Retrieves the language specification that this element, or one of it's direct
    * parents, is associated with.
    *
    * @param pVal[out] The collection
    *
    * @return HRESULT
    *
    */
   public ETList < ILanguage > getLanguages()
   {
      ETList < ILanguage > langs = null;
      ETList < ILanguage > tempLangs = null;

      // Check to see if this element is a transition element
      // first. A transition element only occurs during creation,
      // and it is a mechanism used to help establish a connection
      // between the new element and the future document that it
      // will belong
      if (getAggregator() instanceof ITransitionElement)
      {
         ITransitionElement trans = (ITransitionElement) getAggregator();
         IElement futureOwner = trans.getFutureOwner();
         if (futureOwner != null)
         {
            langs = futureOwner.getLanguages();
         }
      }
      else
      {
         // I do not want to wrap RetrieveLanguagesFromArtifacts with a _VH(...)
         // because the language manager will return an error HRESULT if the language
         // was not found.
         //
         // If the lanuage was not found I do not want to throw out.  I want to get the
         // default language instead if a language can not be found.
         tempLangs = retrieveLanguagesFromArtifacts();

         boolean getDefault = true;
         if (tempLangs != null && tempLangs.size() > 0)
         {
            getDefault = false;
         }

         if (getDefault)
         {
            //we might have to create a new array here and just add
            //default langauge.
            tempLangs = retrieveDefaultLanguage();
         }

         if (tempLangs != null && tempLangs.size() > 0)
         {
            langs = new ETArrayList < ILanguage > (tempLangs.size());
            for (int i = 0; i < tempLangs.size(); i++)
            {
               ILanguage lang = tempLangs.get(i);
               langs.add(lang);
            }
         }
      }
      return langs != null ? langs : new ETArrayList < ILanguage >();
   }

   /**
    *
    * Queries this element for IArtifacts that represent source files. For
    * each one found, retrieves the Language object associated with that
    * file.
    *
    * @param languages[out] The collection of found languages
    *
    * @return HRESULT
    *   - S_OK:    Returned if not artifacts was found or at least on ILanguage
    *              was found.
    *   - S_FALSE: Returned when Soruce File Artifacts was found but there where
    *              no ILanguages associated to the source file.  In which case
    *              the UML language definition will be returned.
    *
    */
   private ETList < ILanguage > retrieveLanguagesFromArtifacts()
   {
      ETList < ILanguage > langs = null;
      ETList < IElement > elems = getSourceFiles();
      int count = 0;
      if (elems != null && elems.size() > 0)
      {
         count = elems.size();
         langs = new ETArrayList < ILanguage > ();
         int j = 0;
         for (int i = 0; i < count; i++)
         {
            IElement elem = elems.get(i);
            ILanguage lang = retrieveLanguageFromArtifact(elem);
            if (lang != null)
            {
               langs.add(lang);
               j++;
            }
         }

         if (count > 0 && j == 0)
         {
            // Found source file artifacts but was not able to find a language
            // associated with the file.
            ILanguage pLanguage = retrieveLanguage("UML");
            if (pLanguage != null)
            {
               langs.add(pLanguage);
            }
         }
      }
      else
      {
         // If we were not able to find any source file artifacts associated to the
         // element then search the Owner element.  If source file artifacts where
         // associated with the element and no langauge where found then we do not
         // support the language.
         //
         // Do not continue to search the Owner elements.  Retrieve the UML language.
         //
         IElement parent = getOwner();
         if (parent != null)
         {
            langs = parent.getLanguages();
         }
      }

      return langs;
   }

   /**
    *
    * Retrieves the language by retrieving a file name from the passed in Element.
    *
    * @param art[in] Generally this is an IArtifact, but it can be anything
    * @param lang[out] The found language
    *
    * @return HRESULT
    *
    */
   private ILanguage retrieveLanguageFromArtifact(IElement elem)
   {
      ILanguage retLang = null;

      // I want to verify that I have an actual source file artifact.
      if (elem instanceof ISourceFileArtifact)
      {
         ISourceFileArtifact art = (ISourceFileArtifact) elem;
         String fileName = art.getFileName();
         if (fileName != null && fileName.length() > 0)
         {
            ICoreProduct prod = ProductRetriever.retrieveProduct();
            if (prod != null)
            {
               ILanguageManager langMan = prod.getLanguageManager();
               if (langMan != null)
               {
                  retLang = langMan.getLanguageForFile(fileName);
               }
            }
         }
      }

      return retLang;
   }

   /**
      *
      * Queries this project for the default language.  The default language
      * details will be returned.
      *
      * @param languages[out] The collection of found languages
      *
      * @return HRESULT
      *
      */
   private ETList < ILanguage > retrieveDefaultLanguage()
   {
      ETList < ILanguage > langs = null;
      IProject proj = getProject();
      if (proj != null)
      {
         String defLang = proj.getDefaultLanguage();
         ILanguage lang = null;
         if (defLang != null && defLang.length() > 0)
         {
            lang = retrieveLanguage(defLang);
         }
         else
         {
            // Opps, A default language is not defined for the project.
            // Therefore, assume UML.
            lang = retrieveLanguage("UML");
         }

         if (lang != null)
         {
            langs = new ETArrayList < ILanguage > ();
            langs.add(lang);
         }
      }
      return langs;
   }

   /**
    * Retrieves the specified language from the language manager.
    *
    * @param langName [in] The name of the language.
    * @param languages [out] The languages that was found.
    */
   private ILanguage retrieveLanguage(String langName)
   {
      ILanguage retLang = null;
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         ILanguageManager langMan = prod.getLanguageManager();
         if (langMan != null)
         {
            retLang = langMan.getLanguage(langName);
         }
      }
      return retLang;
   }

   /**
      *  Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces.
      *      HRESULT SourceFiles([out, retval] IElements* *pVal );
      */
   public ETList < IElement > getSourceFiles()
   {
      ETList < IElement > retVal = null;
      ETList < IElement > artifacts = getAssociatedArtifacts();

      // Check if any of the artifacts are ISourceFileArtifact elements.
      // If the element is a ISourceFileArtifact then add it to the
      // return collection of IElements
      if (artifacts != null)
      {
         int count = artifacts.size();
         for (int i = 0; i < count; i++)
         {
            IElement pEle = artifacts.get(i);
            if (pEle instanceof ISourceFileArtifact)
            {
               if (retVal == null)
               {
                  retVal = new ETArrayList < IElement > ();
               }
               retVal.add(pEle);
            }
         }

         // If we have not been able to find any ISourceFileArtifact elements
         // then check the parent node.  If the parent node has ISoruceFileArtfact
         // elements associated with it then use those.
         //
         // This solves the case where I have a IAttribute element.  IAttribute will
         // most likely not have a source file associated with it.  However the IClassifier
         // that contains the IAttribute will.
         //
         // Since I do not create the retVal object util I find a SourceFileArtifact I can
         // check for NULL to determine if I found a source file.
         if (retVal == null)
         {
            IElement pOwner = getOwner();
            if (pOwner != null)
            {
               retVal = pOwner.getSourceFiles();
            }
         }
      }
      return retVal;
   }

   /**
    * Associates a source file with the model element.
    *
    * @param filename [in] The file name to add.
    */
   public void addSourceFile(String fileName)
   {
      FactoryRetriever ret = FactoryRetriever.instance();
      Object obj = ret.createType("SourceFileArtifact", null);
      if (obj instanceof ISourceFileArtifact)
      {
         ISourceFileArtifact art = (ISourceFileArtifact) obj;
         addElement(art);
         art.setSourceFile(fileName);
      }
   }

   public void addSourceFileNotDuplicate(String fileName)
   {
        if (fileName != null && fileName.length() > 0 && 
            !hasSourceFile(fileName))
        {
            addSourceFile(fileName);
        }
   }
   
   /**
    * Removes a source file from the model element.  The model
    * element will no longer be associatied with the model element.
    *
    * @param filename [in] The file name to remove.
    */
   public void removeSourceFile(String fileName)
   {
      if (fileName != null && fileName.length() > 0)
      {
         ETList < IElement > elems = getSourceFiles();
         if (elems != null)
         {
            int count = elems.size();
            for (int i = 0; i < count; i++)
            {
               IElement elem = elems.get(i);
               if (elem instanceof ISourceFileArtifact)
               {
                  ISourceFileArtifact art = (ISourceFileArtifact) elem;
                  String sourceFile = art.getSourceFile();
                  if (sourceFile.equals(fileName))
                  {
                     //remove from sourcefiles
                     removeElement(elem);
                     // this causes an IndexOutOfBoundsException and seems
                     // unnecessary since this list will be gc'ed when this
                     // method is popped off the stack
                     // elems.remove(i);
                  }
               }
            }
         }
      }
   }

   /**
    *  Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces.
    *      HRESULT SourceFiles2( [in]BSTR lang, [out, retval] IElements* *pVal );
    */
   public ETList < IElement > getSourceFiles2(String language)
   {
      ETList < IElement > retObj = null;
      ETList < IElement > allFiles = getSourceFiles();
      if (allFiles != null)
      {
         int count = allFiles.size();
         for (int i = 0; i < count; i++)
         {
            IElement pItem = allFiles.get(i);
            if (pItem instanceof ISourceFileArtifact)
            {
               ISourceFileArtifact pArt = (ISourceFileArtifact) pItem;
               ILanguage pFileLang = pArt.getLanguage();
               if (pFileLang != null)
               {
                  String langName = pFileLang.getName();
                  if (langName != null && langName.equals(language))
                  {
                     if (retObj == null)
                     {
                        retObj = new ETArrayList < IElement > ();
                     }
                     retObj.add(pArt);
                  }
               }
            }
         }
      }
      return retObj;
   }

   public ETList < IElement > getSourceFiles3(ILanguage lang)
   {
       return lang != null ? getSourceFiles2(lang.getName()) : null;
   }

   /**
    *  Retrieves the SourceFileArtifact that contains the passed-in filename.
    */
    public IElement getSourceFile(String filename)
    {
       ETList<IElement> sourceFiles = getSourceFiles();
       if (sourceFiles == null || sourceFiles.size() == 0)
           return null;

       for (IElement element: sourceFiles)
       {
           if (element instanceof ISourceFileArtifact)
           {
               ISourceFileArtifact sfa = (ISourceFileArtifact) element;
               String fileName = sfa.getFileName();

               if (fileName != null && fileName.length() > 0 
                   && fileName.equals(filename))
               {
                   return (IElement)sfa;
               }
           }
       }
       
       return null;
   }
    
   public boolean hasSourceFile(String fileName)
   {
       return getSourceFile(fileName) == null ? false : true;
   }
   
   /**
    *
    * Retrieves the collection of IStereotypes currently applied to this element
    *
    * @param pStereotypes[out] The collection of stereotypes. IStereotypes is the actual type.
    *
    * @return HRESULT
    *
    */
   public ETList < Object > getAppliedStereotypes()
   {
      ElementCollector < Object > collector = new ElementCollector < Object > ();
      return collector.retrieveElementCollectionWithAttrIDs(this, "appliedStereotype", Object.class);
   }

   /**
    *
    * Retrieves the number of IStereotypes currently applied to this element
    *
    * @param pNumStereotypes[out,retval] The number of stereotypes.
    *
    * @return HRESULT
    *
    */
   public int getNumAppliedStereotypes()
   {
      ETList < Object > objs = getAppliedStereotypes();
      return objs != null ? objs.size() : 0;
   }

   /**
    *
    * Retrieves a collection of stereotypes in string form << xxx, yyy >>.
    * NULL string is returned if no stereotypes exist.
    *
    * @param bHonorAliasing [in] Should the stereotypes be converted to their alias?
    * @param sStereotypeString[out,retval] String of form << xxx, yyy >>.
    *
    * @return HRESULT
    *
    */
   public ETList < String > getAppliedStereotypesAsString()
   {
      ETList < String > retVal = new ETArrayList < String >();
      
      ETList < Object > stereotypes = getAppliedStereotypes();
      for (Object curObj : stereotypes)
      {         
         if (curObj instanceof IStereotype)
         {
            IStereotype sterotype = (IStereotype)curObj;
            retVal.add(sterotype.getNameWithAlias());
         }
      }
      return retVal;
   }

   /**
    *
    * Retrieves a collection of stereotypes in string form << xxx, yyy >>.
    * NULL string is returned if no stereotypes exist.
    *
    * @param bHonorAliasing [in] Should the stereotypes be converted to their alias?
    * @param sStereotypeString[out,retval] String of form << xxx, yyy >>.
    *
    * @return HRESULT
    *
    */
   public String getAppliedStereotypesAsString(boolean bHonorAliasing)
   {
       StringBuffer retVal = new StringBuffer(getAppliedStereotypesList(false));
       if(retVal.length() > 0)
       {
           retVal.insert(0, "<<");
           retVal.append(">>");
       }
       
       return retVal.toString();
       
       /*
      String retStr = "";
      ETList < Object > objs = getAppliedStereotypes();
      if (objs != null)
      {
         int count = objs.size();
         if (count > 0)
         {
            retStr = "<<";
         }
         for (int i = 0; i < count; i++)
         {
            Object obj = objs.get(i);
            if (obj instanceof IStereotype)
            {
               IStereotype stereoType = (IStereotype) obj;
               String name;
               if (bHonorAliasing)
               {
                  name = stereoType.getNameWithAlias();
               }
               else
               {
                  name = stereoType.getName();
               }

               if (i > 0)
               {
                  retStr += ", ";
               }
               retStr += name;
            }
         }

         if (count > 0)
         {
            retStr += ">>";
         }
      }
      return retStr;
      */
   }

   public String getAppliedStereotypesList()
   {
//       StringBuffer retVal = new StringBuffer(getAppliedStereotypesList(true));
//       if(retVal.length() > 0)
//       {
//           retVal.insert(0, "<<");
//           retVal.append(">>");
//       }
//       
//       return retVal.toString();
       return getAppliedStereotypesList(true);
   }
   
   public String getAppliedStereotypesList(boolean bHonorAliasing)
   
   {
      String retStr = "";
      ETList < Object > objs = getAppliedStereotypes();
      if (objs != null)
      {
         int count = objs.size();         
         for (int i = 0; i < count; i++)
         {
            Object obj = objs.get(i);
            if (obj instanceof IStereotype)
            {
               IStereotype stereoType = (IStereotype) obj;
               String name;
               if (bHonorAliasing)
               {
                  name = stereoType.getNameWithAlias();
               }
               else
               {
                  name = stereoType.getName();
               }

               if (i > 0)
               {
                  retStr += ", ";
               }
               retStr += name;
            }
         }
      }
      return retStr;
   }
   
   /**
    *
    * Applies the passed in IStereotype to this element.
    *
    * @param pIStereotype[in] The IStereotype, passed as an IDispatch
    *
    * @return HRESULT
    *
    */
   public void applyStereotype(Object stereotype)
   {
      boolean proceed = true;
      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("PreStereotypeApplied");
         proceed = disp.firePreStereotypeApplied(stereotype, this, payload);
      }

      if (proceed)
      {
         if (stereotype instanceof IStereotype)
         {
            IStereotype stereo = (IStereotype) stereotype;
            addElementByID(stereo, "appliedStereotype");

            // This is a necessary call, as the stereotype has already been named, but since it is
            // not part of the xml document, the picklist manager has not been told about it yet.
            // Adding this method solves the problem where picklists were not showing up for stereotypes
            addToPickList(stereo);

            if (disp != null)
            {
               IEventPayload payload = disp.createPayload("StereotypeApplied");
               disp.fireStereotypeApplied(stereotype, this, payload);
            }
         }
      }
      else
      {
         //cancel the event.
      }
   }

   /**
    * Adds the passed in named element to this elmenent's PickListManager
    *
    * @param namedElement[in] The element to add
    *
    * @return HRESULT
    */
   private void addToPickList(INamedElement namedEle)
   {
      IProject proj = getProject();
      if (proj != null)
      {
         ITypeManager typeMan = proj.getTypeManager();
         if (typeMan != null)
         {
            IPickListManager pickMan = typeMan.getPickListManager();

            // It is possible for this to be null. Most notably
            // when the Project itself is being created and its
            // name is being set.
            if (pickMan != null)
            {
               pickMan.addNamedType(namedEle);
            }
         }
      }
   }

   /**
    *
    * Removes the passed in IStereotype from this element
    *
    * @param pIStereotype[in] The IStereotype, passed as an IDispatch
    *
    * @return HRESULT
    *
    */
   public void removeStereotype(Object stereotype)
   {
      boolean proceed = true;
      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("PreStereotypeDeleted");
         proceed = disp.firePreStereotypeDeleted(stereotype, this, payload);
      }

      if (proceed)
      {
         IStereotype stereo = (IStereotype) stereotype;
         removeElementByID(stereo, "appliedStereotype");
         if (disp != null)
         {
            IEventPayload payload = disp.createPayload("StereotypeDeleted");
            disp.fireStereotypeDeleted(stereotype, this, payload);
         }
      }
      else
      {
         //try to cancel the event
      }
   }

   /**
    *
    * Removes all the IStereotypes from this element.
    *
    * @return HRESULT
    *
    */
   public void removeStereotypes()
   {
      ETList < Object > objs = getAppliedStereotypes();
      if (objs != null && objs.size() > 0)
      {
         int count = objs.size();
         for (int i = count - 1; i >= 0; i--)
         {
            IStereotype stereo = (IStereotype) objs.get(i);
            removeStereotype(stereo);
         }
      }
   }

   /**
    *
    * Applies a Stereotype that matches the passed in name. A profile containing
    * a stereotype of this name must have first been applied to an outer package
    * for this method to work.
    *
    * @param stereotypeName[in]  Name of the Stereotype to apply.
    * @param pStereotype[out]    The corresponding IStereotype, returned as an IDispatch
    *
    * @return HRESULT
    *
    */
   public Object applyStereotype2(String name)
   {
      Object retObj = null;
      if (name != null && name.length() > 0)
      {
          IStereotype stereo = retrieveStereotype(name);
          if (stereo == null) {
              
              //kris richards - UnknownStereotypeCreate pref deleted. Set to PSK_IN_PROJECT_PROFILE
              
              // Establish a new Stereotype
              ProfileManager profMan = new ProfileManager();
              retObj = profMan.establishNewStereotype(this, name);
              
              
          }
          
          if (retObj == null && stereo != null) {
              retObj = stereo;
          }
          
          if (retObj != null) {
              applyStereotype(retObj);
          }
      }
      return retObj;
   }

   /**
    *
    * Removes a Stereotype that matches the passed in name. A profile containing
    * a stereotype of this name must have first been applied to an outer package
    * for this method to work.
    *
    * @param stereotypeName[in] Name of the Stereotype to remove.
    *
    * @return HRESULT
    *
    */
   public void removeStereotype2(String name)
   {
      IStereotype stereo = retrieveStereotype(name);
      if (stereo != null)
      {
         removeStereotype(stereo);
      }
   }

   /**
    *
    * Retrieves the stereotype that matches the name passed in. This method
    * searches through all the PackageImports that reference Profiles, looking
    * for stereotypes in those profiles.
    *
    * @param name[in]      The name of the stereotype to find.
    * @param sType[out]    The found stereotype, else 0.
    *
    * @return HRESULT
    *
    */
   private IStereotype retrieveStereotype(String name)
   {
      IStereotype stereo = null;
      if (this instanceof IPackage)
      {
         // If we're a package, let's check our imports. If we find
         // a stereotype, we're done
         IPackage pack = (IPackage) this;
         stereo = retrieveStereotype(pack, name);
      }

      if (stereo == null)
      {
         IElement owner = getOwner();
         if (owner instanceof IPackage)
         {
             stereo = retrieveStereotype((IPackage) owner, name);
         }
      }
      return stereo;
   }

   /**
    *
    * Retrieves a stereotype by name
    *
    * @param package[in]   The package to search its imports for the stereotype
    * @param name[in]      The name of the stereotype to find
    * @param sType[out]    The found stereotype
    *
    * @return HRESULT
    *
    */
   private IStereotype retrieveStereotype(IPackage pack, String name)
   {
      IStereotype stereo = null;
      if (pack != null)
      {
         ETList < INamedElement > elems = pack.findTypeByNameInImports(name);
         if (elems != null)
         {
            if (elems.size() == 1)
            {
               INamedElement elem = elems.get(0);
               if (elem instanceof IStereotype)
               {
                  stereo = (IStereotype) elem;
               }
            }
         }

         if (stereo == null)
         {
            IElement owner = pack.getOwner();
            if (owner instanceof IPackage)
            {
               IPackage ownPack = (IPackage) owner;
               stereo = retrieveStereotype(ownPack, name);
            }
         }
      }
      return stereo;
   }

   /**
      * Takes the cononical form of stereotypes <<xx,yy>> and sets this elements stereotypes to match the input string.
      */
   public void applyNewStereotypes(String name)
   {
      if (name != null && name.length() > 0)
      {
         // Replace all the various bad characters with nothing.
         String replaceString = "<";
         String replaceWithString = "";
         name = StringUtilities.replaceAllSubstrings(name, replaceString, replaceWithString);

         replaceString = ">";
         name = StringUtilities.replaceAllSubstrings(name, replaceString, replaceWithString);

         replaceString = "[";
         name = StringUtilities.replaceAllSubstrings(name, replaceString, replaceWithString);

         replaceString = "]";
         name = StringUtilities.replaceAllSubstrings(name, replaceString, replaceWithString);

         replaceString = "{";
         name = StringUtilities.replaceAllSubstrings(name, replaceString, replaceWithString);

         replaceString = "}";
         name = StringUtilities.replaceAllSubstrings(name, replaceString, replaceWithString);

         //now split on comma
         String[] strs = name.split(",");
         if (strs != null && strs.length > 0)
         {
            // We need to find a better way, but right now we just remove all stereotypes and
            // add them back in.
            removeStereotypes();

            for (int i = 0; i < strs.length; i++)
            {
               String str = strs[i];
               if (str.trim().length() > 0)
               {
                  applyStereotype2(str);
               }
            }
         }
      }
      else
      {
         removeStereotypes();
      }
   }

   /**
    * Retrieves the applied stereotype that matches the name passed in.
    */
   public Object retrieveAppliedStereotype(String name)
   {
      Object retObj = null;
      if (name != null && name.length() > 0)
      {
         ETList < Object > objs = getAppliedStereotypes();
         if (objs != null)
         {
            int count = objs.size();
            // Find the stereotype that matches by name
            for (int i = 0; i < count; i++)
            {
               IStereotype stereo = (IStereotype) objs.get(i);
               String sName = stereo.getName();
               if (sName.equals(name))
               {
                  retObj = stereo;
                  break;
               }
            }
         }
      }
      return retObj;
   }

   /**
    *
    * Adds a constraint that will be applied to this Element. This element will also
    * physically own the constraint.
    *
    * @param newVal[in] The Constraint to apply and own.
    *
    * @return HRESULT
    *
    */
   public void addOwnedConstraint(IConstraint newVal)
   {
      if (newVal != null)
      {
         newVal.addConstrainedElement(this);
         addElement(newVal);
      }
   }

   /**
    *
    * Removes the constraint from this Element.
    *
    * @param pVal[in] The constraint to remove
    *
    * @return HRESULT
    *
    */
   public void removeOwnedConstraint(IConstraint pVal)
   {
      if (pVal != null)
      {
         pVal.removeConstrainedElement(this);
         removeElement(pVal);
      }
   }

   /**
    *  Retrieves all the Constraints owned by this Element.
    *      HRESULT OwnedConstraints([out, retval] IConstraints** pVal );
    */
   public ETList < IConstraint > getOwnedConstraints()
   {
      ElementCollector < IConstraint > collector = new ElementCollector < IConstraint > ();
      return collector.retrieveElementCollection(m_Node, "UML:Element.ownedElement/UML:Constraint", IConstraint.class);
   }

   /**
    *  Retrieves all the Constraints owned by this Element and returns 
    *  them as a string for appropriate display.
    *      HRESULT OwnedConstraints([out, retval] IConstraints** pVal );
    */
   public String getConstraintsAsString()
   {
      ETList<IConstraint> constrList = getOwnedConstraints();
      
      String str = "";
      
      for (IConstraint constr: constrList)
      {
          if (str.length() > 0)
              str += "; ";
          
          str += constr.getName() + ':' + constr.getExpression();
      }
      
      return str;
   }
   
    public ETList < String > getPossibleCollectionTypes()
    {
        ETList < String > retVal = new ETArrayList();

        ILanguage lang = getLanguages().get(0);
        List < CollectionType > types = lang.getCollectionTypes();

        retVal.add(NbBundle.getMessage(Element.class, "LBL_AS_ARRAY"));
        for(CollectionType type : types)
        {
            retVal.add(type.getFullName());
        }

        return retVal;
    }
    
    public String getPossibleCollectionTypesAsString()
    {
        StringBuffer retVal = new StringBuffer();

        ILanguage lang = getLanguages().get(0);
        List < CollectionType > types = lang.getCollectionTypes();

        retVal.append(NbBundle.getMessage(Element.class, "LBL_AS_ARRAY"));
        for(CollectionType type : types)
        {
            if(retVal.length() > 0)
            {
                retVal.append("|");
            }
            retVal.append(type.getFullName());
        }

        return retVal.toString();
    }

   
   /**
    *
    * Creates a new Constraint. The Constraint is not added to this element.
    *
    * @param sName[in]           The name of the constraint. Can be "".
    * @param sExpression[in]     The expression to use with the constraint. Can be "".
    * @param newConstraint[out]  The new Constraint
    *
    * @return HRESULT
    *
    */
   public IConstraint createConstraint(String name, String expr)
   {
      TypedFactoryRetriever < IConstraint > ret = new TypedFactoryRetriever < IConstraint > ();
      IConstraint cons = ret.createType("Constraint");
      if (cons != null)
      {
         if (name != null && name.length() > 0)
         {
            cons.setName(name);
         }

         if (expr != null && expr.length() > 0)
         {
            cons.setExpression(expr);
         }
      }
      return cons;
   }

   /**
    *
    * Determines whether or not the current element and pElement both reside in the same Project.
    *
    * @param pElement[in]              The element to check against
    * @param projectsAreTheSame[out]   true if both elements are in the same Project, else false
    *
    * @return HRESULT
    * @note If any or both of the elements being checked have NOT been established in a Project yet, this method
    *       will return true, assuming that the elements have just not been added to the Project yet.
    *
    */
   public boolean inSameProject(IElement elem)
   {
      // We'll assume that the projects are the same. It is quite possible that this call
      // will be made before the current element or the element being passed in has been
      // established into a project yet.
      IProject proj = getProject();
      IProject eProj = elem.getProject();
      return proj != null && eProj != null ? proj.isSame(eProj) : true;
   }

   /**
    *
    * Removes all element that should also be deleted or at least modified
    * when this element is deleted.
    *
    * @param thisElement[in] The COM object representing this element
    *
    * @return HRESULT
    *
    */
   protected void performDependentElementCleanup(IVersionableElement elem)
   {
      super.performDependentElementCleanup(elem);

      // Any source or target flow will need to be deleted
      if (elem instanceof IElement)
      {
         IElement thisElem = (IElement) elem;
         thisElem.deleteFlowRelations();
         thisElem.deleteReferenceRelations();
      }
   }

   /**
    *
    * Deletes all Reference relations this element is in.
    *
    * @param element[in] Ignored
    *
    * @return HRESULT
    *
    */
   public void deleteReferenceRelations()
   {
      //delete all referencing references
      ETList < IReference > refs = getReferencingReferences();
      if (refs != null && refs.size() > 0)
      {
         for (int i = 0; i < refs.size(); i++)
         {
            IReference ref = refs.get(i);
            ref.delete();
         }
      }

      //delete all referred references
      refs = getReferredReferences();
      if (refs != null && refs.size() > 0)
      {
         for (int i = 0; i < refs.size(); i++)
         {
            IReference ref = refs.get(i);
            ref.delete();
         }
      }
   }

   /**
    *
    * Duplicates this element. If this element owns other elements,
    * those elements are also duplicated.
    *
    * @see VersionabelElementImpl::PerformDuplication()
    *
    */
   public IVersionableElement performDuplication()
   {
      IVersionableElement elem = super.performDuplication();

      // Perform a Duplicate on all owned elements
      duplicateOwnedElements((IElement) elem);
      duplicatePresentationElements((IElement) elem);
      establishFlowRelations((IElement) elem);
      establishReferenceRelations((IElement) elem);

      return elem;
   }

   /**
    *
    * Makes sure that the IReference relationships know that they are apart
    * of the newly cloned element. If the duplicate element ( clone )
    * coming in has Reference relationship IDs associated with it, it
    * needs to let the Reference relations know to connect to the new clone
    * ID.
    *
    * @param dup[in] The Element just duplicated
    *
    * @return HRESULT
    *
    */
   private void establishReferenceRelations(IElement dup)
   {
      ETList < IReference > refs = getReferencingReferences();
      establishReferenceRelations(true, refs, dup);

      refs = getReferredReferences();
      establishReferenceRelations(false, refs, dup);
   }

   /**
    *
    * For every IReference in the passed in Reference collection, the clone
    * element is connected to it.
    *
    * @param isReferencingElement[in] - true if dup is the referencing element, else
    *                                 - false if it is the referred element
    * @param refs[in] The collection to manipulate
    * @param dup[in] The element to hook to the References
    *
    * @return HRESULT
    *
    */
   private void establishReferenceRelations(boolean isReferencingElement, ETList < IReference > refs, IElement dup)
   {
      if (refs != null)
      {
         int count = refs.size();
         for (int i = 0; i < count; i++)
         {
            IReference ref = refs.get(i);
            IVersionableElement ver = ref.duplicate();
            if (ver instanceof IReference)
            {
               IReference refClone = (IReference) ver;

               // The ReplaceIDs() call replaces all the ids that math
               // the this pointer xmi.id with the xmi.id of dup.
               // This completes the duplication of the reference
               // relationship.
               replaceIds(dup, refClone);

               if (isReferencingElement)
               {
                  dup.addReferencingReference(refClone);
               }
               else
               {
                  refClone.setReferredElement(dup);
               }
            }
         }
      }
   }

   /**
    *
    * Makes sure that the IFlow relationships know that they are apart
    * of the newly cloned element. If the duplicate element ( clone )
    * coming in has Flow relationship IDs associated with it, it
    * needs to let the Flow relations know to connect to the new clone
    * ID.
    *
    * @param clone[in] The Element just cloned
    *
    * @return HRESULT
    *
    */
   private void establishFlowRelations(IElement clone)
   {
      establishFlowRelations(true, getSourceFlows(), clone);
      establishFlowRelations(false, getTargetFlows(), clone);
   }

   /**
    *
    * For every Flow in the passed in Flow collection, the clone
    * element is connected to it.
    *
    * @param flows[in] The collection to manipulate
    * @param clone[in] The element to hook to the Flows
    *
    * @return HRESULT
    *
    */
   private void establishFlowRelations(boolean isSource, ETList < IFlow > flows, IElement clone)
   {
      if (flows != null)
      {
			int count = flows.size();
			if (isSource)
			{
				 for (int i = 0; i < count; i++)
				 {
					flows.get(i).addSource(clone);
				 }
			}
			else
			{
            for (int i = 0; i < count; i++)
            {
					flows.get(i).addTarget(clone);
            }
			}
      }
   }

   /**
    *
    * Dups the presentation elements on element and reconnects
    *
    * @param dup[in] The duplicated element
    *
    * @return HRESULT
    *
    */
   private void duplicatePresentationElements(IElement dup)
   {
      ETList < IPresentationElement > elems = dup.getPresentationElements();
      if (elems != null)
      {
         int count = elems.size();
         for (int i = 0; i < count; i++)
         {
            IPresentationElement pEle = elems.get(i);
            IVersionableElement presVer = pEle.duplicate();

            if (presVer instanceof IPresentationElement)
            {
               IPresentationElement pres = (IPresentationElement) presVer;
               pres.addSubject(dup);
            }
         }
      }
   }

   /**
    *
    * Duplicates all the owned elements that clone possesses
    *
    * @param clone[in] The cloned element
    *
    * @return HRESULT
    *
    */
   private void duplicateOwnedElements(IElement clone)
   {
      ETList < IElement > elems = clone.getElements();
      if (elems != null)
      {
         Node cloneNode = clone.getNode();
         if(cloneNode != null)
         {
            int count = elems.size();
            for (int i = 0; i < count; i++)
            {
               IElement element = elems.get(i);

               // The Duplicate() method removes the cloned
               // element from its owner, so reestablish
               IVersionableElement ver = element.duplicate();

               String elementXMIID = element.getXMIID();
               if (ver instanceof IElement)
               {
                  IElement clonedEle = (IElement) ver;

   //               // Remove the original element from the clone
   //               clone.removeElement(element);
   //
   //               // We have to set the parent node explicitly to
   //               // null to prevent the corruption of dom4j otherwise gets
   //               // up to.
   //               clonedEle.getNode().setParent(null);

                  // Remove the original element from the clone. It is not as simple
                  // as calling RemoveElement(), 'cause when dealing with a cloned element,
                  // the get_parentNode() call returns the original parent! This is supported
                  // by the docs for the cloneNode() call:
                  //
                  // "Remarks
                  // The cloned node has the same property values as this node for the following 
                  // properties: nodeName property, nodeValue property, nodeType property, 
                  // >>>>>>parentNode property<<<<<<<, ownerDocument property, and, if it is an element, 
                  // attributes property. 
                  // The value of the clone's childNodes property depends on the setting of the deep flag parameter.

                  Node ownedElementNode = XMLManip.selectSingleNode(cloneNode, "UML:Element.ownedElement");
                  if( ownedElementNode != null)
                  {
                     String query = "./*[@xmi.id=\"" + elementXMIID + "\"]";
                     XMLManip.removeChild(ownedElementNode, query);
                  }

                  // Add the cloned element
                  clone.addElement(clonedEle);
               }
            }
         }
      }
   }

   /**
    *
    * Deletes all Flow relationships this element is in. This should only
    * be called when this element is to be deleted.
    *
    * @param curElement[in] The COM object representing this element
    *
    * @return HRESULT
    *
    */
   public void deleteFlowRelations()
   {
      //delete source flows
      ETList < IFlow > flows = getSourceFlows();
      if (flows != null && flows.size() > 0)
      {
         for (int i = 0; i < flows.size(); i++)
         {
				flows.get(i).delete();
         }
      }

      //delete target flows
      flows = getTargetFlows();
      if (flows != null && flows.size() > 0)
      {
         for (int i = 0; i < flows.size(); i++)
         {
 				flows.get(i).delete();
         }
      }
   }

   /**
    *
    * Replaces the current elements ( the this pointer ) xmi.id
    * with the xmi.id of dup, anywhere contained in scopingElement
    * that this xmi.id is used.
    *
    * @param dup[in] The element whose XMI id will be used to replace
    *                this element's xmi id with
    * @param scopingElement[in] The element providing the scope of the
    *                           id query.
    * @param additionalQuery[in] An additional query that can be prepended to the
    *                            replacement query. Default is ".//".
    *
    * @return HRESULT
    * @note This method assumes that the xmi.id that is being replaced
    *       by dup's xmi.id is this element ( i.e., the this pointer
    *       element ).
    *
    */
   public void replaceIds(IElement dup, IElement scopingElement)
   {
      replaceIds(dup, scopingElement, ".//");
   }

   public void replaceIds(IElement dup, IElement scopingElement, String additionalQuery)
   {
      replaceIds(getXMIID(), dup.getXMIID(), scopingElement.getNode(), additionalQuery);
   }

   /**
    *
    * For every xml attribute that contains replaceID, that id will be replaced by withID.
    * The query is focused just on the passed in element.
    *
    * @param replaceID[in] The ID to replace
    * @param withID[in] The new ID to replace replaceID with
    * @param scopingNode[in] The node the query will be placed against
    * @param additionalQuery[in] Additional query to use in the replacement query.
    *                            Default is ".//"
    *
    * @return HRESULT
    *
    */
   public void replaceIds(String replaceId, String withId, Node scopingNode, String additionalQuery)
   {
      if (replaceId.length() > 0 && withId.length() > 0)
      {
         String query = additionalQuery;
         query += "@*[contains( ., '";
         query += replaceId;
         query += "')]";

         try
         {
            List list = XMLManip.selectNodeList(scopingNode, query);
            if (list != null)
            {
               for (int i = 0; i < list.size(); i++)
               {
                   replaceXMLAttributeValue((Node) list.get(i), replaceId, withId);
               }
            }

            if (scopingNode instanceof org.dom4j.Element)
            {
               org.dom4j.Element sEle = (org.dom4j.Element) scopingNode;
               // Now make sure that any xml attributes on the scoping node are also replaced
               List map = sEle.attributes();
               if (map != null && map.size() > 0)
               {
                  for (int i = 0; i < map.size(); i++)
                  {
                     replaceXMLAttributeValue((Node) map.get(i), replaceId, withId);
                  }
               }
            }

         }
         catch (Exception e)
         {
         }
      }
   }

   /**
    *
    * Replaces substrings of value of the xml attribute passed in with values
    * found in the other parameters
    *
    * @param node[in]            The node, which should QI to an IXMLDOMAttribute
    * @param replaceStr[in]      The string to replace in the attribute value
    * @param replaceWithStr[in]  The string to replace replaceStr with
    *
    * @return HRESULT
    *
    */
   private void replaceXMLAttributeValue(Node n, String replaceId, String withId)
   {
      if (n instanceof Attribute)
      {
         Attribute attrNode = (Attribute) n;
         String value = attrNode.getValue();
         if (value != null && value.length() > 0)
         {
            attrNode.setValue(StringUtilities.replaceSubString(value, replaceId, withId));
         }
      }
   }

   /**
    * Does this element have an expanded element type or is the expanded element type always the element type?
    */
   public boolean getHasExpandedElementType()
   {
      return false;
   }

   public String toString()
   {
     	return this instanceof INamedElement ?  ((INamedElement) this).getName() : "";
   }
}
