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
