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
import org.netbeans.modules.dbschema.util.SQLTypeUtil;

/** Node representing a column.
 * @see ColumnElement
 */
public class ColumnElementNode extends DBMemberElementNode {
	/** Create a new column node.
	 * @param element column element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ColumnElementNode (ColumnElement element, boolean writeable) {
		super(element, Children.LEAF, writeable);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
		return COLUMN;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));
		ps.put(createTypeProperty(writeable));
		ps.put(createNullableProperty(writeable));
		ps.put(createLengthProperty(writeable));
		ps.put(createPrecisionProperty(writeable));
		ps.put(createScaleProperty(writeable));

		return sheet;
	}

	/** Create a property for the column type.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createTypeProperty (boolean canW) {
		return new ElementProp(PROP_TYPE, /*Integer.TYPE*/String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return SQLTypeUtil.getSqlTypeString(((ColumnElement) element).getType());
			}
        };
	}
    
	/** Create a property for the column nullable.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createNullableProperty (boolean canW) {
		return new ElementProp(PROP_NULLABLE, Boolean.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return Boolean.valueOf(((ColumnElement)element).isNullable());
			}
        };
	}
    
	/** Create a property for the column length.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createLengthProperty (boolean canW) {
		return new ElementProp(PROP_LENGTH, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getLength();
			}
        };
	}
    
	/** Create a property for the column precision.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createPrecisionProperty (boolean canW) {
		return new ElementProp(PROP_PRECISION, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getPrecision();
			}
        };
	}
    
	/** Create a property for the column scale.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createScaleProperty (boolean canW) {
		return new ElementProp(PROP_SCALE, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getScale();
			}
        };
    }
}
