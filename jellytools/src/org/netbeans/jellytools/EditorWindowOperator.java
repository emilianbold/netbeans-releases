/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Container;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Handle the Source Editor window of NetBeans IDE. It hold editor panes
 * which can be tested by {@link EditorOperator}. This operator only enables
 * to switch between panes.
 * <p>
 * Usage:<br>
 * <pre>
 *      EditorWindowOperator ewo = new EditorWindowOperator();
 *      String filename = "MyClass";
 *      EditorOperator eo1 = ewo.selectPage(filename);
 *      // gets currently selected editor
 *      EditorOperator eo2 = ewo.getEditor();
 *      // switches to requested editor and gets EditorOperator instance
 *      EditorOperator eo3 = ewo.getEditor(filename);
 * </pre>
 * @author Jiri.Skrivanek@sun.com
 */
public class EditorWindowOperator extends NbFrameOperator {
    
    /** Components operators. */
    private JTabbedPaneOperator _tbpEditorTabbedPane;
    
    /** Creates new instance of EditorOperator. It waits for frame
     * with title "Source Editor ["+filename.
     * @param filename name of file showed in the title of the source editor
     */
    public EditorWindowOperator(String filename) {
        super(Bundle.getString("org.netbeans.core.windows.Bundle", "CTL_EditorWindow")
        +" ["+filename);
    }
    
    /** Creates new instance of EditorOperator. It waits for frame
     * with title "Source Editor [".
     */
    public EditorWindowOperator() {
        this("");
    }
    
    /** Tries to close Source Editor window and if a file is modified and confirmation
     * dialog appears, it discards all changes for all files.
     * It works also if no file is modified, so it is a safe way how to close
     * Source Editor window and no block further execution.
     */
    public void closeDiscard() {
        produceNoBlocking(new NoBlockingAction("Close Save/Discard dialog") {
            public Object doAction(Object param) {
                String title = Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title");
                int timeout = 10000;
                int time = 0;
                JDialog dialog;
                do {
                    dialog = null;
                    // every 200 ms of 10000 ms try to find dialog
                    while(dialog == null && time < timeout) {
                        dialog = JDialogOperator.findJDialog(title, false, false);
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                            throw new JemmyException("Waiting for Save/Discard dialog interrupted.", e);
                        }
                        time += 200;
                    }
                    if(dialog != null) {
                        String discard = Bundle.getStringTrimmed("org.openide.text.Bundle",
                        "CTL_Discard");
                        new JButtonOperator(new JDialogOperator(dialog), discard).push();
                    }
                } while(dialog != null); // repeat until all changes are discarded
                return(null);
            }
        });
        super.close();
    }
    
    /** Returns operator of tabbed pane holding particular files.
     * @return JTabbedPaneOperator instance of Source editor tabbed pane
     */
    public JTabbedPaneOperator tbpEditorTabbedPane() {
        if(_tbpEditorTabbedPane == null) {
            _tbpEditorTabbedPane = new JTabbedPaneOperator(this);
        }
        return _tbpEditorTabbedPane;
    }
    
    /** Selects page by its label. If only one file is open in Editor, it does
     * nothing.
     * @param label label of page to switch to
     * @return instance of selected EditorOperator
     */
    public EditorOperator selectPage(String label) {
        JTabbedPane tabbed = JTabbedPaneOperator.findJTabbedPane((Container)this.getSource(),
        null, false, false, 0);
        // probably only one file is opened so there is no JTabbedPane
        if(tabbed == null) {
            return getEditor();
        }
        tbpEditorTabbedPane().selectPage(label);
        // return selected editor
        return getEditor();
    }
    
    /** Selects page by its index. If only one file is open in Editor, it does
     * nothing.
     * @param index index of page to be selected
     * @return instance of selected EditorOperator
     * @see EditorOperator
     */
    public EditorOperator selectPage(int index) {
        JTabbedPane tabbed = JTabbedPaneOperator.findJTabbedPane((Container)this.getSource(),
        null, false, false, 0);
        // probably only one file is opened so there is no JTabbedPane
        if(tabbed == null) {
            return getEditor();
        }
        tbpEditorTabbedPane().selectPage(index);
        // return selected editor
        return getEditor();
    }
    
    /** Returns EditorOperator instance of currently selected page in Source
     * Editor window.
     * @return EditorOperator instance of the selected page
     * @see EditorOperator
     */
    public EditorOperator getEditor() {
        EditorOperator ed = new EditorOperator(new ContainerOperator(
                    (Container)tbpEditorTabbedPane().getSelectedComponent()),"");
        ed.copyEnvironment(this);
        return ed;
    }
    
    /** Selects page with given label and returns EditorOperator instance of
     * that page.
     * @param label label of page to be selected
     * @return EditorOperator instance of requested page
     * @see EditorOperator
     */
    public EditorOperator getEditor(String label) {
        return selectPage(label);
    }
    
    /** Selects page with given index and returns EditorOperator instance of
     * that page.
     * @param index index of page to be selected
     * @return EditorOperator instance of requested page
     * @see EditorOperator
     */
    public EditorOperator getEditor(int index) {
        return selectPage(index);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tbpEditorTabbedPane();
        getEditor().verify();
    }
}


