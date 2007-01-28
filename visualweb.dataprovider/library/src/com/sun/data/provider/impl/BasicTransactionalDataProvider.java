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
import com.sun.data.provider.DataListener;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.TransactionalDataProvider;
import java.util.ResourceBundle;

/**
 *
 * @author Joe Nuxoll
 *         Winston Prakash (Buf Fixes and clean up)
 */
public class BasicTransactionalDataProvider extends AbstractDataProvider
    implements TransactionalDataProvider {

    private transient ResourceBundle bundle = null;
    /**
     *
     */
    protected DataProvider provider;

    /**
     *
     * @param provider DataProvider
     */
    public void setDataProvider(DataProvider provider) {
        if (this.provider != null) {
            this.provider.removeDataListener(ears);
        }
        this.provider = provider;
        this.provider.addDataListener(ears);
        changeMap.clear();
        fireProviderChanged();
    }
    
    /**
     * <p>Return the resource bundle containing our localized messages.</p>
     */
    private ResourceBundle getBundle() {

        if (bundle == null) {
            bundle = ResourceBundle.getBundle("com/sun/data/provider/impl/Bundle");
        }
        return bundle;

    }

    /**
     *
     * @return DataProvider
     */
    public DataProvider getDataProvider() {
        return provider;
    }

    /** {@inheritDoc} */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        if (provider == null) {
            return FieldKey.EMPTY_ARRAY;
        }
        return provider.getFieldKeys();
    }

    /** {@inheritDoc} */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        if (provider == null) {
            throw new DataProviderException(getBundle().getString("NO_DATAPROVIDER_SET"));
        }
        return provider.getFieldKey(fieldId);
    }

    /**
     *
     * @param fieldKey FieldKey
     * @return Class
     */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        if (provider == null) {
            throw new DataProviderException(getBundle().getString("arbitraryCharData"));
        }
        return provider.getType(fieldKey);
    }

    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
        if (provider == null) {
            throw new DataProviderException(getBundle().getString("NO_DATAPROVIDER_SET"));
        }
        if (changeMap.containsKey(fieldKey)) {
            return changeMap.get(fieldKey);
        }
        return provider.getValue(fieldKey);
    }

    /** {@inheritDoc} */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        if (provider == null) {
            throw new DataProviderException(getBundle().getString("NO_DATAPROVIDER_SET"));
        }
        return provider.isReadOnly(fieldKey);
    }

    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, Object value) throws DataProviderException {
        if (provider == null) {
            throw new DataProviderException(getBundle().getString("NO_DATAPROVIDER_SET"));
        }
        if (provider.isReadOnly(fieldKey)) {
            throw new DataProviderException(getBundle().getString("FK_READ_ONLY"));
        }
        Object oldValue = getValue(fieldKey);
        changeMap.put(fieldKey, value);
        fireValueChanged(fieldKey, oldValue, value);
    }

    /**
     *
     */
    protected HashMap changeMap = new HashMap();

    /**
     *
     */
    public void commitChanges() throws DataProviderException {
        Iterator i = changeMap.keySet().iterator();
        while (i.hasNext()) {
            FieldKey key = (FieldKey)i.next();
            provider.setValue(key, changeMap.get(key));
        }
        changeMap.clear();
        fireChangesCommitted();
    }

    /**
     *
     */
    public void revertChanges() throws DataProviderException {
        changeMap.clear();
        fireChangesReverted();
        fireProviderChanged();
    }

    // ----------------------------------------------------------- Event Methods

    /** {@inheritDoc} */
    public void addTransactionalDataListener(TransactionalDataListener l) {
        super.addDataListener(l);
    }

    /** {@inheritDoc} */
    public void removeTransactionalDataListener(TransactionalDataListener l) {
        super.removeDataListener(l);
    }

    /** {@inheritDoc} */
    public TransactionalDataListener[] getTransactionalDataListeners() {
        if (dpListeners == null) {
            return new TransactionalDataListener[0];
        } else {
            ArrayList cdpList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof TransactionalDataListener) {
                    cdpList.add(dpListeners[i]);
                }
            }
            return (TransactionalDataListener[])cdpList.toArray(
                new TransactionalDataListener[cdpList.size()]);
        }
    }

    /**
     * Fires a changesCommtted event to each registered {@link
     * TransactionalDataListener}
     *
     * @see TransactionalDataListener#changesCommitted(TransactionalDataProvider)
     */
    protected void fireChangesCommitted() {
        TransactionalDataListener[] cdls = getTransactionalDataListeners();
        for (int i = 0; i < cdls.length; i++) {
            cdls[i].changesCommitted(this);
        }
    }

    /**
     * Fires a changesReverted event to each registered {@link
     * TransactionalDataListener}
     *
     * @see TransactionalDataListener#changesReverted(TransactionalDataProvider)
     */
    protected void fireChangesReverted() {
        TransactionalDataListener[] cdls = getTransactionalDataListeners();
        for (int i = 0; i < cdls.length; i++) {
            cdls[i].changesReverted(this);
        }
    }

    /**
     *
     */
    protected DataListener ears = new DataListener() {
        public void valueChanged(DataProvider provider, FieldKey fieldKey,
            Object oldValue, Object newValue) {
            fireValueChanged(fieldKey, oldValue, newValue);
        }
        public void providerChanged(DataProvider provider) {
            fireProviderChanged();
        }
    };
}
