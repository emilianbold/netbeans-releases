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

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing some type of member element.
 */
public abstract class DBMemberElementNode extends DBElementNode {
	/** Create a new node.
	 *
	 * @param element member element to represent
	 * @param children list of children
	 * @param writeable <code>true</code> to be writable
	 */
	public DBMemberElementNode (DBMemberElement element, Children children, boolean writeable) {
		super(element, children, writeable);
		superSetName(element.getName().getName());
	}
  
	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                DBMemberElement elm = (DBMemberElement) element;

                return elm.getDeclaringTable().getName().getName() + "." + elm.getName().getName(); //NOI18N
			}
		};
	}
}
