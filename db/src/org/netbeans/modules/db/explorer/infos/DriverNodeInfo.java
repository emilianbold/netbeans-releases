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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverSupport;

public class DriverNodeInfo extends DatabaseNodeInfo {

    static final long serialVersionUID =6994829681095273161L;

    public DriverNodeInfo() {
    }

    public DatabaseDriver getDatabaseDriver() {
        return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
    }

    public void setDatabaseDriver(DatabaseDriver drv) {
        put(DatabaseNodeInfo.NAME, drv.getName());
        put(DatabaseNodeInfo.URL, drv.getURL());
        put(DatabaseNodeInfo.PREFIX, drv.getDatabasePrefix());
//        put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
        put(DatabaseNodeInfo.DBDRIVER, drv);
    }

    public void delete() throws IOException {
        try {
            JDBCDriver driver = getJDBCDriver();
            if (driver != null) {
                JDBCDriverManager.getDefault().removeDriver(driver);
            }
        } catch (DatabaseException e) {
            Logger.getLogger(DriverNodeInfo.class.getName()).log(Level.INFO, null, e);
        }
    }
    
    public String getIconBase() {
        return (String) ((checkDriverFiles()) ? get("iconbaseprefered") : get("iconbasepreferednotinstalled")); //NOI18N
    }

    public void setIconBase(String base) {
        if (checkDriverFiles())
            put("iconbaseprefered", base); //NOI18N
        else
            put("iconbasepreferednotinstalled", base); //NOI18N
    }

    private boolean checkDriverFiles() {
        JDBCDriver driver = getJDBCDriver();
        if (driver != null) {
            return JDBCDriverSupport.isAvailable(driver);
        } else {
            return false;
        }
    }
    
    public JDBCDriver getJDBCDriver() {
        DatabaseDriver dbdrv = getDatabaseDriver();
        if (dbdrv == null) {
            return null;
        }
        return dbdrv.getJDBCDriver();
    }
}
