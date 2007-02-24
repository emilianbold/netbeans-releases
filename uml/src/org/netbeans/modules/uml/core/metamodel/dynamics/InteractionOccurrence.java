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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class InteractionOccurrence extends Behavior
       implements IInteractionOccurrence
{
    private IInteractionFragment m_InteractionFragment = 
        new InteractionFragment();
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement#getName()
     */
    public String getName()
    {
        IBehavior b = getBehavior();
        return b != null? b.getName() : null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement#setName(java.lang.String)
     */
    public void setName(String str)
    {
        IBehavior b = getBehavior();
        if (b != null)
            b.setName(str);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#getGates()
     */
    public ETList<IGate> getGates()
    {
        return new ElementCollector<IGate>( )
            .retrieveElementCollection( 
                m_Node, 
                "UML:InteractionOccurrence.actualGate/*", IGate.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#addGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void addGate(IGate gate)
    {
        addChild("UML:InteractionOccurrence.actualGate", 
                 "UML:InteractionOccurrence.actualGate", 
                 gate );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#removeGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void removeGate(IGate gate)
    {
        UMLXMLManip.removeChild(m_Node, gate);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#getInteraction()
     */
    public IInteraction getInteraction()
    {
        IBehavior b = getBehavior();
        return b instanceof IInteraction? (IInteraction) b : null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#setInteraction(org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction)
     */
    public void setInteraction(IInteraction value)
    {
        if (value instanceof IBehavior)
            setBehavior((IBehavior) value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#getBehavior()
     */
    public IBehavior getBehavior()
    {
        return new ElementCollector<IBehavior>( )
            .retrieveSingleElementWithAttrID( this, "refersTo", IBehavior.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence#setBehavior(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
     */
    public void setBehavior(IBehavior value)
    {
        setElement( value, "refersTo" );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InteractionOccurrence", doc, node);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_InteractionFragment.setNode(n);
    }


    ///////// IInteractionOccurrence delegate methods /////////
    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public IInteractionOperand getEnclosingOperand()
    {
        return m_InteractionFragment.getEnclosingOperand();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public ETList<ILifeline> getCoveredLifelines()
    {
        return m_InteractionFragment.getCoveredLifelines();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public ETList<IInterGateConnector> getGateConnectors()
    {
        return m_InteractionFragment.getGateConnectors();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void addCoveredLifeline(ILifeline lifeline)
    {
        m_InteractionFragment.addCoveredLifeline(lifeline);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void addGateConnector(IInterGateConnector connector)
    {
        m_InteractionFragment.addGateConnector(connector);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void removeCoveredLifeline(ILifeline lifeline)
    {
        m_InteractionFragment.removeCoveredLifeline(lifeline);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void removeGateConnector(IInterGateConnector connector)
    {
        m_InteractionFragment.removeGateConnector(connector);
    }
}