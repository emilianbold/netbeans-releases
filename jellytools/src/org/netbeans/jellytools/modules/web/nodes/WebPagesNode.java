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

package org.netbeans.jellytools.modules.web.nodes;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;


/** Node representing "Web Pages" folder */
public class WebPagesNode extends Node {
    
    static final NewFileAction newFileAction = new NewFileAction();
    static final FindAction findAction = new FindAction();
    static final PasteAction pasteAction = new PasteAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
    private static final String treePath = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.project.ui.Bundle",
            "LBL_Node_DocBase");
    
    /**
     * creates new WebPagesNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path
     */
    public WebPagesNode(String projectName) {
        super(new ProjectsTabOperator().getProjectRootNode(projectName), treePath);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[] { newFileAction, findAction, propertiesAction});
    }
        
    /** performs FindAction with this node */
    public void find() {
        findAction.perform(this);
    }
    
    /** performs PasteAction with this node */
    public void paste() {
        pasteAction.perform(this);
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
