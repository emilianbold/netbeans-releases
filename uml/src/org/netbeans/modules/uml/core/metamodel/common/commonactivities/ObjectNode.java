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

    public void onCollectionTypeModified(IMultiplicity mult, IMultiplicityRange range)
    {
        m_TypedElementAggregate.onCollectionTypeModified(mult, range);
    }
}
