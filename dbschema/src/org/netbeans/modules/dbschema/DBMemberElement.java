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
