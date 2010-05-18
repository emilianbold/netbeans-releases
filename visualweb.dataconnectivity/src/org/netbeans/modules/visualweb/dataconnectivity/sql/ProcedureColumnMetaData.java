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
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * just in time Column data used by servernavigator and other clients
 * cached after retrieved
 *
 * @author John Kline
 */
public class ProcedureColumnMetaData {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());

    public static class ColIndex implements ColumnMetaDataHelper.MetaIndex {
        private String name;
        private int    index;
        private ColIndex(String name, int index) {this.name = name; this.index = index;}
        public String getName() {return name;};
        public int getIndex() {return index;};
    }

    public static ColIndex PROCEDURE_CAT   = new ColIndex("PROCEDURE_CAT", 0); //NOI18N
    public static ColIndex PROCEDURE_SCHEM = new ColIndex("PROCEDURE_SCHEM", 1); //NOI18N
    public static ColIndex PROCEDURE_NAME  = new ColIndex("PROCEDURE_NAME", 2); //NOI18N
    public static ColIndex COLUMN_NAME  = new ColIndex("COLUMN_NAME", 3); //NOI18N
    public static ColIndex COLUMN_TYPE  = new ColIndex("COLUMN_TYPE", 4); //NOI18N
    public static ColIndex DATA_TYPE  = new ColIndex("DATA_TYPE", 5); //NOI18N
    public static ColIndex TYPE_NAME  = new ColIndex("TYPE_NAME", 6); //NOI18N
    public static ColIndex PRECISION  = new ColIndex("PRECISION", 7); //NOI18N
    public static ColIndex LENGTH  = new ColIndex("LENGTH", 8); //NOI18N
    public static ColIndex SCALE  = new ColIndex("SCALE", 9); //NOI18N
    public static ColIndex RADIX  = new ColIndex("RADIX", 10); //NOI18N
    public static ColIndex NULLABLE  = new ColIndex("NULLABLE", 11); //NOI18N
    public static ColIndex REMARKS  = new ColIndex("REMARKS", 12); //NOI18N

    private static ColIndex[] metaIndicies = { PROCEDURE_CAT, PROCEDURE_SCHEM, PROCEDURE_NAME,
        COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, TYPE_NAME, PRECISION, LENGTH, SCALE, RADIX,
        NULLABLE, REMARKS};

    private ColumnMetaDataHelper helper = null;

    ProcedureColumnMetaData(ResultSet resultSet) throws SQLException {
        helper = new ColumnMetaDataHelper(metaIndicies, resultSet);
    }

    public Object getMetaInfo(ColIndex metaIndex) throws SQLException {
        return helper.getMetaInfo(metaIndex);
    }

    public String getMetaInfoAsString(ColIndex metaIndex) throws SQLException {
        return helper.getMetaInfoAsString(metaIndex);
    }
}
