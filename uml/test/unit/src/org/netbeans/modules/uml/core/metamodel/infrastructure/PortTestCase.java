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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;

/**
 * Test cases for Port.
 */
public class PortTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PortTestCase.class);
    }

    private IPort port;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        port = factory.createPort(null);
        project.addElement(port);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        port.delete();
    }
    
    public void testAddEnd()
    {
        IConnectorEnd end = factory.createConnectorEnd(null);
        project.addElement(end);
        port.addEnd(end);
        assertEquals(1, port.getEnds().size());
        assertEquals(end.getXMIID(), port.getEnds().get(0).getXMIID());
    }

    public void testRemoveEnd()
    {
        testAddEnd();
        port.removeEnd(port.getEnds().get(0));
        assertEquals(0, port.getEnds().size());
    }

    public void testGetEnds()
    {
        // Tested by testAddEnd.
    }

    public void testSetIsService()
    {
        assertFalse(port.getIsService());
        port.setIsService(true);
        assertTrue(port.getIsService());
        port.setIsService(false);
        assertFalse(port.getIsService());
    }

    public void testGetIsService()
    {
        // Tested by testSetIsService.
    }

    public void testSetIsSignal()
    {
        assertFalse(port.getIsSignal());
        port.setIsSignal(true);
        assertTrue(port.getIsSignal());
        port.setIsSignal(false);
        assertFalse(port.getIsSignal());
    }

    public void testGetIsSignal()
    {
        // Tested by testSetIsSignal.
    }

    public void testSetProtocol()
    {
        IProtocolStateMachine psm = factory.createProtocolStateMachine(null);
        project.addElement(psm);
        port.setProtocol(psm);
        assertEquals(psm.getXMIID(), port.getProtocol().getXMIID());
    }

    public void testGetProtocol()
    {
        // Tested by testSetProtocol.
    }

    public void testRemoveProvidedInterface()
    {
        testAddProvidedInterface();
        port.removeProvidedInterface(port.getProvidedInterfaces().get(0));
        assertEquals(0, port.getProvidedInterfaces().size());
    }

    public void testAddProvidedInterface()
    {
        IInterface intf = factory.createInterface(null);
        project.addOwnedElement(intf);
        port.addProvidedInterface(intf);
        assertEquals(1, port.getProvidedInterfaces().size());
        assertEquals(intf.getXMIID(), 
            port.getProvidedInterfaces().get(0).getXMIID());
    }
    
    public void testGetIsProvidedInterface()
    {
        testAddProvidedInterface();
        assertTrue(port.getIsProvidedInterface(port.getProvidedInterfaces().get(0)));
    }

    public void testGetProvidedInterfaces()
    {
        // Tested by testAddProvidedInterface.
    }

    public void testAddRequiredInterface()
    {
        IInterface intf = factory.createInterface(null);
        project.addOwnedElement(intf);
        port.addRequiredInterface(intf);
        assertEquals(1, port.getRequiredInterfaces().size());
        assertEquals(intf.getXMIID(), 
            port.getRequiredInterfaces().get(0).getXMIID());
    }
    
    public void testGetIsRequiredInterface()
    {
        testAddRequiredInterface();
        assertTrue(port.getIsRequiredInterface(port.getRequiredInterfaces().get(0)));
    }

    public void testRemoveRequiredInterface()
    {
        testAddRequiredInterface();
        port.removeRequiredInterface(port.getRequiredInterfaces().get(0));
        assertEquals(0, port.getRequiredInterfaces().size());
    }

    public void testGetRequiredInterfaces()
    {
        // Tested by testAddRequiredInterface.
    }
}
