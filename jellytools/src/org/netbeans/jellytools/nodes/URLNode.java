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

package org.netbeans.jellytools.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Bookmark (URL) file */
public class URLNode extends Node {
    
    /** creates new URLNode
     * @param treeOperator JTreeOperator tree
     * @param treePath String tree path */    
    public URLNode(JTreeOperator treeOperator, String treePath) {
       super(treeOperator, treePath);
    }

    /** creates new URLNode
     * @param parent parent Node
     * @param treeSubPath String tree path from parent node */    
    public URLNode(Node parent, String treeSubPath) {
       super(parent, treeSubPath);
    }

    /** creates new URLNode
     * @param treeOperator JTreeOperator tree
     * @param path TreePath */    
    public URLNode(JTreeOperator treeOperator, TreePath path) {
       super(treeOperator, path);
    }

    static final OpenAction openAction = new OpenAction();
    static final ActionNoBlock editAction = 
                         new ActionNoBlock(null, 
                                           Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit"));
    static final CutAction cutAction = new CutAction();
    static final CopyAction copyAction = new CopyAction();
    static final PasteAction pasteAction = new PasteAction();
    static final DeleteAction deleteAction = new DeleteAction();
    static final RenameAction renameAction = new RenameAction();
    static final SaveAsTemplateAction saveAsTemplateAction = new SaveAsTemplateAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            openAction,
            editAction,
            cutAction,
            copyAction,
            pasteAction,
            deleteAction,
            renameAction,
            saveAsTemplateAction,
            propertiesAction
        });
    }
    
    /** performs OpenAction with this node */    
    public void open() {
        openAction.perform(this);
    }

    /** performs EditAction with this node */    
    public void edit() {
        editAction.perform(this);
    }

    /** performs CutAction with this node */    
    public void cut() {
        cutAction.perform(this);
    }

    /** performs CopyAction with this node */    
    public void copy() {
        copyAction.perform(this);
    }

    /** performs PasteAction with this node */    
    public void paste() {
        pasteAction.perform(this);
    }

    /** performs DeleteAction with this node */    
    public void delete() {
        deleteAction.perform(this);
    }

    /** performs RenameAction with this node */    
    public void rename() {
        renameAction.perform(this);
    }

    /** performs SaveAsTemplateAction with this node */    
    public void saveAsTemplate() {
        saveAsTemplateAction.perform(this);
    }

    /** performs PropertiesAction with this node */    
    public void properties() {
        propertiesAction.perform(this);
    }
   
}
