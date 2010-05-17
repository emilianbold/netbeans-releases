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


package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
/**
 * Test cases for Clause.
 */
public class ClauseTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClauseTestCase.class);
    }

    private IClause clause;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        clause = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //clause.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(clause);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clause.delete();
    }

    public void testAddToBody()
    {
        IAction act = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
        clause.addToBody(act);
        assertEquals(1, clause.getBody().size());
        assertEquals(act.getXMIID(), clause.getBody().get(0).getXMIID());
    }

    public void testRemoveFromBody()
    {
        testAddToBody();
        clause.removeFromBody(clause.getBody().get(0));
        assertEquals(0, clause.getBody().size());
    }
    
    public void testGetBody()
    {
        // Tested by testAddToBody
    }

    public void testAddPredecessor()
    {
        IClause pred = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //pred.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pred);
        clause.addPredecessor(pred);
        
        assertEquals(1, clause.getPredecessors().size());
        assertEquals(pred.getXMIID(), clause.getPredecessors().get(0).getXMIID());
    }

    public void testRemovePredecessor()
    {
        testAddPredecessor();
        clause.removePredecessor(clause.getPredecessors().get(0));
        assertEquals(0, clause.getPredecessors().size());
    }

    public void testGetPredecessors()
    {
        // Tested by testAddPredecessor.
    }

    public void testAddSuccessor()
    {
        IClause succ = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //succ.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(succ);
        
        clause.addSuccessor(succ);
        assertEquals(1, clause.getSuccessors().size());
        assertEquals(succ.getXMIID(), clause.getSuccessors().get(0).getXMIID());
    }

    public void testRemoveSuccessor()
    {
        testAddSuccessor();
        clause.removeSuccessor(clause.getSuccessors().get(0));
        assertEquals(0, clause.getSuccessors().size());
    }

    public void testGetSuccessors()
    {
        // Tested by testAddSuccessor.
    }

    public void testSetTestOutput()
    {
        IValueSpecification spec = factory.createExpression(null);
        project.addElement(spec);
        clause.setTestOutput(spec);
        assertEquals(spec.getXMIID(), clause.getTestOutput().getXMIID());
    }

    public void testGetTestOutput()
    {
        // Tested by testSetTestOutput.
    }

    public void testAddToTest()
    {
        IAction act = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
        
        clause.addToTest(act);
        assertEquals(1, clause.getTest().size());
        assertEquals(act.getXMIID(), clause.getTest().get(0).getXMIID());
    }
    
    public void testRemoveFromTest()
    {
        testAddToTest();
        clause.removeFromTest(clause.getTest().get(0));
        assertEquals(0, clause.getTest().size());
    }
    
    public void testGetTest()
    {
        // Tested by testAddToTest and testRemoveFromTest
    }
}
