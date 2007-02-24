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
 * File       : CreateObjectAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author Aztec
 */
public class CreateObjectAction
    extends PrimitiveAction
    implements ICreateObjectAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICreateObjectAction#getClassifier()
     */
    public IClassifier getClassifier()
    {
        return new ElementCollector< IClassifier >()
            .retrieveSingleElementWithAttrID(this, "classifier", IClassifier.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICreateObjectAction#getResult()
     */
    public IOutputPin getResult()
    {
        return new ElementCollector< IOutputPin >()
            .retrieveSingleElement(this, "UML:Element.ownedElement/UML:OutputPin", IOutputPin.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICreateObjectAction#setClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setClassifier(IClassifier pClassifier)
    {
        addElementByID(pClassifier, "classifier");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICreateObjectAction#setResult(com.embarcadero.describe.basicactions.IOutputPin)
     */
    public void setResult(IOutputPin pPin)
    {
        addOutput(pPin);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:CreateObjectAction", doc, node);
    }        

}
