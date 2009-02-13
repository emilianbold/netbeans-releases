/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.api.support;

import java.util.HashMap;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.impl.DataModelSchemeAccessor;

/**
 *"model:table", "model:tree:table"
 * @author masha
 */
public final class DataModelSchemeProvider {

    private static DataModelSchemeProvider instance = null;
    private final HashMap<String, DataModelScheme> storageTypesCache = new HashMap<String, DataModelScheme>();

    private DataModelSchemeProvider() {
    }

    public static DataModelSchemeProvider getInstance() {
        if (instance == null) {
            instance = new DataModelSchemeProvider();
        }
        return instance;
    }

    public synchronized DataModelScheme getScheme(String id) {
        if (storageTypesCache.containsKey(id)) {
            return storageTypesCache.get(id);
        }
        DataModelScheme type = DataModelSchemeAccessor.getDefault().createNew(id);
        storageTypesCache.put(id, type);
        return type;
    }
}
