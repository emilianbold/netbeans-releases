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

package gui.action;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test Closing Editor tab.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CloseEditorTab extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** File to be closed */
    private String closeFile;
    
    /** Nodes represent files to be opened */
    private static Node[] openFileNodes;
    
    /**
     * Creates a new instance of CloseEditorTab
     * @param testName the name of the test
     */
    public CloseEditorTab(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of CloseEditorTab
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseEditorTab(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
        prepareFiles();
    }
    
    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        for(int i=0; i<openFileNodes.length; i++) {
            new OpenAction().performPopup(openFileNodes[i]); // fix for mdr+java, opening all files at once causes never ending loop
        }
    }
    
    public ComponentOperator open(){
        //TODO issue 44593 new CloseViewAction().performPopup(new EditorOperator(closeFile));
        new CloseViewAction().performMenu(new EditorOperator("EditServer.java"));
        
        return null;
    }
    
    public void close(){
        EditorOperator.closeDiscardAll();
    }
    
    /**
     * Prepare ten selected file from jEdit project
     */
    protected void prepareFiles(){
        String[][] files_path = getTenSelectedFiles();
        
        openFileNodes = new Node[files_path.length];
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("jEdit");
        
        for(int i=0; i<files_path.length; i++) {
            openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
        }
    }
    
    private static String[][] getTenSelectedFiles(){
        String[][] files_path = {
            {"bsh","Interpreter.java"},
            {"bsh","JThis.java"},
            {"bsh","Name.java"},
            {"bsh","Parser.java"},
            {"bsh","Primitive.java"},
            {"com.microstar.xml","XmlParser.java"},
            {"org.gjt.sp.jedit","BeanShell.java"},
            {"org.gjt.sp.jedit","Buffer.java"},
            {"org.gjt.sp.jedit","EditPane.java"},
            {"org.gjt.sp.jedit","EditPlugin.java"},
            {"org.gjt.sp.jedit","EditServer.java"}
        };
        return files_path;
    }
    
}
