/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import org.netbeans.modules.cnd.debugger.gdb.Variable;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;

/**
 *
 * @author Egor Ushakov
 */
public class ValuePresenter {
    private ValuePresenter() {
    }

    private static Presenter[] presenters = new Presenter[]{new StdStringPresenter()};

    public static String getValue(Variable var) {
        return getValue(var.getType(), var.getValue());
    }

    public static String getValue(String value) {
        return getValue(null, value);
    }

    public static String getValue(String type, String value) {
        for (Presenter vp : presenters) {
            if (vp.accepts(type, value)) {
                return vp.present(type, value);
            }
        }
        return value;
    }

    private static interface Presenter {
        boolean accepts(String type, String value);
        String present(String type, String value);
    }

    private static class StdStringPresenter implements Presenter {
        private static final String VALUE_PREFIX = "_M_p"; // NOI18N
        private static final String TYPE_NAME = "string"; // NOI18N
        private static final String TYPE_NAME2 = "string &"; // NOI18N

        public boolean accepts(String type, String value) {
            // if type is not provided - try to check value
            if (type == null) {
                return checkValue(value);
            }
            return (TYPE_NAME.equals(type) || TYPE_NAME2.equals(type)) && value != null && value.contains(VALUE_PREFIX);
        }

        // checks if value is of string type
        private static boolean checkValue(String value) {
            if (value == null || value.length() < 10) {
                return false;
            }
            int start = value.indexOf('{');
            if (start == -1) {
                return false;
            }
            int end = value.lastIndexOf('}');
            if (end == -1 || end < start+1) {
                return false;
            }
            value = value.substring(start+1, end);
            if (value.indexOf("static npos") == -1) {// NOI18N
                return false;
            }
            return value.indexOf(VALUE_PREFIX) != -1;
        }

        public String present(String type, String value) {
            int pos = value.indexOf(VALUE_PREFIX);
            assert pos > 0;
            pos = value.indexOf('"', pos);
            if (pos > 0) {
                // fix for \" quotes
                if (value.charAt(pos-1) == '\\') {
                    return present(type, value.replace("\\\"", "\"")); // NOI18N
                }
                int end = GdbUtils.findEndOfString(value, pos+1);
                if (end != -1) {
                    return value.substring(pos, end+1);
                }
            }
            return value;
        }
    }
}
