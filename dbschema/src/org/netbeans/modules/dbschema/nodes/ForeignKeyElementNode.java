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

package org.netbeans.modules.dbschema.nodes;

import java.beans.*;

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing a foreign key.
 * @see ForeignKeyElement
 */
public class ForeignKeyElementNode extends DBMemberElementNode {
	/** Create a new foreign key node.
	 * @param element foreign key element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ForeignKeyElementNode(ForeignKeyElement element, TableChildren children, boolean writeable) {
		super(element, children, writeable);
		TableElementFilter filter = new TableElementFilter();
		filter.setOrder(new int[] {TableElementFilter.COLUMN});
		children.setFilter(filter);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
		return FK;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));

		return sheet;
	}
}
