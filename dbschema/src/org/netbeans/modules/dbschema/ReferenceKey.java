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

/** Describes a reference key in a table.
 */
public interface ReferenceKey extends ColumnPairElementHolder {
	//================== Naming ===============================

    /** Get the name of this element.
     * @return the name
     */
    public String getKeyName();
	
    /** Set the name of this element.
    * @param name the name
    * @throws Exception if impossible
    */
    public void setKeyName (String name) throws Exception;


	//================== Tables ===============================

	/** Get the declaring table. 
	 * @return the table that owns this reference key element, or 
	 * <code>null</code> if the element is not attached to any table
	 */
	public TableElement getDeclaringTable ();

	/** Set the declaring table. 
    * @param te the table to set
	 */
	public void setDeclaringTable (TableElement te);

	/** Get the referenced table of the reference key.
	 * @return the referenced table
	 */
	public TableElement getReferencedTable();


	//================== Columns ===============================

	// column convenience methods

	/** Get all referenced columns in this reference key.
	 * @return the columns
	 */
	public ColumnElement[] getReferencedColumns ();
    
	/** Get all local columns in this reference key.
	 * @return the columns
	 */
	public ColumnElement[] getLocalColumns();

	// end column convenience methods
}
