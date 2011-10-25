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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.management.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.ServiceInfoDataStorageImpl;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSessionServiceInfoStorageFactory;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.ProxyDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Lookup;

public final class DataStorageManager {

    private Collection<? extends DataStorageFactory> dataStorageFactories;//this is to create new ones
    private final Collection<? extends PersistentDataStorageFactory> perstistentDataStorageFactories;//this is to create new persistent ones
    private Map<DLightSession, List<DataStorage>> activeDataStorages = new HashMap<DLightSession, List<DataStorage>>();
    private Map<String, List<DataStorage>> activeStorages =
            new HashMap<String, List<DataStorage>>();//the list of storages - unique key is used as a key
    private Map<String, List<DLightSession>> sharedStoragesSessions =
            new HashMap<String, List<DLightSession>>();//the list of storages - unique key is used as a key    
    private Map<String, ServiceInfoDataStorage> serviceInfoStorages = new HashMap<String, ServiceInfoDataStorage>();//the key - is shared storage
    private static final Logger log = DLightLogger.getLogger(DataStorageManager.class);
    private static final DataStorageManager instance = new DataStorageManager();
    private DLightSession lastSession;

    private DataStorageManager() {
        dataStorageFactories = Lookup.getDefault().lookupAll(DataStorageFactory.class);
        log.log(Level.FINE, "{0} data storage(s) found!", dataStorageFactories.size()); // NOI18N
        perstistentDataStorageFactories = Lookup.getDefault().lookupAll(PersistentDataStorageFactory.class);
        log.log(Level.FINE, "{0} persistent data storage(s) found!", dataStorageFactories.size()); // NOI18N

    }

    public static DataStorageManager getInstance() {
        return instance;
    }

    public void closeSession(DLightSession session) {
        if (session == null) {
            return;
        }
        List<DataStorage> storages = activeDataStorages.get(session);
        if (storages != null) {
            for (DataStorage storage : storages) {
                if (!storage.shutdown()) {
                    log.log(Level.FINEST, "DataStorage {0} is not closed", storage);//NOI18N
                } else {
                    log.log(Level.FINEST, "DataStorage {0} successfully closed", storage);//NOI18N
                }
            }
        }
        String storageUniqueKey = DLightSessionAccessor.getDefault().getSharedStorageUniqueKey(session);
        if (storageUniqueKey != null) {
            ServiceInfoDataStorage serviceInfoStorage = serviceInfoStorages.get(storageUniqueKey);
            if (serviceInfoStorage != null && serviceInfoStorage instanceof DataStorage){
                ((DataStorage)serviceInfoStorage).shutdown();
            }
            List<DLightSession> sessions = sharedStoragesSessions.get(storageUniqueKey);
            if (sessions != null && sessions.contains(session)) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    //close all storages
                    Collection<DataStorage> sharedStorages = activeStorages.get(storageUniqueKey);
                    for (DataStorage storage : sharedStorages) {
                        if (!storage.shutdown()) {
                            log.log(Level.FINEST, "Shared storage with key {0} DataStorage {1} is not closed", new Object[]{storageUniqueKey, storage});//NOI18N
                        } else {
                            log.log(Level.FINEST, "DataStorage with key {0} {1} successfully closed", new Object[]{storageUniqueKey, storage});//NOI18N
                        }
                    }                   
                    activeStorages.remove(storageUniqueKey);
                }
            }
        }
        activeDataStorages.remove(session);
    }

    public void clearActiveStorages(DLightSession session) {
        List<DataStorage> storages = activeDataStorages.get(session);
        if (storages != null) {
            storages.clear();
        }
        lastSession = session;
    }

    /**
     * Returns existing or new instance of DataStorage
     * for requested schema (if it can be found within all available DataStorages)
     *
     * If collector requires several storages and infrastructure cannot provide
     * *all* of them, then an empty list is returned.
     * So this method guarantees to return either fully loaded map or nothing.
     *
     */
    public synchronized Map<DataStorageType, DataStorage> getDataStoragesFor(DLightSession session, DataCollector<?> collector) {
        if (session == null || collector == null) {
            return Collections.<DataStorageType, DataStorage>emptyMap();
        }

        Map<DataStorageType, DataStorage> result = new HashMap<DataStorageType, DataStorage>();

        for (DataStorageType type : collector.getRequiredDataStorageTypes()) {
            final DataStorage dataStorage = getDataStorageFor(session, type, collector.getDataTablesMetadata());

            if (dataStorage == null) {
                DLightLogger.getLogger(DataStorageManager.class).log(Level.INFO,
                        "DataStorageManager.getDataStoragesFor(DLightSession, DataCollector<?>):" // NOI18N
                        + " cannot find storage of type {0} [session={1}, collector={2}]." // NOI18N
                        + " NO storages will be returned", // NOI18N
                        new Object[]{type, session.getDisplayName(), collector.getName()});
                return Collections.<DataStorageType, DataStorage>emptyMap();
            }

            result.put(type, dataStorage);
         }

         return result;
     }

    public synchronized ServiceInfoDataStorage getServiceInfoDataStorageFor(String uniqueKey) {
        if (uniqueKey == null) {
            return null;
        }
        ServiceInfoDataStorage result = serviceInfoStorages.get(uniqueKey);
        if (result == null) {
            result = (new DLightSessionServiceInfoStorageFactory()).openStorage(uniqueKey);
            serviceInfoStorages.put(uniqueKey, result);
        }
        return result;
    }

    /**
     * Gets the storage using the unique key, the method is synchronized as we
     * should be sure we have created the only instance needed.
     * @param uniqueKey
     * @param tableMetadatas
     * @return
     */
    public synchronized Collection<DataStorage> getDataStorage(String uniqueKey, List<DataTableMetadata> tableMetadatas) {
        if (uniqueKey == null) {
            return null;
        }
        List<DataStorage> uniqueStorages = activeStorages.get(uniqueKey);
        Collection<DataStorage> result = new ArrayList<DataStorage>();
        if (uniqueStorages != null) {            
            for (DataStorage storage : uniqueStorages) {
                storage.createTables(tableMetadatas);
                result.add(storage);
            }
            return result;
        }
        
        DLightLogger.getLogger(DataStorageManager.class).log(Level.FINE,
                "DataStorageManager.getDataStorage(Session, String, DataStorageType, DataTableMetadat) " //NOI18N
                + "NO STORAGE  found  in the list: NEED TO OPEN again ={0} ", uniqueKey);//NOI18N
        //if no storage was created - create the new one
        if (perstistentDataStorageFactories != null) {
            for (PersistentDataStorageFactory<?> persistentStorageFactory : perstistentDataStorageFactories) {
                //we should open here the persistente storage
                DataStorage newStorage = persistentStorageFactory.openStorage(uniqueKey);
                if (newStorage != null) {
                    if (newStorage instanceof ProxyDataStorage) {
                        ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                        DataStorage backendStorage = getDataStorage(null, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                        proxyStorage.attachTo(backendStorage);
                        uniqueStorages = activeStorages.get(uniqueKey);
                    }
                    newStorage.createTables(tableMetadatas);
                    if (uniqueStorages == null) {
                        uniqueStorages = new ArrayList<DataStorage>();
                    }
                    uniqueStorages.add(newStorage);
                    activeStorages.put(uniqueKey, uniqueStorages);
                } 
            }
        }
        for (DataStorageFactory<?> storageFactory : dataStorageFactories) {

            if (storageFactory instanceof PersistentDataStorageFactory<?> && !perstistentDataStorageFactories.contains((PersistentDataStorageFactory<?>)storageFactory)) {
                //check if it was not opened already as PerstistentDataStorageFactory                
                DataStorage newStorage = ((PersistentDataStorageFactory<?>) storageFactory).openStorage(uniqueKey);
                if (newStorage == null){//it IS persistent but cannot open
                    newStorage = storageFactory.createStorage();
                }
                if (newStorage != null && newStorage instanceof ProxyDataStorage) {
                    ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                    DataStorage backendStorage = getDataStorage(null, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                    proxyStorage.attachTo(backendStorage);
                    uniqueStorages = activeStorages.get(uniqueKey);
                }
                if (newStorage != null) {
                    newStorage.createTables(tableMetadatas);
                    if (uniqueStorages == null) {
                        uniqueStorages = new ArrayList<DataStorage>();
                    }
                    uniqueStorages.add(newStorage);

                    activeStorages.put(uniqueKey, uniqueStorages);
                }
            } else  if (!(storageFactory instanceof PersistentDataStorageFactory<?>)) {
                DataStorage newStorage = storageFactory.createStorage();
                if (newStorage!= null && newStorage instanceof ProxyDataStorage) {
                    ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                    DataStorage backendStorage = getDataStorage(null, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                    proxyStorage.attachTo(backendStorage);
                    uniqueStorages = activeStorages.get(uniqueKey);
                }
                if (newStorage != null) {
                    newStorage.createTables(tableMetadatas);
                    if (uniqueStorages == null) {
                        uniqueStorages = new ArrayList<DataStorage>();
                    }
                    uniqueStorages.add(newStorage);
                    activeStorages.put(uniqueKey, uniqueStorages);
                }
            }
        }

        return activeStorages.get(uniqueKey);
    }

    public synchronized Collection<DataStorage> getStorages(String uniqueKey, DataTableMetadata dataMetadata) {
        if (uniqueKey == null) {
            return null;
        }
        //for each key we should keep several storages, including ServiceInfoDataStorage
        List<DataStorage> result = activeStorages.get(uniqueKey);
        if (result != null) {
            return result;
        }
        result = new ArrayList<DataStorage>();
        //PersistentDataStorageFactory<?> persistentDataStorageFactories = Lookup.getDefault().lookupAll(DataStorageFactory.class);

        for (PersistentDataStorageFactory<?> factory : perstistentDataStorageFactories) {
            try {
                DLightLogger.getLogger(DataStorageManager.class).log(Level.FINE,
                        "Trying to open storage with the uniqueID={0} from the DataStorageManager", new String[]{uniqueKey});//NOI18N                
                DataStorage storage = factory.openStorage(uniqueKey);
                if (storage != null) {
                    result.add(storage);
                }
            } catch (Throwable e) {
//                log.log(Level.SEVERE, "Exception has beem occurred while trying to open storage with the key=" + uniqueKey, e);
            }
        }
        if (!result.isEmpty()) {
            activeStorages.put(uniqueKey, result);
        }
        return result;

    }

    public synchronized Collection<DataStorage> getStorages(String uniqueKey) {
        if (uniqueKey == null) {
            return null;
        }
        //for each key we should keep several storages, including ServiceInfoDataStorage
        List<DataStorage> result = activeStorages.get(uniqueKey);
        if (result != null) {
            return result;
        }
        result = new ArrayList<DataStorage>();
        //PersistentDataStorageFactory<?> persistentDataStorageFactories = Lookup.getDefault().lookupAll(DataStorageFactory.class);

        for (PersistentDataStorageFactory<?> factory : perstistentDataStorageFactories) {
            try {
                DLightLogger.getLogger(DataStorageManager.class).log(Level.FINE,
                        "Trying to open storage with the uniqueID={0} from the DataStorageManager", new String[]{uniqueKey});//NOI18N                
                DataStorage storage = factory.openStorage(uniqueKey);
                if (storage != null) {
                    result.add(storage);
                }
            } catch (Throwable e) {
//                log.log(Level.SEVERE, "Exception has beem occurred while trying to open storage with the key=" + uniqueKey, e);
            }
        }
        if (!result.isEmpty()) {
            activeStorages.put(uniqueKey, result);
        }
        return result;

    }

    /**
     * Gets the storage using the unique key, the method is synchronized as we
     * should be sure we have created the only instance needed.
     * @param uniqueKey
     * @param storageType
     * @param tableMetadatas
     * @return
     */
    public synchronized DataStorage getDataStorage(DLightSession session, String uniqueKey, DataStorageType storageType, List<DataTableMetadata> tableMetadatas) {
        if (uniqueKey == null) {
            return null;
        }
        List<DataStorage> uniqueStorages = activeStorages.get(uniqueKey);
        if (uniqueStorages != null) {
            for (DataStorage storage : uniqueStorages) {
                if (storage.supportsType(storageType)) {
                    storage.createTables(tableMetadatas);
                    return storage;
                }
            }
        }
        DLightLogger.getLogger(DataStorageManager.class).log(Level.FINE,
                "DataStorageManager.getDataStorage(Session, String, DataStorageType, DataTableMetadat) "//NOI18N
                + "NO STORAGE  found  in the list: NEED TO OPEN again ={0} ", uniqueKey);//NOI18N
        //if no storage was created - create the new one
        if (perstistentDataStorageFactories != null) {
            for (PersistentDataStorageFactory<?> storage : perstistentDataStorageFactories) {
                //we should open here the persistente storage

                if (storage.getStorageTypes().contains(storageType)) {
                    DataStorage newStorage = storage.openStorage(uniqueKey);
                    if (newStorage != null) {
                        if (newStorage instanceof ProxyDataStorage) {
                            ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                            DataStorage backendStorage = getDataStorage(session, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                            proxyStorage.attachTo(backendStorage);
                            uniqueStorages = activeStorages.get(uniqueKey);
                        }
                        newStorage.createTables(tableMetadatas);
                        if (uniqueStorages == null) {
                            uniqueStorages = new ArrayList<DataStorage>();
                        }
                        uniqueStorages.add(newStorage);
                        if (session != null) {
                            List<DLightSession> sessions = sharedStoragesSessions.get(uniqueKey);
                            if (sessions == null) {
                                sessions = new ArrayList<DLightSession>();
                            }
                            if (!sessions.contains(session)) {
                                sessions.add(session);
                                sharedStoragesSessions.put(uniqueKey, sessions);
                            }
                        }
                        activeStorages.put(uniqueKey, uniqueStorages);
                        return newStorage;
                    } else {
                        newStorage = storage.createStorage(uniqueKey);
                        if (newStorage instanceof ProxyDataStorage) {
                            ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                            DataStorage backendStorage = getDataStorage(session, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                            proxyStorage.attachTo(backendStorage);
                            uniqueStorages = activeStorages.get(uniqueKey);
                        }
                        newStorage.createTables(tableMetadatas);
                        if (uniqueStorages == null) {
                            uniqueStorages = new ArrayList<DataStorage>();
                        }
                        uniqueStorages.add(newStorage);
                        if (session != null) {
                            List<DLightSession> sessions = sharedStoragesSessions.get(uniqueKey);
                            if (sessions == null) {
                                sessions = new ArrayList<DLightSession>();
                            }
                            if (!sessions.contains(session)) {
                                sessions.add(session);
                                sharedStoragesSessions.put(uniqueKey, sessions);
                            }
                        }
                        activeStorages.put(uniqueKey, uniqueStorages);
                        return newStorage;
                    }
                }
            }
        }
        for (DataStorageFactory<?> storage : dataStorageFactories) {

            if (storage instanceof PersistentDataStorageFactory<?> && storage.getStorageTypes().contains(storageType)) {
                DataStorage newStorage = ((PersistentDataStorageFactory<?>) storage).openStorage(uniqueKey);
                if (newStorage instanceof ProxyDataStorage) {
                    ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                    DataStorage backendStorage = getDataStorage(session, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                    proxyStorage.attachTo(backendStorage);
                    uniqueStorages = activeStorages.get(uniqueKey);
                }
                if (newStorage != null) {
                    newStorage.createTables(tableMetadatas);
                    if (uniqueStorages == null) {
                        uniqueStorages = new ArrayList<DataStorage>();
                    }
                    uniqueStorages.add(newStorage);
                    if (session != null) {
                        List<DLightSession> sessions = sharedStoragesSessions.get(uniqueKey);
                        if (sessions == null) {
                            sessions = new ArrayList<DLightSession>();
                        }
                        if (!sessions.contains(session)) {
                            sessions.add(session);
                            sharedStoragesSessions.put(uniqueKey, sessions);
                        }
                    }
                    activeStorages.put(uniqueKey, uniqueStorages);
                    return newStorage;
                }
            } else if (storage.getStorageTypes().contains(storageType)) {
                DataStorage newStorage = storage.createStorage();
                if (newStorage instanceof ProxyDataStorage) {
                    ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                    DataStorage backendStorage = getDataStorage(session, uniqueKey, proxyStorage.getBackendDataStorageType(), proxyStorage.getBackendTablesMetadata());
                    proxyStorage.attachTo(backendStorage);
                    uniqueStorages = activeStorages.get(uniqueKey);
                }
                if (newStorage != null) {
                    newStorage.createTables(tableMetadatas);
                    if (uniqueStorages == null) {
                        uniqueStorages = new ArrayList<DataStorage>();
                    }
                    uniqueStorages.add(newStorage);
                    if (session != null) {
                        List<DLightSession> sessions = sharedStoragesSessions.get(uniqueKey);
                        if (sessions == null) {
                            sessions = new ArrayList<DLightSession>();
                        }
                        if (!sessions.contains(session)) {
                            sessions.add(session);
                            sharedStoragesSessions.put(uniqueKey, sessions);
                        }
                    }
                    activeStorages.put(uniqueKey, uniqueStorages);
                    return newStorage;
                }
            }
        }

        return null;
    }

//    private DataStorage getDataStorage(DataStorageType storageType) {
//        return getDataStorageFor(lastSession, storageType, Collections.<DataTableMetadata>emptyList());
//    }
    private synchronized  DataStorage getDataStorageFor(DLightSession session, DataStorageType storageType, List<DataTableMetadata> tableMetadatas) {
        if (session == null) {
            return null;
        }
        DLightSessionAccessor accessor = DLightSessionAccessor.getDefault();
        if (accessor.isUsingSharedStorage(session)) {
            return getDataStorage(session, accessor.getSharedStorageUniqueKey(session), storageType, tableMetadatas);
        }
        List<DataStorage> activeSessionStorages = activeDataStorages.get(session);
        if (activeSessionStorages != null) {
            for (DataStorage storage : activeSessionStorages) {
                if (storage.supportsType(storageType)) {
                    storage.createTables(tableMetadatas);
                    return storage;
                }
            }
        }
        //if no storage was created - create the new one
        for (DataStorageFactory<?> storage : dataStorageFactories) {
            if (storage.getStorageTypes().contains(storageType)) {
                DataStorage newStorage = storage.createStorage();
                if (newStorage instanceof ProxyDataStorage) {
                    ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                    DataStorageType requiredStorageType = proxyStorage.getBackendDataStorageType();
                    List<DataTableMetadata> requiredTables = proxyStorage.getBackendTablesMetadata();
                    DataStorage backendStorage = getDataStorageFor(session, requiredStorageType, requiredTables);
                    if (backendStorage == null) {
                        // Means that required (for this proxy) storage cannot be found ...
                        continue;
                    }
                    proxyStorage.attachTo(backendStorage);
                }

                if (newStorage != null) {
                    newStorage.createTables(tableMetadatas);
                    if (activeSessionStorages == null) {
                        activeSessionStorages = new ArrayList<DataStorage>();
                    }
                    activeSessionStorages.add(newStorage);
                    activeDataStorages.put(session, activeSessionStorages);
                    return newStorage;
                }
            }
        }
        if (perstistentDataStorageFactories != null) {
            for (PersistentDataStorageFactory<?> storage : perstistentDataStorageFactories) {
                //we should open here the persistente storage

                if (storage.getStorageTypes().contains(storageType)) {
                    DataStorage newStorage = storage.createStorage();
                    if (newStorage != null) {
                        if (newStorage instanceof ProxyDataStorage) {
                            ProxyDataStorage proxyStorage = (ProxyDataStorage) newStorage;
                            DataStorageType requiredStorageType = proxyStorage.getBackendDataStorageType();
                            List<DataTableMetadata> requiredTables = proxyStorage.getBackendTablesMetadata();
                            DataStorage backendStorage = getDataStorageFor(session, requiredStorageType, requiredTables);
                            if (backendStorage == null) {
                                continue;
                            }
                            proxyStorage.attachTo(backendStorage);
                        }

                        newStorage.createTables(tableMetadatas);

                        return newStorage;
                    }
                }
            }
        }
        return null;
    }
}
