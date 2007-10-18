/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.openide.util.Utilities;

/**
 *
 * @author Peter Liu
 */
public class ParameterInfo {

    private String name;
    private Class type;
    private String typeName;
    private Object defaultValue;
    private boolean isQueryParam;
    private QName qname;

    public ParameterInfo(String name, Class type) {
        this(name, type, null);
    }
    
    public ParameterInfo(QName qname, Class type, String typeName) {
        this(qname.getLocalPart(), type, typeName);
        this.qname = qname;
    }
    
    public ParameterInfo(String name, Class type, String typeName) {
        this.name = name;
        this.type = type;
        this.typeName = typeName;
        this.defaultValue = null;
        this.isQueryParam = isQualifiedParameterType(type);
    }

    private static boolean isQualifiedParameterType(Class type) {
        return type.isPrimitive() || type.equals(String.class) ||
               Utils.getValueOfMethod(type) != null || 
               Utils.getConstructorWithStringParam(type) != null;
    }
    
    public String getName() {
        return name;
    }
    
    public QName getQName() {
        if (qname == null) {
            qname = new QName(name);
        }
        return qname;
    }

    public Class getType() {
        return type;
    }

    public String getTypeName() {
        if (typeName == null) {
            return type.getName();
        }
        return typeName;
    }

    public String getSimpleTypeName() {
        return type.getSimpleName();
    }
    
    public void setDefaultValue(Object value) {
        this.defaultValue = value;
    }

    public Object getDefaultValue() {
        if (defaultValue == null) {
            defaultValue = generateDefaultValue();
        }
        return defaultValue;
    }

    public boolean isQueryParam() {
        return isQueryParam;
    }

    public void setIsQueryParam(boolean flag) {
        this.isQueryParam = flag;
    }
    
    private Object generateDefaultValue() {
        if (type == Integer.class || type == Short.class || type == Long.class ||
                type == Float.class || type == Double.class) {
            try {
                return type.getConstructor(String.class).newInstance("0"); //NOI18N
            } catch (Exception ex) {
                return null;
            }
        }
        
        if (type == Boolean.class) {
            return Boolean.FALSE;
        }
    
        if (type == Character.class) {
            return new Character('\0');
        }
        
        return null;
    }
}