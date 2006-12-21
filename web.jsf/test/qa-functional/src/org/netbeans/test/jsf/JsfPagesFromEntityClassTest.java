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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.jsf;

import javax.swing.JComboBox;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JLabelOperator;

/** Test creation of JSF pages from entity classes. We expect web application
 * with JSF framework is created.
 *
 * @author Lukasz Grela
 * @author Jiri Skrivanek
 */
public class JsfPagesFromEntityClassTest extends JellyTestCase{
    public static final String PROJECT_NAME = "myjsfproject";

    public JsfPagesFromEntityClassTest(String s) {
        super(s);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JsfPagesFromEntityClassTest("testCreateEntityClassAndPU"));
        suite.addTest(new JsfPagesFromEntityClassTest("testCreateJSFPagesFromEntityClass"));
        return suite;
    }
    
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }    

    /** Create Entity class and persistence unit. */
    public void testCreateEntityClassAndPU(){
        NewFileWizardOperator entity = NewFileWizardOperator.invoke();
        String filetype = "Entity Class";
        entity.selectProject(PROJECT_NAME);
        entity.selectCategory("Persistence");
        entity.selectFileType(filetype);
        entity.next();
        NewFileNameLocationStepOperator locationOper = new NewFileNameLocationStepOperator();
        locationOper.setPackage("mypackage");
        new JButtonOperator(locationOper, "Create Persistence Unit").pushNoBlock();
        
        NbDialogOperator persistenceDialog = new NbDialogOperator("Create Persistence Unit");
        new JComboBoxOperator(
                (JComboBox)new JLabelOperator(persistenceDialog, "Data Source").getLabelFor()).selectItem("jdbc/sample");
        new JButtonOperator(persistenceDialog, "Create").push();
        
        locationOper.finish();
    }

    /** Create JSF Pages from entity class. */
    public void testCreateJSFPagesFromEntityClass(){
        NewFileWizardOperator jsf_pages = NewFileWizardOperator.invoke();
        jsf_pages.selectCategory("Persistence");
        jsf_pages.selectFileType("JSF Pages from Entity Class");
        jsf_pages.close();
        // TODO - review next code when issue 91200 is fixed
        /*
        jsf_pages.next();
        NbDialogOperator dialog = new NbDialogOperator("New JSF Pages from Entity Class");
        new JButtonOperator(dialog,2).push();
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT")).push();
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH")).push();
         
        // verify
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        new Node(webPages, "newEntity|Detail.jsp");
        new Node(webPages, "newEntity|Edit.jsp");
        new Node(webPages, "newEntity|List.jsp");
        new Node(webPages, "newEntity|New.jsp");

         */
    }
}

