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

import java.awt.Image;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.actions.*;

import org.netbeans.modules.cnd.execution.*;

/**
 *  A node to represent an orphaned Elf object file. An orphan is an object
 *  file without a matching source file.
 */
public class OrphanedElfNode extends CCFNode {

    private static final String ICON_BASE = "org/netbeans/modules/cnd/loaders/"; // NOI18N
    private static final String ORPHAN_ICON = ICON_BASE + "OrphanedElfObject.gif"; // NOI18N

    public OrphanedElfNode(OrphanedElfObject obj) {
	this(obj, Children.LEAF);
    }

    private OrphanedElfObject getOrphanedElfObject() {
	return (OrphanedElfObject) getDataObject();
    }

    public OrphanedElfNode(OrphanedElfObject obj, Children ch) {
	super(obj, ch);
	setIconBaseWithExtension(ORPHAN_ICON);
    }
}
