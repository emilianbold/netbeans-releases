/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for fetching scripts stored in separate files.
 *
 * @author Jan Stola
 */
public class Script {
    /** Base folder where the scripts are located.  */
    private static final String RESOURCE_BASE = "/org/netbeans/modules/web/inspect/script/"; // NOI18N
    /** Extension of the script files. */
    private static final String EXTENSION = ".js"; // NOI18N
    /** Map with already loaded scripts, it maps name of the script to the script. */
    private static Map<String,String> scripts = Collections.synchronizedMap(new HashMap<String,String>());

    /**
     * Returns script with the specified name.
     * 
     * @param name name of the script.
     * @return script with the specified name.
     */
    public static String getScript(String name) {
        String script;
        if (scripts.containsKey(name)) {
            script = scripts.get(name);
        } else {
            String resourceName = RESOURCE_BASE + name + EXTENSION;
            script = loadScript(resourceName);
            scripts.put(name, script);
        }
        return script;
    }

    /**
     * Loads the script with the specified resource name.
     * 
     * @param resourceName resource name of the script.
     * @return script with the specified resource name.
     */
    private static String loadScript(String resourceName) {
        InputStream stream = Script.class.getResourceAsStream(resourceName);
        String script = null;
        if (stream != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                script = removeLicense(sb.toString());
            } catch (IOException ioex) {
                Logger.getLogger(Script.class.getName()).log(Level.INFO, null, ioex);
            }
        }
        return script;
    }

    /**
     * Removes the license header from the loaded content of a script file.
     * 
     * @param script content of a script file that should be stripped of
     * the license.
     * @return script without the license header.
     */
    private static String removeLicense(String script) {
        if (script.trim().startsWith("/*")) { // NOI18N
            int endOfComment = script.indexOf("*/"); // NOI18N
            script = script.substring(endOfComment+2).trim();
        }
        return script;
    }
    
}
