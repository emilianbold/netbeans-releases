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

/*
 * CreatingPropertiesFileFromMainWindow1.java
 *
 * This is autometed test for netBeans version 40.
 *
 * 1. Open New Wizard. Use menu File|New ... from main window.
 * 2. Select from wizard Templates|Other|Properties File and click Next button.
 * 3. There is set default file name and package name. Do not change these values.
 * 4. Confirm wizard.
 * 5. Wait to properties file appeared in Explorer.
 * RESULT: New properties file will be add (with default name - properties.properties) to adequate place in Explorer and opened in editor.
 *
 * Created on 16. September 2002
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import org.netbeans.jellytools.*;
import org.netbeans.jemmy.QueueTool;
import lib.PropertiesEditorTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/**
 *
 * @author  Petr Felenda - QA Engineer ( petr.felenda@sun.com )
 */
public class CreatingPropertiesFileFromMainWindow1 extends PropertiesEditorTestCase {
    
    /*
     * Definition of member variables
     */
    
    
    
    /**
     * Constructor - Creates a new instance of this class
     */
    public CreatingPropertiesFileFromMainWindow1() {
        super("testCreatingPropertiesFileFromMainWindow1");
    }
    
    
    /**
     * This method contains body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromMainWindow1() {
        
        // open project
        openDefaultProject();
        
        /*
         * 1st step of testcase ( here is used toolbar's icon for opening wizard )
         * There will be opened New Wizard from Main Window Toolbar ( icon 'New' from toolbar 'System' )
         */
        
        MainWindowOperator mainWindowOp = MainWindowOperator.getDefault();
        mainWindowOp.menuBar().pushMenuNoBlock("File"+menuSeparator+"New File...",menuSeparator);
        
        /*
         * 2nd step of testcase
         * Select from wizard Other|Properties File and click next button.
         */
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        
        
        /*
         * 3rd step of testcase
         * (here is nothing happen)
         * There is set default name and package. Do not change these values
         * ( package must be added because autotemed tests add jars and mount file-
         * systems witch are don't have deterministic order
         */
        // it must be selected a Folder to place the file ( is this a bug ? )
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        JTextFieldOperator jtfo = new JTextFieldOperator(nameStepOper,2);
        jtfo.setText("src");
        
        /*
         * 4th step of testcase
         * Confirm wizard
         */
        newWizard.finish();
        
        
        /*
         *  Result
         * Should be added new properties file (with default name) to adequate place in
         * explorer and opened in editor.
         */
        if ( ! existsFileInEditor(WIZARD_DEFAULT_PROPERTIES_FILE_NAME) )
            fail("File "+ WIZARD_DEFAULT_PROPERTIES_FILE_NAME +" not found in Editor window");
        if ( ! existsFileInExplorer("<default package>",WIZARD_DEFAULT_PROPERTIES_FILE_NAME+".properties") )
            fail("File "+ WIZARD_DEFAULT_PROPERTIES_FILE_NAME +" not found in explorer");
        
    }
    
    public void tearDown() {
        log("Teardown");
        closeFiles();
    }
    
    
    
}
