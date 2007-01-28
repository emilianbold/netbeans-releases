/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
