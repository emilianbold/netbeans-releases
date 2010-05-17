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



/**
 *
 * @author Alexandr Scherbatiy
 */

public class REOperationTestBlockTryCatchFinally extends REOperationTestCase{
    
    
    
    String javaProjectName = "REOperationExceptionHandling";
    String umlProjectName  = "REOperationExceptionHandling_UML";
    
    
    
    //String operationPath  = "Model|test|Test|Operations";
    String operationPath  = "Model|test|Test";
    String operationName  = "testBlockTryCatch";
    
    String diagramNamePrefix = "BlockTryCatchFinally";
    
    String operationNodeName = operationPath + "|" + operationName ;
    
    
    /** Creates a new instance of RE_OperationTests */
    public REOperationTestBlockTryCatchFinally(String name) {
	super(name);
    }
    
    public static NbTestSuite suite() {
	return new NbTestSuite(REOperationTestBlockTryCatchFinally.class);
    }
    
    
     public void testREProject(){
        openProject(umlProjectName, javaProjectName);
    }

    
    public void testTestBlockTryCatchFinally1(){
	
	String diagramName = diagramNamePrefix + "_1";
	
	createDiagram( operationNodeName + "1", diagramName);
	DiagramOperator diagram = new DiagramOperator(diagramName);
	delay(5000);
	
	showDiagramElements(diagram);
	
	LifelineOperator self = new LifelineOperator(diagram, "self", "Test") ;
	LifelineOperator thread = new LifelineOperator(diagram, "", "Thread") ;
	LifelineOperator exception = new LifelineOperator(diagram, "", "Exception") ;
	
	//LifelineOperator interruptedException = new LifelineOperator(diagram, "e", "InterruptedException") ;
	
	CombinedFragmentOperator comb = new CombinedFragmentOperator(diagram, "assert");
	ExpressionOperator finallyExpr = new ExpressionOperator(diagram, "[Finally]");
	
	
	try{
	    ExpressionOperator tryExpr = new ExpressionOperator(diagram, "[Catch]");
	}catch (Exception e){
	    fail(78414, "Issue: There is an empty expression body for 'catch' block");
	    
	}
	
	//MessageOperator message = new MessageOperator(diagram, "");
	
	//assertTrue(self.equals(setData1.getFromLifeline()));
	//assertTrue(self.equals(setData1.getToLifeline()));
	
	
	diagram.close();
    }
    
}
