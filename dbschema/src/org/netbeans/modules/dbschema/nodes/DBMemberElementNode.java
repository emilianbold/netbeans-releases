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
