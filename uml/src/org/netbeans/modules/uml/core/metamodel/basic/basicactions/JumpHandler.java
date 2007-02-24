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

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class JumpHandler extends Element implements IJumpHandler
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#addProtectedAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addProtectedAction(final IAction pAction)
    {
        new ElementConnector< IJumpHandler >().addChildAndConnect( 
                                                this, 
                                                true, 
                                                "protectedAction",
                                                "protectedAction", 
                                                pAction, 
                                                new IBackPointer<IJumpHandler>() {
                                                    public void execute(IJumpHandler obj) {
                                                        pAction.addJumpHandler(obj);
                                                    }
                                                }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#getBody()
     */
    public IHandlerAction getBody()
    {
        ElementCollector< IHandlerAction > col = new ElementCollector< IHandlerAction >();
        return col.retrieveSingleElementWithAttrID( this, "body", IHandlerAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#getIsDefault()
     */
    public boolean getIsDefault()
    {
        return getBooleanAttributeValue("isDefault",false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#getJumpType()
     */
    public ISignal getJumpType()
    {
        ElementCollector< ISignal > col = new ElementCollector< ISignal >();
        return col.retrieveSingleElementWithAttrID( this, "jumpType", ISignal.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#getProtectedActions()
     */
    public ETList<IAction> getProtectedActions()
    {
        ElementCollector< IAction > col = new ElementCollector< IAction >();
        return col.retrieveElementCollectionWithAttrIDs(this, "protectedAction", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#removeProtectedAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeProtectedAction(final IAction pAction)
    {
        new ElementConnector< IJumpHandler >().removeByID( 
                                                this,
                                                pAction,
                                                "protectedAction",
                                                new IBackPointer<IJumpHandler>() {
                                                    public void execute(IJumpHandler obj) {
                                                        pAction.removeJumpHandler(obj);
                                                    }
                                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#setBody(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IHandlerAction)
     */
    public void setBody(final IHandlerAction value)
    {
        new ElementConnector<IJumpHandler>().setSingleElementAndConnect(
                                                this,
                                                value,
                                                "body",
                                                new IBackPointer<IHandlerAction>() {
                                                    public void execute(IHandlerAction obj) {
                                                        obj.addHandler(JumpHandler.this);
                                                    }
                                                },
                                                new IBackPointer<IHandlerAction>() {
                                                     public void execute(IHandlerAction obj) {
                                                         obj.removeHandler(JumpHandler.this);
                                                     }
                                                }
                                                
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#setIsDefault(boolean)
     */
    public void setIsDefault(boolean value)
    {
        setBooleanAttributeValue("isDefault", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler#setJumpType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal)
     */
    public void setJumpType(ISignal value)
    {
        addElementByID(value, "jumpType");
    }
    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:JumpHandler", doc, parent);
    }

}
