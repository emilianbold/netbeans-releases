/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.loaders;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.*;

import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;

import org.netbeans.modules.cnd.execution.*;

/** A node to represent an Elf core object.
 *
 */
public class CoreElfNode extends CCFNode {

    public CoreElfNode(CoreElfObject obj) {
	this(obj, Children.LEAF);
    }

    public CoreElfNode(CoreElfObject obj, Children ch) {
	super(obj, ch);
	setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/CoreElfIcon.gif");	    // NOI18N
    }

    protected Sheet createSheet() {
	Sheet sheet = super.createSheet();

	Sheet.Set set = sheet.get(BinaryExecSupport.PROP_EXECUTION);
	if (set == null) {
	    set = new Sheet.Set();
	    set.setName(BinaryExecSupport.PROP_EXECUTION);
	    set.setDisplayName(NbBundle.getBundle(CoreElfNode.class).
		    getString("displayNameForExeElfNodeExecSheet"));  // NOI18N
	    set.setShortDescription(NbBundle.getBundle(CoreElfNode.class).
		    getString("hintForExeElfNodeExecSheet"));   // NOI18N
	    BinaryExecSupport es = ((BinaryExecSupport)
				getCookie(BinaryExecSupport.class));
	    if (es != null) {
		es.addProperties(set);
	    }

	    // Trick from org/apache/tools/ant/module/nodes/AntProjectNode.java
	    // Get rid of Arguments property and the Execution property;
	    // corefiles can only be debugged.
	    set.remove (ExecutionSupport.PROP_FILE_PARAMS);
	    set.remove (ExecutionSupport.PROP_EXECUTION);
	    
	    sheet.put(set);
	}
	return sheet;
    }

    private CoreElfObject getCoreElfObject() {
	return (CoreElfObject) getDataObject();
    }
}
