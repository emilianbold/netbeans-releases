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

package org.netbeans.modules.cnd.debugger.gdb;

import java.io.File;
import java.util.Map;

/**
 *
 * @author eu155513
 */
public class EnvUtils {
    private EnvUtils() {
        
    }

    public static String getKey(String envEntry) {
        int idx = envEntry.indexOf('=');
        if (idx != -1) {
            return envEntry.substring(0, idx);
        } else {
            return envEntry;
        }
    }

    public static String getValue(String envEntry) {
        int idx = envEntry.indexOf('=');
        if (idx != -1) {
            return envEntry.substring(idx + 1);
        } else {
            return "";
        }
    }

    /**
     * Converts environment array (of strings in 'a=b' format)
     * into a map (key a, value b)
     * if oldenv is not null then use it as a map
     * @param env
     * @return
     */
    public static void appendEnv(Map<String, String> oldenv, String[] env) {
        for (String var : env) {
            int idx = var.indexOf('=');
            if (idx != -1) {
                appendPath(oldenv, getKey(var), getValue(var));
            }
        }
    }

    /**
     * Appends key=value pair to the env map.
     * If key=oldvalue already existed in the map then new pair will be key=oldvalue:value
     * (separator depends on the platform)
     * @param env
     * @param key
     * @param value
     * @return
     */
    public static boolean appendPath(Map<String, String> env, String key, String value) {
        String oldVal = env.get(key);
        if (oldVal != null) {
            //TODO: check remote!
            value = oldVal + File.pathSeparator + value;
        }
        return env.put(key, value) != null;
    }
}
