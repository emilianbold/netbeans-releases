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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InvocationNode.
 */
public class InvocationNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InvocationNodeTestCase.class);
    }

    private IInvocationNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (IInvocationNode)FactoryRetriever.instance().createType("InvocationNode", null);
        //node.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(node);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        node.delete();
    }

    
    public void testCreateCondition()
    {
        assertNotNull(node.createCondition("yipe"));
    }

    public void testSetIsMultipleInvocation()
    {
        node.setIsMultipleInvocation(true);
        assertTrue(node.getIsMultipleInvocation());
        node.setIsMultipleInvocation(false);
        assertFalse(node.getIsMultipleInvocation());
    }

    public void testGetIsMultipleInvocation()
    {
        // Tested by testSetIsMultipleInvocation.
    }

    public void testSetIsSynchronous()
    {
        node.setIsSynchronous(true);
        assertTrue(node.getIsSynchronous());
        node.setIsSynchronous(false);
        assertFalse(node.getIsSynchronous());
    }

    public void testGetIsSynchronous()
    {
        // Tested by testSetIsSynchronous.
    }

    public void testAddLocalPostCondition()
    {
        IConstraint cond = node.createCondition("xyzzy");
        node.addLocalPostCondition(cond);
        assertEquals(1, node.getLocalPostConditions().size());
        assertEquals(cond.getXMIID(), node.getLocalPostConditions().get(0).getXMIID());
    }

    public void testRemoveLocalPostcondition()
    {
        testAddLocalPostCondition();
        node.removeLocalPostcondition(node.getLocalPostConditions().get(0));
        assertEquals(0, node.getLocalPostConditions().size());
    }
    
    public void testGetLocalPostConditions()
    {
        // Tested by testAddLocalPostCondition.
    }

    public void testAddLocalPrecondition()
    {
        IConstraint cond = node.createCondition("xyzzy");
        node.addLocalPrecondition(cond);
        assertEquals(1, node.getLocalPreconditions().size());
        assertEquals(cond.getXMIID(), node.getLocalPreconditions().get(0).getXMIID());
    }

    public void testRemoveLocalPrecondition()
    {
        testAddLocalPrecondition();
        node.removeLocalPrecondition(node.getLocalPreconditions().get(0));
        assertEquals(0, node.getLocalPreconditions().size());
    }

    public void testGetLocalPreconditions()
    {
        // Tested by testAddLocalPrecondition.
    }

    public void testSetMultiplicity()
    {
        IMultiplicity mul = factory.createMultiplicity(null);
        project.addElement(mul);
        node.setMultiplicity(mul);
        assertEquals(mul.getXMIID(), node.getMultiplicity().getXMIID());
    }

    public void testGetMultiplicity()
    {
        // Tested by testSetMultiplicity.
    }
}
