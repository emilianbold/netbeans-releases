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
