/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package com.sun.data.provider.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;

/**
 * <p>This {@link com.sun.data.provider.DataProvider} wraps access to a standard
 * {@link Map}.  This class can use regular {@link FieldKey} objects as keys
 * (Map key will be the FieldKey's fieldId), or can use {@link MapFieldKey}
 * objects if a non-string key is desired.</p>
 *
 * <p>NOTE about Serializable:  By default, this class uses a {@link HashMap}
 * as its internal data storage, which is a Serializable implementation of
 * {@link Map}.  The internal storage can be swapped out using the
 * <code>setMap(Map)</code> method.  For this class to remain Serializable,
 * the contained Map must be a Serializable implementation.  Also, and more
 * importantly, the contents of the storage Map must be Serializable as well
 * for this class to successfully be serialized.</p>
 *
 * @author Joe Nuxoll
 */
public class MapDataProvider extends AbstractDataProvider {

    /**
     * MapFieldKey is a {@link FieldKey} that contains an untyped Object to use
     * as a key for a map value.
     */
    public class MapFieldKey extends FieldKey {

        /**
         * Constructs a new MapFieldKey using the specified mapKey as the object
         * map key.
         *
         * @param mapKey The desired Object to use as a map key
         */
        public MapFieldKey(Object mapKey) {
            super(String.valueOf(mapKey));
            this.mapKey = mapKey;
        }

        /**
         * Storage for the object map key
         */
        protected Object mapKey;

        /**
         * @return The Object map key
         */
        public Object getMapKey() {
            return mapKey;
        }

        /**
         * Standard equals implementation.  This method compares the mapKey
         * objects if they exist - then defaults to comparing ids.
         *
         * @param o Object to compare equality
         * @return true if equal, false if not
         * @see FieldKey#equals(Object)
         * @see Object#equals(Object)
         */
        public boolean equals(Object o) {
            if (o instanceof MapFieldKey) {
                MapFieldKey mdk = (MapFieldKey)o;
                return mapKey == mdk.mapKey ||
                    (mapKey != null && mapKey.equals(mdk.mapKey));
            }
            return super.equals(o);
        }
    }

    /**
     * The internal storage {@link Map}, initially a {@link HashMap}
     */
    protected Map map = new HashMap();

    /**
     * Constructs a new MapDataProvider using a default {@link HashMap} as the
     * internal storage.
     */
    public MapDataProvider() {}

    /**
     * <p>Constructs a new MapDataProvider using the specified Map as the
     * internal storage.</p>
     *
     * <p>NOTE about Serializable:  By default, this class uses a {@link HashMap}
     * as its internal data storage, which is a Serializable implementation of
     * {@link Map}.  The internal storage can be swapped out using the
     * <code>setMap(Map)</code> method.  For this class to remain Serializable,
     * the contained Map must be a Serializable implementation.  Also, and more
     * importantly, the contents of the storage Map must be Serializable as well
     * for this class to successfully be serialized.</p>
     *
     * @param map The Map to use as internal storage
     */
    public MapDataProvider(Map map) {
        setMap(map);
    }

    /**
     * @return Map being used as internal storage.
     */
    public Map getMap() {
        return map;
    }

    /**
     * <p>Sets the {@link Map} to use as internal storage.</p>
     *
     * <p>NOTE about Serializable:  By default, this class uses a {@link HashMap}
     * as its internal data storage, which is a Serializable implementation of
     * {@link Map}.  The internal storage can be swapped out using the
     * <code>setMap(Map)</code> method.  For this class to remain Serializable,
     * the contained Map must be a Serializable implementation.  Also, and more
     * importantly, the contents of the storage Map must be Serializable as well
     * for this class to successfully be serialized.</p>
     *
     * @param map Map to use as internal storage
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * Refreshes the list of FieldKeys to reflect the current contents of the
     * Map.  This is necessary because the storage Map could be manipulated at
     * any time by external means.
     */
    protected void refreshFieldKeys() {
        super.clearFieldKeys();
        ArrayList keys = new ArrayList();
        Iterator kit = map.keySet().iterator();
        while (kit.hasNext()) {
            Object o = kit.next();
            if (o instanceof FieldKey) {
                keys.add(o);
            } else {
                keys.add(new MapFieldKey(o));
            }
        }
        super.addFieldKeys((FieldKey[])keys.toArray(new FieldKey[keys.size()]));
        super.sortFieldKeys();
    }

    /**
     * @return FieldKey[] The current set of FieldKeys in the Map (after
     * completing a refresh)
     */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        refreshFieldKeys();
        return super.getFieldKeys();
    }

    /**
     * Returns a {@link FieldKey} corresponding to the specified id.
     *
     * @param fieldId The desired id to retrieve a FieldKey for
     * @return FieldKey The FieldKey for the specified id (after completing a
     * refresh)
     */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        refreshFieldKeys();
        return super.getFieldKey(fieldId);
    }

    /**
     * Returns the value stored under the specified FieldKey in the Map.  The
     * passed FieldKey may be a {@link FieldKey} or a {@link MapFieldKey}.
     *
     * @param fieldKey The desired FieldKey to retieve the value for
     * @return Object The object stored in the Map under the specified FieldKey
     */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
        
        if( java.beans.Beans.isDesignTime() && (map == null || map.isEmpty() ) ) {
            // Fill the object with design time fake data
            map = (Map)AbstractDataProvider.getFakeData(map.getClass());
        }
        
        if (fieldKey instanceof MapFieldKey) {
            return map.get(((MapFieldKey)fieldKey).mapKey);
        }
        return map.get(fieldKey.getFieldId());
    }

    /**
     *
     * @param fieldKey FieldKey
     * @return Class
     */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        Object o = getValue(fieldKey);
        return o != null ? o.getClass() : null;
    }

    /**
     * None of the Map entries are read-only, so this method always returns
     * false.
     *
     * @param fieldKey The specified FieldKey (ignored)
     * @return This method will always return false, as none of the Map entries
     *         are read-only.
     */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        return false;
    }

    /**
     * Puts the specified value in the Map under the specified FieldKey.  If the
     * passed FieldKey is a {@link MapFieldKey}, the MapFieldKey.mapKey will be
     * used as the key, otherwise the FieldKey.id will be used as the key in the
     * underlying Map.  This method will result in a valueChanged event being
     * fired to all {@link com.sun.data.provider.DataListener} that are
     * listening to this DataProvider.
     *
     * @param fieldKey The desired FieldKey to store the value under
     * @param value The desired Object to store in the Map
     */
    public void setValue(FieldKey fieldKey, Object value)
        throws DataProviderException {

        Object oldValue = getValue(fieldKey);
        if (fieldKey instanceof MapFieldKey) {
            map.put(((MapFieldKey)fieldKey).mapKey, value);
        } else {
            map.put(fieldKey.getFieldId(), value);
        }
        fireValueChanged(fieldKey, oldValue, value);
    }
}
