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
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
/**
 * Test cases for CombinedFragment.
 */
public class CombinedFragmentTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CombinedFragmentTestCase.class);
    }

    private ICombinedFragment combinedFragment;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        combinedFragment = new TypedFactoryRetriever<ICombinedFragment>()
                            .createType("CombinedFragment");
        project.addElement(combinedFragment);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        combinedFragment.delete();
    }

    public void testAddGate()
    {
        assertEquals(0, combinedFragment.getGates().size());
        
        IGate g = (IGate)FactoryRetriever.instance().createType("Gate", null);
        //g.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(g);
        
        combinedFragment.addGate(g);
        
        assertEquals(1, combinedFragment.getGates().size());
        assertEquals(g.getXMIID(), combinedFragment.getGates().get(0).getXMIID());
    }

    public void testRemoveGate()
    {
        testAddGate();
        combinedFragment.removeGate(combinedFragment.getGates().get(0));
        assertEquals(0, combinedFragment.getGates().size());
    }

    public void testGetGates()
    {
        // Tested by testAddGate.
    }

    public void testInsertOperand()
    {
        IInteractionOperand op1 = combinedFragment.createOperand(),
                            op2 = combinedFragment.createOperand();
        
        IInteractionOperand op3 = 
            new TypedFactoryRetriever<IInteractionOperand>()
                .createType("InteractionOperand");
        
        combinedFragment.insertOperand(op3, op1);
        
        // The order of operands should now be 3 1 2
        assertEquals(3, combinedFragment.getOperands().size());
        assertEquals(op3.getXMIID(), 
                combinedFragment.getOperands().get(0).getXMIID());
        assertEquals(op1.getXMIID(), 
                combinedFragment.getOperands().get(1).getXMIID());
        assertEquals(op2.getXMIID(), 
                combinedFragment.getOperands().get(2).getXMIID());
        
        combinedFragment.removeOperand(op3);
        assertEquals(2, combinedFragment.getOperands().size());
        
        combinedFragment.insertOperand(op3, op2);
        
        // The order of operands should now be 1 3 2
        assertEquals(op1.getXMIID(), 
                combinedFragment.getOperands().get(0).getXMIID());
        assertEquals(op3.getXMIID(), 
                combinedFragment.getOperands().get(1).getXMIID());
        assertEquals(op2.getXMIID(), 
                combinedFragment.getOperands().get(2).getXMIID());
        
        combinedFragment.removeOperand(op3);
        assertEquals(2, combinedFragment.getOperands().size());

        combinedFragment.insertOperand(op3, null);
        
        // The order of operands should now be 1 2 3
        assertEquals(op1.getXMIID(), 
                combinedFragment.getOperands().get(0).getXMIID());
        assertEquals(op2.getXMIID(), 
                combinedFragment.getOperands().get(1).getXMIID());
        assertEquals(op3.getXMIID(), 
                combinedFragment.getOperands().get(2).getXMIID());
    }

    public void testCreateOperand()
    {
        // Tested by testInsertOperand
    }

    public void testAddOperand()
    {
        IInteractionOperand op = 
                new TypedFactoryRetriever<IInteractionOperand>()
                    .createType("InteractionOperand");
        combinedFragment.addOperand(op);
        
        assertEquals(1, combinedFragment.getOperands().size());
        assertEquals(op.getXMIID(), 
                combinedFragment.getOperands().get(0).getXMIID());
    }

    public void testRemoveOperand()
    {
        testAddOperand();
        combinedFragment.removeOperand(combinedFragment.getOperands().get(0));
        assertEquals(0, combinedFragment.getOperands().size());
    }

    public void testGetOperands()
    {
        // Tested by testAddOperand.
    }

    public void testSetOperator()
    {
        combinedFragment.setOperator(IInteractionOperator.IO_FILTER);
        assertEquals(IInteractionOperator.IO_FILTER, 
                combinedFragment.getOperator());
        
        combinedFragment.setOperator(IInteractionOperator.IO_LOOP);
        assertEquals(IInteractionOperator.IO_LOOP, 
                combinedFragment.getOperator());

        combinedFragment.setOperator(IInteractionOperator.IO_PAR);
        assertEquals(IInteractionOperator.IO_PAR, 
                combinedFragment.getOperator());
    }

    public void testGetOperator()
    {
        // Tested by testSetOperator.
    }
}
