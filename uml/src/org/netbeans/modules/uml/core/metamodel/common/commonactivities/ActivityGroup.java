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

/*
 * File       : ActivityGroup.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ActivityGroup extends Namespace implements IActivityGroup
{
	private IActivityNode m_ActivityNodeAggregate = new ActivityNode();
    private IRedefinableElement m_RedefineAggregate = new RedefinableElement();

	public void setNode(Node node)
	{
		super.setNode(node);
        m_ActivityNodeAggregate.setNode(node);
		m_RedefineAggregate.setNode(node);
	}


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#addEdgeContent(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void addEdgeContent(final IActivityEdge pEdge)
    {
        new ElementConnector<IActivityGroup>()
            .addChildAndConnect(this, true, "edgeContents", "edgeContents",
                                pEdge,
                                new IBackPointer<IActivityGroup>() {
                                    public void execute(IActivityGroup obj) {
                                        pEdge.addGroup(obj);
                                    }
                                }
        );  

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#addNodeContent(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode)
     */
    public void addNodeContent(final IActivityNode pNode)
    {
        new ElementConnector<IActivityGroup>()
            .addChildAndConnect(this, true, "nodeContents", "nodeContents",
                                pNode,
                                new IBackPointer<IActivityGroup>() {
                                    public void execute(IActivityGroup obj) {
                                        pNode.addGroup(obj);
                                    }
                                }
        ); 
    }
    
    /**
     *
     * This is an override from INamespace. If element is Actually an ActivityNode,
     * AddNodeContent will be called, as that element should be owned by the Activity.
     * If it is not an ActivityNode, then the NamespaceImpl::
     Element is called
     */


    public boolean addOwnedElement (INamedElement element)
    {
       boolean retVal = true;
       
       if( element instanceof IActivityNode)
         addNodeContent( (IActivityNode)element );
       else
         retVal = super.addOwnedElement( element );    
       
       return retVal;
    }    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#addSubGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
     */
    public void addSubGroup(IActivityGroup pGroup)
    {
        addOwnedElement(pGroup);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#getActivity()
     */
    public IActivity getActivity()
    {
		return OwnerRetriever.getOwnerByType(this, IActivity.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#getEdgeContents()
     */
    public ETList<IActivityEdge> getEdgeContents()
    {
        return new ElementCollector< IActivityEdge >().
            retrieveElementCollectionWithAttrIDs(this, "edgeContents", IActivityEdge.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#getNodeContents()
     */
    public ETList<IActivityNode> getNodeContents()
    {
        return new ElementCollector< IActivityNode >().
            retrieveElementCollectionWithAttrIDs(this, "nodeContents", IActivityNode.class);
    }
    
    public ETList<INamedElement> getOwnedElements()
    {

 
        ETList< IActivityNode > nodes = getNodeContents();
        ETList< INamedElement > ownedEls = super.getOwnedElements();

        if( nodes != null)
        {
            CollectionTranslator< IActivityNode,INamedElement > activityTrans = 
                new CollectionTranslator< IActivityNode,INamedElement >();
            activityTrans.addToCollection(nodes, ownedEls);
        }
        return ownedEls;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#getSubGroups()
     */
    public ETList<IActivityGroup> getSubGroups()
    {
        return new ElementCollector< IActivityGroup >()
            .retrieveElementCollection( (IElement)this,"UML:Element.ownedElement/*", IActivityGroup.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#removeEdgeContent(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void removeEdgeContent(final IActivityEdge pEdge)
    {
        new ElementConnector< IActivityGroup >().
            removeByID( this, 
                        pEdge, 
                        "edgeContents", 
                        new IBackPointer<IActivityGroup>() {
                            public void execute(IActivityGroup obj) {
                                pEdge.removeGroup(obj);
                            }
                        }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#removeNodeContent(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode)
     */
    public void removeNodeContent(final IActivityNode pNode)
    {
        new ElementConnector< IActivityGroup >().
            removeByID( this, 
                        pNode, 
                        "nodeContents", 
                        new IBackPointer<IActivityGroup>() {
                            public void execute(IActivityGroup obj) {
                                pNode.removeGroup(obj);
                            }
                        }
        );
    }
    
    public void removeOwnedElement(INamedElement element)
    {
       
        if( element instanceof IActivityNode)
             removeNodeContent( (IActivityNode)element );
        else
             super.removeOwnedElement( element );    
    }  

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#removeSubGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
     */
    public void removeSubGroup(IActivityGroup pGroup)
    {
        removeOwnedElement(pGroup);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup#setActivity(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity)
     */
    public void setActivity(IActivity value)
    {
      if(value != null)
        value.addGroup(this);
    }
	
	public void addOutgoingEdge( IActivityEdge pEdge )
	{
		m_ActivityNodeAggregate.addOutgoingEdge(pEdge);
	}
	
	/**
	 * method RemoveOutgoingEdge
	*/
	public void removeOutgoingEdge( IActivityEdge pEdge )
	{
		m_ActivityNodeAggregate.removeOutgoingEdge(pEdge);
	}

	/**
	 * property OutgoingEdges
	*/
	public ETList<IActivityEdge> getOutgoingEdges()
	{
		return m_ActivityNodeAggregate.getOutgoingEdges();
	}

	/**
	 * method AddIncomingEdge
	*/
	public void addIncomingEdge( IActivityEdge pEdge )
	{
		m_ActivityNodeAggregate.addIncomingEdge(pEdge);
	}

	/**
	 * method RemoveIncomingEdge
	*/
	public void removeIncomingEdge( IActivityEdge pEdge )
	{
		m_ActivityNodeAggregate.removeIncomingEdge(pEdge);
	}

	/**
	 * property IncomingEdges
	*/
	public ETList<IActivityEdge> getIncomingEdges()
	{
		return m_ActivityNodeAggregate.getIncomingEdges();
	}

	/**
	 * method AddGroup
	*/
	public void addGroup( IActivityGroup pGroup )
	{
		m_ActivityNodeAggregate.addGroup(pGroup);
	}

	/**
	 * method RemoveGroup
	*/
	public void removeGroup( IActivityGroup pGroup )
	{
		m_ActivityNodeAggregate.removeGroup(pGroup);
	}

	/**
	 * property Groups
	*/
	public ETList<IActivityGroup> getGroups()
	{
		return m_ActivityNodeAggregate.getGroups();
	}
	
	/**
	 * @param element
	 * @return
	 */
	public long addRedefinedElement(IRedefinableElement element)
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }
		 return m_RedefineAggregate.addRedefinedElement(element);
	}

	/**
	 * @param element
	 * @return
	 */
	public long addRedefiningElement(IRedefinableElement element)
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }
		 return m_RedefineAggregate.addRedefiningElement(element); 
	}

	/**
	 * @return
	 */
	public boolean getIsFinal()
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
	   return m_RedefineAggregate.getIsFinal();
	}

	/**
	 * @return
	 */
	public boolean getIsRedefined()
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
	   return m_RedefineAggregate.getIsRedefined();
	}

	/**
	 * @return
	 */
	public boolean getIsRedefining()
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
	   return m_RedefineAggregate.getIsRedefining();
	}

	/**
	 * @return
	 */
	public long getRedefinedElementCount()
	{
	   return m_RedefineAggregate.getRedefinedElementCount();
	}

	/**
	 * @return
	 */
	public ETList<IRedefinableElement> getRedefinedElements()
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
		 return m_RedefineAggregate.getRedefinedElements();
	}

	/**
	 * @return
	 */
	public long getRedefiningElementCount()
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
		 return m_RedefineAggregate.getRedefiningElementCount();
	}

	/**
	 * @return
	 */
  
	public ETList<IRedefinableElement> getRedefiningElements()
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
		 return m_RedefineAggregate.getRedefiningElements();
	}

   

	/**
	 * @param element
	 * @return
	 */
	public long removeRedefinedElement(IRedefinableElement element)
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
		 return m_RedefineAggregate.removeRedefinedElement(element);
	}

	/**
	 * @param element
	 * @return
	 */
	public long removeRedefiningElement(IRedefinableElement element)
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
		 return m_RedefineAggregate.removeRedefiningElement(element);
	}

	/**
	 * @param value
	 */
	public void setIsFinal(boolean value)
	{
		 if(m_RedefineAggregate == null)
		 {
			 m_RedefineAggregate = new RedefinableElement();
		 }   	
		 m_RedefineAggregate.setIsFinal(value);
	}
}
