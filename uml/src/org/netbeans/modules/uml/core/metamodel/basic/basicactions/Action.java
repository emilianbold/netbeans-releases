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

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author gautams
 */
public class Action extends Element implements IAction 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#addInput(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void addInput(IValueSpecification pPin) 
    {
        addElement(pPin);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#addJumpHandler(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler)
     */
    public void addJumpHandler(final IJumpHandler pHandler) 
    {
        new ElementConnector<IAction>()
            .addChildAndConnect(this, true, "jumpHandler", "jumpHandler",
                                pHandler,
                                new IBackPointer<IAction>() {
                                    public void execute(IAction obj) {
                                        pHandler.addProtectedAction(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#addOutput(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin)
     */
    public void addOutput(IOutputPin pPin) 
    {
        addElement(pPin);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#addPredecessor(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addPredecessor(final IAction pAction) 
    {
        new ElementConnector<IAction>()
            .addChildAndConnect(this, true, "predecessor", "predecessor",
                                pAction,
                                new IBackPointer<IAction>() {
                                    public void execute(IAction obj) {
                                        pAction.addSuccessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#addSuccessor(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addSuccessor(final IAction pAction) 
    {
        new ElementConnector<IAction>()
            .addChildAndConnect(this, true, "successor", "successor",
                                pAction,
                                new IBackPointer<IAction>() {
                                    public void execute(IAction obj) {
                                        pAction.addPredecessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#getInputs()
     */
    public ETList <IValueSpecification> getInputs() 
    {
        ElementCollector<IValueSpecification> collector = new ElementCollector<IValueSpecification>();
        return collector.retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*[ not( name(.) = 'UML:OutputPin' )]", IValueSpecification.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#getIsReadOnly()
     */
    public boolean getIsReadOnly() 
    {
         return getBooleanAttributeValue("isReadOnly", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#getJumpHandlers()
     */
    public ETList <IJumpHandler> getJumpHandlers() 
    {
        ElementCollector<IJumpHandler> collector = new ElementCollector<IJumpHandler>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"jumpHandler", IJumpHandler.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#getOutputs()
     */
    public ETList <IOutputPin> getOutputs() 
    {
        ElementCollector<IOutputPin> collector = new ElementCollector<IOutputPin>();
        return collector.retrieveElementCollection((IElement)this, "UML:Element.ownedElement/UML:OutputPin", IOutputPin.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#getPredecessors()
     */
    public ETList <IAction> getPredecessors() 
    {
        ElementCollector<IAction> collector = new ElementCollector<IAction>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"predecessor", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#getSuccessors()
     */
    public ETList <IAction> getSuccessors() 
    {
        ElementCollector<IAction> collector = new ElementCollector<IAction>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"successor", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#removeInput(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void removeInput(IValueSpecification pPin) 
    {
        removeElement(pPin);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#removeJumpHandler(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IJumpHandler)
     */
    public void removeJumpHandler(final IJumpHandler pHandler) 
    {
        new ElementConnector<IAction>()
            .removeByID(this, pHandler, "jumpHandler",
                                new IBackPointer<IAction>() {
                                    public void execute(IAction obj) {
                                        pHandler.removeProtectedAction(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#removeOutput(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin)
     */
    public void removeOutput(IOutputPin pPin) 
    {
        removeElement(pPin);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#removePredecessor(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removePredecessor(final IAction pAction) 
    {
        new ElementConnector<IAction>()
            .removeByID(this, pAction, "predecessor",
                                new IBackPointer<IAction>() {
                                    public void execute(IAction obj) {
                                        pAction.removeSuccessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#removeSuccessor(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeSuccessor(final IAction pAction) 
    {
        new ElementConnector<IAction>()
            .removeByID(this, pAction, "successor",
                                new IBackPointer<IAction>() {
                                    public void execute(IAction obj) {
                                        pAction.removePredecessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction#setIsReadOnly(boolean)
     */
    public void setIsReadOnly(boolean value) 
    {
        setBooleanAttributeValue("isReadOnly", value);
    }

}
