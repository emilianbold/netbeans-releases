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

package gui.action;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.QueueTool;


/**
 * Test of opening files if Editor is already opened.
 * OpenFilesNoCloneableEditor is used as a base for tests of opening files
 * when editor is already opened.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditorWithOpenedEditor extends OpenFilesNoCloneableEditor {

    /** Name of file to pre-open */
    public static String fileName_preopen;
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditorWithOpenedEditor(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of OpenFilesNoCloneableEditorWithOpenedEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditorWithOpenedEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public void testOpening20kBPropertiesFile(){
        WAIT_AFTER_OPEN = 1500;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle20kB.properties";
        fileName_preopen = "Bundle.properties";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpening20kBPictureFile(){
        WAIT_AFTER_OPEN = 1500;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        fileName_preopen = "Main.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    public void initialize(){
        super.initialize();
	SourcePackagesNode spn=new SourcePackagesNode("PerformanceTestData");
	Node n=new Node(spn,"org.netbeans.test.performance|" + fileName_preopen);
        new OpenAction().performPopup(n);
    }
    
}
