/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.db.h2;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.dlight.db.h2.RcFile.FormatException;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory.Mode;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author masha
 */
@ServiceProviders({
    @ServiceProvider(service = DataStorageFactory.class,  position = 10)
})
public class H2DataStorageFactory extends SQLDataStorageFactory<H2DataStorage> {

    private static RcFile rcFile;
    static final String H2_DATA_STORAGE_TYPE = "db:sql:h2"; // NOI18N
    private final Collection<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();
    //should keep somehow the storages: store them somehow

    {

        String rcFileName = System.getProperty("dlight.h2.rcfile"); // NOI18N
        if (rcFileName == null) {
            String homePath = System.getProperty("user.home");// NOI18N
            if (homePath != null) {
                try {
                    File homeDir = new File(homePath);
                    rcFile = new RcFile(new File(homeDir, ".h2rc"));// NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (FormatException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            try {
                rcFile = new RcFile(new File(rcFileName));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (FormatException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public H2DataStorageFactory() {
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(H2_DATA_STORAGE_TYPE));
        supportedStorageTypes.addAll(super.getStorageTypes());

    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return supportedStorageTypes;


    }

    @Override
    public H2DataStorage createStorage() {
        try {
            return new H2DataStorage();




        } catch (SQLException ex) {
            DLightLogger.getLogger(H2DataStorageFactory.class).log(Level.SEVERE, null, ex);



            return null;
        }


    }

    @Override
    public H2DataStorage createStorage(String uniqueKey) {
        try {
            H2DataStorage result = new H2DataStorage(true);
            result.isPersistent = true;
            rcFile.put("h2.storages", uniqueKey, result.dbURL);//NOI18N
            rcFile.save();


            return result;
        } catch (IOException ex) {
            DLightLogger.getLogger(H2DataStorageFactory.class).log(Level.SEVERE, null, ex);
            return null;
        } catch (SQLException ex) {
            DLightLogger.getLogger(H2DataStorageFactory.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public H2DataStorage openStorage(String uniqueKey) {
        try {
            //find dburl
            String dbURL = rcFile.get("h2.storages", uniqueKey);// NOI18N
            if (dbURL != null) {
                H2DataStorage result = new H2DataStorage(dbURL);
                result.loadSchema();
                result.isPersistent = true;
                return result;
            }
        } catch (SQLException ex) {
            DLightLogger.getLogger(H2DataStorageFactory.class).log(Level.SEVERE, null, ex);
        }


        return null;


    }

    public String getUniqueKey(H2DataStorage storage) {
        try {
            //generate key, put in the
            rcFile.put("h2.storages", storage.dbURL, storage.dbURL);//NOI18N
            rcFile.save();
            return storage.dbURL;
        } catch (IOException ex) {
            DLightLogger.getLogger(H2DataStorageFactory.class).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public H2DataStorage openStorage(String uniqueKey, Mode mode) {
        return openStorage(uniqueKey);

    }
}
