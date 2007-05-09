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

import java.util.HashMap;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;

/**
 * Test of opening files.
 *
 * @author  lmartinek@netbeans.org
 */
public class OpenJ2EEFiles extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Node to be opened/edited */
    public static Node openNode ;
    
    /** Folder with data */
    public static String fileProject;
    
    /** Folder with data  */
    public static String filePath;
    
    /** Name of file to open */
    public static String editorTitle;
    
    /** Menu item name that opens the editor */
    public static String menuItem;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org/openide/actions/Bundle", "Open");
    
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org/openide/actions/Bundle", "Edit");
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public OpenJ2EEFiles(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenJ2EEFiles(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testOpeningJava(){
        WAIT_AFTER_OPEN = 4000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication-EJBModule";
        filePath = "Source Packages|test|TestSessionRemote.java";
        editorTitle = "TestSessionRemote.java";
        menuItem = OPEN;
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        doMeasurement();
    }
    
    public void testOpeningSessionBean(){
        WAIT_AFTER_OPEN = 4000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication-EJBModule";
        filePath = "Enterprise Beans|TestSessionSB";
        editorTitle = "TestSessionBean.java";
        menuItem = OPEN;
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        doMeasurement();
    }

    public void testOpeningEntityBean(){
        WAIT_AFTER_OPEN = 4000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication-EJBModule";
        filePath = "Enterprise Beans|TestEntityEB";
        editorTitle = "TestEntityBean.java";
        menuItem = OPEN;
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        doMeasurement();
    }
    
    public void testOpeningEjbJarXml(){
        WAIT_AFTER_OPEN = 3000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication-EJBModule";
        filePath = "Configuration Files|ejb-jar.xml";
        editorTitle = "ejb-jar.xml";
        menuItem = OPEN;
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();    
        doMeasurement();
    }
    
    public void testOpeningSunEjbJarXml(){
        WAIT_AFTER_OPEN = 3000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication-EJBModule";
        filePath = "Configuration Files|sun-ejb-jar.xml";
        editorTitle = "sun-ejb-jar.xml";
        menuItem = OPEN;
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();    
        doMeasurement();
    }

    public void testOpeningApplicationXml(){
        WAIT_AFTER_OPEN = 2000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication";
        filePath = "Configuration Files|application.xml";
        editorTitle = "application.xml";
        menuItem = EDIT;
        //repaintManager().setOnlyEditor(true);
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        doMeasurement();
    }
    
    public void testOpeningSunApplicationXml(){
        WAIT_AFTER_OPEN = 2000;
        //MEASURE_PAINT_NUMBER = 1;
        setJavaEditorCaretFilteringOn();
        fileProject = "TestApplication";
        filePath = "Configuration Files|sun-application.xml";
        editorTitle = "sun-application.xml";
        menuItem = OPEN;
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();   
        doMeasurement();
    }
    
    public void initialize(){
        //repaintManager().setOnlyEditor(true);
        EditorOperator.closeDiscardAll();
        BaseOptions options = BaseOptions.getOptions(JavaKit.class);
        options.setStatusBarCaretDelay(0);
        HashMap props = new HashMap();
        props.put(org.netbeans.editor.SettingsNames.CODE_FOLDING_ENABLE, Boolean.FALSE);
        options.setCodeFoldingProps(props);
        /* TODO doesn't work after retouche integration
        JavaSettings java_settings = JavaSettings.getDefault();
        java_settings.setShowOverriding(false);
        java_settings.enableCompileStatus(false);
         */ 
    }

    public void shutdown(){
        //repaintManager().setOnlyEditor(false);
        repaintManager().resetRegionFilters();   
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        JTreeOperator tree = new ProjectsTabOperator().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        this.openNode = new Node(new ProjectRootNode(tree, fileProject), filePath);
        
        if (this.openNode == null) {
            throw new Error ("Cannot find node ["+ filePath + "] in project [" + fileProject + "]");
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open(){
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error ("Cannot get context menu for node ["+ filePath + "] in project [" + fileProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(this.menuItem);
        }
        catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error ("Cannot push menu item "+this.menuItem+" of node ["+ filePath + "] in project [" + fileProject + "]");
        }
        log("------------------------- after open ------------");
        return new TopComponentOperator(this.editorTitle);
    }
    
    public void close(){
        if (testedComponentOperator != null) {
            // HACK
            new SaveAllAction().performAPI();
            ((TopComponentOperator)testedComponentOperator).close();
            /*
            if (getName().equals("testOpeningSunApplicationXml")) {
                ((TopComponentOperator)testedComponentOperator).close();
            } else {
                ((TopComponentOperator)testedComponentOperator).closeDiscard();
            }
            */
        }
        else {
            throw new Error ("no component to close");
        }
    }
    
}
