/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
