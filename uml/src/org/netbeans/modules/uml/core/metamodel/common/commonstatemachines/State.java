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
 * File       : State.java
 * Created on : Sep 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class State extends StateVertex implements IState
{
    public String getExpandedElementType()
    {
        if (getIsComposite()) return "CompositeState";
        else if (getIsSubmachineState()) return "SubmachineState";
        else return getElementType();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#addContent(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion)
     */
    public void addContent(IRegion pReg)
    {
        super.addOwnedElement(pReg);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#addDefferableEvent(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent)
     */
    public void addDefferableEvent(IEvent pEvent)
    {
        addElementByID(pEvent, "defferableEvent");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getContents()
     */
    public ETList<IRegion> getContents()
    {
        return new ElementCollector< IRegion >()
            .retrieveElementCollection((IState)this, "UML:Element.ownedElement/*", IRegion.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getDeferrableEvents()
     */
    public ETList<IEvent> getDeferrableEvents()
    {
        return new ElementCollector<IEvent>()
            .retrieveElementCollectionWithAttrIDs(this, "deferrableEvent", IEvent.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getDoActivity()
     */
    public IProcedure getDoActivity()
    {
        return new ElementCollector< IProcedure >()
            .retrieveSingleElement(this, "UML:State.doActivity/*", IProcedure.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getEntry()
     */
    public IProcedure getEntry()
    {
        return new ElementCollector< IProcedure >()
            .retrieveSingleElement( this, "UML:State.entry/*", IProcedure.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getExit()
     */
    public IProcedure getExit()
    {
        return new ElementCollector< IProcedure >()
            .retrieveSingleElement( this, "UML:State.exit/*", IProcedure.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getFirstContent()
     */
    public IRegion getFirstContent()
    {
        ETList<IRegion> regions = getContents();
        
        if(regions.size() > 0)
            return regions.get(0);
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getIsComposite()
     */
    public boolean getIsComposite()
    {
        return getBooleanAttributeValue("isComposite", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getIsOrthogonal()
     */
    public boolean getIsOrthogonal()
    {
        return getBooleanAttributeValue("isOrthogonal", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getIsSimple()
     */
    public boolean getIsSimple()
    {
        return getBooleanAttributeValue("isSimple", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getIsSubmachineState()
     */
    public boolean getIsSubmachineState()
    {
        return getBooleanAttributeValue("isSubmachineState", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getStateInvariant()
     */
    public IConstraint getStateInvariant()
    {
        return new ElementCollector< IConstraint >()
            .retrieveSingleElement(this, "UML:Element.ownedElement/UML:Constraint", IConstraint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#getSubmachine()
     */
    public IStateMachine getSubmachine()
    {
        return new ElementCollector< IStateMachine >()
            .retrieveSingleElementWithAttrID(this, "submachine", IStateMachine.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#removeContent(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion)
     */
    public void removeContent(IRegion pReg)
    {
        removeElement(pReg);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#removeDeferrableEvent(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent)
     */
    public void removeDeferrableEvent(IEvent pEvent)
    {
        removeElementByID(pEvent, "defferableEvent");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setDoActivity(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure)
     */
    public void setDoActivity(IProcedure value)
    {
        if (value != null)
           UMLXMLManip.setAttributeValue(value, "owner", getXMIID());
        addChild("UML:State.doActivity", "UML:State.doActivity", value);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setEntry(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure)
     */
    public void setEntry(IProcedure value)
    {
        if (value != null)
           UMLXMLManip.setAttributeValue(value, "owner", getXMIID());
        addChild("UML:State.entry", "UML:State.entry", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setExit(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure)
     */
    public void setExit(IProcedure value)
    {
        if (value != null)
           UMLXMLManip.setAttributeValue(value, "owner", getXMIID());
        addChild("UML:State.exit", "UML:State.exit", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setIsComposite(boolean)
     */
    public void setIsComposite(boolean value)
    {
        if(value)
            ensureAtLeastOneContentRegionExists();

        setBooleanAttributeValue("isComposite", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setIsOrthogonal(boolean)
     */
    public void setIsOrthogonal(boolean value)
    {
        if(value)
            ensureAtLeastOneContentRegionExists();

         setBooleanAttributeValue("isOrthogonal", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setIsSimple(boolean)
     */
    public void setIsSimple(boolean value)
    {
        setBooleanAttributeValue("isSimple", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setIsSubmachineState(boolean)
     */
    public void setIsSubmachineState(boolean value)
    {
        setBooleanAttributeValue("isSubmachineState", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState#setStateInvariant(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void setStateInvariant(IConstraint value)
    {
        addElement(value);
    }
    
    
    public void setSubmachine(IStateMachine value)
    {
        new ElementConnector< IState >()
            .setSingleElementAndConnect(
                    this, 
                    value, 
                    "submachine", 
                    new IBackPointer<IStateMachine>()
                    {
                        public void execute(IStateMachine obj)
                        {
                            obj.addSubmachineState(State.this);
                        }
                    },
                    new IBackPointer<IStateMachine>()
                    {
                        public void execute(IStateMachine obj)
                        {
                            obj.removeSubmachineState(State.this);
                        }
                    }
         );

    }
    
    public boolean addOwnedElement(INamedElement element)
    {
       boolean retVal = true;
       
        if(element instanceof IStateVertex)
        {
            IRegion region = getFirstContent();
            if(region != null)
                region.addSubVertex((IStateVertex)element);
        }
        else
        {
            retVal = super.addOwnedElement(element);
        }
       
       return retVal;
    }
    
    protected void ensureAtLeastOneContentRegionExists()
    {
        ETList<IRegion> regions = getContents();
        if(regions.size() > 0)
            return;
        IRegion region 
            = new TypedFactoryRetriever<IRegion>().createType("Region");
            
        if(region != null)
            addContent(region);            
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:State", doc, node);
        ensureAtLeastOneContentRegionExists();
    }     

	/**
	 * Does this element have an expanded element type or is the expanded element type always the element type?
	 */
	public boolean getHasExpandedElementType()
	{
		return true;
	}

	/**
	 *
	 * Retrieves the elements that this StateMachine owns dependent on how many regions
	 * the StateMachine contains. This allows the tree and property editor to display 
	 * the Region nodes only when multiple regions are present. If there is only one,
	 * then the contents of that region can be displayed without requiring the one
	 * tree node.
	 *
	 * @param pVal[out] The collection of Namespace elements.
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamespace> getContainedElements()
	{
		ETList<INamespace> retObj = null;
		ETList<IRegion> regions = getContents();
		if (regions != null)
		{
			int count = regions.size();
			if (count == 1)
			{
				IRegion pRegion = regions.get(0);
				retObj = getRegionContents(pRegion);
			}
			else
			{
				retObj = new ETArrayList<INamespace>();
				for (int i=0; i<count; i++)
				{
					IRegion pReg = regions.get(i);
					retObj.add(pReg);
				}
			}
		}
		return retObj;
	}

	/**
	 *
	 * Retrieves the contents of the passed in IRegion and places them in the 
	 * passed in Namespaces collection
	 *
	 * @param region[in] The region to get.
	 * @param spaces[in] The collection that will be populated 
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamespace> getRegionContents(IRegion region)
	{
		ETList<INamespace> retObj = null;
		if (region != null)
		{
			ETList<INamedElement> ownedElems = region.getOwnedElements();
			if (ownedElems != null)
			{
				retObj = new ETArrayList<INamespace>();
				int count = ownedElems.size();
				for (int i=0; i<count; i++)
				{
					INamedElement namedEle = ownedElems.get(i);
					if (namedEle instanceof INamespace)
					{
						retObj.add((INamespace)namedEle);
					}
				}
			}
		}
		return retObj;
	}
}
