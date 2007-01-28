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
package com.sun.jsfcl.std.property;

import java.beans.PropertyEditorSupport;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;

/**
 * @author eric
 *
 * @deprecated
 */
public abstract class AbstractPropertyEditor extends PropertyEditorSupport implements
    PropertyEditor2 {
    protected DesignProperty liveProperty;

    public static Class getClassNamed(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Convert a string into a string that can be directly inserted into java source.
     * @param str
     * @return
     */
    public static String stringToJavaSourceString(String str) {
        StringBuffer buf = new StringBuffer(str.length() * 6); // x -> \u1234
        buf.append("\""); //NOI18N
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\b':
                    buf.append("\\b");
                    break; // NOI18N
                case '\t':
                    buf.append("\\t");
                    break; // NOI18N
                case '\n':
                    buf.append("\\n");
                    break; // NOI18N
                case '\f':
                    buf.append("\\f");
                    break; // NOI18N
                case '\r':
                    buf.append("\\r");
                    break; // NOI18N
                case '\"':
                    buf.append("\\\"");
                    break; // NOI18N
                    //        case '\'': buf.append("\\'"); break; // NOI18N
                case '\\':
                    buf.append("\\\\");
                    break; // NOI18N
                default:
                    if (c >= 0x0020 && c <= 0x007f) {
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N
                        String hex = Integer.toHexString(c);
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            buf.append('0');
                        }
                        buf.append(hex);
                    }
            }
        }
        buf.append("\""); // NOI18N
        return buf.toString();
    }

    public void attachToNewDesignProperty() {
    }

    public DesignProperty getDesignProperty() {
        return liveProperty;
    }

    public DesignProject getProject() {
        return getDesignProperty().getDesignBean().getDesignContext().getProject();
    }

    /* (non-Javadoc)
     * @see com.sun.rave.designtime.PropertyEditor2#setDesignProperty(com.sun.rave.designtime.DesignProperty)
     */
    public void setDesignProperty(DesignProperty prop) {
        liveProperty = prop;
        attachToNewDesignProperty();
    }

    protected void unsetProperty() {
        //!CQ AAAAKKK!!!! NEVER try to mutate the liveProperty!!!
        //getDesignProperty().unset();
    }
}
