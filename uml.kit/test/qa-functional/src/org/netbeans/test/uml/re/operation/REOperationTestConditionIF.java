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


/*
 * RE_OperationTests.java
 *
 * Created on May 23, 2006, 12:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.uml.re.operation;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.customelements.CombinedFragmentOperator;
import org.netbeans.test.umllib.customelements.ExpressionOperator;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.customelements.MessageOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 *
 * @author Alexandr Scherbatiy
 */


public class REOperationTestConditionIF extends REOperationTestCase{

    
    
    
    String javaProjectName = "REOperationConditionIF";
    String umlProjectName  = "REOperationConditionIF_UML";
    
    
    
    String operationPath  = "Model|test|Test|Operations";
    String operationName  = "testConditionIf";
    
    String operationNodeName = operationPath + "|" + operationName ;
    
    
    /** Creates a new instance of RE_OperationTests */
    public REOperationTestConditionIF(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
         return new NbTestSuite(REOperationTestConditionIF.class);
    }
    
    
      public void testREProject(){
        openProject(umlProjectName, javaProjectName);
    }

    
    public void testConditionIf1(){
        
        String diagramName = "1";
        
        createDiagram( operationNodeName + "1", diagramName);
        DiagramOperator diagram = new DiagramOperator(diagramName);
        delay(5000);
        
        showDiagramElements(diagram);
        
        LifelineOperator self = new LifelineOperator(diagram, "self", "Test") ;
        LifelineOperator a = new LifelineOperator(diagram, "a", "int") ;
        LifelineOperator b = new LifelineOperator(diagram, "b", "int") ;
        
        
        CombinedFragmentOperator comb1 = new CombinedFragmentOperator(diagram, "alt", 0);
        CombinedFragmentOperator comb2 = new CombinedFragmentOperator(diagram, "alt", 1);
        
        ExpressionOperator expr1 = new ExpressionOperator(diagram, "[a > b]");
        ExpressionOperator expr2 = new ExpressionOperator(diagram, "[Else]", 0);

       // ExpressionOperator errExpr = new ExpressionOperator(diagram, "[(a + b) != a * b]");
       // assertNull(78391, "Issue: Mistake in parsed expression on Interaction operand " + errExpr.getName(), errExpr);
        
        ExpressionOperator expr3 = new ExpressionOperator(diagram, "[(a + b) != (a * b)]");
        ExpressionOperator expr4 = new ExpressionOperator(diagram, "[Else]", 1);
        
          
        diagram.close();
    }

    
    public void testConditionIf2(){
     
        String diagramName = "2";
     
        createDiagram( operationNodeName + "2", diagramName);
        DiagramOperator diagram = new DiagramOperator(diagramName);

        showDiagramElements(diagram);

        LifelineOperator self = new LifelineOperator(diagram, "self", "Test") ;

        LifelineOperator cls = new LifelineOperator(diagram, "", "Class") ;
        LifelineOperator method = new LifelineOperator(diagram, "method", "Method") ;

        //LifelineOperator methodArray = new LifelineOperator(diagram, "method[]", "Method") ;
        
        CombinedFragmentOperator alt = new CombinedFragmentOperator(diagram, "alt");

        
        ExpressionOperator expr1 = new ExpressionOperator(diagram, "[method != null && method.length > 0]");
        ExpressionOperator expr2 = new ExpressionOperator(diagram, "[Else]");
     
        try {
          MessageOperator getClassMessage = new MessageOperator(diagram, "public Class  getClass(  )");
          assertTrue(self.equals(getClassMessage.getFromLifeline()));
          assertTrue(self.equals(getClassMessage.getToLifeline()));
        } catch (org.netbeans.jemmy.TimeoutExpiredException e){
           fail(121394, " Message public Class getClass() is missing "  );
        }
          
        MessageOperator getNameMessage = new MessageOperator(diagram, "public String  getName(  )");
        
        
        
        //System.out.println("Message name = " + clsMessage.getName());
        
        LifelineOperator f = getNameMessage.getFromLifeline();
        LifelineOperator t = getNameMessage.getToLifeline();
        
        System.out.println("from = " + f.getName());
        System.out.println("to   = " + t.getName());
        
        
        //assertFalse(78375, "Issue:  Array element method is called from array object.", getNameMessage.getToLifeline().equals(methodArray));
        
        
        
        diagram.close();
     
        
     
     
    }

    public void testConditionIf3(){
        
        String diagramName = "3";
        
        createDiagram( operationNodeName + "3", diagramName);
        DiagramOperator diagram = new DiagramOperator(diagramName);
        
        showDiagramElements(diagram);
        
        LifelineOperator self = new LifelineOperator(diagram, "self", "Test") ;
        LifelineOperator count = new LifelineOperator(diagram, "count", "Integer") ;
        
        
        CombinedFragmentOperator alt1 = new CombinedFragmentOperator(diagram, "alt",0);
        ExpressionOperator expr1 = new ExpressionOperator(diagram, "[list != null]");
        ExpressionOperator expr2 = new ExpressionOperator(diagram, "[Else]", 0);
        
        CombinedFragmentOperator alt2 = new CombinedFragmentOperator(diagram, "alt", 1);

        ExpressionOperator expr3 = new ExpressionOperator(diagram, "[0 < list.size() && list.size() < 3]");
        ExpressionOperator expr4 = new ExpressionOperator(diagram, "[Else]", 1);
        
        CombinedFragmentOperator alt3 = new CombinedFragmentOperator(diagram, "alt", 2);

        ExpressionOperator expr5 = new ExpressionOperator(diagram, "[0 < list.size() && list.size() < 3]");
        ExpressionOperator expr6 = new ExpressionOperator(diagram, "[Else]", 2);
        
        try {
            MessageOperator size = new MessageOperator(diagram, "public int  size(  )");
            System.out.println("Message name = " + size.getName());
            LifelineOperator linkedlist = new LifelineOperator(diagram, "list", "List");
            assertTrue(self.equals(size.getFromLifeline()));
            assertTrue(linkedlist.equals(size.getToLifeline()));   // Change after Issue 78355 fixing
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
            if (e.getMessage().equals("Wait for Chooser for Message Element: public int  size(  )")) {
                fail(121421, " Message public int  size(  ) is missing ");
            }
        }

        
        //assertNull(78355, "Issue: Lifeline has type of initialized object instead of declared type of variable.", linkedlist);
        
        LifelineOperator list = new LifelineOperator(diagram, "list", "List") ;

        diagram.close();
        
    }

    public void testConditionIf4(){
        
        String diagramName = "4";
        
        createDiagram( operationNodeName + "4", diagramName);
        DiagramOperator diagram = new DiagramOperator(diagramName);
        delay(5000);
        
        showDiagramElements(diagram);
        
        LifelineOperator self = new LifelineOperator(diagram, "self", "Test") ;
        LifelineOperator a = new LifelineOperator(diagram, "a", "boolean") ;
        LifelineOperator b = new LifelineOperator(diagram, "b", "boolean") ;

        
        
        CombinedFragmentOperator alt1 = new CombinedFragmentOperator(diagram, "alt",0);
        ExpressionOperator expr11 = new ExpressionOperator(diagram, "[a & b]");
        ExpressionOperator expr12 = new ExpressionOperator(diagram, "[Else]", 0);
        
        CombinedFragmentOperator alt2 = new CombinedFragmentOperator(diagram, "alt",1);

       // ExpressionOperator errExpr = new ExpressionOperator(diagram, "[! a & b || (! a ^ ! b]");
       // assertNull(78391, "Issue: Mistake in parsed expression on Interaction operand " + errExpr.getName(), errExpr);

        ExpressionOperator expr21 = new ExpressionOperator(diagram, "[! (a & b) || (! a ^ ! b)]");
        ExpressionOperator expr22 = new ExpressionOperator(diagram, "[Else]", 1);
        
        
        
        diagram.close();
        
    }
    
    
    public void testConditionIfRootElements(){
     
        Node modelNode = new Node(umlProject.getProjectNode(), "Model");
        assertFalse(78349, "Issues: Datatypes are not placed under their packages: \"java.lang.reflect.Method\" is under UML Model node!", modelNode.isChildPresent("Method"));
     
    }
    
    //*/
    
}
