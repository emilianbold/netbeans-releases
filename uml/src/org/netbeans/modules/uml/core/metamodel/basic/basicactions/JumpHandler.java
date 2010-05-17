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
