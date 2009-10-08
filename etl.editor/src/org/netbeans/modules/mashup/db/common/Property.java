/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.mashup.db.common;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Simple class to bind property key, type, current value, required flag and (optional)
 * default value information for a key-value property.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class Property implements Cloneable, Comparable {

    /* Log4J category string */
    private static final String LOG_CATEGORY = Property.class.getName();

    /* Key name */
    private String name;

    /* Property type */
    private Class type;

    /* Flag indicating whether a value is required */
    private boolean required;

    /* Refrsh column definition if this this property has been changed */
    private boolean refresh;

    /* (Optional) default value */
    private Object defaultValue;

    /* Current property value */
    private Object value;
    private static transient final Logger mLogger = Logger.getLogger(Property.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new default instance of Property */
    public Property() {
        required = true;
        defaultValue = null;
        refresh = true;
    }

    /**
     * Creates a new instance of Property with the given key name, type, and required
     * value flag.
     * 
     * @param myKey key name
     * @param myType value type
     * @param isRequired true if required, false otherwise
     */
    public Property(String myKey, Class myType, boolean isRequired) {
        this();
        name = myKey;
        type = myType;
        required = isRequired;
    }

    /**
     * Creates a new instance of Property with the given key name, type, default value,
     * and required value flag.
     * 
     * @param myKey key name
     * @param myType value type
     * @param myDefault default value
     * @param isRefreshRequired true if required, false otherwise
     */
    public Property(String myKey, Class myType, String myDefault, boolean isRefreshRequired) {
        this(myKey, myType, true);
        setDefault(myDefault);
        setRefresh(isRefreshRequired);
    }

    public Property(String myKey, Class myType, String myDefault) {
        this(myKey, myType, true);
        setDefault(myDefault);
    }

    /**
     * Creates a Map of pure key-value mappings from the given Map of keys to Property
     * instances. All other object classes are ignored.
     * 
     * @param properties Map of Property instances to be transformed
     * @return new Map of simple key-value mappings, based on contents of properties
     */
    public static Map createKeyValueMapFrom(Map properties) {
        Map newMap = new HashMap();

        Iterator propIter = properties.entrySet().iterator();
        while (propIter.hasNext()) {
            Map.Entry entry = (Map.Entry) propIter.next();
            Object obj = entry.getValue();

            if (obj instanceof Property) {
                Property prop = (Property) obj;
                if (prop.getValue() instanceof String) {
                    newMap.put(prop.getName(), StringUtil.escapeControlChars(prop.getValue().toString()));
                } else {
                    newMap.put(prop.getName(), prop.getValue());
                }
            }
        }

        return newMap;
    }

    /**
     * Gets property type
     * 
     * @return type
     */
    public Class getType() {
        return this.type;
    }

    /**
     * Sets Java type of this property's value object.
     * 
     * @param newType new type (e.g., Foo.class.getName() for instance of Foo)
     */
    public void setType(String newType) {
        try {
            type = Class.forName(newType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            type = String.class;
        }

    }

    /**
     * Gets name of this property.
     * 
     * @return name of this property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of this property.
     * 
     * @param newName new name of this property
     */
    public void setName(String newName) {
        name = newName;
    }

    public void setRefresh(boolean isRefreshRequired) {
        refresh = isRefreshRequired;
    }

    public boolean isRefreshRequired() {
        return refresh;
    }

    /**
     * Gets current value of property
     * 
     * @return current value
     */
    public Object getValue() {
        return (value != null) ? value : defaultValue;
    }

    /**
     * Sets current value of property to the given object. If newValue is null and this is
     * a required property, an IllegalArgumentException is thrown unless a default value
     * is defined. If newValue cannot be converted to this Property's given type, a
     * ClassCastException is thrown.
     * 
     * @param newValue new value of property.
     */
    public void setValue(Object newValue) {
        if (newValue == null) {
            if ((defaultValue == null) && required) {
                throw new IllegalArgumentException("Must supply non-null value for required property.");
            }
            newValue = null;
            return;
        }

        Class newValClass = newValue.getClass();
        if (type == newValClass) {
            if (type == String.class) {
                value = StringUtil.unescapeControlChars(newValue.toString());
            } else {
                value = newValue;
            }
        } else {
            try {
                String unescaped = StringUtil.unescapeControlChars(newValue.toString());
                Constructor constr = type.getConstructor(new Class[]{String.class});
                value = constr.newInstance(new Object[]{unescaped});
            } catch (Exception ex) {
                throw new ClassCastException("Value of type " + newValClass.getName() + " cannot be assigned to this property.");
            }
        }
    }

    /**
     * Gets default value, if any, for this property.
     * 
     * @return default value, or null if none is set.
     */
    public Object getDefault() {
        return defaultValue;
    }

    /**
     * Sets default value, if any, for this property.
     * 
     * @param def default value, or null for no default
     */
    public void setDefault(String def) {
        String cooked = StringUtil.unescapeControlChars(def);

        try {
            Constructor constr = type.getConstructor(new Class[]{String.class});
            this.defaultValue = constr.newInstance(new Object[]{cooked});
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT052: Could not construct default value of type {0}with parameter = {1}", type, def), ex);
            defaultValue = null;
        }
    }

    /**
     * Indicates whether this property requires a non-null, valid value.
     * 
     * @return true if valid value is required, false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether this property requires a non-null, valid value.
     * 
     * @param required true if valid value is required, false otherwise
     */
    public void setRequired(String required) {
        this.required = Boolean.valueOf(required).booleanValue();
    }

    /**
     * Indicates whether the current value of this property is valid, based on its type.
     * 
     * @return true if valid, false otherwise.
     */
    public boolean isValid() {
        return isValidValue(getValue());
    }

    /**
     * Indicates whether the given value would be a valid value for this property.
     * 
     * @param aValue value to test
     * @return true if valid, false otherwise.
     */
    public boolean isValidValue(Object aValue) {
        if (aValue == null) {
            return !required;
        }

        return (type == aValue.getClass());
    }

    /**
     * Puts key-value information to the given Map, provided that the value is valid.
     * 
     * @param aMap Map to receive this instance's key-value information
     * @return true if key-value information is valid and put succeeded, false otherwise.
     */
    public boolean putValueTo(Map aMap) {
        if (isValid()) {
            aMap.put(name, getValue());
            return true;
        }

        return false;
    }

    /**
     * Gets key-value information from the given Map, provided that the value in the map
     * would be a valid value.
     * 
     * @param aMap Map from which to get this instance's key-value information
     * @return true if key-value information is valid and get succeeded, false otherwise.
     */
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

    /**
     * Overrides default implementation to return value, if any, of this property.
     * 
     * @return value of property, or null if no value currently exists
     */
    public String toString() {
        return (getValue() != null) ? getValue().toString() : null;
    }

    /**
     * Gets key-value pair in canonical Java properties format, e.g., "key=value".
     * 
     * @return key-value pair in canonical Java properties format, or empty string if
     *         current value is invalid
     */
    public String getKeyValuePair() {
        String myValue = (getValue() != null) ? StringUtil.escapeControlChars(getValue().toString()) : "";

        return (isValid()) ? (name + "='" + StringUtil.replaceInString(myValue, "'", "\'") + "'") : "";
    }

    /**
     * Overrides default implementation to allow cloning of this instance.
     * 
     * @return clone of this instance
     */
    public Object clone() {
        try {
            return (Property) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Overrides default implementation.
     * 
     * @param o Object to compare for equality against this instance.
     * @return true if o is equivalent to this, false otherwise
     */
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

    /**
     * Overrides default implementation to compute its value based on member variables.
     * 
     * @return computed hash code
     */
    public int hashCode() {
        int hashCode = 0;

        hashCode += (name != null) ? name.hashCode() : 0;
        hashCode += type.hashCode();
        hashCode += (getValue() != null) ? getValue().hashCode() : 0;

        // NOTE: don't include defaultValue or required in computation
        // as they are used in determining response for getValue()
        return hashCode;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     * <p>
     * Note: this class has a natural ordering that is inconsistent with equals.
     * 
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less
     *         than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it from being
     *         compared to this Object.
     */
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
