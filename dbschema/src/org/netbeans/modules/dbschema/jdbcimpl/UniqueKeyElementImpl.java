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

import org.netbeans.modules.dbschema.*;

public class UniqueKeyElementImpl extends KeyElementImpl implements UniqueKeyElement.Impl {

    private boolean _primary;

    /** Creates new UniqueKeyElementImpl */
    public UniqueKeyElementImpl() {
		this(null, false);
    }
  
    public UniqueKeyElementImpl(String name, boolean primary) {
        super(name);   //the same as index name
        _primary = primary;
    }

    /** Get the primary key flag of the unique key.
     * @return true if this unique key is a primary key, false otherwise
     */
    public boolean isPrimaryKey() {
        return _primary;
    }
  
    /** Set the primary key flag of the unique key.
     * @param flag the flag
     * @throws DBException if impossible
     */
    public void setPrimaryKey(boolean primary) throws DBException {
        _primary = primary;
    }
}
