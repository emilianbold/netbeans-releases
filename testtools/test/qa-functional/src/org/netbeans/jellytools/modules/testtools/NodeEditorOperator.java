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

package org.netbeans.jellytools.modules.testtools;

/*
 * NodeEditorOperator.java
 *
 * Created on 8/30/02 3:46 PM
 */
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Node Editor" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class NodeEditorOperator extends NbDialogOperator {

    /** Creates new NodeEditorOperator that can handle it.
     */
    public NodeEditorOperator() {
        super("Node Editor");
    }

    private JSplitPaneOperator _sppJSplitPane;
    private JTreeOperator _treeNodeAndActions;
    private PropertySheetOperator _propertySheet;
    private JButtonOperator _btCustomizer;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JSplitPane in this dialog.
     * @return JSplitPaneOperator
     */
    public JSplitPaneOperator sppJSplitPane() {
        if (_sppJSplitPane==null) {
            _sppJSplitPane = new JSplitPaneOperator(this);
        }
        return _sppJSplitPane;
    }

    /** Tries to find null JTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeNodeAndActions() {
        if (_treeNodeAndActions==null) {
            _treeNodeAndActions = new JTreeOperator(sppJSplitPane());
        }
        return _treeNodeAndActions;
    }

    /** Tries to find PropertySheet in this dialog.
     * @return JToggleButtonOperator
     */
    public PropertySheetOperator propertySheet() {
        if (_propertySheet==null) {
            _propertySheet = new PropertySheetOperator(sppJSplitPane());
        }
        return _propertySheet;
    }

    /** Tries to find "" ToolbarButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizer() {
        if (_btCustomizer==null) {
            _btCustomizer = new JButtonOperator(sppJSplitPane(), "");
        }
        return _btCustomizer;
    }

    /** Tries to find "" ToolbarButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(sppJSplitPane(), "", 1);
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "" ToolbarButton
     */
    public void customizer() {
        btCustomizer().push();
    }

    /** clicks on "" ToolbarButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NodeEditorOperator by accessing all its components.
     */
    public void verify() {
        sppJSplitPane();
        treeNodeAndActions();
        propertySheet();
        btCustomizer();
        btHelp();
    }
}

