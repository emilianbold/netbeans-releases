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

import java.util.*;
import java.beans.PropertyEditor;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabaseTypePropertySupport extends DatabasePropertySupport
{
    private int[] types;
    private String[] names;

    public DatabaseTypePropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable, boolean expert)
    {
        super(name, type, displayName, shortDescription, rep, writable);
        repository = rep;
        int i = 0;

        Specification spec = (Specification)((DatabaseNodeInfo)repository).getSpecification();
        if (spec != null && writable) {
            Map tmap = ((Specification)((DatabaseNodeInfo)repository).getSpecification()).getTypeMap();
            if (tmap == null) tmap = new HashMap(1);
            Iterator enu = tmap.keySet().iterator();
            types = new int[tmap.size()];
            names = new String[tmap.size()];
            while(enu.hasNext()) {
                String key = (String)enu.next();
                int xtype = Specification.getType(key);
                String code = (String)tmap.get(key);
                types[i] = xtype;
                names[i++] = code;
            }
        } else {
            types = new int[] {0};
            names = new String[] {name};
        }

        if (expert) setExpert(true);
    }

    public PropertyEditor getPropertyEditor ()
    {
        PropertyEditor pe = new DatabaseTypePropertyEditor(types, names);
        return pe;
    }
}
