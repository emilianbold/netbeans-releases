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

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Message.
 */
public class MessageTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MessageTestCase.class);
    }

    private IMessage m;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        m = createType("Message");
    }
    
    public void testResetAutoNumber()
    {
        // We can't really test this in isolation, but this is tested in
        // Interaction.
    }

    public void testGetAutoNumber()
    {
        assertEquals("1", m.getAutoNumber());
    }

    public void testSetConnector()
    {
        IConnector c = createType("Connector");
        m.setConnector(c);
        assertEquals(c.getXMIID(), m.getConnector().getXMIID());
    }

    public void testGetConnector()
    {
        // Tested by testSetConnector.
    }

    public void testSetInitiatingAction()
    {
        IExecutionOccurrence eo = createType("ActionOccurrence");
        m.setInitiatingAction(eo);
        assertEquals(eo.getXMIID(), m.getInitiatingAction().getXMIID());
    }

    public void testGetInitiatingAction()
    {
        // Tested by testSetInitiatingAction.
    }

    public void testSetInteractionOperand()
    {
        IEventOccurrence se = createType("EventOccurrence"),
                         re = createType("EventOccurrence");
        IAtomicFragment  sef = createType("AtomicFragment"),
                         ref = createType("AtomicFragment");
        sef.addElement(se);
        ref.addElement(re);
        
        m.setSendEvent(se);
        m.setReceiveEvent(re);
        
        IInteractionOperand op = createType("InteractionOperand");
        m.setInteractionOperand(op);
        assertEquals(op.getXMIID(), m.getInteractionOperand().getXMIID());
    }

    public void testGetInteractionOperand()
    {
        // Tested by testSetInteractionOperand.
    }

    public void testSetInteraction()
    {
        IInteraction inter = createType("Interaction");
        m.setInteraction(inter);
        assertEquals(inter.getXMIID(), m.getInteraction().getXMIID());
    }

    public void testGetInteraction()
    {
        // Tested by testSetInteraction.
    }

    public void testSetKind()
    {
        m.setKind(BaseElement.MK_CREATE);
        assertEquals(BaseElement.MK_CREATE, m.getKind());
        
        m.setKind(BaseElement.MK_SYNCHRONOUS);
        assertEquals(BaseElement.MK_SYNCHRONOUS, m.getKind());
    }

    public void testGetKind()
    {
        // Tested by testSetKind.
    }

    public void testSetOperationInvoked()
    {
        IOperation op = createType("Operation");
        IEventOccurrence se = createType("EventOccurrence"),
                         re = createType("EventOccurrence");
        se.setStartExec((IActionOccurrence) createType("ActionOccurrence"));
        re.setFinishExec((IProcedureOccurrence) createType("ProcedureOccurrence"));
        
        m.setSendEvent(se);
        m.setReceiveEvent(re);
        
        IInteraction inter = createType("Interaction");
        m.setInteraction(inter);
        
        m.setOperationInvoked(op);
        assertEquals(op.getXMIID(), m.getOperationInvoked().getXMIID());
    }

    public void testGetOperationInvoked()
    {
        // Tested by testSetOperationInvoked.
    }

    public void testSetReceiveEvent()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        m.setReceiveEvent(eo);
        assertEquals(eo.getXMIID(), m.getReceiveEvent().getXMIID());
    }

    public void testGetReceiveEvent()
    {
        // Tested by testSetReceiveEvent.
    }

    public void testGetReceivingClassifier()
    {
        testSetReceiveEvent();
        ILifeline life = createType("Lifeline");
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        m.getReceiveEvent().setLifeline(life);
        assertEquals(c.getXMIID(), m.getReceivingClassifier().getXMIID());
    }

    public void testGetReceivingLifeline()
    {
        testSetReceiveEvent();
        ILifeline life = createType("Lifeline");
        m.getReceiveEvent().setLifeline(life);
        assertEquals(life.getXMIID(), m.getReceivingLifeline().getXMIID());
    }

    public void testGetReceivingOperations()
    {
        testSetReceiveEvent();
        ILifeline life = createType("Lifeline");
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        m.getReceiveEvent().setLifeline(life);
        
        
        IOperation op = c.createOperation("int", "a");
        c.addOperation(op);
        
        assertEquals(1, m.getReceivingOperations().size());
        assertEquals(op.getXMIID(), m.getReceivingOperations().get(0).getXMIID());
    }

    public void testGetRecurrence()
    {
        // AZTEC: TODO: How to test?
    }

    public void testSetSendEvent()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        m.setSendEvent(eo);
        assertEquals(eo.getXMIID(), m.getSendEvent().getXMIID());
    }

    public void testGetSendEvent()
    {
        // Tested by testSetSendEvent.
    }

    public void testGetSendingClassifier()
    {
        testSetSendEvent();
        ILifeline life = createType("Lifeline");
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        m.getSendEvent().setLifeline(life);
        assertEquals(c.getXMIID(), m.getSendingClassifier().getXMIID());
    }

    public void testGetSendingLifeline()
    {
        testSetSendEvent();
        ILifeline life = createType("Lifeline");
        m.getSendEvent().setLifeline(life);
        assertEquals(life.getXMIID(), m.getSendingLifeline().getXMIID());
    }

    public void testSetSendingMessage()
    {
        IMessage sending = createType("Message");
        m.setSendingMessage(sending);
        assertEquals(sending.getXMIID(), m.getSendingMessage().getXMIID());
    }

    public void testGetSendingMessage()
    {
        // Tested by testSetSendingMessage.
    }
}
