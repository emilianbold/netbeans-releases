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

package gui.menu;


import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test List Of The Recent Opened Windows popup menu on Editor Window down button if 10 files opened
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class EditorDownButtonPopupMenu extends testUtilities.PerformanceTestCase {
    
    private static EditorWindowOperator editor;
    
    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
    }
    
    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
    }
    
    
    public void testEditorDownButtonPopupMenu(){
        doMeasurement();
    }
    
    protected void initialize(){
        openFiles();
        editor = new EditorWindowOperator();
    }
    
    public void prepare(){
        
    }
    
    public ComponentOperator open(){
        editor.btDown().clickForPopup();
        ComponentOperator popupComponent = new ComponentOperator(editor.btDown().getContainer(ComponentSearcher.getTrueChooser("org.netbeans.core.windows.view.ui.RecentViewListDlg")));
        return popupComponent;
  }
    
    protected void shutdown(){
        editor.closeDiscard();
    }
    
    /**
     * Get a prepared java file (Main20kB.java), create 10 copies and 
     * open it in editor.
     */
    protected void openFiles(){
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
        
        Node[] openFileNodes = new Node[files_path.length];
            
        for(int i=0; i<files_path.length; i++) {
                openFileNodes[i] = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit41"),"Source Packages" + '|' +  files_path[i][0] + '|' + files_path[i][1]);
        }
            
        new OpenAction().performAPI(openFileNodes);
    }
}
