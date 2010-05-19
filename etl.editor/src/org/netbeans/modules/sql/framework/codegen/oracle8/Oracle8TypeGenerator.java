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

package org.netbeans.modules.sql.framework.codegen.oracle8;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.sql.framework.codegen.AbstractTypeGenerator;
import org.netbeans.modules.sql.framework.codegen.DataType;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class Oracle8TypeGenerator extends AbstractTypeGenerator {

    private static HashMap supportedDataTypes;

    public Map createSupportedDataTypesMap() {
        if (supportedDataTypes == null) {

            supportedDataTypes = new HashMap();

            // bit is mapped to numeric
            supportedDataTypes.put(new Integer(Types.BIT), DataType.NUMEBER);

            // tinyint, bigint is mapped to numeric
            supportedDataTypes.put(new Integer(Types.BIGINT), DataType.NUMEBER);
            supportedDataTypes.put(new Integer(Types.TINYINT), DataType.NUMEBER);
            supportedDataTypes.put(new Integer(Types.SMALLINT), DataType.SMALLINT);
            supportedDataTypes.put(new Integer(Types.INTEGER), DataType.INTEGER);

            supportedDataTypes.put(new Integer(Types.DECIMAL), DataType.DECIMAL);
            supportedDataTypes.put(new Integer(Types.DOUBLE), DataType.DOUBLE_PRECISION);
            supportedDataTypes.put(new Integer(Types.FLOAT), DataType.FLOAT);
            supportedDataTypes.put(new Integer(Types.NUMERIC), DataType.NUMEBER);
            supportedDataTypes.put(new Integer(Types.REAL), DataType.REAL);

            // time is mapped to date
            supportedDataTypes.put(new Integer(Types.TIME), DataType.DATE);
            // timestamp is mapped to date
            supportedDataTypes.put(new Integer(Types.TIMESTAMP), DataType.DATE);
            supportedDataTypes.put(new Integer(Types.DATE), DataType.DATE);

            supportedDataTypes.put(new Integer(Types.CHAR), DataType.CHAR);
            supportedDataTypes.put(new Integer(Types.VARCHAR), DataType.VARCHAR);
            supportedDataTypes.put(new Integer(Types.LONGVARCHAR), DataType.ORACLE_LONG);

            supportedDataTypes.put(new Integer(Types.BINARY), DataType.ORACLE_RAW);
            supportedDataTypes.put(new Integer(Types.VARBINARY), DataType.ORACLE_RAW);
            supportedDataTypes.put(new Integer(Types.LONGVARBINARY), DataType.BLOB);

            supportedDataTypes.put(new Integer(Types.BLOB), DataType.BLOB);
            supportedDataTypes.put(new Integer(Types.CLOB), DataType.CLOB);

            supportedDataTypes.put(new Integer(Types.OTHER), DataType.VARCHAR);

            supportedDataTypes.put(new Integer(Types.ARRAY), DataType.ORACLE_VARRAY);
            supportedDataTypes.put(new Integer(Types.REF), DataType.REF);
            supportedDataTypes.put(new Integer(Types.STRUCT), DataType.ORACLE_OBJECT);
        }

        return supportedDataTypes;
    }
}