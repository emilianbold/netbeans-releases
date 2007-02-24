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

public class AtomicFragment extends InteractionFragment
    implements IAtomicFragment
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IAtomicFragment#getImplicitGate()
     */
    public IGate getImplicitGate()
    {
        return new ElementCollector<IGate>().retrieveSingleElement(
                getNode(), "UML:AtomicFragment.implicitGate/*", IGate.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IAtomicFragment#setImplicitGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void setImplicitGate(IGate value)
    {
        addChild("UML:AtomicFragment.implicitGate", 
                 "UML:AtomicFragment.implicitGate", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IAtomicFragment#getEventOccurrence()
     */
    public IEventOccurrence getEvent()
    {
        return new ElementCollector<IEventOccurrence>().retrieveSingleElement(
                getNode(), "UML:Element.ownedElement/UML:EventOccurrence", IEventOccurrence.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IAtomicFragment#setEventOccurrence(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setEvent(IEventOccurrence value)
    {
        addElement(value);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:AtomicFragment", doc, node);
    }
}