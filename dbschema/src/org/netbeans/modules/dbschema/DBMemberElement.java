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

package org.netbeans.modules.dbschema;

import java.io.*;

/** Superclass for containable database metadata members
 * (columns, indexes, and foreign keys). Provides support
 * for associating this element with a declaring table.
 */
public abstract class DBMemberElement extends DBElement implements Cloneable {
	/** the table to which this element belongs */
	private TableElement declaringTable;

	/** Default constructor
     */
    protected DBMemberElement() {
	}

	/** Creates a member element.
	 * @param impl the pluggable implementation
	 * @param declaringTable the table to which this element belongs, or 
	 * <code>null</code> if unattached
	 */
	protected DBMemberElement(Impl impl, TableElement declaringTable) {
		super(impl);
        this.declaringTable = declaringTable;
	}

    /** Returns the implementation of the element.
	 * @return the current implementation.
     */
	final Impl getMemberImpl() {
        return (Impl) getElementImpl();
    }

	/* This should be automatically synchronized
	* when a DBMemberElement is added to the table. */
    
	/** Gets the declaring table. 
	 * @return the table that owns this member element, or <code>null</code> 
	 * if the element is not attached to any table
	 */
	public TableElement getDeclaringTable() {
        return declaringTable;
    }

	/** Sets the declaring table. 
	 * @param te the table that owns this member element
	 */
	public void setDeclaringTable(TableElement te) {
        if (declaringTable == null)
            declaringTable = te;
    }

	/** Pluggable implementation of member elements.
	 * @see DBMemberElement
	 */
	public interface Impl extends DBElement.Impl {
    }

	/** Default implementation of the Impl interface.
	 * It just holds the property values.
	 */
	static abstract class Memory extends DBElement.Memory implements DBMemberElement.Impl {
		/** Default */
		public Memory() {
			super();
		}

		/** Copy */
		public Memory(DBMemberElement el) {
			super(el);
		}
	}
}
