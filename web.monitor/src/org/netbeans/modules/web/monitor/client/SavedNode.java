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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * @author Ana von Klopp
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;

public class SavedNode extends AbstractNode {

    public SavedNode(Children ch) {
	super(ch);
	setIconBaseWithExtension("org/netbeans/modules/web/monitor/client/icons/folder.gif"); //NOI18N
	setName(NbBundle.getBundle(SavedNode.class).getString("MON_Saved_Transactions_22"));
    }
    protected SystemAction[] createActions () {
	return new SystemAction[] {
	    SystemAction.get(DeleteSavedAction.class),
	};
    }
} // SavedNode
