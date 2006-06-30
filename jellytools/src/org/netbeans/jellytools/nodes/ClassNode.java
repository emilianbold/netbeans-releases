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

package org.netbeans.jellytools.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Class file */
public class ClassNode extends Node {

    /** creates new ClassNode
     * @param treeOperator JTreeOperator tree
     * @param treePath String tree path */
    public ClassNode(JTreeOperator treeOperator, String treePath) {
       super(treeOperator, treePath);
    }

    /** creates new ClassNode
     * @param parent parent Node
     * @param treeSubPath String tree path from parent node */    
    public ClassNode(Node parent, String treeSubPath) {
       super(parent, treeSubPath);
    }

    /** creates new ClassNode
     * @param treeOperator JTreeOperator tree
     * @param path TreePath */    
    public ClassNode(JTreeOperator treeOperator, TreePath path) {
       super(treeOperator, path);
    }

    static final CutAction cutAction = new CutAction();
    static final CopyAction copyAction = new CopyAction();
    static final PasteAction pasteAction = new PasteAction();
    static final DeleteAction deleteAction = new DeleteAction();
    static final SaveAsTemplateAction saveAsTemplateAction = new SaveAsTemplateAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
    
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            cutAction,
            copyAction,
            pasteAction,
            deleteAction,
            saveAsTemplateAction,
            propertiesAction
        });
    }

/*   protected static final Action[] javaActions = new Action[] {
        cutAction,
        copyAction,
        deleteAction,
    };
    
    Action[] getActions() {
        return javaActions;
    }*/

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

    /** performs SaveAsTemplateAction with this node */    
    public void saveAsTemplate() {
        saveAsTemplateAction.perform(this);
    }

    /** performs PropertiesAction with this node */    
    public void properties() {
        propertiesAction.perform(this);
    }
}
