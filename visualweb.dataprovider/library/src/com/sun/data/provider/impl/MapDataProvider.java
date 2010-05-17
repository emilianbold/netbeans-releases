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
