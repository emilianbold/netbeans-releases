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


package org.netbeans.test.uml.sqd;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.NewPackageWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.tests.DiagramCreation;

/**
 *
 * @author psb
 * @spec UML/SQDDiagram.xml
 */
public class SequenceDiagramCreation extends DiagramCreation {


    
    /** Need to be defined because of JUnit */
    public SequenceDiagramCreation(String name) {
        super(name,NewDiagramWizardOperator.SEQUENCE_DIAGRAM,"SequenceDiagramProjectADC","org.netbeans.test.uml.sqd.SequenceDiagramCreation");
    }
     public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite(SequenceDiagramCreation.class);
//         
        NbTestSuite suite = new NbTestSuite();
       
        suite.addTest(new SequenceDiagramCreation("testAddDiagramModel"));
        suite.addTest(new SequenceDiagramCreation("testAddDiagramDiagram"));
        suite.addTest(new SequenceDiagramCreation("testAddPackageWithDiagram"));
//nb7.0 tests are comment due to open diagram bugs 132674 
        //      suite.addTest(new SequenceDiagramCreation("testOpenExistingWithDoubleClick"));
//        suite.addTest(new SequenceDiagramCreation("testOpenExistingWithOpen"));
//        suite.addTest(new SequenceDiagramCreation("testOpenExistingWithEnter"));
         suite.addTest(new SequenceDiagramCreation("testChanges"));
//       suite.addTest(new SequenceDiagramCreation("testSaveChangesSaveAll"));
//         suite.addTest(new SequenceDiagramCreation("testSaveChangesSaveDocument"));
//        suite.addTest(new SequenceDiagramCreation("testSaveNewWithSaveDocument"));
//      suite.addTest(new SequenceDiagramCreation("testSaveChangesAfterCloseDocument"));
//        suite.addTest(new SequenceDiagramCreation("testSaveChangesAfterCloseAll"));
//        suite.addTest(new SequenceDiagramCreation("testDontSaveNewAfterCloseWindow"));
//        suite.addTest(new SequenceDiagramCreation("testSaveNewWithCtrlS"));
       suite.addTest(new SequenceDiagramCreation("testCancelClose"));
        suite.addTest(new SequenceDiagramCreation("testCrossInSaveOnClose"));
//        suite.addTest(new SequenceDiagramCreation("testSaveByOneAfterCloseAll	"));
//        suite.addTest(new SequenceDiagramCreation("testDeleteEmptyNewDiagram"));
//      suite.addTest(new SequenceDiagramCreation("testDeleteModifiedDiagram"));
//          suite.addTest(new SequenceDiagramCreation("testSaveAllAfterCloseAll"));

        return suite;
    }

}
