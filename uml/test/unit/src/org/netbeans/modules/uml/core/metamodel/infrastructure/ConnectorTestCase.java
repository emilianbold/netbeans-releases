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
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;

/**
 * Test cases for Connector.
 */
public class ConnectorTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConnectorTestCase.class);
    }

    private IConnector con;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        con = factory.createConnector(null);
        project.addElement(con);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        con.delete();
    }
    
    public void testAddBehavior()
    {
        IBehavior b = (IBehavior)FactoryRetriever.instance().createType("Procedure", null);
        //b.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(b);
        
        con.addBehavior(b);
        assertEquals(1, con.getBehaviors().size());
        assertEquals(b.getXMIID(), con.getBehaviors().get(0).getXMIID());
    }

    public void testRemoveBehavior()
    {
        testAddBehavior();
        con.removeBehavior(con.getBehaviors().get(0));
        assertEquals(0, con.getBehaviors().size());
    }
    
    public void testGetBehaviors()
    {
        // Tested by testAddBehavior.
    }

    public void testAddEnd()
    {
        IConnectorEnd end = factory.createConnectorEnd(null);
        project.addElement(end);
        con.addEnd(end);
        assertEquals(1, con.getEnds().size());
        assertEquals(end.getXMIID(), con.getEnds().get(0).getXMIID());
    }
    
    public void testRemoveEnd()
    {
        testAddEnd();
        con.removeEnd(con.getEnds().get(0));
        assertEquals(0, con.getEnds().size());
    }
    
    public void testGetEnds()
    {
        // Tested by testAddEnd.
    }
    
    public void testSetFrom()
    {
        IConnectorEnd end = factory.createConnectorEnd(null);
        project.addElement(end);
        con.setFrom(end);
        assertEquals(end.getXMIID(), con.getFrom().getXMIID());
    }
    
    public void testGetFrom()
    {
        // Tested by testSetFrom.
    }
    
    public void testSetTo()
    {
        IConnectorEnd from = factory.createConnectorEnd(null);
        project.addElement(from);
        
        IConnectorEnd to = factory.createConnectorEnd(null);
        project.addElement(to);
        con.setFrom(from);
        con.setTo(to);
        
        assertEquals(to.getXMIID(), con.getTo().getXMIID());
    }
    
    public void testGetTo()
    {
        // Tested by testSetTo.
    }
}
