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
 * File       : ClearAssociationAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;

/**
 * @author Aztec
 */
public class ClearAssociationAction
    extends PrimitiveAction
    implements IClearAssociationAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClearAssociationAction#getAssociation()
     */
    public IAssociation getAssociation()
    {
        return new ElementCollector< IAssociation >()
            .retrieveSingleElementWithAttrID(this, "association", IAssociation.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClearAssociationAction#getObject()
     */
    public IInputPin getObject()
    {
        return new ElementCollector< IInputPin >()
            .retrieveSingleElement(this, "UML:Element.ownedElement/UML:InputPin", IInputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClearAssociationAction#setAssociation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation)
     */
    public void setAssociation(IAssociation pAssoc)
    {
        addElementByID(pAssoc, "association");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClearAssociationAction#setObject(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin)
     */
    public void setObject(IInputPin pInputPin)
    {
        addInput(pInputPin);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:ClearAssociationAction", doc, node);
    }     

}
