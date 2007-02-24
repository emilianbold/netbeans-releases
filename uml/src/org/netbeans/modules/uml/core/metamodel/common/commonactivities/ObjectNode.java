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
 * File       : ObjectNode.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.TypedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ObjectNode extends ActivityNode implements IObjectNode
{
	private TypedElement m_TypedElementAggregate = new TypedElement();
	
	public void setNode(Node node)
	{
		super.setNode(node);
		m_TypedElementAggregate.setNode(node);
	}
	
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#addInState(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState)
     */
    public void addInState(IState pState)
    {
        addElementByID(pState, "inState");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#getInStates()
     */
    public ETList<IState> getInStates()
    {
        return new ElementCollector< IState >().
            retrieveElementCollectionWithAttrIDs(this, "inState", IState.class);
    }
    
    public int getOrdering()
    {
        return getObjectNodeOrderingKind("ordering");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#getSelection()
     */
    public IBehavior getSelection()
    {
        return new ElementCollector< IBehavior >()
            .retrieveSingleElementWithAttrID( this,"selection", IBehavior.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#getUpperBound()
     */
    public IValueSpecification getUpperBound()
    {
        return new ElementCollector< IValueSpecification >()
            .retrieveSingleElement( this, "UML:ObjectNode.upperBound/*", IValueSpecification.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#removeInState(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState)
     */
    public void removeInState(IState pState)
    {
        removeElementByID(pState, "inState");
    }
    
    public void setOrdering(int nKind)
    {
        setObjectNodeOrderingKind("ordering",nKind);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#setSelection(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
     */
    public void setSelection(IBehavior value)
    {
        setElement(value, "selection");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode#setUpperBound(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void setUpperBound(IValueSpecification value)
    {
        addChild("UML:ObjectNode.upperBound", "UML:ObjectNode.upperBound", value);
    }

	//TypedElement methods
	public void setType(IClassifier classifier)
	{			
		m_TypedElementAggregate.setType(classifier);	
	}
	
	public IClassifier getType()
	{
		return m_TypedElementAggregate.getType();
	}

	public IMultiplicity getMultiplicity()
	{
	   return m_TypedElementAggregate.getMultiplicity();
	}

	public void setMultiplicity(  IMultiplicity  newVal)
	{
		m_TypedElementAggregate.setMultiplicity(newVal);
	}
	
	public void performDuplicationProcess( ITypedElement dupType )
	{
		m_TypedElementAggregate.performDuplicationProcess(dupType);
	}
	public String processProposedType(String type)
	{
		return m_TypedElementAggregate.processProposedType(type);
	}
	public void setIsSet(boolean val)
	{		
		m_TypedElementAggregate.setIsSet(val);	
	}
	public boolean getIsSet()
	{
		return  m_TypedElementAggregate.getIsSet();
	}
	public String getTypeID()
	{
		return m_TypedElementAggregate.getTypeID();
	}			

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	 */
	public boolean onPreLowerModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
	{
		return m_TypedElementAggregate.onPreLowerModified(mult, range, proposedValue);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onLowerModified(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onLowerModified(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	 */
	public boolean onPreUpperModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
	{
		return m_TypedElementAggregate.onPreUpperModified(mult, range, proposedValue);	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onUpperModified(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onUpperModified(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public boolean onPreRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
	{
		return m_TypedElementAggregate.onPreRangeAdded(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onRangeAdded(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public boolean onPreRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
	{
		return m_TypedElementAggregate.onPreRangeRemoved(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onRangeRemoved(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean)
	 */
	public boolean onPreOrderModified(IMultiplicity mult, boolean proposedValue)
	{
		return m_TypedElementAggregate.onPreOrderModified(mult, proposedValue);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
	 */
	public void onOrderModified(IMultiplicity mult) 
	{
		m_TypedElementAggregate.onOrderModified(mult);
	}			

}
