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
 * File CreatingPropertiesFileFromExplorer1.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 16. September 2002
 *
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import org.netbeans.jellytools.*;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jellytools.nodes.SourcePackagesNode;


/**
 *
 * @author  Petr Felenda - QA Engineer ( petr.felenda@sun.com )
 */
public class CreatingPropertiesFileFromExplorer1 extends PropertiesEditorTestCase {
    
    /*
     * Definition of member variables and objects
     */
    final String FILE_NAME = "testFileExplorer1" ;
    final String PACKAGE_PATH = "samples";
    
    
    
    /**
     *  Constructor - creates a new instance of CreatingPropertiesFileFromExplorer1
     */
    public CreatingPropertiesFileFromExplorer1() {
        super("testCreatingPropertiesFileFromExplorer1");
    }
    
    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromExplorer1() {
        
        
        // open project
        openDefaultProject();
        
        /*
         * 1st step of testcase
         * In explorer create new properties file. Right click on any directory and
         * select in appeared context menu New|Other|Properties File.
         */
        log(PACKAGE_PATH);
        SourcePackagesNode spn = new SourcePackagesNode(defaultProjectName);
        Node node = new Node(spn,PACKAGE_PATH);
        
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"File/Folder...",menuSeparator);
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        
        /*
         * 2nd step of testcase
         * Type name to appeared wizard.
         */
        NewFileNameLocationStepOperator nameStepOper = new NewFileNameLocationStepOperator();
        nameStepOper.setObjectName(FILE_NAME);
        
        
        /*
         * 3rd step of testcase
         * Confirm wizard. Press Finish button.
         */
        newWizard.finish();
        
        /*
         * Result
         * Should be created new file in explorer and opened in editor.
         */
        if ( ! existsFileInEditor(FILE_NAME) )
            fail("File "+ FILE_NAME +" not found in Editor window");
        if ( ! existsFileInExplorer("samples",FILE_NAME) )
            fail("File "+ FILE_NAME +" not found in explorer");
    }
    
    public void tearDown() {
        log("Teardown");
        closeFiles();
    }
    
    
    
    
}
