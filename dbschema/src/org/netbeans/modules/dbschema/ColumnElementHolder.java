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

/** Describes an object which holds a list of columns.
 */
public interface ColumnElementHolder {
	/** Add a new column to the holder.
	 *  @param el the column to add
	 * @throws Exception if impossible
	 */
	public void addColumn (ColumnElement el) throws Exception;

	/** Add some new columns to the holder.
	 *  @param els the columns to add
	 * @throws Exception if impossible
	 */
	public void addColumns (ColumnElement[] els) throws Exception;

	/** Remove a column from the holder.
	 *  @param el the column to remove
	 * @throws Exception if impossible
	 */
	public void removeColumn (ColumnElement el) throws Exception;

	/** Remove some columns from the holder.
	 *  @param els the columns to remove
	 * @throws Exception if impossible
	 */
	public void removeColumns (ColumnElement[] els) throws Exception;

	/** Set the columns for this holder.
	 * Previous columns are removed.
	 * @param els the new columns
	 * @throws Exception if impossible
	 */
	public void setColumns (ColumnElement[] els) throws Exception;

	/** Get all columns in this holder.
	 * @return the columns
	 */
	public ColumnElement[] getColumns ();

	/** Find a column by name.
	 * @param name the name of the column for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public ColumnElement getColumn (DBIdentifier name);
}
