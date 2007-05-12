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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.faces.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.faces.model.ListDataModel;
import com.sun.rave.faces.util.ComponentBundle;

public class DefaultTableDataModel extends ListDataModel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(DefaultTableDataModel.class);

    public static List newDefaultData() {
        ArrayList rows = new ArrayList();
        for (int i = 0; i < 4; i++) {
            HashMap row = new HashMap();
            row.put("COLUMN1", bundle.getMessage("defaultTblCell", "1", String.valueOf(i))); //NOI18N
            row.put("COLUMN2", bundle.getMessage("defaultTblCell", "2", String.valueOf(i))); //NOI18N
            row.put("COLUMN3", bundle.getMessage("defaultTblCell", "3", String.valueOf(i))); //NOI18N
            row.put("COLUMN4", bundle.getMessage("defaultTblCell", "4", String.valueOf(i))); //NOI18N
            row.put("COLUMN5", bundle.getMessage("defaultTblCell", "5", String.valueOf(i))); //NOI18N
            row.put("COLUMN6", bundle.getMessage("defaultTblCell", "6", String.valueOf(i))); //NOI18N
            row.put("COLUMN7", bundle.getMessage("defaultTblCell", "7", String.valueOf(i))); //NOI18N
            row.put("COLUMN8", bundle.getMessage("defaultTblCell", "8", String.valueOf(i))); //NOI18N
            row.put("COLUMN9", bundle.getMessage("defaultTblCell", "9", String.valueOf(i))); //NOI18N
            rows.add(row);
        }
        return rows;
    }

    public DefaultTableDataModel() {
        super(newDefaultData());
    }

}
