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

import org.netbeans.modules.dbschema.*;

public class ColumnPairElementImpl extends DBMemberElementImpl implements ColumnPairElement.Impl {

    /** Creates new ForeignColumnElementImpl */
    public ColumnPairElementImpl() {
    }

    public ColumnPairElementImpl(String name) {
        super(name);
    }  
}
