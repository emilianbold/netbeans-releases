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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;

/**
 * Test cases for Operation.
 */
public class OperationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(OperationTestCase.class);
    }

    private IOperation  op;
    private IClassifier cl;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        cl = createClass("Trellis");
        op = cl.createOperation("int", "almond");
        cl.addOperation(op);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        op.delete();
        cl.delete();
    }
    
	public void testChangeDocumentation()
	{
		op.setDocumentation("This is test");
		assertEquals("This is test", op.getDocumentation());
		op.setDocumentation("");
		assertEquals("", op.getDocumentation());
		op.setDocumentation("This is test");
		assertEquals("This is test", op.getDocumentation());
		op.setDocumentation(null);
		assertEquals("", op.getDocumentation());
	}

    public void testAddParameters()
    {
        IOperation op1 = cl.createOperation("int", "positivity");
        IParameter p1 = op1.createParameter("int", "z");
        assertNotNull(p1);
        op1.addParameter(p1);
        assertEquals(2, op1.getParameters().size());
        
    	IOperation op = cl.createOperation("char", "negativity");
        IParameter p = op.createParameter("java::lang::String", "z");
        assertNotNull(p);
        op.addParameter(p);
        assertEquals(2, op.getParameters().size());

		IParameter par2 = op.getParameters().get(1);
		assertEquals("String", par2.getTypeName());
    }

    public void testSetIsConstructor()
    {
        assertFalse(op.getIsConstructor());
        op.setIsConstructor(true);
        assertTrue(op.getIsConstructor());
        op.setIsConstructor(false);
        assertFalse(op.getIsConstructor());
    }
    
    public void testGetIsConstructor()
    {
        // Tested by setIsConstructor.
    }
    
    public void testSetIsDelegate()
    {
        assertFalse(op.getIsDelegate());
        op.setIsDelegate(true);
        assertTrue(op.getIsDelegate());
        op.setIsDelegate(false);
        assertFalse(op.getIsDelegate());
    }
    
    public void testGetIsDelegate()
    {
        // Tested by setIsDelegate.
    }
    
    public void testSetIsFriend()
    {
        assertFalse(op.getIsFriend());
        op.setIsFriend(true);
        assertTrue(op.getIsFriend());
        op.setIsFriend(false);
        assertFalse(op.getIsFriend());
    }
    
    public void testGetIsFriend()
    {
        // Tested by setIsFriend.
    }
    
    public void testSetIsIndexer()
    {
        assertFalse(op.getIsIndexer());
        op.setIsIndexer(true);
        assertTrue(op.getIsIndexer());
        op.setIsIndexer(false);
        assertFalse(op.getIsIndexer());
    }
    
    public void testGetIsIndexer()
    {
        // Tested by setIsIndexer.
    }
    
    public void testSetIsOverride()
    {
        assertFalse(op.getIsOverride());
        op.setIsOverride(true);
        assertTrue(op.getIsOverride());
        op.setIsOverride(false);
        assertFalse(op.getIsOverride());
    }
    
    public void testGetIsOverride()
    {
        // Tested by setIsOverride.
    }
    
    public void testSetIsProperty()
    {
        assertFalse(op.getIsProperty());
        op.setIsProperty(true);
        assertTrue(op.getIsProperty());
        op.setIsProperty(false);
        assertFalse(op.getIsProperty());
    }
    
    public void testGetIsProperty()
    {
        // Tested by setIsProperty.
    }
    
    public void testSetIsQuery()
    {
        assertFalse(op.getIsQuery());
        op.setIsQuery(true);
        assertTrue(op.getIsQuery());
        op.setIsQuery(false);
        assertFalse(op.getIsQuery());
    }
    
    public void testGetIsQuery()
    {
        // Tested by setIsQuery.
    }
    
    public void testSetIsSubroutine()
    {
        assertFalse(op.getIsSubroutine());
        op.setIsSubroutine(true);
        assertTrue(op.getIsSubroutine());
        op.setIsSubroutine(false);
        assertFalse(op.getIsSubroutine());
    }
    
    public void testGetIsSubroutine()
    {
        // Tested by setIsSubroutine.
    }
    
    public void testSetIsVirtual()
    {
        assertFalse(op.getIsVirtual());
        op.setIsVirtual(true);
        assertTrue(op.getIsVirtual());
        op.setIsVirtual(false);
        assertFalse(op.getIsVirtual());
    }
    
    public void testGetIsVirtual()
    {
        // Tested by setIsVirtual.
    }

    public void testAddPostCondition()
    {
        IConstraint c = factory.createConstraint(null);
        op.addPostCondition(c);
        assertEquals(1, op.getPostConditions().size());
        assertEquals(c.getXMIID(), op.getPostConditions().get(0).getXMIID());
    }
    
    public void testRemovePostCondition()
    {
        testAddPostCondition();
        op.removePostCondition(op.getPostConditions().get(0));
        assertEquals(0, op.getPostConditions().size());
    }
    
    public void testGetPostConditions()
    {
        // Tested by testAddPostCondition and testRemovePostCondition
    }

    public void testAddPreCondition()
    {
        IConstraint c = factory.createConstraint(null);
        op.addPreCondition(c);
        assertEquals(1, op.getPreConditions().size());
        assertEquals(c.getXMIID(), op.getPreConditions().get(0).getXMIID());
    }
    
    public void testRemovePreCondition()
    {
        testAddPreCondition();
        op.removePostCondition(op.getPreConditions().get(0));
        assertEquals(0, op.getPreConditions().size());
    }
    
    public void testGetPreConditions()
    {
        // Tested by testAddPreCondition and testRemovePreCondition
    }
    

    public void testAddRaisedException()
    {
        IClassifier exc = createClass("AtlantisRisingException");
        op.addRaisedException(exc);
        
        assertEquals(1, op.getRaisedExceptions().size());
        assertEquals(exc.getXMIID(), op.getRaisedExceptions().get(0).getXMIID());
    }

    public void testAddRaisedException2()
    {
        op.addRaisedException2("EntropyException");
        assertEquals(1, op.getRaisedExceptions().size());
        assertNotNull(op.getRaisedExceptions().get(0));
    }
    
    public void testRemoveRaisedException()
    {
        testAddRaisedException();
        op.removeRaisedException(op.getRaisedExceptions().get(0));
        assertEquals(0, op.getRaisedExceptions().size());
    }
    
//    public void testGetRaisedExceptions()
//    {
//        // Tested by testAddRaisedException, etc.
//    }
}
