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
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Folder */
public class FolderNode extends Node {
    
    static final ExploreFromHereAction exploreFromHereAction = new ExploreFromHereAction();
    static final FindAction findAction = new FindAction();
    static final RefreshFolderAction refreshFolderAction = new RefreshFolderAction();
    static final CompileAction compileAction = new CompileAction();
    static final CompileAllAction compileAllAction = new CompileAllAction();
    static final BuildAction buildAction = new BuildAction();
    static final BuildAllAction buildAllAction = new BuildAllAction();
    static final CleanAction cleanAction = new CleanAction();
    static final CleanAllAction cleanAllAction = new CleanAllAction();
    static final CopyAction copyAction = new CopyAction();
    static final PasteAction pasteAction = new PasteAction();
    static final PasteCopyAction pasteCopyAction = new PasteCopyAction();
    static final PasteLinkAction pasteLinkAction = new PasteLinkAction();
    static final CutAction cutAction = new CutAction();
    static final DeleteAction deleteAction = new DeleteAction();
    static final RenameAction renameAction = new RenameAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
    static final NewFileAction newFileAction = new NewFileAction();
    
    /*   protected static final Action[] folderActions = new Action[] {
        copyAction
    };*/
    
    /** creates new FolderNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */    
    public FolderNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new FolderNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */    
    public FolderNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }
    
    /** creates new FolderNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */    
    public FolderNode(Node parent, String treePath) {
        super(parent, treePath);
    }
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            exploreFromHereAction,
            findAction,
            refreshFolderAction,
            copyAction,
            cutAction,
            deleteAction,
            renameAction,
            propertiesAction
        });
    }
    
/*    Action[] getActions() {
	return(folderActions);
    }*/
    
    /** performs ExploreFromHereAction with this node */    
    public void exploreFromHere() {
        exploreFromHereAction.perform(this);
    }
    
    /** performs FindAction with this node */    
    public void find() {
        findAction.perform(this);
    }
    
    /** performs RefreshFolderAction with this node */    
    public void refreshFolder() {
        refreshFolderAction.perform(this);
    }
    
    /** performs CompileAction with this node */    
    public void compile() {
        compileAction.perform(this);
    }

    /** performs CompileAllAction with this node */    
    public void compileAll() {
        compileAllAction.perform(this);
    }
    
    /** performs BuildAction with this node */    
    public void build() {
        buildAction.perform(this);
    }
    
    /** performs BuildAllAction with this node */    
    public void buildAll() {
        buildAllAction.perform(this);
    }
    
    /** performs CleanAction with this node */    
    public void clean() {
        cleanAction.perform(this);
    }
    
    /** performs CleanAllAction with this node */    
    public void cleanAll() {
        cleanAllAction.perform(this);
    }

    /** performs CopyAction with this node */    
    public void copy() {
        copyAction.perform(this);
    }
    
    /** performs PasteAction with this node */    
    public void paste() {
        pasteAction.perform(this);
    }

    /** performs PasteCopyAction with this node */    
    public void pasteCopy() {
        pasteCopyAction.perform(this);
    }
    
    /** performs PasteLinkAction with this node */    
    public void pasteLink() {
        pasteLinkAction.perform(this);
    }
    
    /** performs CutAction with this node */    
    public void cut() {
        cutAction.perform(this);
    }
    
    /** performs DeleteAction with this node */    
    public void delete() {
        deleteAction.perform(this);
    }
    
    /** performs RenameAction with this node */    
    public void rename() {
        renameAction.perform(this);
    }
    
    /** performs PropertiesAction with this node */    
    public void properties() {
        propertiesAction.perform(this);
    }
    
    /** performs NewFileAction with this node */    
    public void newFile() {
        newFileAction.perform(this);
    }

    /** performs NewFileAction with this node
     * @param templateName template name from sub menu
     */    
    public void newFile(String templateName) {
        new NewFileAction(templateName).perform(this);
    }
}
