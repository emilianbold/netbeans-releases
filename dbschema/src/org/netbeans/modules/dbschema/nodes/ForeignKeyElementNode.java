/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
