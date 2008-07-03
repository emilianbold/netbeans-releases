/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;

/**
 *
 * @author gordonp
 */
public class RemoteServerSetup {
    
    private static Logger log = Logger.getLogger("cnd.remote.logger");
    private static final String dirPath = ".netbeans/6.5/cnd2/scripts"; // NOI18N
    
    private static Map<String, Double> setupMap;
    private static Map<String, List<String>> updateMap;
    
    static {
        setupMap = new HashMap<String, Double>();
        setupMap.put(".netbeans/6.5/cnd2/scripts/getCompilerSets.bash", Double.valueOf(1.0));
        updateMap = new HashMap<String, List<String>>();
    }
    
    protected static void setup(String name) {
        List<String> list = updateMap.remove(name);
        
        for (String script : list) {
            if (script.equals(dirPath)) {
                System.out.println("Update: Create directory");
            } else {
                System.out.println("Update: Update " + script + " to " + setupMap.get(script));
            }
        }
    }

    static boolean needsSetupOrUpdate(String name) {
        String cmd = "PATH=/bin:/usr/bin:$PATH  grep VERSION= " + dirPath + "/* /dev/null 2>&1 "; // NOI18N
        List<String> updateList = new ArrayList<String>();
        
        RemoteCommandSupport support = new RemoteCommandSupport(name, cmd);
        String val = support.toString();
        for (String line : val.split("\n")) {
            if (line.endsWith(" .netbeans/6.5/cnd2/scripts/*")) {
                updateList.add(".netbeans/6.5/cnd2/scripts/");
                break;
            }
            int pos;
            String script;
            Double installedVersion;
            try {
                pos = line.indexOf(':');
                script = line.substring(0, pos);
                installedVersion = Double.valueOf(line.substring(pos + 9));
                Double expectedVersion = setupMap.get(script);
                if (expectedVersion > installedVersion) {
                    updateList.add(script);
                }
            } catch (NumberFormatException nfe) {
                log.warning("RemoteServerSetup: Bad response from remote grep comand (NFE parsing version)");
            }
        }
        if (!updateList.isEmpty()) {
            updateMap.put(name, updateList);
            return true;
        } else {
            return false;
        }
    }

}
