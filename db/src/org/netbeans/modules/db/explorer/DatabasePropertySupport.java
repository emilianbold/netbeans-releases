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
