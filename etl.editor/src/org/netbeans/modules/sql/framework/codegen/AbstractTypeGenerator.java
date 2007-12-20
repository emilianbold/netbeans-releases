/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.codegen;

import java.util.Map;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class AbstractTypeGenerator implements TypeGenerator {

    /* Log4J cateogry */
    private static final String LOG_CATEGORY = AbstractTypeGenerator.class.getName();
    private Map supportedJdbcType = null;

    public abstract Map createSupportedDataTypesMap();

    public AbstractTypeGenerator() {
        this.supportedJdbcType = createSupportedDataTypesMap();
    }

    public String generate(int jdbcType, int precision, int scale) {
        String result = this.getCompatibleDataTypeName(jdbcType);
        if (precision > 0 && isPrecisionRequired(jdbcType)) {
            result += "(" + precision;
            if (scale > 0 && isScaleRequired(jdbcType)) {
                result += ", " + scale + ")";
            } else {
                result += ")";
            }
        }
        return result;
    }

    public String getCompatibleDataTypeName(int jdbcType) {
        DataType dataType = (DataType) supportedJdbcType.get(new Integer(jdbcType));

        if (dataType != null) {
            return dataType.getDataTypeName();
        }
        throw new IllegalArgumentException(LOG_CATEGORY + " - Unsupported SQL datatype supplied: " + jdbcType);
    }

    public int getCompatibleJdbcType(int jdbcType) throws IllegalArgumentException {
        DataType dataType = (DataType) supportedJdbcType.get(new Integer(jdbcType));

        if (dataType != null) {
            return dataType.getJdbcType();
        }
        throw new IllegalArgumentException(LOG_CATEGORY + " - Unsupported SQL datatype supplied: " + jdbcType);
    }

    public boolean isPrecisionRequired(int jdbcType) {
        return SQLUtils.isPrecisionRequired(jdbcType);
    }

    public boolean isScaleRequired(int type) {
        return SQLUtils.isScaleRequired(type);
    }
}