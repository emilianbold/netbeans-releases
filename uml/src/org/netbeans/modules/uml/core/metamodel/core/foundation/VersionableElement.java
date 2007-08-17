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

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;


public class VersionableElement implements IVersionableElement
{
    /**
     * The XML Node on which this element is based. All access to it should be
     * through its accessor and mutator, so that subclasses can point our 
     * methods at other Nodes.
     */
    protected Node m_Node = null;
    protected org.dom4j.Node m_DOMNode = null;
    private WeakReference aggregator = new WeakReference( this );
    private boolean m_IsCloned = false;
	
    public void setAggregator(IVersionableElement aggregator)
    {
        this.aggregator = new WeakReference( aggregator );
    }
    
    protected IVersionableElement getAggregator()
    {
       return (IVersionableElement)aggregator.get();
    }
    
    /**
     *  Retrieves the XML node associated with this element.
     * HRESULT Node([out, retval] IXMLDOMNode* *pVal);
     */
    public Node getNode() {
        return m_Node;
    }
    
    public org.dom4j.Node getDOM4JNode() {
        return m_DOMNode;
    }
      /**
       *  Sets the XML node associated with this element.
       * HRESULT Node([in] IXMLDOMNode* newVal);
       */
      public void setNode(Node n) {
          m_Node = n;
      }
      
  /**
   *  Initializes the internal XML node.
   * HRESULT PrepareNode( [ in, defaultvalue(0)] IXMLDOMNode* parentNode );
   */
  public void prepareNode(Node node) {
  	if (node != null)
  	{
  		Document doc = node.getDocument();
  		establishNodePresence(doc, node);
  		org.dom4j.Element elem = getElementNode();
  		establishNodeAttributes(elem);
  	}
  }
  
  /**
 * Need to be implemented in child classes.
 */
	public void establishNodeAttributes(Element elem) {
	}
	
	/**
   * This is not implemented here. This is implemented in sub objects of this type.
   */
  public void establishNodePresence( Document doc , Node node )
  {
  }
  
  /**
   * Retrieves the VersionMe flag. This flag indicates whether or not this 
   * element is to be versioned when saved next. This is a temporary state
   * of this element, as the VersionMe flag can only exist on elements that
   * have not previously been versioned. Once done, this call, as well as the
   * put_VersionMe(), have no effect.
   *
   * @param pVal[out] The VersionMe flag. true indicates that this element
   *						  needs to be versioned. false means that the element does
   *						  not need to be versioned.
   *
   * @return HRESULTs
   * @see VersionableElementImpl::put_VersionMe()
   */
  public boolean isMarkForExtraction() 
  {
  	boolean retVal = false;
  	org.dom4j.Element element = getElementNode();
  	if (element != null)
  	{
  		String value = element.attributeValue("versionMe");
  		if (value.equals("true"))
  		{
  			retVal = true;
  		}
  	}
    return retVal;
  }
  
  /**
   * Sets the "versionMe" XML attribute on this element's DOM node. However, this
   * operation will only occur if this element has not previously been versioned.
   * If the element has been versioned, this is a noop. If the flag is set, it
   * acts as a dirty flag as well, indicating to the system that this element
   * needs to be saved.
   *
   * @param newVal[in] The flag indicating whether or not this element is to be 
   *						  versioned
   *
   * @return HRESULTs
   *    - S_FALSE      The flag was not set because the element has already been
   *                   versioned.
   *    - S_OK         Flag was set
   *    - E_INVALIDARG m_Node has not been established
   */
  public void setMarkForExtraction(boolean newVal) 
  {
	// Need to make sure this element has not already been versioned. This
	// is done by looking for the href attribute or the versionFile attribute
	if (!isVersioned())
	{
		org.dom4j.Element element = getElementNode();
		if (element != null)
		{
			if (newVal)
			{
				// We need to set the "loadedVersion" attribute, so that we can tell that this elements is currently
				// in memory, and handle it appropriately. This will prevent the unnecessary "reloading" of this event.
				// There is logic in ExternalFileManager that looks for the non-existance of the "loadedVersion" attribute
				// to kick the load logic, which is necessary, but not when dealing with elements that are in
				// the process of being extracted.
				XMLManip.setAttributeValue(element, "loadedVersion", "true");
			}
			UMLXMLManip.setAttributeValue(this, "versionMe", newVal ? "true" : "false");
		}
	}
  }

  /**
   * If this particular element is to be versioned or has been versioned, the
   * "isDirty" XML attribute is injected into the DOM node and set to the value
   * dictated by newVal. If this element has not been versioned, or is not marked
   * to be versioned, the owning document is marked as dirty.
   *
   * @param newVal[in] - true to indicate that this element is dirty, else 
   *						   - false
   *
   * @return S_OK
   */
  public void setDirty(boolean newVal) 
  {
  	boolean dirty = isDirty();
  	if (dirty != newVal)
  	{
  		org.dom4j.Element element = getElementNode();
  		if (element != null)
  		{
  			EventContextManager man = new EventContextManager();
  			if (!man.isNoEffectModification())
  			{
				// Retrieve the isDirty attribute. If it doesn't exist, then find 
				// a dirty flag somewhere in the ancester axis
  				String value = newVal ? "true" : "false";
  				
  				org.dom4j.Attribute attr = element.attribute("isDirty");
  				if (attr != null)
  				{
  					XMLManip.setAttributeValue(element, "isDirty", value);
  				}
  				else
  				{
  					try
  					{
  						//somehow dom4j is throwing exception in doing this
  					org.dom4j.Node parent = element.selectSingleNode("ancestor::*[@isDirty][1]");
  					if (parent != null)
  					{
  						XMLManip.setAttributeValue(parent, "isDirty", value);
  					}
  				}
  					catch (Exception e)
  					{
						//somehow dom4j is throwing exception in doing this, so am going to manually get the parent
						//with isDirty attribute
						Node parent = element.getParent();
						while (parent != null && parent instanceof Element)
						{
							Attribute parAttr = ((Element)parent).attribute("isDirty");
							if (parAttr != null)
							{
								XMLManip.setAttributeValue(parent, "isDirty", value);
								break;
							}
							parent = parent.getParent();
						}
  					}
  				}
  			}
  		}
  	}
  }
  
  /**
   *
   * Determines whether or not this element is dirty. If this element is not
   * version controlled, it will always return false.
   *
   * @param dirty[out] true if this element is dirty, else false
   *
   * @return HRESULT
   *
   */
  public boolean isDirty() 
  {
  	boolean dirty = false;
  	if (isVersioned())
  	{
  		//String flag = XMLManip.getAttributeValue(getNode(), "isDirty");
      Node myNode = getNode();
      
      if(myNode instanceof org.dom4j.Element)
      {
         org.dom4j.Element myElement = (org.dom4j.Element)myNode;
         String flag = myElement.attributeValue("isDirty");
         if ( flag != null && flag.equals("true"))
         {
            dirty = true;
         }
      }
  	}
    return dirty;
  }
  
  /**
   *
   * Simply a pass through to the internal GetID() call.
   *
   * @return HRESULTs
   # @see GetID()
   */
  public String getXMIID() 
  {
    return getId();
  }
  
  /**
   * Sets the XML id of this node.  Right now the only one using this is the gui
   * when a presentation object is created and needs to be set back to its original
   * id when the graphical node was first created.
   *
   * @param newVal[in]
   * 
   * @result S_OK
   */
  public void setXMIID(String newVal) 
  {
	if (getNode() != null)
	{
		org.dom4j.Element element = getElementNode();
		if (element != null)
		{
			String curId = XMLManip.getAttributeValue(element, "xmi.id");
			
			FactoryRetriever ret = FactoryRetriever.instance();
			
			if (curId != null && curId.length() > 0)
			{
				ret.removeObject(curId);
			}
			XMLManip.setAttributeValue(element, "xmi.id", newVal);
			ret.addObject(this);
		}
	}
  }
  
  /**
   *
   * Determines whether or not this element is the same as the passed in element.
   * The comparison is made between the XMI ids.
   *
   * @param element[in] The element to check against this one. If 0, result will equal
   *                    false.
   * @param result[out] true if the elements are the same, else false
   *
   * @return HRESULTs
   *
   */
  public boolean isSame(IVersionableElement elem) {
  	boolean same = false;
    String xmiid;
  	if (elem != null && (xmiid = elem.getXMIID()) != null && xmiid.length() > 0)
  	{
  		if (xmiid.equals(getId()))
  		{
  			same = true;
  		}
  	}
    return same;
  }
   
  /**
   *
   * Determines whether or not this element is the same as the passed in element.
   * The comparison is made between the XMI ids.
   *
   * @param obj The object to compare.
   *
   * @see #isSame(Lorg/netbeans/modules/uml/core/metamodel/core/foundation/IVersionableElement)
   *
   */
   public boolean equals(Object obj)
   {
      boolean retVal = false;

      if (obj instanceof IVersionableElement)
      {
         retVal = isSame((IVersionableElement)obj);         
      }
      else
      {
         retVal = super.equals(obj);
      }

      return retVal;
   }

  /**
   *
   * Retrieves the value of the xmi.id XML attribute for this node.
   *
   * @return The value of the xmi.id
   *
   */
private String getId() {
	String id = "";
	if (getNode() != null)
	{
		org.dom4j.Element elem = getElementNode();
		if (elem != null)
		{
			id = elem.attributeValue("xmi.id");
		}
	}
	return id;
}

	/**
	 *
	 * Deletes this element from the existing document. All references to the element
	 * are removed.
	 *
	 * @return HRESULT
	 *
	 */
	 public void delete() 
	 {
		// Pass in 0 here so that affected elements will
		// be calculated
	 	List ls = new ArrayList();
	 	ls.add(new Integer(0));
	 	fullDelete(ls);
	 }
	 
	/**
	 *
	 * Called to see if this element has already been deleted.
	 *
	 * @param isDeleted[out] True if deleted, else false
	 *
	 * @return HRESULT
	 *
	 */
  public boolean isDeleted() {
  	boolean retVal = false;
  	String value = XMLManip.getAttributeValue(getNode(), "isDeleted");
  	if (value != null && value.length() > 0)
  	{
  		retVal = (value.equalsIgnoreCase("true")) ? true : false;
  	}
    return retVal;
  }
  
  /**
   *
   * Before performing the actual delete, this method makes sure that
   * no other object references this element. If there is a reference, the
   * element is not deleted.
   *
   *
   * @return HRESULT
   *
   */
  public boolean safeDelete() {
  	boolean retVal = false;
  	boolean isDeleted = isDeleted();
  	if (!isDeleted)
  	{
		// First remove this element from the current DOM document. Then we'll do
		// searches.
  		Document doc = getDocument();
  		if (doc != null)
  		{
  			Element parent = getNode().getParent();
  			parent.remove(getNode());
  			String xmiid = getXMIID();
  			List list = getAllAffectedElements(doc, xmiid);
  			if (list != null)
  			{
  				if (list.size() == 0)
  				{
  					fullDelete(list);
  					retVal = true;
  				}
  				else
  				{
					// Someone is referencing the element, so it
					// is not safe to delete...
  				}
  			}
  		}
  	}
    return retVal;
  }

  /**
  * Retrieves the document that owns this node.
  *
  * @param doc[out] The retrieved document
  * @return HRESULTs
  *
  */ 
  protected Document getDocument() {
	Document doc = null;
	Node n = getNode();
	if (n != null)
	{
		doc = n.getDocument();
	}
    if (doc == null && getAggregator() instanceof ITransitionElement)
    {
        IElement owner = ((ITransitionElement) getAggregator()).getFutureOwner();
        if (owner != null)
        {
            if (owner instanceof VersionableElement)
                doc = ((VersionableElement) owner).getDocument();
            else if (owner.getNode() != null)
                doc = owner.getNode().getDocument();
        }
    }
	return doc;
  }

  /**
   * Retrieves the Document containing the given IElement. This method is
   * guaranteed to work even for ITransitionElement instances, and for IElements
   * that are children of ITransitionElements.
   * 
   * @param e The IElement whose Document we want.
   * @return The Document located, or null if the element is not rooted in a
   *          Document.
   */
  protected Document getDocument(IElement e)
  {
      if (e == null) return null;
      
	 // Walk up element hierarchy and get the Document off the root owner.
      
	 while (e.getOwner() != null)
		 e = e.getOwner();
      
      Node n = e.getNode();
      if (n == null) return null;
	 return n.getDocument();
  }

/**
   *
   * Performs the actual delete of this element from the Project ( DOM ).
   *
   * @param affectedElements[in] Elements that will be affected by this deletion.
   *                             If 0 is passed, the elements will be calculated.
   *
   * @return HRESULT
   *
   */
private void fullDelete(List list) {
	Node n = getNode();
	if (n != null)
	{
		if (!XMLManip.getAttributeBooleanValue(n, "isDeleted"))
		{
			IProject proj = null;
			// Retrieve the IProject that this elemetn is a part of, so that
			// we can properly add this element to the queue of elements that
			// need to be disposed of when the user saves the Project next.
			if (this instanceof IElement)
			{
				IElement ele = (IElement)this;
				proj = ele.getProject();
			}
			boolean proceed = firePreDelete(this);
			if (proceed)
			{
				// The following block of code, down to the FireDelete, used
				// to take place AFTER the CleanReferences. Here is why we 
				// changed it:
				// When the user (addin) gets a Delete, he still wants to
				// navigate relationships that HE has created so that he 
				// can do other stuff that HE needs to do. For example, during
				// and attribute creation, the JavaRequestProcessor created 
				// Read and Write accessors and created Realization dependencies
				// between the attribute and these accessors. When the 
				// attribute is deleted, these accessors need to be deleted. But,
				// the RP cannot find them if the realizations have been cleaned 
				// up prior to getting the event. We don't want to do this stuff
				// in the predelete, because a delete might be denied AFTER the
				// RP responded and deleted the accessors (this is the common
				// problem of "deny" versus "undo"). So, we want the element
				// to be deleted from the document, but not de-referenced until after
				// the event has fired.  And, just to be absolutely clear on the 
				// point, this has nothing to do with the clone of the element.
				// The RP is correctly using the clone, but the xmiid of the clone
				// is the same as the original, and it is this id that is queried
				// for in the document.
         
				// Now set the "isDeleted" xml attribute on the current node
				// in order to indicate to anyone caring that this element has
				// been deleted, but just hasn't gone out of memory yet...
         
				// Retrieve the node AFTER the fire of the pre delete, as it is 
				// possible ( with version control ) to have the actual node
				// replaced
				if (n instanceof org.dom4j.Element)
				{
					org.dom4j.Element elem = (org.dom4j.Element)n;
					elem.addAttribute("isDeleted", "true");
					if (proj != null)
					{
						// Tell the Project that it is dirty
						proj.setChildrenDirty(true);
						IElementDisposal dispos = proj.getElementDisposal();
						if (dispos != null)
						{
							dispos.queueForDisposal(this);
						}
					}
					fireDelete(this);
				}
				performDependentElementCleanup(this);

				// Remove the node from the main tree. When we save, the element
				// will be gone.
				String xmiid = getXMIID();
				Document doc = getDocument();
				if (n != null)
				{
					Element parent = n.getParent();
					if (parent != null)
					{
						parent.remove(n);
						if (list != null){
							// Find out where this element is being referenced
							// throughout the document 
							cleanReferences(doc, xmiid);
						} 
					}
				}
				
				if (proj != null)
				{
					// Notify the TypeManager that this type has been deleted.
					ITypeManager typeMan = proj.getTypeManager();
					if (typeMan != null)
					{
						typeMan.addToDeletedIds(xmiid);
					}
				}
			}
		}
	}
  }

/**
 *
 * Fires the low-level "ElementDeleted" event.
 *
 * @param ver[in] The COM object that represents this impl instance
 *
 * @return HRESULT
 *
 */
protected void fireDelete(IVersionableElement element) {
	EventDispatchRetriever ret = EventDispatchRetriever.instance();
	Object obj = ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
	if (obj instanceof IElementLifeTimeEventDispatcher)
	{
		IElementLifeTimeEventDispatcher disp = (IElementLifeTimeEventDispatcher)obj;
		IEventPayload payload = disp.createPayload("ElementDeleted");
		disp.fireElementDeleted(element, payload); 
	}
	
}
/**
 *
 * Fires the low level "ElementPreDelete" event.
 *
 * @param ver[in] The COM object that represents this impl instance
 *
 * @return - true if the deletion of this element should occur, else
 *         - false if not
 *
 */
protected boolean firePreDelete(IVersionableElement element) {
	boolean proceed = true;
	EventDispatchRetriever ret = EventDispatchRetriever.instance();
	Object obj = ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
	if (obj instanceof IElementLifeTimeEventDispatcher)
	{
		IElementLifeTimeEventDispatcher disp = (IElementLifeTimeEventDispatcher)obj;
		IEventPayload payload = disp.createPayload("ElementPreDelete");
		proceed = disp.fireElementPreDelete(element, payload); 
	}
	return proceed;
}

/**
 *
 * Removes all references to the passed-in ID from any element referring to it.
 *
 * @param doc[in] The document to fully query
 * @param xmiID[in] The id to remove references to
 *
 * @return HRESULT
 *
 */
private void cleanReferences(Document doc, String xmiid) {
   
    if((this instanceof org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier) && 
       (!(this instanceof org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation)))
    {
       UMLXMLManip.cleanReferences(doc, xmiid);
    }
}
/**
   *
   * Retrieves all the XML attribute elements that refer in some way to the passed in XMI ID.
   *
   * @param doc[in] The document that contains the elements to retrieve
   * @param xmiID[in] The id of the element to match against
   * @param elements[out] All XML attribute elements that reference the passed in ID
   *
   * @return HRESULT
   * @note The elements collection returned is filled with IXMLDOMAttribute objects
   *
   */
protected List getAllAffectedElements(Document doc, String xmiid) {
	List toRet = UMLXMLManip.getAllAffectedElements(doc, xmiid);
	return toRet;
}

/**
 *
 * Retrieves the file name that this element is housed in. This will only be valid
 * if this element has been versioned.
 *
 * @param pVal[out] The filename
 *
 * @return HRESULT
 *
 */
  public String getVersionedFileName() {
  	String retVal = "";
  	if (isVersioned())
  	{
  		String href = XMLManip.getAttributeValue(getNode(), "href");
  		if (href.length() > 0)
  		{
			// The href attribute value is generally in the form
			// of filename#xpathlocation. We just want the filename
  			ETPairT<String, String> obj = URILocator.uriparts(href);
  			if (obj != null)
  			{
  				retVal = obj.getParamOne();
  			}
  		}
  	}
    return retVal;
  }
  /**
   *  The name of the file that this element is versioned in. If the element has not been versioned, this value is ignored.
   * HRESULT VersionedFileName([in] BSTR newVal);
   */
  public void setVersionedFileName(String str) 
  {
  	//c++ code not doing anything.
  }
  
  /**
   *
   * Determines whether or not this element has been previously versioned.
   *
   * @param bIsVersioned[out] - true if this element has been versioned, else
   *                          - VARAINT_FALSE.
   *
   * @return HRESULT
   *
   */
  public boolean isVersioned() {
  	boolean versioned = false;
  	if (isVersioned(null))
  	{
  		versioned = true;
  	}
    return versioned;
  }

  /**
   * Checks to see if this element has been previously versioned or not.
   *
   * @param element[in] The element to check. If 0, then the element of this
   *                    node is used
   *
   * @return True if this element has previously been versioned, else false.
   */
  public boolean isVersioned( Element nodeElement)
  {
  	boolean versioned = false;
  	org.dom4j.Element element = nodeElement;
  	if (element == null)
  	{
  		element = getElementNode();
  	}
  	
  	if (element != null)
  	{
  	org.dom4j.Attribute attr = element.attribute("href");
  	org.dom4j.Attribute attr1 = element.attribute("isVersioned");
  	
  	if (attr != null || attr1 != null)
  	{
  		versioned = true;
		}
  	}
  	
  	return versioned;
  }

  /**
   *
   * Saves the contents of this element out to the etx file if this element
   * is indeed versioned. If the element is not dirty, then the only thing that
   * will happen as a result of this call is that the element will be unloaded
   * from memory
   *
   * @param bSaved[out] - true if the element was saved, else 
   *                    - false. It will return false if the element
   *                      didn't need to be saved.
   *
   * @return HRESULT
   *
   */
  public boolean saveIfVersioned() 
  {
  	boolean saved = false;

  	saved = isDirty();
  	
	// Regardless of dirty state, if the element is versioned, we need to call
	// the PushExternalNode in order to unload the element from memory
	if (isVersioned())
	{
		ExternalFileManager man = new ExternalFileManager();
		man.setRootFileName(this);
		man.pushExternalNode(getNode());
	}
  	
    return saved;
  }
  
  /**
   *
   * Duplicates this element. The duplicate element is the same as the current element
   * except that it will be assigned a new XMI id and removed from the Namespace that the 
   * current element is in. Also, if the current element is version controlled, the 
   * duplicate element will NOT have the version control information established.
   *
   * @param dup[out] The duplicated element
   *
   * @return HRESULT
   *
   */
  public IVersionableElement duplicate() {
  	IVersionableElement dup = null;
  	
	EventDispatchRetriever ret = EventDispatchRetriever.instance();
	IElementLifeTimeEventDispatcher disp = null;
	Object obj = ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
	if (obj instanceof IElementLifeTimeEventDispatcher)
	{
		disp = (IElementLifeTimeEventDispatcher)obj;
	}
	boolean proceed = true;
	if (disp != null)
	{
		IEventPayload payload = disp.createPayload("ElementPreDuplicated");
		proceed = disp.fireElementPreDuplicated(this, payload);
	}
	if (proceed)
	{
	    // Don't fire any events while duplicating
	    boolean orig = EventBlocker.startBlocking();
	    try
	    {
	        dup = performDuplication();
	    }
	    finally
	    {
	        EventBlocker.stopBlocking(orig);
	    }
        
		if (disp != null && dup != null)
		{
			IEventPayload payload = disp.createPayload("ElementDuplicated");
			disp.fireElementDuplicated(dup, payload);
		}
	}
  	
    return dup;
  }
  
  /**
   *
   * This routine returns the URI to this element, if the element has been extracted
   * ( most likely due to version control ) from the Project's DOM. This routine
   * is smart in that if this element is encapsulated by an element that was extracted,
   * that element's URI is used to build this ones.
   *
   * @param uri[out] The URI of this object. If it is determined that this element 
   *                 or any of its parents has not been extracted, then the XMI id of 
   *                 this element is returned
   *
   * @return HRESULT
   *
   */
  public String getVersionedURI() 
  {
  	String uri = "";
  	boolean modified = false;
  	modified = verifyInMemoryStatus();
  	String versionedURI = UMLXMLManip.getVersionedURI(getNode()).toString();
  	if (versionedURI != null && versionedURI.length() > 0)
  	{
  		uri = versionedURI;
  	}
    return uri;
  }
  
  /**
   *
   * Removes all version control information from this element. Once this is done,
   * the element will appear as if it had never been versioned.
   *
   * @return HRESULT
   *
   */
  public void removeVersionInformation() 
  {
  	String versionedFile = getVersionedFileName();
  	String href = UMLXMLManip.getAttributeValue(getNode(), "href");
  	if (href != null && href.length() > 0)
  	{
  		cleanVersionReferences(href);
  	}
  	
  	if (getNode() instanceof org.dom4j.Element)
  	{
  		org.dom4j.Element element = (org.dom4j.Element)getNode();
  		org.dom4j.Attribute attr = element.attribute("isDirty");
  		if (attr != null)
  		{
  			element.remove(attr);
  		}

		attr = element.attribute("isVersioned");
		if (attr != null)
		{
			element.remove(attr);
		}

		attr = element.attribute("loadedVersion");
		if (attr != null)
		{
			element.remove(attr);
		}

		attr = element.attribute("href");
		if (attr != null)
		{
			element.remove(attr);
		}
  	}
  	
  	if (versionedFile != null && versionedFile.length() > 0)
  	{
		// Remove the versioned .etx file. Make sure that
		// we DO NOT remove the .etd file
  		if (this instanceof IElement)
  		{
  			IElement curVer = (IElement)this;

			// Be sure to set the dirty flag of this element's owner. This
			// was causing problems while saving if the user just removed
			// a class from source control and then tried to save. They
			// would get "Access Denied" errors.
  			IElement owner = curVer.getOwner();
  			if (owner != null)
  			{
  				owner.setDirty(true);
  			}
  		}
  		
  		if (this instanceof IProject)
  		{
  		}
  		else
  		{
  			File file = new File(versionedFile);
  			file.delete();
  		}
  	}
  }
  
  /**
   *
   * Removes all references to the uri version of this element's xmi id with
   * the raw xmi id. The element will also not be a part of the type file
   * once done
   *
   * @param href[in] Ignored
   *
   * @return HRESULT
   *
   */
	private void cleanVersionReferences(String href)
	{
		IProject proj = getProject();
		if (proj != null)
		{
			ITypeManager typeMan = proj.getTypeManager();
			if (typeMan != null)
			{
				typeMan.removeFromTypeLookup(this);
			}
		}
	}
  /**
   * Retrieves a relative path from the Project that this element is in
   * and the path passed in.
   *
   * @param path[in] The path to make relative
   *
   * @return HRESULT
   *
   */
  public String retrieveRelativePath( String path )
  {
	 return UMLXMLManip.retrieveRelativePath( getNode(), path );
  }
  
  /**
   * Verifies that this element is properly represented in memory. During version control
   * processing, it is possible for an element to be 'orphaned' in memory, as the parent
   * elements of this element could have been unloaded from memory, but due to a client
   * holding on to this object, this object remains. This method ensures that proper
   * object ownership is maintained.
   *
   * @param wasModified[out] - true if the xml representation behind this element
   *                           has been changed as a result of verification, else
   *                         - false if not
   *
   * @return HRESULT
   */
  public boolean verifyInMemoryStatus() {
  	boolean wasModified = false;
  	
  	if (getNode() != null)
  	{
		// Check to see if the node is in a state that needs to be verified.
		// We may just want to always call VerifyInMemoryStatus eventually,
		// but I want to minimize the amount of work we're doing in this
		// routine, as all paths lead here. ;)
  		Node parentNode = getNode().getParent();
  		if (parentNode == null)
  		{
			// We have an orphaned element, so Verify
  			if (this instanceof IElement)
  			{
  				IElement actual = (IElement)this;
  				IProject proj = actual.getProject();
  				if (proj != null)
  				{
  					ITypeManager typeMan = proj.getTypeManager();
  					if (typeMan != null)
  					{
  						wasModified = typeMan.verifyInMemoryStatus(actual);
  					}
  				}
  			}
  		}
  	}
  	
    return wasModified;
  }

  /**
   *  Retrieves the line number associated with this element.
   * HRESULT LineNumber([out, retval] long * lineNumber );
   */
  public int getLineNumber() 
  {
  	int lineNum = -1;
  	String value = XMLManip.getAttributeValue(getNode(), "lineNumber");
  	if (value != null && value.length() > 0)
  	{
  		lineNum = Integer.parseInt(value);
  	}
    return lineNum;
  }
  
  /**
   *  Sets the line number associated with this element.
   * HRESULT LineNumber([in] long lineNumber );
   */
  public void setLineNumber(int num) 
  {
  	String value = Integer.toString(num);
  	XMLManip.setAttributeValue(getNode(), "lineNumber", value);
  }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#getElementNode()
	 */
	public org.dom4j.Element getElementNode() {
        // Define based on getNode(), because we're likely to override getNode()
        // in subclasses.
		return (org.dom4j.Element) getNode();
	}
	
	/**
	 * Creates the actual XML node of the passed in type and establishes the created 
	 * element as a child of the passed in parent node. All elements that wish to participate
	 * in the persistence mechanism must call this method.
	 *
	 * @param nodeName[in] The name of the node to create, such as "UML:Model"
	 * @param doc[in]	   The document the node will be a part of
	 * @param parent[in]   The parent of the new node
	 *
	 * @return HRESULTs
	 *
	 */
	protected void buildNodePresence(String nodeName, Document doc, Node parent)
	{
		try {
         Element parentElement = (org.dom4j.Element)parent;
			org.dom4j.Element element = parentElement.addElement( XMLManip.getQName(parentElement, nodeName));
                
			//parent.appendChild(element);
			Node n = (Node)element;
			setNode(n);

			// The importance of the xmi.id attribute is all important.
			// The next 3 lines used to be in EstablishNodeAttributes(),
			// but after we added the in memory object list found on
			// FactoryRetriever ( 1/25/02 ), the ID needed to be established
			// before the call to EstablishNodePresence. Interestingly, all
			// objects participating in the persistence mechanism must call
			// BuildNodePresence before doing anything else, so this is 
			// a perfect place to establish the ID.
			String idStr = UMLXMLManip.generateId(true);
			setXMIID(idStr);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
    /**
	 *
	 * This method is designed to be overridden in subsequent sub-classes.
	 *
	 * @param thisElement[in] The COM object that represents this impl class
	 *
	 * @return HRESULT
	 *
	 */
	protected void performDependentElementCleanup(IVersionableElement elem)
	{
		
	}
	
	/**
	 *
	 * Performs the part of the duplication task specific to VersionableElement.
	 *
	 * @param dup[out] The duplicated element
	 *
	 * @return HRESULT
	 *
	 */
	protected IVersionableElement performDuplication()
	{
		IVersionableElement retEle = null;
		TypedFactoryRetriever ret = new TypedFactoryRetriever();
		IVersionableElement clone = (IVersionableElement)ret.clone(getNode());
		
		if (clone != null)
		{
			// OK, we have the clone, now we need to create new XMI IDs
			String idStr = UMLXMLManip.generateId(true);
			Node clonedNode = clone.getNode();
			if (clonedNode.getNodeType() == Node.ELEMENT_NODE)
			{
				org.dom4j.Element clonedElement = (org.dom4j.Element)clonedNode;
				
				// Use the XMLManip in this case so we don't cause a set event.
				XMLManip.setAttributeValue(clonedElement, "xmi.id", idStr);
				
				// Now check to see if this element has been versioned. If it has, we
				// need to remove the xml attributes that tell us that it has so that
				// this element will be clean of version control constructs. In this way,
				// the element is "new" as far as version control is concerned.
				boolean isVersioned = false;
				isVersioned = clone.isVersioned();
				if (isVersioned)
				{
					clonedElement.attribute("isVersioned").detach();
					clonedElement.attribute("href").detach();
					clonedElement.attribute("scmID").detach();
				}
                                
                                // Since wew are duplicating the element
                                // it is now a new model element.  It
                                // is not a copy of the model element.
                                clonedElement.attribute(FactoryRetriever.IS_CLONED_ATTR).detach();
			}
			retEle = clone;
		}
                
                FactoryRetriever retriever = FactoryRetriever.instance();
                retriever.clearClonedStatus(this);
		return retEle;
	}

	/**
	 *
	 * Does an insert of the nodes coming in. This is a generic method that assumes that existingVer
	 * is currently owned in some way by the current VersionableElement, and that newVer is being inserted
	 * into the same collection that existingVer is a part of.
	 *
	 * @param owningElement[in]   The actual xml element that is the direct owner of the elements. If this
	 *                            parameter is 0, the current element will be queried for the "UML:Element.ownedElement"
	 *                            element, and that will be used as the owning element.
	 * @param existingVer[in]     The existing element in the collection. Can be 0.
	 * @param newVer[in]          The new element to insert before existingVer.
	 *
	 * @return HRESULT
	 *
	 */
//	public void insertNode(org.dom4j.Element owningElement, IVersionableElement existingVer,
//							IVersionableElement newVer)
//	{
//		Node existingNode = existingVer.getNode();
//		Node newNode = newVer.getNode();
//		
//		if (owningElement == null)
//		{
//			// Retrieve the UML:Element.owndElement element
//			owningElement = (Element) XMLManip.selectSingleNode(getNode(), "UML:Element.ownedElement");
//		}
//		
//		if (owningElement != null)
//		{
//			owningElement.insertBefore(newNode, existingNode);
//		}
//	}

	public void insertNode(org.dom4j.Element owningElement, IVersionableElement existingVer,
							IVersionableElement newVer)
	{
		Node existingNode = existingVer != null? existingVer.getNode() : null;
		Node newNode = newVer.getNode();
		
		if (owningElement == null)
		{
			// Retrieve the UML:Element.owndElement element
			owningElement = (org.dom4j.Element) XMLManip.selectSingleNode(getNode(), "UML:Element.ownedElement");
		}
		
		if (owningElement != null)
		{
            newNode.detach();
            if (existingNode == null)
                owningElement.add(newNode);
            else
            {
                // There's no clean way to insertBefore in dom4j.
                List elements = owningElement.elements();
                boolean readd = false;
                for (Iterator iter = elements.iterator(); iter.hasNext();)
                {
                    Element element = (Element) iter.next();
                    if (element.equals(existingNode))
                    {
                        owningElement.add(newNode);
                        readd = true;
                    }
                    
                    if (readd)
                    {
                        element.detach();
                        owningElement.add(element);
                    }
                }
            }
		}
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setDom4JNode(org.dom4j.Node)
	 */
	public void setDom4JNode(org.dom4j.Node n)
	{
		m_DOMNode = n;
        setNode(n);
	}
	
	/**
	 *
	 * Retrieves the IProject this element is in.
	 *
	 * @param proj[out]        The IProject disguised as in IDispatch
	 * @param thisElement[out] If thisElement comes in as a non-null pointer,
	 *                         will contain the COM object representing this
	 *                         impl class
	 *
	 * @return HRESULT
	 *
	 */
	public IProject getProject()
	{
		IProject proj = null;
		if (this instanceof IElement)
		{
			IElement element = (IElement)this;
			proj = element.getProject();
		}
		return proj;
	}
	
	protected void finalize()
	{
		try
		{
			super.finalize();
			FactoryRetriever ret = FactoryRetriever.instance();
         if(isClone() == false)
         {
            String xmiid = XMLManip.getAttributeValue(m_Node, "xmi.id");
            if (xmiid != null && xmiid.length() > 0)
            {
               ret.removeObject(xmiid);
            }
         }
		}
		catch (Throwable e)
		{
		}
	}
   
   public boolean isClone()
   {
      return m_IsCloned;
   }
   
   public void setIsClone(boolean value)
   {
      m_IsCloned = value;
   }
}
