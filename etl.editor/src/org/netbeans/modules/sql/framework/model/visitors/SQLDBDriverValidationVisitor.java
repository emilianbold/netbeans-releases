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
package org.netbeans.modules.sql.framework.model.visitors;

import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.DBConnectionFactory;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.impl.ValidationInfoImpl;
import org.openide.util.Exceptions;
import java.util.Properties;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * @author Nilesh Apte
 */
public class SQLDBDriverValidationVisitor {

    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    public void visit(SQLDBModel etlDBModel) throws BaseException, Exception {
        DBConnectionDefinition connDef = null;
        connDef = etlDBModel.getETLDBConnectionDefinition();

        if (connDef != null) {
            // Get connection definition properties
            Properties conProps = connDef.getConnectionProperties();
            String driverName = conProps.getProperty(DBConnectionFactory.PROP_DRIVERCLASS);
            if (!isDriverRegistered(driverName)) {
                // Driver Not found
                ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, "Unable to locate driver: " + driverName + ". Please register the driver with Database Explorer (Services Tab).", ValidationInfo.VALIDATION_ERROR);
                this.validationInfoList.add(vInfo);
            }
        } else {
            ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, "Connection Definition is NULL in ETL DB Model: " + etlDBModel.getDisplayName(), ValidationInfo.VALIDATION_ERROR);
            this.validationInfoList.add(vInfo);
        }
    }

    private boolean isDriverRegistered(String driverName) throws Exception {
        JDBCDriver[] drivers = null;
        boolean driverFound = false;
        try {
            drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        } catch (Exception ex) {
            driverFound = false;
        }
        if (driverName.equals("org.axiondb.jdbc.AxionDriver")) {
            driverFound = true;
        } else {
            if (drivers.length == 0) {
                driverFound = false;
            } else {
                driverFound = true;
            }
        }
        return driverFound;
    }

    public void visit(SQLDefinition definition) {
        for (SQLDBModel etlDBModel : definition.getAllDatabases()) {
            try {
                visit(etlDBModel);
            } catch (BaseException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, ex.getMessage(), ValidationInfo.VALIDATION_ERROR);
                this.validationInfoList.add(vInfo);
            }
        }
    }
}