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
 * CreatingPropertiesFileFromMainWindow2.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 18. September 2002
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import junit.textui.TestRunner;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jellytools.nodes.PropertiesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author  Petr Felenda - QA Engineer (petr.felenda@sun.com)
 */
public class CreatingPropertiesFileFromMainWindow2 extends PropertiesEditorTestCase {
    
    /*
     * Definition of member variables and objects
     */
    final String FILE_NAME = "testPropertiesFile" ;
    
    
    
    
    /**
     * Constructor - Creates a new instance of this class
     */
    public CreatingPropertiesFileFromMainWindow2() {
        super("testCreatingPropertiesFileFromMainWindow2");
    }
    
    
    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromMainWindow2() {
        
        // open project
        openProject("properties_test2");
        openDefaultProject();
        
        
        /*
         * 1st step of testcase ( here is used toolbar's icon for opening wizard )
         * There will be opened New Wizard from Main Window Toolbar ( icon 'New File...' from toolbar 'System' )
         */
        MainWindowOperator mainWindowOp = MainWindowOperator.getDefault();
        mainWindowOp.getToolbarButton(mainWindowOp.getToolbar("File"),"New File...").pushNoBlock();
        
        
        /*
         * 2nd step of testcase
         * Select from wizard Other|Properties File and click next button.
         */
        NewFileWizardOperator nwo = new NewFileWizardOperator();
        nwo.selectCategory(WIZARD_CATEGORY_FILE);
        nwo.selectFileType(WIZARD_FILE_TYPE);
        nwo.selectProject("properties_test2");
        nwo.next();
        
        /*
         * 3rd step of testcase
         * Type name and select directory.
         */
        NewFileNameLocationStepOperator nfnlsp = new NewFileNameLocationStepOperator();
        nfnlsp.setObjectName(FILE_NAME);
        JTextFieldOperator jtfo = new JTextFieldOperator(nfnlsp,2);
        jtfo.setText("src"+File.separator+"examples");

        
       
        /*
         * 4th step of testcase
         * Confirm wizard
         */
        nfnlsp.finish();
        
        
        /*
         *  Result
         * Should be added new properties file to adequate place in explorer and opened in editor
         */
        if ( ! existsFileInEditor(FILE_NAME) )
            fail("File "+ FILE_NAME +" not found in Editor window");
        if ( ! existsFileInExplorer("examples",FILE_NAME) ) 
            fail("File "+ FILE_NAME +" not found in explorer");

        
    }
    
    public void tearDown() {
        log("Teardown");
        closeFiles();
    }
}




