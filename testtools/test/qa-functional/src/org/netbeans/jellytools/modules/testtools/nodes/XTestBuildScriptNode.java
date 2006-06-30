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

package org.netbeans.jellytools.modules.testtools.nodes;

/*
 * XTestBuildScriptNode.java
 *
 * Created on July 23, 2002, 2:09 PM
 */
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.RepositoryTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class XTestBuildScriptNode extends Node {

    static final OpenAction openAction = new OpenAction();
    static final ExploreFromHereAction exploreFromHereAction = new ExploreFromHereAction();
    static final CleanAction cleanAction = new CleanAction();
    static final CompileAction compileAction = new CompileAction();
    static final ExecuteAction executeAction = new ExecuteAction();
    static final Action cleanResultsAction = new Action(null, "Clean Results");
    static final CutAction cutAction = new CutAction();
    static final CopyAction copyAction = new CopyAction();
    static final PasteAction pasteAction = new PasteAction();
    static final ChangeOrderAction changeOrderAction = new ChangeOrderAction();
    static final DeleteAction deleteAction = new DeleteAction();
    static final RenameAction renameAction = new RenameAction();
    static final SaveAsTemplateAction saveAsTemplateAction = new SaveAsTemplateAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
    
    /** Creates a new instance of XTestBuildScriptNode */
    public XTestBuildScriptNode(Node parent, String subpath) {
        super(parent, subpath);
    }

    /** Creates a new instance of XTestBuildScriptNode */
    public XTestBuildScriptNode(JTreeOperator tree, String path) {
        super(tree, path);
    }

    /** Creates a new instance of XTestBuildScriptNode */
    public XTestBuildScriptNode(String subpath) {
        super(new RepositoryTabOperator().tree(), subpath);
    }

    /** performs OpenAction with this node */    
    public void open() {
        openAction.perform(this);
    }

    /** performs ExploreFromHereAction with this node */    
    public void exploreFromHere() {
        exploreFromHereAction.perform(this);
    }

    /** performs CompileAction with this node */    
    public void compile() {
        compileAction.perform(this);
    }

    /** performs CleanResultsAction with this node */    
    public void cleanResults() {
        cleanResultsAction.perform(this);
    }

    /** performs ExecuteAction with this node */    
    public void execute() {
        executeAction.perform(this);
    }

    /** performs CleanAction with this node */    
    public void clean() {
        cleanAction.perform(this);
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

    /** performs ChangeOrder with this node */    
    public void changeOrder() {
        changeOrderAction.perform(this);
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
   
    public void verifyPopup() {
        JPopupMenuOperator popup=callPopup();
        new JMenuItemOperator(popup, "Open");
        new JMenuItemOperator(popup, "Explore From Here");
        new JMenuItemOperator(popup, "Clean");
        new JMenuItemOperator(popup, "Compile");
        new JMenuItemOperator(popup, "Execute");
        new JMenuItemOperator(popup, "Clean Results");
        new JMenuItemOperator(popup, "Cut");
        new JMenuItemOperator(popup, "Copy");
        new JMenuItemOperator(popup, "Paste");
        new JMenuItemOperator(popup, "Change Order");
        new JMenuItemOperator(popup, "Delete");
        new JMenuItemOperator(popup, "Rename");
        new JMenuItemOperator(popup, "Save As Template");
        new JMenuItemOperator(popup, "Properties");
        try {
            popup.pushKey(KeyEvent.VK_ESCAPE);
        } catch (Exception e) {}
    }
}
