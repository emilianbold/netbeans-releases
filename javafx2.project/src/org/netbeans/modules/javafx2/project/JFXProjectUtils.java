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
package org.netbeans.modules.javafx2.project;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for JavaFX 2.0 Project
 * 
 * @author Petr Somol
 */
public final class JFXProjectUtils {

    /**
     * Returns list of JavaFX 2.0 JavaScript callback entries.
     * In future should read the list from the current platform
     * (directly from FX SDK or Ant taks).
     * Current list taken from
     * http://javaweb.us.oracle.com/~in81039/new-dt/js-api/Callbacks.html
     * 
     * @param IDE java platform name
     * @return callback entries
     */
    public static Map<String,List<String>/*|null*/> getJSCallbacks(String platformName) {
        final String[][] c = {
            {"onDeployError", "app", "mismatchEvent"}, // NOI18N
            {"onGetNoPluginMessage", "app"}, // NOI18N
            {"onGetSplash", "app"}, // NOI18N
            {"onInstallFinished", "placeholder", "component", "status", "relaunchNeeded"}, // NOI18N
            {"onInstallNeeded", "app", "platform", "cb", "isAutoinstall", "needRelaunch", "launchFunc"}, // NOI18N
            {"onInstallStarted", "placeholder", "component", "isAuto", "restartNeeded"}, // NOI18N
            {"onJavascriptReady", "id"}, // NOI18N
            {"onRuntimeError", "id", "code"} // NOI18N
        };
        Map<String,List<String>/*|null*/> m = new LinkedHashMap<String,List<String>/*|null*/>();
        for(int i = 0; i < c.length; i++) {
            String[] s = c[i];
            assert s.length > 0;
            List<String> l = null;
            if(s.length > 1) {
                l = new ArrayList<String>();
                for(int j = 1; j < s.length; j++) {
                    l.add(s[j]);
                }
            }
            m.put(s[0], l);
        }
        return m;
    }
    
}
