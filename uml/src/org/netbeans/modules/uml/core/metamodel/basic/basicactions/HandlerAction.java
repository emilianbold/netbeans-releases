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

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class HandlerAction extends PrimitiveAction implements IHandlerAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#addHandler(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler)
     */
    public void addHandler(IJumpHandler pHandler)
    {
        addElementByID( pHandler, "jumpHandler");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#getBody()
     */
    public IAction getBody()
    {
        ElementCollector<IAction> collector = new ElementCollector<IAction>();
        return collector.retrieveSingleElement(this, "UML:HandlerAction.body/*", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#getHandlers()
     */
    public ETList<IJumpHandler> getHandlers()
    {
        ElementCollector<IJumpHandler> collector = new ElementCollector<IJumpHandler>();
        return collector.retrieveElementCollectionWithAttrIDs(this, "jumpHandler", IJumpHandler.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#getJumpValue()
     */
    public IOutputPin getJumpValue()
    {
        ElementCollector<IOutputPin> collector = new ElementCollector<IOutputPin>();
//        return collector.retrieveSingleElement(this, "UML:Element.ownedElement/UML:OutputPin/*");
        return collector.retrieveSingleElement(this, "UML:Element.ownedElement/UML:OutputPin", IOutputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#removeHandler(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler)
     */
    public void removeHandler(IJumpHandler pHandler)
    {
        removeElementByID( pHandler, "jumpHandler");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#setBody(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void setBody(IAction value)
    {
        addChild("UML:HandlerAction.body", "UML:HandlerAction.body", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction#setJumpValue(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin)
     */
    public void setJumpValue(IOutputPin value)
    {
        addOutput(value);
    }
    
    /* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
	 */
	public void establishNodePresence(Document doc, Node node)
	{
		buildNodePresence("UML:HandlerAction", doc, node);
	}


}
