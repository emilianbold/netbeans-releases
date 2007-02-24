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

package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ContactManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class CombinedFragment extends InteractionFragment 
    implements ICombinedFragment
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#delete()
     */
    public void delete()
    {
        IInteractionOperand toOperand = getEnclosingOperand();
        if (toOperand != null)
        {
            ETList<IInteractionOperand> operands = getOperands();
            if (operands != null)
            {
                for (int i = 0; i < operands.size(); ++i)
                {
                    IInteractionOperand fromOperand = operands.get(i);
                    if (fromOperand != null)
                        moveMessages(fromOperand, toOperand);
                }
            }
        }
        super.delete();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#getGates()
     */
    public ETList<IGate> getGates()
    {
        return new ElementCollector< IGate >().retrieveElementCollection( 
                getNode(), "UML:CombinedFragment.expressionGate/*", IGate.class);
    }
    
    public void removeGate(IGate gate)
    {
        UMLXMLManip.removeChild(getNode(), gate);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#addGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void addGate(IGate gate)
    {
        addChild( "UML:CombinedFragment.expressionGate",
                  "UML:CombinedFragment.expressionGate", gate );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#getOperands()
     */
    public ETList<IInteractionOperand> getOperands()
    {
        return new ElementCollector<IInteractionOperand>().
            retrieveElementCollection( 
                getNode(), "UML:CombinedFragment.operand/*", IInteractionOperand.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#createOperand()
     */
    public IInteractionOperand createOperand()
    {
        IInteractionOperand op = 
            new TypedFactoryRetriever<IInteractionOperand>()
                .createType("InteractionOperand");
        if (op != null)
            addOperand(op);
        return op;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#insertOperand(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
     */
    public void insertOperand(IInteractionOperand pOperand,
            IInteractionOperand pBeforeOperand)
    {
        if (pOperand == null)
            pOperand = new TypedFactoryRetriever<IInteractionOperand>()
                .createType("InteractionOperand");
            
        if (pOperand != null)
        {
            insertChildBefore(
                    "UML:CombinedFragment.operand",
                    "UML:CombinedFragment.operand",
                    pOperand,
                    pBeforeOperand );
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#removeOperand(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
     */
    public void removeOperand(IInteractionOperand op)
    {
        UMLXMLManip.removeChild(getNode(), op);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#addOperand(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
     */
    public void addOperand(IInteractionOperand op)
    {
    	ContactManager.setElement(op, this, "owner");
        addChild("UML:CombinedFragment.operand", 
                "UML:CombinedFragment.operand", op);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#getOperator()
     */
    public int getOperator()
    {
        return getInteractionOperator("interactionOperator");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment#setOperator(org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator)
     */
    public void setOperator(int value)
    {
        setInteractionOperator("interactionOperator", value);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:CombinedFragment", doc, node);
    }
    
    private void moveMessages(IInteractionOperand from, IInteractionOperand to)
    {
        ETList<IMessage> msgs = from.getCoveredMessages();
        if (msgs != null)
        {
            for (int i = msgs.size() - 1; i >= 0; --i)
            {
                IMessage msg = msgs.get(i);
                if (msg != null)
                    msg.setInteractionOperand(to);
            }
        }
    }
}