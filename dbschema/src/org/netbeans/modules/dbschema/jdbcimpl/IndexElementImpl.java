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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.*;

public class IndexElementImpl extends DBMemberElementImpl implements IndexElement.Impl {

    private DBElementsCollection columns;
    private TableElementImpl tei;
    private boolean _unique;
    
    /** Creates new IndexElementImpl */
    public IndexElementImpl() {
       this(null, null, false);
    }

    public IndexElementImpl(TableElementImpl tei, String name, boolean unique) {
		super(name);
		columns = new DBElementsCollection(tei, new ColumnElement[0]);
        
        //workaround for bug #4396371
        //http://andorra.eng:8080/cgi-bin/ws.exe/bugtraq/bug.hts?where=bugid_value%3D4396371
        Object hc = String.valueOf(columns.hashCode());
        while (DBElementsCollection.instances.contains(hc)) {
    		columns = new DBElementsCollection(tei, new ColumnElement[0]);
            hc = String.valueOf(columns.hashCode());
        }
        DBElementsCollection.instances.add(hc);

		this.tei = tei;
		_unique = unique;
    }
  
    /** Get the unique flag of the index.
     * @return true if it is a unique index, false otherwise
     */
    public boolean isUnique() {
        return _unique;
    }
  
    /** Set the unique flag of the index.
     * @param unique the flag
     * @throws DBException if impossible
     */
    public void setUnique(boolean unique) throws DBException {
        _unique = unique;
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
  
    protected void initColumns(LinkedList idxs) {
        LinkedList columnsList = new LinkedList();
        String name, info;
        int start, end;
        
        try {
            for (int i = 0; i < idxs.size(); i++) {
                info = idxs.get(i).toString();
                start = info.indexOf('.');
                end = info.lastIndexOf('.');

                name = info.substring(0, start);                    
                if (name.equals(this.getName().getName()))
                    columnsList.add(info.substring(start + 1, end));
            }

            for (int i = 0; i < columnsList.size(); i++) {
                ColumnElement c = ((IndexElement) element).getDeclaringTable().getColumn(DBIdentifier.create(columnsList.get(i).toString()));
                if (c != null)
                    changeColumns(new ColumnElement[] {c}, DBElement.Impl.ADD);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
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
