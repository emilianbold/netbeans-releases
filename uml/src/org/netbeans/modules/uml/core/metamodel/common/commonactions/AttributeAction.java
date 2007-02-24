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
 * File       : AttributeAction.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;

/**
 * @author Aztec
 */
public class AttributeAction extends PrimitiveAction 
                                implements IAttributeAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IAttributeAction#getAttribute()
     */
    public IAttribute getAttribute()
    {
        return new ElementCollector< IAttribute >()
        .retrieveSingleElementWithAttrID(this, "attribute", IAttribute.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IAttributeAction#getObject()
     */
    public IInputPin getObject()
    {
        return new ElementCollector< IInputPin >()
            .retrieveSingleElement(this, "UML:AttributeAction.object/*", IInputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IAttributeAction#setAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void setAttribute(IAttribute attr)
    {
        addElementByID(attr, "attribute");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IAttributeAction#setObject(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin)
     */
    public void setObject(IInputPin inputPin)
    {
        addChild("UML:AttributeAction.object","UML:AttributeAction.object", inputPin);
    }

}
