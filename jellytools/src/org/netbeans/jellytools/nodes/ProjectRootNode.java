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

import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.*;
import javax.swing.tree.TreePath;

/** project root node class 
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ProjectRootNode extends Node {
    
    static final AddNewAction addNewAction = new AddNewAction();
    static final AddExistingAction addExistingAction = new AddExistingAction();
    static final RefreshFolderAction refreshFolderAction = new RefreshFolderAction();
    static final FindAction findAction = new FindAction();
    static final ChangeOrderAction changeOrderAction = new ChangeOrderAction();
    static final CompileProjectAction compileProjectAction = new CompileProjectAction();
    static final BuildProjectAction buildProjectAction = new BuildProjectAction();
    static final ExecuteProjectAction executeProjectAction = new ExecuteProjectAction();
    static final DebugProjectAction debugProjectAction = new DebugProjectAction();
    static final PasteAction pasteAction = new PasteAction();
    static final RenameAction renameAction = new RenameAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            addExistingAction,
            addNewAction,
            refreshFolderAction,
            findAction,
            changeOrderAction,
            compileProjectAction,
            buildProjectAction,
            executeProjectAction,
            debugProjectAction,
            pasteAction,
            renameAction,
            propertiesAction
        });
    }
    
//    protected static final Action[] rootActions = new Action[] {
        //  addNewAction,
        //addExistingAction
//    };
    
    /** creates new ProjectRootNode instance
     * @param treeOperator treeOperator JTreeOperator of tree with Filesystems repository */    
    public ProjectRootNode(JTreeOperator treeOperator) {
        super(treeOperator, "");
    }
    
/*    protected Action[] getActions() {
        return rootActions;
    }*/
    
    /** opens dialog for adding existing object into project */    
    public void addExisting() {
        addExistingAction.perform(this);
    }
    
    /** opens new wizard for adding existing object into project */    
    public void addNew() {
        addNewAction.perform(this);
    }
    
    /** refreshes folder */    
    public void refreshFolder() {
        refreshFolderAction.perform(this);
    }
    
    /** opens Search Filesystems dialog */    
    public void find() {
        findAction.perform(this);
    }
    
    /** opens Change Order dialog */    
    public void changeOrder() {
        changeOrderAction.perform(this);
    }
    
    /** compiles project */    
    public void compileProject() {
        compileProjectAction.perform(this);
    }
    
    /** build project */    
    public void buildProject() {
        buildProjectAction.perform(this);
    }
    
    /** executes project */    
    public void executeProject() {
        executeProjectAction.perform(this);
    }
    
    /** starts debugging of project */    
    public void debugProject() {
        debugProjectAction.perform(this);
    }
    
    /** pastes object from clipboard into project */    
    public void paste() {
        pasteAction.perform(this);
    }
    
    /** renames project */    
    public void rename() {
        renameAction.perform(this);
    }
    
    /** opens properties of project */    
    public void properties() {
        propertiesAction.perform(this);
    }
}