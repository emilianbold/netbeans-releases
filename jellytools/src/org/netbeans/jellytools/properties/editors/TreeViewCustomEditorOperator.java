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


package org.netbeans.jellytools.properties.editors;

import javax.swing.JDialog;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling TreeView Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class TreeViewCustomEditorOperator extends NbDialogOperator {
    
    private JTreeOperator _tree;

    /** Creates new TreeViewCustomEditorOperator
     * @param title String title of custom editor */    
    public TreeViewCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new TreeViewCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public TreeViewCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** returns selected node name
     * @return String name of selected node */    
    public String getNodeValue() {
        TreePath tp=tree().getSelectionPath();
        if (tp==null) return null;
        return tp.getLastPathComponent().toString();
    }
    
    /** returns selected node path
     * @return String path of selected node */    
    public String getPathValue() {
        TreePath tp=tree().getSelectionPath();
        if (tp==null) return null;
        return new Node(tree(), tp).getPath();
    }
    
    /** sets selected node
     * @param treePath String path of node to be selected */    
    public void setPathValue(String treePath) {
        tree().selectPath(tree().findPath(treePath, "|"));
    }
    
    /** getter for JTreeOperator
     * @return JTreeOperator */    
    public JTreeOperator tree() {
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
}
