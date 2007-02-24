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
 * File       : LoopAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class LoopAction extends CompositeAction implements ILoopAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#addToBody(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addToBody(IAction pAction)
    {
        addChild("UML:LoopAction.body", "UML:LoopAction.body", pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#addToTest(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addToTest(IAction pAction)
    {
        addChild("UML:LoopAction.test", "UML:LoopAction.test", pAction); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#getBody()
     */
    public ETList<IAction> getBody()
    {
        return new ElementCollector< IAction >()
            .retrieveElementCollection((ILoopAction)this, "UML:LoopAction.body/*", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#getIsTestedFirst()
     */
    public boolean getIsTestedFirst()
    {
        return getBooleanAttributeValue("isTestedFirst", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#getTest()
     */
    public ETList<IAction> getTest()
    {
        return new ElementCollector< IAction >()
            .retrieveElementCollection((ILoopAction)this, "UML:LoopAction.test/*", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#getTestOutput()
     */
    public IValueSpecification getTestOutput()
    {
        return new ElementCollector< IValueSpecification >()
            .retrieveSingleElementWithAttrID(this, "testOutput", IValueSpecification.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#removeFromBody(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeFromBody(IAction pAction)
    {
        UMLXMLManip.removeChild(m_Node, pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#removeFromTest(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeFromTest(IAction pAction)
    {
        UMLXMLManip.removeChild(m_Node, pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#setIsTestedFirst(boolean)
     */
    public void setIsTestedFirst(boolean isTestedFirst)
    {
        setBooleanAttributeValue("isTestedFirst", isTestedFirst);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILoopAction#setTestOutput(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void setTestOutput(IValueSpecification pTestOutput)
    {
        addElementByID(pTestOutput, "testOutput");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:LoopAction", doc, node);
    }      

}
