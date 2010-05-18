/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
