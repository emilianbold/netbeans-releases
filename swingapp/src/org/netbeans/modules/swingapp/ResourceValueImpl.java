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

package org.netbeans.modules.swingapp;

import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.ResourceValue;
import org.openide.filesystems.FileObject;

/**
 * Implementation of ResourceValue from the form editor. Besides the key and
 * value required by the ResourceValue interface, the implementation also keeps
 * the raw string value (as stored in the properties file), the actual value
 * type, level of storage (class, package, application), whether the value is
 * locale-sensitive, the source file (form) where the value is used, etc.
 * 
 * @author Tomas Pavek
 */
final class ResourceValueImpl implements ResourceValue {

    private String key;
    private Class valueType;
    private String classPathResourceName;
    private String stringValue;
    private Object value;
    private boolean internationalized;
    private int storageLevel;
    private FileObject sourceFile;

    private String[] allData; // all data across all locales, used for undo

    ResourceValueImpl(String key, Class type,
                      Object value, String cpResourceName, String stringValue,
                      boolean i18n, int level, FileObject srcFile)
    {
        this.key = key;
        this.valueType = type;
        this.value = value;
        this.classPathResourceName = cpResourceName;
        this.stringValue = stringValue;
        this.internationalized = i18n;
        this.storageLevel = level;
        this.sourceFile = srcFile;
    }

    ResourceValueImpl(ResourceValueImpl resVal) {
        this(resVal.key, resVal.valueType, resVal.value, resVal.classPathResourceName,
             resVal.stringValue, resVal.internationalized, resVal.storageLevel,
             resVal.sourceFile);
    }

    // ResourceValue implementation
    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    Class getValueType() {
        return valueType;
    }

    // ResourceValue implementation
    public Object getValue() {
        return value;
    }

    void setValue(Object value) {
        this.value = value;
    }

    void setClassPathResourceName(String cpResName) {
        classPathResourceName = cpResName;
    }

    // ResourceValue implementation
    public String getClassPathResourceName() {
        return classPathResourceName;
    }

    void setStringValue(String strValue) {
        stringValue = strValue;
    }

    String getStringValue() {
        return stringValue;
    }

    void setInternationalized(boolean i) {
        internationalized = i;
    }

    boolean isInternationalized() {
        return internationalized;
    }

    void setStorageLevel(int level) {
        storageLevel = level;
    }

    int getStorageLevel() {
        return storageLevel;
    }

    FileObject getSourceFile() {
        return sourceFile;
    }

    void setAllData(String[] data) {
        allData = data;
    }

    String[] getAllData() {
        return allData;
    }

    // ResourceValue implementation
    public String getJavaInitializationCode() {
        String pre = null;
        String resMapCode = AppFrameworkSupport.getResourceMapCode(sourceFile);
        String methodCode;
        String param = "\"" + key + "\""; // NOI18N
        String[] paramCode = null;
        // there are different methods in ResourceMap based on the value type
        if (valueType == String.class) {
            methodCode = ".getString"; // NOI18N
        } else if (valueType == Color.class) {
            methodCode = ".getColor"; // NOI18N
        } else if (valueType == Font.class) {
            methodCode = ".getFont"; // NOI18N
        } else if (valueType == Icon.class) {
            methodCode = ".getIcon"; // NOI18N
        } else {// unknown type
            pre = "(" + valueType.getName() + ")"; // NOI18N
            methodCode = ".getObject"; // NOI18N
            paramCode = new String[] { param, valueType.getName() + ".class" }; // NOI18N
        }
        if (paramCode == null) {
            paramCode = new String[] { param };
        }

        StringBuilder buf = new StringBuilder();
        if (pre != null) {
            buf.append(pre);
        }
        // The code for getting the resource map is quite long - worth "caching"
        // in a variable. Using special code mark to encode 3 data elements:
        // - the code to replace
        // - the type of variable to declare for the code
        // - suggested variable name
        buf.append(CODE_MARK_VARIABLE_SUBST).append(resMapCode);
        buf.append(CODE_MARK_VARIABLE_SUBST).append(application.ResourceMap.class.getName());
        buf.append(CODE_MARK_VARIABLE_SUBST).append("resourceMap"); // NOI18N
        buf.append(CODE_MARK_LINE_COMMENT + "NOI18N"); // NOI18N
        buf.append(CODE_MARK_END); // indicates that a normal code follows
        buf.append(methodCode).append("("); // NOI18N
        for (int i=0; i < paramCode.length; i++) {
            buf.append(paramCode[i]);
            if (i+1 < paramCode.length) {
                buf.append(", "); // NOI18N
            }
        }
        buf.append(")"); // NOI18N
        return buf.toString();
    }

    // special code marks recognized by form editor:
    private static final String CODE_MARK_END = "*/\n\\0"; // NOI18N
    private static final String CODE_MARK_LINE_COMMENT = "*/\n\\1"; // NOI18N
    private static final String CODE_MARK_VARIABLE_SUBST = "*/\n\\2"; // NOI18N

    // FormDesignValue implementation
    public Object getDesignValue() {
        return value;
    }

    // FormDesignValue implementation
    public Object getDesignValue(Object target) {
        return null;
    }

    // FormDesignValue implementation
    public String getDescription() {
        return "<" + getKey() + ">"; // NOI18N
    }

    // FormDesignValue implementation
    public Object copy(FormProperty targetFormProperty) {
        // TBD...
        return getDesignValue();
    }
}
