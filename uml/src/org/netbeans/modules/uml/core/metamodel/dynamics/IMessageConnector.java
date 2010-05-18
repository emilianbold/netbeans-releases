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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public interface IMessageConnector extends IConnector
{
    /**
     * Retrieves the destination lifeline this connector is connected to.
     *
     * @return The lifeline
     */
    public ILifeline getToLifeline();

    /**
     * Sets the destination lifeline this connector is connected to.
     *
     * @param lifeline The lifeline
     */
    public void setToLifeline(ILifeline lifeline);
    
    /**
     * Retrieves the lifeline that is the source of this connector.
     * 
     * @return The lifeline
     */
    public ILifeline getFromLifeline();
    
    /**
     * Sets the lifeline that is the source of this connector.
     * 
     * @param lifeline The lifeline
     */
    public void setFromLifeline(ILifeline lifeline);
    
    /**
     * Retrieves the lifelines on the ends of this connector
     *
     * @return The lifeline collection
     */
    public ETList<ILifeline> getConnectedLifelines();

    /**
     * Retrieves the messages associated with this connector.
     *
     * @return The collection of messages
     */
    public ETList<IMessage> getMessages();

    /**
     * Removes the given message from this connector's list of messages.
     *
     * @param message The message to remove.
     */
    public void removeMessage(IMessage message);
    
    /**
     * Adds the given message to this connector's list of messages.
     *
     * @param message The message to add.
     */
    public void addMessage(IMessage message);
    
    /**
     * Creates a new IMessage, placing it on the IInteraction that owns this 
     * connector, then associates the IOperation with the IMessage
     *
     * ASSUMPTIONS: This was written for the collaboration diagram messages. 
     *              We're assuming we're not crossing interactions.
     *
     * @param  directionKind  The direction the message should go (FROM to TO or 
     *                        TO to FROM)
     * @param  oper           The operation to associate with the new message.
     * 
     * @return message        The new IMessage
     */
    public IMessage addMessage(int nDirectionKind, IOperation pOper);
}
