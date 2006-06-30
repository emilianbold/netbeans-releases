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
