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

/** Describes an object which holds a list of column pairs.
 */
public interface ColumnPairElementHolder {
	/** Add a new column pair to the holder.
	 *  @param pair the pair to add
	 * @throws Exception if impossible
	 */
	public void addColumnPair (ColumnPairElement pair) throws Exception;

	/** Add some new column pairs to the holder.
	 *  @param pairs the column pairs to add
	 * @throws Exception if impossible
	 */
	public void addColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Remove a column pair from the holder.
	 *  @param pair the column pair to remove
	 * @throws Exception if impossible
	 */
	public void removeColumnPair (ColumnPairElement pair) throws Exception;

	/** Remove some column pairs from the holder.
	 *  @param pairs the column pairs to remove
	 * @throws Exception if impossible
	 */
	public void removeColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Set the column pairs for this holder.
	 * Previous column pairs are removed.
	 * @param pairs the new column pairs
	 * @throws Exception if impossible
	 */
	public void setColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Get all column pairs in this holder.
	 * @return the column pairs
	 */
	public ColumnPairElement[] getColumnPairs ();

	/** Find a column pair by name.
	 * @param name the name of the column pair for which to look
	 * @return the column pair or <code>null</code> if not found
	 */
	public ColumnPairElement getColumnPair (DBIdentifier name);
}
