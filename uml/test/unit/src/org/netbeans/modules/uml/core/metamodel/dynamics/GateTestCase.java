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
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
/**
 * Test cases for Gate.
 */
public class GateTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(GateTestCase.class);
    }

    private IGate gate;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        gate = new TypedFactoryRetriever<IGate>()
                            .createType("Gate");
        project.addElement(gate);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        gate.delete();
    }

    public void testSetFromConnector()
    {
        IInterGateConnector gc = (IInterGateConnector)FactoryRetriever.instance().createType("InterGateConnector", null);
        //gc.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(gc);
        gate.setFromConnector(gc);
        assertEquals(gc.getXMIID(), gate.getFromConnector().getXMIID());
    }

    public void testGetFromConnector()
    {
        // Tested by testSetFromConnector.
    }

    public void testSetInteraction()
    {
        IInteraction inter = new TypedFactoryRetriever<IInteraction>()
                                .createType("Interaction");
        project.addElement(inter);
        gate.setInteraction(inter);
        assertEquals(inter.getXMIID(), gate.getInteraction().getXMIID());
    }

    public void testGetInteraction()
    {
        // Tested by testSetInteraction.
    }

    public void testSetToConnector()
    {
        IInterGateConnector gc = (IInterGateConnector)FactoryRetriever.instance().createType("InterGateConnector", null);
        //gc.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(gc);
        gate.setToConnector(gc);
        assertEquals(gc.getXMIID(), gate.getToConnector().getXMIID());
    }

    public void testGetToConnector()
    {
        // Tested by testSetToConnector.
    }
}
