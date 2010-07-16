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

package org.netbeans.modules.edm.editor.utils;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.logging.Level;
import org.netbeans.modules.edm.model.EDMException;
import java.sql.DriverManager;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * DBConnectionFactory is used to serve out SQLDBSession The actual physical
 * connection handling is implemented by classes that extend this class. This
 * class is a singleton.
 * 
 * @author Ahimanikya Satapathy
 */
public class DBConnectionFactory {

    public static final String PROP_DBTYPE = "dbType";
    public static final String PROP_DRIVERCLASS = "DRIVER";
    public static final String PROP_PASSWORD = "password";
    public static final String PROP_URL = "url";
    public static final String PROP_USERNAME = "username";

    protected DBConnectionFactory() {
    }

    public void closeConnection(Connection con) {
    }

    public static Connection getConnection(DBConnectionParameters conDef) throws EDMException {
        Connection conn = null;
        String driver = conDef.getDriverClass();
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            Logger.global.log(Level.SEVERE, null, e);
        }

        String username = conDef.getUserName();
        String password = conDef.getPassword();
        String url = conDef.getConnectionURL();
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new EDMException(e);
        }

        return conn;
    }
}
