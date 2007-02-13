/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package prepare;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.Operator;

import org.netbeans.junit.NbTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened NB plug-in.
 * Open 3 java files and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.
 *
 * @author Marian.Mirilovic@sun.com
 */
public class PrepareIDEForPluginComplexMeasurements extends PrepareIDEForComplexMeasurements {
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public PrepareIDEForPluginComplexMeasurements(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PrepareIDEForComplexMeasurements("closeWelcome"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeAllDocuments"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeMemoryToolbar"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeUIGesturesToolbar"));
        suite.addTest(new PrepareIDEForPluginComplexMeasurements("openFiles"));
        suite.addTest(new PrepareIDEForComplexMeasurements("saveStatus"));
        return suite;
    }

    
    /**
     * Open 3 selected files from jEdit project.
     */
    public void openFiles(){
        
        try {
            String[][] files_path = {
                {"org.myorg.systemproperties","AllPropsChildren.java"},
                {"org.myorg.systemproperties","AllPropsNode.java"},
                {"org.myorg.systemproperties","OnePropNode.java"},
                {"org.myorg.systemproperties","PropertiesNotifier.java"},
                {"org.myorg.systemproperties","RefreshPropsAction.java"}
            };
            
            Node[] openFileNodes = new Node[files_path.length];
            Node node;
            
            // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
            ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("SystemProperties");
            projectNode.expand();
            
            SourcePackagesNode sourceNode = new SourcePackagesNode(projectNode);
            sourceNode.expand();
            
            // create exactly (full match) and case sensitively comparing comparator
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            sourceNode.setComparator(comparator);
            
            for(int i=0; i<files_path.length; i++) {
                node = new Node(sourceNode,files_path[i][0]);
                node.expand();
                
                openFileNodes[i] = new Node(node,files_path[i][1]);
                
                //try to avoid issue 56825
                openFileNodes[i].select();
                
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
            }
            
            // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
            // it doesn't finish in the real-time -> hard to reproduced by hand
            try {
                new OpenAction().performAPI(openFileNodes);
            }catch(Exception exc){
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes);
                err.println("issue 56825 : Success");
            }
            
            
            // check whether files are opened in editor
            for(int i=0; i<files_path.length; i++) {
                new EditorOperator(files_path[i][1]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
            
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
}
