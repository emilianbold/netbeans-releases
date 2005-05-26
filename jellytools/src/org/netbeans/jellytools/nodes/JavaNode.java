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

package org.netbeans.jellytools.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Java source */
public class JavaNode extends Node {
    
    /** Finds Java Node in given tree.
     * @param treeOperator JTreeOperator tree
     * @param path path to node from root (e.g. MyProject|src|mypackage|MyClass)
     */    
    public JavaNode(JTreeOperator treeOperator, String path) {
       super(treeOperator, path);
    }

    /** Finds java node under given parent node.
     * @param parent parent Node
     * @param subPath path from parent node (e.g. mypackage|MyClass)
     */    
    public JavaNode(Node parent, String subPath) {
       super(parent, subPath);
    }

    static final OpenAction openAction = new OpenAction();
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
    
    /** performs OpenAction with this node */    
    public void open() {
        openAction.perform(this);
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
