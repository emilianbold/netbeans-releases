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


package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;

import org.dom4j.Document;
import org.dom4j.Node;



public class BroadCastSignalAction extends PrimitiveAction implements IBroadCastSignalAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBroadCastSignalAction#getSignal()
     */
    public ISignal getSignal()
    {
        ElementCollector <ISignal> col = new ElementCollector <ISignal>();
        return col.retrieveSingleElementWithAttrID(this, "signal", ISignal.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBroadCastSignalAction#setSignal(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal)
     */
    public void setSignal(ISignal signal)
    {
        addElementByID(signal, "signal");
    }

    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:BroadCastSignalAction", doc, parent);
    }
}
