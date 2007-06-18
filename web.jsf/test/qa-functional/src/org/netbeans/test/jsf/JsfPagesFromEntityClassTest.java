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
package org.netbeans.test.jsf;

import javax.swing.JComboBox;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JLabelOperator;

/** Test creation of JSF pages from entity classes. We expect web application
 * with JSF framework is created. This feature has beed dropped from NB6.0
 * (see issue 92009). We can test only creation of Entity Class and Persistence Unit.
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
}

