/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Form */
public class FormNode extends Node {
    
    /** creates new FormNode
     * @param treeOperator JTreeOperator tree
     * @param treePath String tree path */    
    public FormNode(JTreeOperator treeOperator, String treePath) {
       super(treeOperator, treePath);
    }

    /** creates new FormNode
     * @param parent parent Node
     * @param treeSubPath String tree path from parent node */    
    public FormNode(Node parent, String treeSubPath) {
       super(parent, treeSubPath);
    }

    /** creates new FormNode
     * @param treeOperator JTreeOperator tree
     * @param path TreePath */    
    public FormNode(JTreeOperator treeOperator, TreePath path) {
       super(treeOperator, path);
    }

    static final OpenAction openAction = new OpenAction();
    static final EditAction editAction = new EditAction();
    static final CompileAction compileAction = new CompileAction();
    static final CutAction cutAction = new CutAction();
    static final CopyAction copyAction = new CopyAction();
    static final PasteAction pasteAction = new PasteAction();
    static final AddClassAction addClassAction = new AddClassAction();
    static final AddInterfaceAction addInterfaceAction = new AddInterfaceAction();
    static final DeleteAction deleteAction = new DeleteAction();
    static final SaveAsTemplateAction saveAsTemplateAction = new SaveAsTemplateAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            openAction,
            editAction,
            compileAction,
            cutAction,
            copyAction,
            pasteAction,
            addClassAction,
            addInterfaceAction,
            deleteAction,
            saveAsTemplateAction,
            propertiesAction
        });
    }
    
/*   protected static final Action[] javaActions = new Action[] {
        cutAction,
        copyAction,
        deleteAction,
        compileAction,
    };
    
    Action[] getActions() {
        return javaActions;
    }*/

    /** performs OpenAction with this node */    
    public void open() {
        openAction.perform(this);
    }

    /** performs EditAction with this node */    
    public void edit() {
        editAction.perform(this);
    }

    /** performs CompileAction with this node */    
    public void compile() {
        compileAction.perform(this);
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

    /** performs AddClassAction with this node */    
    public void addClass() {
        addClassAction.perform(this);
    }

    /** performs AddInterfaceAction with this node */    
    public void addInterface() {
        addInterfaceAction.perform(this);
    }

    /** performs DeleteAction with this node */    
    public void delete() {
        deleteAction.perform(this);
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
