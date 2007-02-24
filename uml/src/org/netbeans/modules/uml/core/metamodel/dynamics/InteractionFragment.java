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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class InteractionFragment extends NamedElement
        implements IInteractionFragment
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#addGateConnector(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void addGateConnector(final IInterGateConnector connector)
    {
        new ElementConnector<IInteractionFragment>()
            .addChildAndConnect( 
                this, false, "UML:InteractionFragment.gateConnector",
                "UML:InteractionFragment.gateConnector", connector,
                new IBackPointer<IInteractionFragment>()
                {
                    public void execute(IInteractionFragment frag)
                    {
                        connector.setFragment(frag);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#removeGateConnector(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void removeGateConnector(final IInterGateConnector connector)
    {
        new ElementConnector<IInteractionFragment>()
            .removeElement( this, connector, 
                "UML:InteractionFragment.gateConnector/*",
                new IBackPointer<IInteractionFragment>()
                {
                    public void execute(IInteractionFragment frag)
                    {
                        connector.setFragment(frag);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#getGateConnectors()
     */
    public ETList<IInterGateConnector> getGateConnectors()
    {
        return new ElementCollector<IInterGateConnector>()
            .retrieveElementCollection( 
                m_Node, "UML:InteractionFragment.gateConnector/*", IInterGateConnector.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#addCoveredLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void addCoveredLifeline(final ILifeline line)
    {
        new ElementConnector<IInteractionFragment>()
            .addChildAndConnect( this, true, "covered", "covered", line, 
                new IBackPointer<IInteractionFragment>()
                {
                    public void execute(IInteractionFragment frag)
                    {
                        line.addCoveringFragment(frag);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#removeCoveredLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void removeCoveredLifeline(final ILifeline line)
    {
        new ElementConnector< IInteractionFragment >( )
            .removeByID( this, line, "covered", 
                new IBackPointer<IInteractionFragment>( )
                {
                    public void execute(IInteractionFragment frag)
                    {
                        line.removeCoveringFragment(frag);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#getCoveredLifelines()
     */
    public ETList<ILifeline> getCoveredLifelines()
    {
        return new ElementCollector<ILifeline>( )
            .retrieveElementCollectionWithAttrIDs( 
                this, "covered", ILifeline.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment#getEnclosingOperand()
     */
    public IInteractionOperand getEnclosingOperand()
    {
        IElement owner = getOwner();
        return owner instanceof IInteractionOperand? 
                               (IInteractionOperand) owner : null;
    }
}