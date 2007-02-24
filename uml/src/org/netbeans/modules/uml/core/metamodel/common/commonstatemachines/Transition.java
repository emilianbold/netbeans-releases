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
 * File       : Transition.java
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
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class Transition extends NamedElement implements ITransition
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#addReferredOperation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void addReferredOperation(IOperation pOper)
    {
        addElementByID(pOper, "referred");
    }
    
    protected IConstraint createCondition(String condition)
    {
        IConstraint constraint =
                        new TypedFactoryRetriever<IConstraint>()
                            .createType("Constraint");
        
        if(constraint != null)
            constraint.setExpression(condition);
            
        return constraint;
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#createPostCondition(java.lang.String)
     */
    public IConstraint createPostCondition(String condition)
    {
        return createCondition(condition);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#createPreCondition(java.lang.String)
     */
    public IConstraint createPreCondition(String condition)
    {
        return createCondition(condition);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getContainer()
     */
    public IRegion getContainer()
    {
		return OwnerRetriever.getOwnerByType(this, IRegion.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getEffect()
     */
    public IProcedure getEffect()
    {
        return new ElementCollector< IProcedure >()
            .retrieveSingleElement(this, "UML:Element.ownedElement/UML:Procedure", IProcedure.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getGuard()
     */
    public IConstraint getGuard()
    {
        return new ElementCollector< IConstraint >()
            .retrieveSingleElement(this, "UML:Element.ownedElement/UML:Constraint", IConstraint.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getIsInternal()
     */
    public boolean getIsInternal()
    {
        return getBooleanAttributeValue("isInternal", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getPostCondition()
     */
    public IConstraint getPostCondition()
    {
        IConstraint constraint 
            = new ElementCollector< IConstraint >()
                    .retrieveSingleElement(this, 
                                            "UML:Transition.postCondition/*", IConstraint.class);
        if(constraint == null)
        {
            constraint = new TypedFactoryRetriever<IConstraint>().createType("Constraint");
            if(constraint != null)
            {
				setPostCondition(constraint);
            }
        }
        return constraint;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getPreCondition()
     */
    public IConstraint getPreCondition()
    {
        IConstraint constraint 
            = new ElementCollector< IConstraint >()
                    .retrieveSingleElement(this, 
                                            "UML:Transition.preCondition/*", IConstraint.class);
        if(constraint == null)
        {
            constraint = new TypedFactoryRetriever<IConstraint>().createType("Constraint");
            if(constraint != null)
            {
				setPreCondition(constraint);
            }
        }
        return constraint;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getReferredOperations()
     */
    public ETList<IOperation> getReferredOperations()
    {
        return new ElementCollector< IOperation >()
            .retrieveElementCollectionWithAttrIDs(this, "referred", IOperation.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getSource()
     */
    public IStateVertex getSource()
    {
        return new ElementCollector< IStateVertex >()
            .retrieveSingleElementWithAttrID(this, "source", IStateVertex.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getTarget()
     */
    public IStateVertex getTarget()
    {
        return new ElementCollector< IStateVertex >()
            .retrieveSingleElementWithAttrID(this, "target", IStateVertex.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#getTrigger()
     */
    public IEvent getTrigger()
    {
        return new ElementCollector< IEvent >()
            .retrieveSingleElementWithAttrID(this, "trigger", IEvent.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#removeReferredOperation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void removeReferredOperation(IOperation pOper)
    {
        removeElementByID(pOper, "referred");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setContainer(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion)
     */
    public void setContainer(IRegion value)
    {
        setOwner(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setEffect(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure)
     */
    public void setEffect(IProcedure value)
    {
        addElement(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setGuard(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void setGuard(IConstraint value)
    {
        addElement(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setIsInternal(boolean)
     */
    public void setIsInternal(boolean value)
    {
        setBooleanAttributeValue("isInternal", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setPostCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void setPostCondition(IConstraint value)
    {
        addChild("UML:Transition.postCondition"
                    , "UML:Transition.postCondition"
                    , value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setPreCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void setPreCondition(IConstraint value)
    {
        addChild("UML:Transition.preCondition"
                    , "UML:Transition.preCondition"
                    , value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setSource(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex)
     */
    public void setSource(IStateVertex value)
    {
        new ElementConnector< ITransition >()
            .setSingleElementAndConnect(
                            this, 
                            value,
                            "source", 
                            new IBackPointer<IStateVertex>()
                            {
                                public void execute(IStateVertex obj)
                                {
                                    obj.addOutgoingTransition(Transition.this);
                                }
                            }
                            ,
                            new IBackPointer<IStateVertex>()
                            {
                                public void execute(IStateVertex obj)
                                {
                                    obj.removeOutgoingTransition(Transition.this);
                                }
                            }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setTarget(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex)
     */
    public void setTarget(IStateVertex value)
    {
        new ElementConnector< ITransition >()
            .setSingleElementAndConnect(
                            this, 
                            value,
                            "target", 
                            new IBackPointer<IStateVertex>()
                            {
                                public void execute(IStateVertex obj)
                                {
                                    obj.addIncomingTransition(Transition.this);
                                }
                            }
                            ,
                            new IBackPointer<IStateVertex>()
                            {
                                public void execute(IStateVertex obj)
                                {
                                    obj.removeIncomingTransition(Transition.this);
                                }
                            }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition#setTrigger(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent)
     */
    public void setTrigger(IEvent value)
    {
        setElement(value,"trigger");
    }

    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Transition", doc, node);
    }

}
