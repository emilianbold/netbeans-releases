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

public abstract class DBMemberElementImpl extends DBElementImpl implements DBMemberElement.Impl {
    /** Creates new DBMemberElementImpl */
    public DBMemberElementImpl() {
		this(null);
    }

	/** Creates new DBMemberElementImpl with the specified name */
    public DBMemberElementImpl (String name) {
        super(name);
	}
    
    /** Get the name of this element.
    * @return the name
    */
    public DBIdentifier getName() {
        if (_name.getFullName() == null)
            _name.setFullName(((DBMemberElement) element).getDeclaringTable().getName().getFullName() + "." + _name.getName());
            
        return _name;
    }

}
