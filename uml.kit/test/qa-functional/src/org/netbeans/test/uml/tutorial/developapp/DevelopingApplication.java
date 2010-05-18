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
 * DevelopingApplications.java
 *
 * Created on January 27, 2006, 12:20 PM
 */

package org.netbeans.test.uml.tutorial.developapp;

import java.util.LinkedList;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramTypes;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.Utils;
import org.netbeans.test.umllib.values.Arg;
import org.netbeans.test.umllib.values.DefaultType;
import org.netbeans.test.umllib.values.attributes.AttributeContainerOperator;
import org.netbeans.test.umllib.values.attributes.AttributeElement;
import org.netbeans.test.umllib.values.operatons.OperationContainerOperator;
import org.netbeans.test.umllib.values.operatons.OperationElement;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.umllib.values.Argument;

/**
 *
 * @author Alexandr Scherbatiy
 */


public class DevelopingApplication extends UMLTestCase {
    
    public static final String PROJECT_NAME_JAVA = "JavaPrj";
    public static final String PROJECT_NAME_UML  = "UMLPrj";
    
    public static final String DIAGRAM_CLASS_NAME_BANK  = "BankClassDiagram";
    
    public static final String CLASS_NAME_BANK_ACCOUNT  = "BankAccount";
    public static final String CLASS_NAME_CHECKING      = "Cheking";
    public static final String CLASS_NAME_ACCOUNT_TEST  = "AccountTest";

    public static final String PACKAGE_NAME_BANK_PACK  = "bankpack";
    
    public static final String INTERFACE_NAME_BANK     = "Bank";
    
    /** Creates a new instance of DevelopingApplications */
    public DevelopingApplication(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        return new NbTestSuite(DevelopingApplication.class);
    }
    
    
    protected void setUp() {
	    System.setOut(getLog());
            Utils.waitScanningClassPath();
    }
    
    
    public void testDefiningClassElements(){
	
        JavaProject javaProject = JavaProject. createProject(PROJECT_NAME_JAVA, ProjectType.JAVA_APPLICATION, null, false, false);
        UMLProject umlProject   = UMLProject.  createProject(PROJECT_NAME_UML, ProjectType.UML_JAVA_PLATFORM_MODEL);

        Node umlModelNode = new Node(umlProject.getProjectNode(),"Model");
        
        DiagramOperator bankClassDiagram = DiagramOperator.createDiagram(DIAGRAM_CLASS_NAME_BANK, DiagramTypes.CLASS, umlModelNode);
        new TopComponentOperator(DIAGRAM_CLASS_NAME_BANK);
        try{Thread.sleep(3000); } catch (Exception e){ e.printStackTrace();} 
        
        DiagramElementOperator bankAccountElement = bankClassDiagram.putElementOnDiagram(CLASS_NAME_BANK_ACCOUNT, ElementTypes.CLASS);

	
    }
    
   
    
    public void testAttributes(){
        
        UMLProject umlProject = new UMLProject(PROJECT_NAME_UML, ProjectType.UML_JAVA_PLATFORM_MODEL);
        
        DiagramOperator bankClassDiagram = new DiagramOperator(DIAGRAM_CLASS_NAME_BANK);        
        DiagramElementOperator bankAccountElement = new DiagramElementOperator(bankClassDiagram, CLASS_NAME_BANK_ACCOUNT, ElementTypes.CLASS);

        AttributeContainerOperator attributes = new AttributeContainerOperator(bankAccountElement);
        
        AttributeElement balanceAttribute = new AttributeElement("balance");
        attributes.addAttribute(balanceAttribute);
        try{ Thread.sleep(2000); } catch (Exception e){}
        
        AttributeElement newBalanceAttribute = attributes.getAttribute("balance");
        
        assertTrue("There should be " + balanceAttribute + " instead of " + newBalanceAttribute, balanceAttribute.isEqual(newBalanceAttribute));
        
        OperationContainerOperator operations = new OperationContainerOperator(bankAccountElement);
        
        OperationElement getBalanceOperation = operations.getOperation("getBalance");
        OperationElement setBalanceOperation = operations.getOperation("setBalance");
        System.out.println("" + getBalanceOperation);
        System.out.println("" + setBalanceOperation);
        
        assertTrue("There should be " + getBalanceOperation + " in the BankAccount class."  , getBalanceOperation != null);
        assertTrue("There should be " + setBalanceOperation + " in the BankAccount class."  , setBalanceOperation != null);
        
    }

    
    public void testOperations(){
        
        DiagramOperator bankClassDiagram = new DiagramOperator(DIAGRAM_CLASS_NAME_BANK);
        DiagramElementOperator bankAccountElement = new DiagramElementOperator(bankClassDiagram, CLASS_NAME_BANK_ACCOUNT, ElementTypes.CLASS);

        try{ Thread.sleep(1000); } catch (Exception e){}
        
        OperationContainerOperator operations = new OperationContainerOperator(bankAccountElement);
        // withdraw ( int amount )
        OperationElement withdrawOperation = new OperationElement("withdraw");
        operations.addOperation(withdrawOperation);
        
        OperationElement newWithdraw = operations.getOperation("withdraw");
        
        assertTrue("There should be " + withdrawOperation + " in the BankAccount class instead of " + newWithdraw, withdrawOperation.isEqual(newWithdraw));
        
    }

    
    public void testAddingMoreElements(){
        
        UMLProject umlProject = new UMLProject(PROJECT_NAME_UML, ProjectType.UML_JAVA_PLATFORM_MODEL);
        DiagramOperator bankClassDiagram = new DiagramOperator(DIAGRAM_CLASS_NAME_BANK);        
        DiagramElementOperator bankPackage = bankClassDiagram.putElementOnDiagram(PACKAGE_NAME_BANK_PACK, ElementTypes.PACKAGE);
        
        DiagramElementOperator bankInterface = bankClassDiagram.putElementOnDiagram(INTERFACE_NAME_BANK, ElementTypes.INTERFACE);

        OperationContainerOperator bankOperations = new OperationContainerOperator(bankInterface);
        
        Argument depositAmountArgument = new Arg(DefaultType.INT,"amount");
        
        LinkedList<Argument> argList = new LinkedList<Argument>();
        argList.add(depositAmountArgument);
        OperationElement setDepositOperation = new OperationElement("deposit", argList);
        bankOperations.addOperation(setDepositOperation);
        
        OperationElement getDepositOperation = bankOperations.getOperation("deposit");

        assertTrue( new AssertTrueOperations(getDepositOperation, setDepositOperation));
        
        
        DiagramElementOperator checkingClass = bankClassDiagram.putElementOnDiagram(CLASS_NAME_CHECKING, ElementTypes.CLASS, 350, 100);
        DiagramElementOperator accountTestClass = bankClassDiagram.putElementOnDiagram(CLASS_NAME_ACCOUNT_TEST, ElementTypes.CLASS, 350, 300);
        
        try{ Thread.sleep(2000); } catch (Exception e){}
        
    }

    
    
    protected void tearDown() throws Exception {
	//super.tearDown();
        //Utils.tearDown();

	Utils.closeSaveDialog();
        Utils.closeExitDialog();
    }
    
    interface AssertTrueInterface{
        boolean assertTrue();
        String getMessage();
        
    }
    
    class AssertTrueOperations implements AssertTrueInterface{
        OperationElement op1;
        OperationElement op2;
        
        AssertTrueOperations(OperationElement op1, OperationElement op2){
            this.op1 = op1;
            this.op2 = op2;
        }
        
        public boolean assertTrue() {
            return op1.isEqual(op2);
        }

        public String getMessage() {
         return "There should be " + op2 + " in the class instead of " + op1;
        }
    }
    
    
    
    class AssertTrueAttributes implements AssertTrueInterface{
        AttributeElement attr1;
        AttributeElement attr2;
        
        AssertTrueAttributes(AttributeElement attr1, AttributeElement attr2){
            this.attr1 = attr1;
            this.attr2 = attr2;
        }
        
        public boolean assertTrue() {
            return attr1.isEqual(attr2);
        }

        public String getMessage() {
         return "There should be " + attr2 + " in the class instead of " + attr1;
        }
    }
    
    
    protected void assertTrue(AssertTrueInterface asertTrue){
        assertTrue(asertTrue.getMessage(), asertTrue.assertTrue());
    }
    
}




