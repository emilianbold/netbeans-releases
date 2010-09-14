/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.berkelydb;

import java.io.File;
import java.util.SortedMap;

//import com.sleepycat.bind.EntryBinding;
//import com.sleepycat.bind.serial.ClassCatalog;
//import com.sleepycat.bind.serial.SerialBinding;
//import com.sleepycat.bind.serial.StoredClassCatalog;
//import com.sleepycat.collections.StoredSortedMap;
//import com.sleepycat.je.Database;
//import com.sleepycat.je.DatabaseConfig;
//import com.sleepycat.je.DatabaseException;
//import com.sleepycat.je.Environment;
//import com.sleepycat.je.EnvironmentConfig;
//import java.util.HashMap;
//import java.util.Map;
//import org.netbeans.modules.cnd.repository.spi.DatabaseStorage;
//import org.openide.util.Lookup;
//import org.openide.util.Lookup.Result;

/**
 *
 * @author Alexander Simon
 */
public class DatabaseStorageImpl {

    public DatabaseStorageImpl(File homeDir) {
    }

    public SortedMap<?, ?> getMap(String id) {
        return null;
    }

    public void close() {
    }

    //    private DatabaseImpl db;
//    private ViewImpl view;
//
//    public DatabaseStorageImpl(File homeDir) {
//        db = new DatabaseImpl(homeDir);
//        view = new ViewImpl(db);
//    }
//
//    public StoredSortedMap<?, ?> getMap(String id) {
//        return view.getMap(id);
//    }
//
//    public void close() throws DatabaseException {
//        db.close();
//    }
//
//    private final static class DatabaseImpl {
//
//        private static final String CLASS_CATALOG = "java_class_catalog";
//        private Environment env;
//        private StoredClassCatalog javaCatalog;
//        private Map<String, Database> tables = new HashMap<String, Database>();
//
//        public DatabaseImpl(File homeDirectory) throws DatabaseException {
//            EnvironmentConfig envConfig = new EnvironmentConfig();
//            envConfig.setTransactional(true);
//            envConfig.setAllowCreate(true);
//            env = new Environment(homeDirectory, envConfig);
//            DatabaseConfig dbConfig = new DatabaseConfig();
//            dbConfig.setAllowCreate(true);
//            Database catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
//            javaCatalog = new StoredClassCatalog(catalogDb);
//            Result<DatabaseStorage> lookupResult = Lookup.getDefault().lookupResult(DatabaseStorage.class);
//            for (DatabaseStorage instance : lookupResult.allInstances()) {
//                String id = instance.getTableName();
//                tables.put(id, env.openDatabase(null, id, dbConfig));
//            }
//        }
//
//        public final Environment getEnvironment() {
//            return env;
//        }
//
//        public final StoredClassCatalog getClassCatalog() {
//            return javaCatalog;
//        }
//
//        public final Database getDatabase(String tableName) {
//            return tables.get(tableName);
//        }
//
//        public void close() throws DatabaseException {
//            for (Database table : tables.values()) {
//                table.close();
//            }
//            javaCatalog.close();
//            env.close();
//        }
//    }
//
//    private final static class ViewImpl {
//
//        private Map<String, StoredSortedMap<?, ?>> views = new HashMap<String, StoredSortedMap<?, ?>>();
//
//        public ViewImpl(DatabaseImpl db) {
//            ClassCatalog catalog = db.getClassCatalog();
//            Result<DatabaseStorage> lookupResult = Lookup.getDefault().lookupResult(DatabaseStorage.class);
//            for (DatabaseStorage instance : lookupResult.allInstances()) {
//                String id = instance.getTableName();
//                @SuppressWarnings("unchecked")
//                EntryBinding keyBinding = new SerialBinding(catalog, instance.getKeyClass());
//                @SuppressWarnings("unchecked")
//                EntryBinding dataBinding = new SerialBinding(catalog, instance.getDataClass());
//                @SuppressWarnings("unchecked")
//                StoredSortedMap map = new StoredSortedMap(db.getDatabase(id), keyBinding, dataBinding, true);
//                views.put(id, map);
//            }
//        }
//
//        public final StoredSortedMap<?, ?> getMap(String tableName) {
//            return views.get(tableName);
//        }
//    }
}
