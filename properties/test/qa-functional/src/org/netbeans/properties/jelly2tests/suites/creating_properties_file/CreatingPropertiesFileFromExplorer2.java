/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CreatingPropertiesFileFromExplorer2.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 19. brezen 2002, 11:07
 *
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import org.netbeans.jellytools.*;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;


/**
 *
 * @author  Petr Felenda - QA Engineer
 */
public class CreatingPropertiesFileFromExplorer2 extends PropertiesEditorTestCase {
    
    /*
     * Definition of member variables and objects
     */
    final String PACKAGE_PATH = "examples";
    final String FILE_NAME = "testFileExplorer2";
    
    
    /**
     * Constructor - Creates a new instance of CreatingPropertiesFileFromExplorer2
     */
    public CreatingPropertiesFileFromExplorer2() {
        super("testCreatingPropertiesFileFromExplorer2");
    }
    
    
    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromExplorer2() {
        
        
        // open project
        openDefaultProject();
        
        /*
         * 1st step of testcase
         * In explorer create new properties file. Right click on any directory and
         * select in appeared context menu New|Other|Properties File.
         */
        Node node = new Node(new SourcePackagesNode(defaultProjectName),PACKAGE_PATH);
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"File/Folder...",menuSeparator);
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        //type class name
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.setObjectName(FILE_NAME);
        //push finish
        newWizard.finish();
        
       
        /*
         * 2nd step of testcase
         * In explorer create new properties file. Right click on any directory and select
         * in appeared context menu New|Other|Properties File
         */
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"File/Folder...",menuSeparator);
        newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        
        /*
         * 3th step of testcase
         * Type name to appeared wizard.(as same name as previous case)
         */
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.setObjectName(FILE_NAME);
        
        
        /*
         * 4th step of testcase - Result
         * Try confirm wizard. 'Finish' button should be disabled.
         */
        if ( nfnlso.btFinish().isEnabled() == true )
            fail("Button finish is enabled and should be disabled.Because file with this name exist.");
        else
            log("Button is disabled. (Ok)");
        
        
        /*
         * 5th step of testcase
         * Cancel wizard. Click to 'Cancel' button.
         */
        nfnlso.btCancel().push();
        
    }
    
    public void tearDown() {
        log("Teardown");
        closeFiles();
    }
    
    
    
}
