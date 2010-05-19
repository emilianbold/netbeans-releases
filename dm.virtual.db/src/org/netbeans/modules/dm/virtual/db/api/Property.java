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
package org.netbeans.modules.dm.virtual.db.api;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.openide.util.NbBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;

/**
 * Simple class to bind property key, type, current value, required flag and (optional)
 * default value information for a key-value property.
 * 
 * @author Ahimanikya Satapathy
 */
public class Property implements Cloneable, Comparable {

    private String name;
    private Class type;
    private boolean required;
    private boolean refresh;
    private Object defaultValue;
    private Object value;
    private static Logger mLogger = Logger.getLogger(Property.class.getName());

    public Property() {
        required = true;
        defaultValue = null;
        refresh = true;
    }

    public Property(String myKey, Class myType, boolean isRequired) {
        this();
        name = myKey;
        type = myType;
        required = isRequired;
    }

    public Property(String myKey, Class myType, String myDefault, boolean isRefreshRequired) {
        this(myKey, myType, true);
        setDefault(myDefault);
        setRefresh(isRefreshRequired);
    }

    public Property(String myKey, Class myType, String myDefault) {
        this(myKey, myType, true);
        setDefault(myDefault);
    }

    public static Map createKeyValueMapFrom(Map properties) {
        Map newMap = new HashMap();

        Iterator propIter = properties.entrySet().iterator();
        while (propIter.hasNext()) {
            Map.Entry entry = (Map.Entry) propIter.next();
            Object obj = entry.getValue();

            if (obj instanceof Property) {
                Property prop = (Property) obj;
                if (prop.getValue() instanceof String) {
                    newMap.put(prop.getName(), VirtualDBUtil.escapeControlChars(prop.getValue().toString()));
                } else {
                    newMap.put(prop.getName(), prop.getValue());
                }
            }
        }

        return newMap;
    }

    public Class getType() {
        return this.type;
    }

    public void setType(String newType) {
        try {
            type = Class.forName(newType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            type = String.class;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public void setRefresh(boolean isRefreshRequired) {
        refresh = isRefreshRequired;
    }

    public boolean isRefreshRequired() {
        return refresh;
    }

    public Object getValue() {
        return (value != null) ? value : defaultValue;
    }

    public void setValue(Object newValue) {
        if (newValue == null) {
            if ((defaultValue == null) && required) {
                throw new IllegalArgumentException(NbBundle.getMessage(Property.class, "MSG_supplied_nullValue"));
            }
            newValue = null;
            return;
        }

        Class newValClass = newValue.getClass();
        if (type == newValClass) {
            if (type == String.class) {
                value = VirtualDBUtil.unescapeControlChars(newValue.toString());
            } else {
                value = newValue;
            }
        } else {
            try {
                String unescaped = VirtualDBUtil.unescapeControlChars(newValue.toString());
                Constructor constr = type.getConstructor(new Class[]{String.class});
                value = constr.newInstance(new Object[]{unescaped});
            } catch (Exception ex) {
                throw new ClassCastException(NbBundle.getMessage(Property.class, "MSG_type_value") + newValClass.getName() + NbBundle.getMessage(Property.class, "MSG_cannot_assign"));
            }
        }
    }

    public Object getDefault() {
        return defaultValue;
    }

    public void setDefault(String def) {
        String cooked = VirtualDBUtil.unescapeControlChars(def);

        try {
            Constructor constr = type.getConstructor(new Class[]{String.class});
            this.defaultValue = constr.newInstance(new Object[]{cooked});
        } catch (Exception ex) {            
            mLogger.log(Level.INFO, NbBundle.getMessage(Property.class, "MSG_constructor",type,def) +ex);
            defaultValue = null;
        }
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = Boolean.valueOf(required).booleanValue();
    }

    public boolean isValid() {
        return isValidValue(getValue());
    }

    public boolean isValidValue(Object aValue) {
        if (aValue == null) {
            return !required;
        }

        return (type == aValue.getClass());
    }

    public boolean putValueTo(Map aMap) {
        if (isValid()) {
            aMap.put(name, getValue());
            return true;
        }

        return false;
    }

    public boolean getValueFrom(Map aMap) {
        Object o = aMap.get(name);
        Object newValue = null;

        if (o instanceof Property) {
            newValue = ((Property) o).getValue();
        } else {
            newValue = o;
        }

        if (isValidValue(newValue)) {
            setValue(newValue);
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return (getValue() != null) ? getValue().toString() : null;
    }

    public String getKeyValuePair() {
        String myValue = (getValue() != null) ? VirtualDBUtil.escapeControlChars(getValue().toString()) : "";

        return (isValid()) ? (name + "='" + VirtualDBUtil.replaceInString(myValue, "'", "\'") + "'") : "";
    }

    @Override
    public Object clone() {
        try {
            return (Property) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (null == o) {
            return false;
        }

        boolean response = false;
        if (o instanceof Property) {
            response = true;
            Property src = (Property) o;

            response &= (name != null) ? name.equals(src.name) : (src.name == null);

            // NOTE: don't include defaultValue or required in comparison
            // as they are used in determining response for getValue()
            Object myValue = getValue();
            Object srcValue = src.getValue();
            response &= (myValue != null) ? myValue.equals(srcValue) : (srcValue == null);

            response &= (type == src.type);
        }

        return response;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        hashCode += (name != null) ? name.hashCode() : 0;
        hashCode += type.hashCode();
        hashCode += (getValue() != null) ? getValue().hashCode() : 0;

        // NOTE: don't include defaultValue or required in computation
        // as they are used in determining response for getValue()
        return hashCode;
    }

    public int compareTo(Object o) {
        if (o == this) {
            return 0;
        } else if (o == null) {
            return -1;
        }

        Property p = (Property) o;
        return name.compareTo(p.getName());
    }
}
