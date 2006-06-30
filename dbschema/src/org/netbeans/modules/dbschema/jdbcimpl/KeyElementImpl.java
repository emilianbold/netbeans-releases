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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.util.*;

import org.netbeans.modules.dbschema.*;

public abstract class KeyElementImpl extends DBMemberElementImpl implements KeyElement.Impl {
    private DBElementsCollection columns;

    /** Creates new KeyElementImpl */
    public KeyElementImpl() {
    }

	/** Creates new KeyElementImpl with the specified name */
    public KeyElementImpl (String name) {
        super(name);
		columns = initializeCollection();

        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        Object hc = String.valueOf(columns.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		columns = initializeCollection();
            hc = String.valueOf(columns.hashCode());
        }
        DBElementsCollection.instances.add(hc);
	}

    protected DBElementsCollection initializeCollection() {
        return new DBElementsCollection(this, new ColumnElement[0]);
    }

    /** Change the set of columns.
     * @param elems the columns to change
     * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception DBException if the action cannot be handled
     */
    public void changeColumns(ColumnElement[] elems,int action) throws DBException {
        columns.changeElements(elems, action);
    }

    /** Get all columns.
     * @return the columns
     */
    public ColumnElement[] getColumns() {
        DBElement[] dbe = columns.getElements();
        return (ColumnElement[]) Arrays.asList(dbe).toArray(new ColumnElement[dbe.length]);
    }
  
    /** Find a column by name.
     * @param name the name for which to look
     * @return the column, or <code>null</code> if it does not exist
     */
    public ColumnElement getColumn(DBIdentifier name) {
		return (ColumnElement) columns.find(name);
    }

	/** Returns the table collection of this schema element.  This method
	 * should only be used internally and for cloning and archiving.
	 * @return the table collection of this schema element
	 */
	public DBElementsCollection getColumnCollection () {
		return columns;
	}

	/** Set the table collection of this claschemass element to the supplied
	 * collection.  This method should only be used internally and for
	 * cloning and archiving.
	 * @param collection the table collection of this schema element
	 */
	public void setColumnCollection (DBElementsCollection collection) {
		columns = collection;
	}
}
