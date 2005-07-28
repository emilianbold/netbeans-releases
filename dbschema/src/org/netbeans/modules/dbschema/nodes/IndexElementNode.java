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

/** Node representing an index.
 * @see IndexElement
 */
public class IndexElementNode extends DBMemberElementNode {
	/** Create a new index node.
	 * @param element index element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public IndexElementNode (IndexElement element, TableChildren children, boolean writeable) {
		super(element, children, writeable);
		TableElementFilter filter = new TableElementFilter();
		filter.setOrder(new int[] {TableElementFilter.COLUMN});
        filter.setSorted(false);
		children.setFilter(filter);
	}
    
	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
		return INDEX;
	}

	/* Creates property set for this node */
	protected Sheet createSheet ()
	{
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));
		ps.put(createUniqueProperty(writeable));

		return sheet;
	}

    /** Create a property for the index unique.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createUniqueProperty (boolean canW) {
		return new ElementProp(PROP_UNIQUE, Boolean.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return Boolean.valueOf(((IndexElement)element).isUnique());
			}
		};
	}
}
