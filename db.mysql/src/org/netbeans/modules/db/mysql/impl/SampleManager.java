/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.impl;

import java.io.File;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * A utility class for creating sample databases 
 * 
 * @author David Van Couvering
 */
public class SampleManager {
    /**
     *  Enumeration of valid sample database names
     */
    public enum SampleName {
        sample, vir, travel
    };

    private static final String MODULE_JAR_FILE = 
            "modules/org-netbeans-modules-db-mysql.jar";
    private static final String RESOURCE_DIR_PATH =
            "org/netbeans/modules/db/mysql/resources";
    
    public static boolean isSampleName(String name) {
        SampleName[] samples = SampleName.values();
        for ( SampleName sample : samples ) {
            if (sample.toString().equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static void createSample(String sampleName, DatabaseConnection dbconn) 
            throws DatabaseException {     
        if ( ! isSampleName(sampleName)) {
            throw new DatabaseException(NbBundle.getMessage(
                    DatabaseServer.class, 
                    "MSG_NoSuchSample", sampleName));
        }
             
        DataObject sqlDO = getSQLDataObject(sampleName);
        
        try {
            if ( ! DatabaseUtils.ensureConnected(dbconn) ) {
                return;
            } 
            
            OpenCookie openCookie = (OpenCookie)sqlDO.getCookie(OpenCookie.class);
            openCookie.open();

            SQLExecuteCookie sqlCookie = (SQLExecuteCookie)sqlDO.getCookie(
                    SQLExecuteCookie.class);

            sqlCookie.setDatabaseConnection(dbconn);
            sqlCookie.execute();
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(
                    Utils.getMessage(
                        "MSG_ErrorExecutingSampleSQL", sampleName, 
                        e.getMessage()));
            dbe.initCause(e);
            throw dbe;
        } finally {
            if ( sqlDO != null ) {
                CloseCookie closeCookie = 
                        (CloseCookie)sqlDO.getCookie(CloseCookie.class);
                
                if ( closeCookie != null ) {
                    closeCookie.close();
                }
            }
        }

    }
        
    private static DataObject getSQLDataObject(String sampleName) 
            throws DatabaseException {
        
        try {
            File jarfile = InstalledFileLocator.getDefault().locate(
                MODULE_JAR_FILE, null, false); // NOI18N
    
            JarFileSystem jarfs = new JarFileSystem();

            jarfs.setJarFile(jarfile);

            String filename = "/create-" + sampleName + ".sql";
            FileObject sqlFO = jarfs.findResource(RESOURCE_DIR_PATH + filename);

            return DataObject.find(sqlFO);
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(
                    Utils.getMessage(
                        "MSG_ErrorLoadingSampleSQL", sampleName, 
                        e.getMessage()));
            dbe.initCause(e);
            throw dbe;
        }
    }
}
