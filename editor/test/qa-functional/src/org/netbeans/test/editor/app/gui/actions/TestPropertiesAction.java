/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.TestNode;
import org.netbeans.test.editor.app.gui.PropertiesDialog;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestPropertiesAction extends TreeNodeAction {
    
    /** Creates new TestPropertiesAction */
    public TestPropertiesAction() {
    }
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
	if (activatedNodes.length == 1)
	    return true;
	else
	    return false;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
	if (activatedNodes != null && activatedNodes.length == 1) {
	    TestNode node=activatedNodes[0].getTestNode();
	    PropertiesDialog dlg=new PropertiesDialog(Main.frame,node);
	    dlg.show();
	}
    }
    
    public String getHelpCtx() {
	return "Shows properties of selected node.";
    }
    
    public String getName() {
	return "Properties";
    }
    
    
}
