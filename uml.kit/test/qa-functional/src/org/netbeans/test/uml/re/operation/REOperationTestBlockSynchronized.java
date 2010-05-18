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
import org.netbeans.test.uml.re.operation.REOperationTestCase;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.customelements.CombinedFragmentOperator;
import org.netbeans.test.umllib.customelements.ExpressionOperator;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.customelements.MessageOperator;

/**
 *
 * @author Alexandr Scherbatiy
 */

public class REOperationTestBlockSynchronized extends REOperationTestCase{
    
    
    
    String javaProjectName = "REOperationBlockSynchronized";
    String umlProjectName  = "REOperationBlockSynchronized_UML";
    
    
    
    String operationPath  = "Model|test|Test|Operations";
    String operationName  = "testBlockSynchronized";
    
    String diagramNamePrefix = "BlockSynchronized";
    
    String operationNodeName = operationPath + "|" + operationName ;
    
    
    /** Creates a new instance of RE_OperationTests */
    public REOperationTestBlockSynchronized(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        return new NbTestSuite(REOperationTestBlockSynchronized.class);
    }
    
    
    public void testREProject(){
        openProject(umlProjectName, javaProjectName);
    }
    
    public void testBlockSynchronized1(){
        
        String diagramName = diagramNamePrefix + "_1";
        
        createDiagram( operationNodeName + "1", diagramName);
        DiagramOperator diagram = new DiagramOperator(diagramName);
        delay(5000);
        
        showDiagramElements(diagram);
        
        LifelineOperator self = new LifelineOperator(diagram, "self", "Test") ;
        LifelineOperator data1 = new LifelineOperator(diagram, "data1", "Object") ;
        LifelineOperator data2 = new LifelineOperator(diagram, "data2", "Object") ;
        
        CombinedFragmentOperator comb = new CombinedFragmentOperator(diagram, "region");
        ExpressionOperator expr = new ExpressionOperator(diagram, "[this]");
        
        
        MessageOperator setData1 = new MessageOperator(diagram, "public void  setData1( Object data1 )");
        
        assertTrue(self.equals(setData1.getFromLifeline()));
        assertTrue(self.equals(setData1.getToLifeline()));
        
        MessageOperator setData2 = new MessageOperator(diagram, "public void  setData2( Object data2 )");
        
        assertTrue(self.equals(setData2.getFromLifeline()));
        assertTrue(self.equals(setData2.getToLifeline()));
        
        diagram.close();
    }
    
}
