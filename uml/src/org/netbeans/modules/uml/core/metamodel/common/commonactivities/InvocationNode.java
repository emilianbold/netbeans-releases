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
 * File       : InvocationNode.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class InvocationNode extends ActivityNode implements IInvocationNode
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#addLocalPostCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void addLocalPostCondition(IConstraint pConstraint)
    {
        addChild("UML:InvocationNode.localPostCondition", "UML:InvocationNode.localPostCondition", pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#addLocalPrecondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void addLocalPrecondition(IConstraint pConstraint)
    {
        addChild("UML:InvocationNode.localPreCondition", "UML:InvocationNode.localPreCondition", pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#createCondition(java.lang.String)
     */
    public IConstraint createCondition(String condition)
    {
        TypedFactoryRetriever<IConstraint> factory 
                        = new TypedFactoryRetriever<IConstraint>();
        IConstraint constraint = factory.createType("Constraint");
        if(constraint != null)
            constraint.setExpression(condition);
        return constraint;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getIsMultipleInvocation()
     */
    public boolean getIsMultipleInvocation()
    {
        return getBooleanAttributeValue("isMultipleInvocation",false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getIsSynchronous()
     */
    public boolean getIsSynchronous()
    {
        return getBooleanAttributeValue("isSynchronous", true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getLocalPostConditions()
     */
    public ETList<IConstraint> getLocalPostConditions()
    {
        return new ElementCollector< IConstraint >()
            .retrieveElementCollection((IElement)this,"UML:InvocationNode.localPostCondition/*", IConstraint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getLocalPreconditions()
     */
    public ETList<IConstraint> getLocalPreconditions()
    {
        return new ElementCollector< IConstraint >()
            .retrieveElementCollection((IElement)this,"UML:InvocationNode.localPreCondition/*", IConstraint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getMultiplicity()
     */
    public IMultiplicity getMultiplicity()
    {
        return new ElementCollector< IMultiplicity >()
            .retrieveSingleElement(this, "UML:InvocationNode.multiplicity/*", IMultiplicity.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#removeLocalPostcondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void removeLocalPostcondition(IConstraint pConstraint)
    {
        UMLXMLManip.removeChild(m_Node, pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#removeLocalPrecondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void removeLocalPrecondition(IConstraint pConstraint)
    {
        UMLXMLManip.removeChild(m_Node, pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#setIsMultipleInvocation(boolean)
     */
    public void setIsMultipleInvocation(boolean isMulInvoc)
    {
        setBooleanAttributeValue("isMultipleInvocation", isMulInvoc);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#setIsSynchronous(boolean)
     */
    public void setIsSynchronous(boolean sync)
    {
        setBooleanAttributeValue("isSynchronous", sync);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#setMultiplicity(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
     */
    public void setMultiplicity(IMultiplicity mul)
    {
        addChild("UML:InvocationNode.multiplicity", "UML:InvocationNode.multiplicity", mul);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InvocationNode", doc, node);
    }       

    public String getRangeAsString()
    {
        return getMultiplicity().getRangeAsString();
    }

}
