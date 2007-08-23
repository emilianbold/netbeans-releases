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

package footprint;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * Measure J2EE Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org
 */
public class J2EEProjectWorkflow extends org.netbeans.performance.test.utilities.MemoryFootprintTestCase {
    
    private String j2eeproject, j2eeproject_ejb, j2eeproject_war, j2eeproject_app;
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     */
    public J2EEProjectWorkflow(String testName) {
        super(testName);
        prefix = "J2EE Project Workflow |";
    }
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public J2EEProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "J2EE Project Workflow |";
    }
    
    @Override
    public void setUp() {
        // do nothing
    }
    
    public void prepare() {
    }
    
    public void initialize() {
        super.initialize();
        EPFootprintUtilities.closeAllDocuments();
        EPFootprintUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
        // Create, edit, build and execute a sample J2EE project
        // Create, edit, build and execute a sample J2EE project
        j2eeproject = EPFootprintUtilities.creatJ2EEeproject("Enterprise", "Enterprise Application", true);  // NOI18N
        j2eeproject_ejb = j2eeproject + "-ejb";
        j2eeproject_war = j2eeproject + "-war";
        j2eeproject_app = j2eeproject + "-app-client";
        
        EPFootprintUtilities.openFile(new Node(new ProjectsTabOperator().getProjectRootNode(j2eeproject_war), EPFootprintUtilities.WEB_PAGES + "|index.jsp"),"index.jsp", true);
        EPFootprintUtilities.insertToFile("index.jsp", 11, "Hello World", true);
        
        new EditAction().perform(new Node(new ProjectsTabOperator().getProjectRootNode(j2eeproject_war), "Configuration Files|sun-web.xml")); // NOI18N
        TopComponentOperator xmlEditor = new TopComponentOperator("sun-web.xml");

        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "    <property name=\"javaEncoding\" value=\"UTF8\">", true);
        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "      <description>Encoding for generated Java servlet.</description>", true);
        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "    </property>", true);

       
        if (xmlEditor.isModified() )
            xmlEditor.saveDocument();
        
        Node node = new Node(new SourcePackagesNode(j2eeproject_app), new SourcePackagesNode(j2eeproject_app).getChildren()[0]+"|Main.java" );
        EPFootprintUtilities.openFile(node,"Main.java",true);
        EPFootprintUtilities.insertToFile("Main.java", 20, "System.out.println(\"Hello World\");",true);
        
        new SaveAllAction().performAPI();
        
        EPFootprintUtilities.buildproject(j2eeproject);
        //runProject(j2seproject,true);
        //debugProject(j2seproject,true);
        //testProject(j2seproject);
        //collapseProject(j2seproject);
        
        return null;
    }
    
    public void close(){
        EPFootprintUtilities.deleteProject(j2eeproject);
        EPFootprintUtilities.deleteProject(j2eeproject_war);
        EPFootprintUtilities.deleteProject(j2eeproject_ejb);
        EPFootprintUtilities.deleteProject(j2eeproject_app,false);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new J2EEProjectWorkflow("measureMemoryFooprint"));
    }
    
}
