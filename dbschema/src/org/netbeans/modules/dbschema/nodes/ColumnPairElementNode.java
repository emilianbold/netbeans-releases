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

/** Node representing a column pair.
 * @see ColumnElement
 */
public class ColumnPairElementNode extends DBMemberElementNode {
	/** Create a new column pair node.
	 * @param element column pair element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ColumnPairElementNode(ColumnPairElement element,boolean writeable) {
		super(element, Children.LEAF, writeable);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
        //PENDING - column pair element should be here
		return COLUMN;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));
		ps.put(createLocalColumnProperty(writeable));
		ps.put(createReferencedColumnProperty(writeable));

		return sheet;
	}
    
	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                return localColumnName() + ";" + referencedColumnName(); //NOI18N
			}
		};
	}

	/** Create a property for the column pair local column.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createLocalColumnProperty(boolean canW) {
		return new ElementProp(PROP_LOCAL_COLUMN, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return localColumnName();
			}
		};
	}
    
	/** Create a property for the column pair referenced column.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createReferencedColumnProperty(boolean canW) {
		return new ElementProp(PROP_REFERENCED_COLUMN, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return referencedColumnName();
			}
		};
	}
    
    private String localColumnName() {
        ColumnElement elm = ((ColumnPairElement) element).getLocalColumn();
        return elm.getDeclaringTable().getName().getName() + "." + elm.getName().getName(); //NOI18N
    }
    
    private String referencedColumnName() {
        ColumnElement elm = ((ColumnPairElement) element).getReferencedColumn();
        return elm.getDeclaringTable().getName().getName() + "." + elm.getName().getName(); //NOI18N
    }
    
}
