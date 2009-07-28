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
package org.netbeans.modules.nativeexecution.support;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;

/**
 * This map is a wrapper of Map&lt;String, String&gt; that expangs
 * macros on insertion...
 */
public class MacroMap {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final MacroExpander macroExpander;
    private final TreeMap<String, String> map;

    public MacroMap(MacroExpander macroExpander) {
        this.macroExpander = macroExpander;
        this.map = new TreeMap<String, String>();
    }

    public final void putAll(MacroMap envVariables) {
        for (String key : envVariables.map.keySet()) {
            put(key, envVariables.get(key));
        }
    }

    public String put(String key, String value) {
        if (value == null) {
            log.log(Level.INFO, "Attempt to set env variable '%s' with null value", value); // NOI18N
        }

        String result = value;

        Map<String, String> oneElementMap = new HashMap<String, String>();
        String val = map.get(key);

        if (val != null) {
            oneElementMap.put(key, val);
        }

        try {
            result = macroExpander.expandMacros(value, oneElementMap);
        } catch (ParseException ex) {
        }

        return map.put(key, result);
    }

    public String get(String key) {
        String result = map.get(key);
//        Map<String, String> butOneElementMap = new HashMap<String, String>();
//        butOneElementMap.putAll(map);
//        butOneElementMap.remove(key);
//
//        if (result != null) {
//            try {
//                result = macroExpander.expandMacros(result, butOneElementMap);
//            } catch (ParseException ex) {
//            }
//        }

        return result;
    }

    @Override
    public final String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{"); // NOI18N

        for (String key : map.keySet()) {
            buf.append(key);
            buf.append("="); // NOI18N
            buf.append(get(key));
            buf.append(", "); // NOI18N
        }

        buf.append("}"); // NOI18N
        return buf.toString();
    }

    public final boolean isEmpty() {
        return map.isEmpty();
    }

    public final Iterable<String> keySet() {
        return map.keySet();
    }
}
