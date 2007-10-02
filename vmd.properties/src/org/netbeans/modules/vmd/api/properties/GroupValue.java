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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.api.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Karol Harezlak
 */
/**
 * This class provide support to handle more that one DesignComponent property
 * in the single custom property editor. It can store more that one value of the property
 * in the map of the proprties values. This class is used in the GroupPropertyEditor custom property editor.
 */
public final class GroupValue {

    private Map<String, Object> valuesMap;
    private List<String> propertyNames;

    /**
     * Creates GroupValue instance with given list of DesignComponent properties
     * names list.
     * @param propertyNames list of properites names.
     */
    public GroupValue(List<String> propertyNames) {
        this.valuesMap = new HashMap<String, Object>();
        this.propertyNames = new ArrayList<String>(propertyNames);
    }

    /**
     * Returns value for given Design Component property name.
     * @param propertyName DesignComponent property name
     * @return property value
     */
    public Object getValue(String propertyName) {
        if (!propertyName.contains(propertyName)) {
            throw new IllegalArgumentException("Invalid propertyName"); //NOI18N
        }
        return valuesMap.get(propertyName);
    }

    /**
     * Returns array of property names available for this GroupValue.
     * @return
     */
    public String[] getPropertyNames() {
        return propertyNames.toArray(new String[propertyNames.size()]);
    }

    /**
     * Put value into the property map of values.
     * @param propertyName used as a key in the property value map
     * @param value property value
     */
    public void putValue(String propertyName, Object value) {
        if (!propertyName.contains(propertyName)) {
            throw new IllegalArgumentException("Invalid propertyName"); //NOI18N
        }
        valuesMap.put(propertyName, value);
    }
}