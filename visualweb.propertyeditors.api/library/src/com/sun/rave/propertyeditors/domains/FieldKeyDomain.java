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
package com.sun.rave.propertyeditors.domains;

import java.util.Iterator;
import java.util.TreeSet;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.DataProvider;
import com.sun.rave.designtime.DesignProperty;

/**
 * Domain of field keys available for this property's data provider.
 */
public class FieldKeyDomain extends AttachedDomain {

    public static final String DATA_PROVIDER_PROPERTY_NAME = "dataProviderPropertyName";

    /**
     *
     */
    public Element[] getElements() {

        DesignProperty prop = getDesignProperty();

        // If we have not been attached yet, there is nothing we can do
        // except return an empty list
        if (prop == null) {
            return Element.EMPTY_ARRAY;
        }

        TreeSet set = new TreeSet();

        String dpPropName = (String)prop.getPropertyDescriptor().getValue(DATA_PROVIDER_PROPERTY_NAME);

        // see if the DATA_PROVIDER_PROPERTY_NAME key has been set on the
        // PropertyDescriptor for this property.  If so, we can fetch the keys
        // from the DataProvider set on that property
        if (dpPropName != null && dpPropName.length() > 0) {
            DesignProperty dpProp = prop.getDesignBean().getProperty(dpPropName);
            if (dpProp != null && DataProvider.class.isAssignableFrom(dpProp.getPropertyDescriptor().getPropertyType())) {
                DataProvider dp = (DataProvider)dpProp.getValue();
                if (dp != null) {
                    try{
                        FieldKey[] keys = dp.getFieldKeys();
                        for (int j = 0; j < keys.length; j++) {
                            if (!set.contains(keys[j])) {
                                set.add(keys[j]);
                            }
                        }
                    }catch(Exception exc){
                        exc.printStackTrace();
                    }
                }
            }

        } else {

            // see if the DATA_PROVIDER_PROPERTY_NAME key has NOT been set on the
            // PropertyDescriptor for this property, scan all the properties on
            // the bean for properties of type DataProvider and fetch all the keys
            DesignProperty[] props = prop.getDesignBean().getProperties();
            for (int i = 0; i < props.length; i++) {
                Class propType = props[i].getPropertyDescriptor().getPropertyType();
                if (DataProvider.class.isAssignableFrom(propType)) {
                    DataProvider dp = (DataProvider)props[i].getValue();
                    if (dp != null) {
                        try{
                            FieldKey[] keys = dp.getFieldKeys();
                            for (int j = 0; j < keys.length; j++) {
                                if (!set.contains(keys[j])) {
                                    set.add(keys[j]);
                                }
                            }
                        }catch(Exception exc){
                            exc.printStackTrace();
                        }
                    }
                }
            }
        }

        // Construct a list of elements of the found keys
        Element elements[] = new Element[set.size()];
        Iterator keys = set.iterator();
        int n = 0;
        while (keys.hasNext()) {
            FieldKey key = (FieldKey)keys.next();
            elements[n++] = new Element(key.getFieldId(), key.getDisplayName());
        }
        return elements;

    }
}
