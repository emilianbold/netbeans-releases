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
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
}
