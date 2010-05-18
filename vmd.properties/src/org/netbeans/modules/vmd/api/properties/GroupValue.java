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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
