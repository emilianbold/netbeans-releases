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
package org.netbeans.modules.edm.model.visitors;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.DBConnectionFactory;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.netbeans.modules.edm.model.impl.ValidationInfoImpl;
import org.openide.util.Exceptions;
import java.util.Properties;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.openide.util.NbBundle;

/**
 * @author Nilesh Apte
 */
public class SQLDBDriverValidationVisitor {

    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    public void visit(SQLDBModel etlDBModel) throws EDMException, Exception {
        DBConnectionDefinition connDef = null;
        connDef = etlDBModel.getETLDBConnectionDefinition();

        if (connDef != null) {
            // Get connection definition properties
            Properties conProps = connDef.getConnectionProperties();
            String driverName = conProps.getProperty(DBConnectionFactory.PROP_DRIVERCLASS);
            if (!isDriverRegistered(driverName)) {
                // Driver Not found
                ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, NbBundle.getMessage(SQLDBDriverValidationVisitor.class, "ERROR_to_locate_driver") + driverName + NbBundle.getMessage(SQLDBDriverValidationVisitor.class, "ERROR_Please_register_the_driver"), ValidationInfo.VALIDATION_ERROR);
                this.validationInfoList.add(vInfo);
            }
        } else {
            ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, NbBundle.getMessage(SQLDBDriverValidationVisitor.class, "ERROR_Connection_Definition_is_NULL") + etlDBModel.getDisplayName(), ValidationInfo.VALIDATION_ERROR);
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
            } catch (EDMException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, ex.getMessage(), ValidationInfo.VALIDATION_ERROR);
                this.validationInfoList.add(vInfo);
            }
        }
    }
}