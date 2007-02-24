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
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;


public class Gate extends NamedElement implements IGate
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#getToConnector()
     */
    public IInterGateConnector getToConnector()
    {
        return new ElementCollector<IInterGateConnector>()
            .retrieveSingleElementWithAttrID( this, "toConnector", IInterGateConnector.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#setToConnector(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void setToConnector(final IInterGateConnector gateConnector)
    {
        new ElementConnector<IGate>()
            .addChildAndConnect( this, true, "toConnector", "toConnector", 
                gateConnector, 
                new IBackPointer<IGate>()
                {
                    public void execute(IGate g)
                    {
                        gateConnector.setToGate(g);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#getFromConnector()
     */
    public IInterGateConnector getFromConnector()
    {
        return new ElementCollector<IInterGateConnector>()
            .retrieveSingleElementWithAttrID( this, "fromConnector", IInterGateConnector.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#setFromConnector(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void setFromConnector(final IInterGateConnector gateConnector)
    {
        new ElementConnector<IGate>()
            .addChildAndConnect( this, true, "fromConnector", "fromConnector", 
                gateConnector, 
                new IBackPointer<IGate>()
                {
                    public void execute(IGate g)
                    {
                        gateConnector.setFromGate(g);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#getInteraction()
     */
    public IInteraction getInteraction()
    {
        INamespace space = getNamespace();
        return space instanceof IInteraction? (IInteraction) space : null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#setInteraction(org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction)
     */
    public void setInteraction(IInteraction inter)
    {
        if (inter instanceof INamespace)
            setNamespace((INamespace) inter);
    }
    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:Gate", doc, parent);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#performDependentElementCleanup(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement)
     */
    protected void performDependentElementCleanup(IVersionableElement elem)
    {
        IInterGateConnector connector = getToConnector();
        if (connector != null)
            connector.delete();
        
        connector = getFromConnector();
        if (connector != null)
            connector.delete();
        
        super.performDependentElementCleanup(elem);
    }
}