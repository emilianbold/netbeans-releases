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

package org.netbeans.upgrade.systemoptions;

import java.util.Iterator;
import java.util.List;
import org.netbeans.upgrade.systemoptions.SerParser.ArrayWrapper;
import org.netbeans.upgrade.systemoptions.SerParser.NameValue;
import org.netbeans.upgrade.systemoptions.SerParser.ObjectWrapper;

/**
 *
 * @author rmatous
 */
final class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {}

    
    static String valueFromObjectWrapper(final Object value) {
        String stringvalue = null;
        if (value instanceof ObjectWrapper) {
            List l = ((SerParser.ObjectWrapper)value).data;
            if (l.size() == 1) {
                Object o = l.get(0);
                if (o instanceof NameValue) {
                    Object key = null;
                    stringvalue = ((NameValue) o).value.toString();
                }
            }
            if (stringvalue == null) {
                stringvalue = ((ObjectWrapper) value).classdesc.name;
            }
        }  else if (value instanceof String && !"null".equals(value)) {
            stringvalue = value.toString();
            
        } else if (value instanceof SerParser.ArrayWrapper && "[Ljava.lang.String;".equals(((SerParser.ArrayWrapper)value).classdesc.name)) {
            StringBuffer sb = new StringBuffer();
            List es = ((SerParser.ArrayWrapper)value).values;
            for (Iterator it = es.iterator(); it.hasNext();) {
                sb.append((String)it.next());
                if (it.hasNext()) {
                    sb.append(" , ");
                }                
            }
            stringvalue = sb.toString();            
        } else if (value instanceof SerParser.ArrayWrapper && "[[Ljava.lang.String;".equals(((SerParser.ArrayWrapper)value).classdesc.name)) {
            StringBuffer sb = new StringBuffer();
            List awl = ((SerParser.ArrayWrapper)value).values;
            for (Iterator it = awl.iterator(); it.hasNext();) {
                SerParser.ArrayWrapper aw = (SerParser.ArrayWrapper)it.next();
                sb.append(valueFromObjectWrapper(aw));
                if (it.hasNext()) {
                    sb.append(" | ");
                }
            }
            stringvalue = sb.toString();            
        } else {
            stringvalue = "unknown";//value.toString();
        }
        return stringvalue;
    }
    
    static String getClassNameFromObject(final Object value) {
        String clsName = null;
        if (value instanceof ObjectWrapper) {
            clsName = prettify(((ObjectWrapper) value).classdesc.name);
        }  else if (value instanceof ArrayWrapper) {
            clsName = prettify(((ArrayWrapper) value).classdesc.name);
        }  else {
            clsName = prettify(value.getClass().getName());
        }
        return clsName;
    }
    
    static String prettify(String type) {
        if (type.equals("B")) { // NOI18N
            return "byte"; // NOI18N
        } else if (type.equals("S")) { // NOI18N
            return "short"; // NOI18N
        } else if (type.equals("I")) { // NOI18N
            return "int"; // NOI18N
        } else if (type.equals("J")) { // NOI18N
            return "long"; // NOI18N
        } else if (type.equals("F")) { // NOI18N
            return "float"; // NOI18N
        } else if (type.equals("D")) { // NOI18N
            return "double"; // NOI18N
        } else if (type.equals("C")) { // NOI18N
            return "char"; // NOI18N
        } else if (type.equals("Z")) { // NOI18N
            return "boolean"; // NOI18N
        } else if (type.startsWith("L") && type.endsWith(";")) { // NOI18N
            String fqn = type.substring(1, type.length() - 1).replace('/', '.').replace('$', '.'); // NOI18N
            return fqn;
        }
        if (!type.startsWith("[")) {
            if (type.startsWith("L")) {
                return type.substring(1);
            }
            if (type.endsWith(";")) {
                return type.substring(0,type.length()-1);
            }
        }
        return type;
    }
}
