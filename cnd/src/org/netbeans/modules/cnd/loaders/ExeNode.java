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

import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;

/** A node to represent an Elf executable object */
public class ExeNode extends CndDataNode {

    public ExeNode(ExeObject obj) {
	this(obj, Children.LEAF);
    }

    public ExeNode(ExeObject obj, Children ch) {
	super(obj, ch);
	setIconBaseWithExtension("org/netbeans/modules/cnd/loaders/ExeIcon.gif"); // NOI18N
    }

    private ExeElfObject getExeElfObject() {
	return (ExeElfObject) getDataObject();
    }

    // Example of adding Executor / Debugger / Arguments to node:
    protected Sheet createSheet() {
	Sheet sheet = super.createSheet();

	Sheet.Set set = sheet.get(BinaryExecSupport.PROP_EXECUTION);
	if (set == null) {
	    set = new Sheet.Set();
	    set.setName(BinaryExecSupport.PROP_EXECUTION);
	    set.setDisplayName(NbBundle.getBundle(ExeNode.class).
		    getString("displayNameForExeElfNodeExecSheet"));  // NOI18N
	    set.setShortDescription(NbBundle.getBundle(ExeNode.class).
		    getString("hintForExeElfNodeExecSheet"));   // NOI18N
	    BinaryExecSupport es = ((BinaryExecSupport)
				getCookie(BinaryExecSupport.class));
	    if (es != null) {
		es.addProperties(set);
	    }
	    sheet.put(set);
	}
        
	return sheet;
    }
}
