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

package org.netbeans.modules.db.explorer;

import org.openide.nodes.PropertySupport;

import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabasePropertySupport extends PropertySupport {
    protected Object repository;

    public DatabasePropertySupport(String name, Class type, String displayName, String shortDescription, Object rep, boolean writable) {
        super(name, type, displayName, shortDescription, true, writable);
        repository = rep;
    }

    public Object getValue() throws IllegalAccessException, IllegalArgumentException {
        String code = getName();
        Object rval = ((DatabaseNodeInfo) repository).getProperty(code);
        return rval;
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException {
        String code = getName();
        ((DatabaseNodeInfo) repository).setProperty(code, val);
    }
    
    /* Can write the value of the property.
    * Returns the value passed into constructor.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite () {
        if (repository instanceof ConnectionNodeInfo) {
            String propName = getName();
            if ("db".equals(propName) || "driver".equals(propName) || "schema".equals(propName) || "user".equals(propName) || "rememberpwd".equals(propName)) //NOI18N
                return ((ConnectionNodeInfo) repository).getConnection() == null ? true : false;
            else
                return super.canWrite();
        } else
            return super.canWrite();
    }
}
