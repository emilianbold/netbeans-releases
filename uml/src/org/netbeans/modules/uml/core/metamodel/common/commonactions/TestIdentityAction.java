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
 * File       : TestIdentityAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;

/**
 * @author Aztec
 */
public class TestIdentityAction
    extends PrimitiveAction
    implements ITestIdentityAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ITestIdentityAction#getFirst()
     */
    public IInputPin getFirst()
    {
        return new ElementCollector< IInputPin >()
            .retrieveSingleElement(this, "UML:TestIdentityAction.first/*", IInputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ITestIdentityAction#getResult()
     */
    public IOutputPin getResult()
    {
        return new ElementCollector< IOutputPin >()
            .retrieveSingleElement(this, "UML:Element.ownedElement/UML:OutputPin", IOutputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ITestIdentityAction#getSecond()
     */
    public IInputPin getSecond()
    {
        return new ElementCollector< IInputPin >()
            .retrieveSingleElement(this, "UML:TestIdentityAction.second/*", IInputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ITestIdentityAction#setFirst(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin)
     */
    public void setFirst(IInputPin pPin)
    {
        addChild("UML:TestIdentityAction.first"
                    ,"UML:TestIdentityAction.first"
                    , pPin);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ITestIdentityAction#setResult(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin)
     */
    public void setResult(IOutputPin pPin)
    {
        addOutput(pPin);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ITestIdentityAction#setSecond(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin)
     */
    public void setSecond(IInputPin pPin)
    {
        addChild("UML:TestIdentityAction.second"
                    ,"UML:TestIdentityAction.second"
                    , pPin);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:TestIdentityAction", doc, node);
    }      

}
