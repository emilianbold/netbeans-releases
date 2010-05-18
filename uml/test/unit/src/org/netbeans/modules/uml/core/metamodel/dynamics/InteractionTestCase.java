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
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for Interaction.
 */
public class InteractionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InteractionTestCase.class);
    }

    private IInteraction inter;
    private IMessage m1, m2, m3;
    private ILifeline last;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        inter = createType("Interaction");

        last = null;
        m1 = createMessage();
        m2 = createMessage();
        m3 = createMessage();
        
        inter.addMessage(m1);
        inter.addMessage(m2);
        inter.addMessage(m3);
    }
    
    private IMessage createMessage()
    {
        IMessage m = createType("Message");
        IEventOccurrence eo = createType("EventOccurrence");
        eo.setLifeline(last != null? last : (ILifeline) createType("Lifeline"));
        m.setSendEvent(eo);
        
        eo = createType("EventOccurrence");
        eo.setLifeline(last = createType("Lifeline"));
        m.setReceiveEvent(eo);
        
        return m;
    }

    public void testResetAutoNumbers()
    {
        assertEquals("1.1.1", m3.getAutoNumber());
        inter.resetAutoNumbers(m1);
        inter.removeMessage(m1);

        // What is the expected number, anyway?
        assertEquals("1.1", m3.getAutoNumber());
    }

    public void testAddConnector()
    {
        IConnector c = createType("Connector");
        inter.addConnector(c);
        assertEquals(1, inter.getConnectors().size());
        assertEquals(c.getXMIID(), inter.getConnectors().get(0).getXMIID());
    }

    public void testRemoveConnector()
    {
        testAddConnector();
        inter.removeConnector(inter.getConnectors().get(0));
        assertEquals(0, inter.getConnectors().size());
    }

    public void testGetConnectors()
    {
        // Tested by testAddConnector.
    }

    public void testAddEventOccurrence()
    {
        IEventOccurrence occ = createType("EventOccurrence");
        inter.addEventOccurrence(occ);
        assertEquals(1, inter.getEventOccurrences().size());
        assertEquals(occ.getXMIID(), inter.getEventOccurrences().get(0).getXMIID());
    }

    public void testRemoveEventOccurrence()
    {
        testAddEventOccurrence();
        inter.removeEventOccurrence(inter.getEventOccurrences().get(0));
        assertEquals(0, inter.getEventOccurrences().size());
    }

    public void testGetEventOccurrences()
    {
        // Tested by testAddEventOccurrence.
    }

    public void testAddGate()
    {
        IGate g = createType("Gate");
        inter.addGate(g);
        assertEquals(1, inter.getGates().size());
        assertEquals(g.getXMIID(), inter.getGates().get(0).getXMIID());
    }

    public void testRemoveGate()
    {
        testAddGate();
        inter.removeGate(inter.getGates().get(0));
        assertEquals(0, inter.getGates().size());
    }

    public void testGetGates()
    {
        // Tested by testAddGate.
    }

    public void testGetGeneralOrderings()
    {
        IGeneralOrdering go = createType("GeneralOrdering");
        inter.addElement(go);
        assertEquals(1, inter.getGeneralOrderings().size());
        assertEquals(go.getXMIID(), inter.getGeneralOrderings().get(0).getXMIID());
    }

    public void testAddLifeline()
    {
        ILifeline life = createType("Lifeline");
        inter.addLifeline(life);
        assertEquals(1, inter.getLifelines().size());
        assertEquals(life.getXMIID(), inter.getLifelines().get(0).getXMIID());
    }

    public void testRemoveLifeline()
    {
        testAddLifeline();
        inter.removeLifeline(inter.getLifelines().get(0));
        assertEquals(0, inter.getLifelines().size());
    }

    public void testGetLifelines()
    {
        // Tested by testAddLifeline.
    }

    public void testCreateMessage()
    {
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction((IInteraction) createType("Interaction"));
        IInteractionFragment toOwner = createType("Interaction");
        IOperation op = createType("Operation");
        
        assertNotNull(inter.createMessage(toElement, toOwner, op, 
                        BaseElement.MK_SYNCHRONOUS));
    }

    public void testInsertMessage()
    {
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction((IInteraction) createType("Interaction"));
        IInteractionFragment toOwner = createType("Interaction");
        IOperation op = createType("Operation");
        
        assertNotNull(inter.insertMessage(m3, toElement, toOwner, op, 
                        BaseElement.MK_SYNCHRONOUS));
    }

    public void testHandleMessageAdded()
    {
        // No code to test
    }

    public void testInsertMessageBefore()
    {
        IMessage m4 = createMessage();
        inter.insertMessageBefore(m4, m3);
        
        assertEquals(4, inter.getMessages().size());
        assertEquals(m4.getXMIID(), inter.getMessages().get(2).getXMIID());
    }

    public void testHandleMessageDeleted()
    {
        // No code to test
    }

    public void testAddMessage()
    {
        // Tested by setup
    }

    public void testRemoveMessage()
    {
        inter.removeMessage(m2);
        assertEquals(2, inter.getMessages().size());
        assertEquals(m1.getXMIID(), inter.getMessages().get(0).getXMIID());
        assertEquals(m3.getXMIID(), inter.getMessages().get(1).getXMIID());        
    }

    public void testGetMessages()
    {
        // Tested all over the shop.
    }
}
