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

import java.io.IOException;

import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.*;

import org.netbeans.modules.cnd.execution.*;

/** A node to represent a Shell data object.
 *
 */
public class ShellDataNode extends CCFDataNode {

    /** We need this in several places */
    private ShellExecSupport mes;


    /** Construct the DataNode */
    public ShellDataNode(ShellDataObject obj) {
	this(obj, Children.LEAF);
    }

    /** Construct the DataNode */
    public ShellDataNode(ShellDataObject obj, Children ch) {
	super(obj, ch, "org/netbeans/modules/cnd/loaders/ShellDataIcon.gif"); // NOI18N

	getCookieSet().add(getSupport());
    }

    /** Get the support for methods which need it */
    private final ShellExecSupport getSupport() {
	if (mes == null) {
	    mes = (ShellExecSupport) getCookie(ShellExecSupport.class);
	}

	return mes;
    }


    /** Create the properties sheet for the node */
    protected Sheet createSheet() {
	// Just add properties to default property tab (they used to be in a special 'Building Tab')
	Sheet defaultSheet = super.createSheet();
        Sheet.Set defaultSet = defaultSheet.get(Sheet.PROPERTIES);
	getSupport().addProperties(defaultSet);
	return defaultSheet;
    }

    public javax.swing.Action getPreferredAction() {
	return SystemAction.get(org.openide.actions.OpenAction.class);
    }
}
