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

package org.netbeans.modules.cnd.remote.mapper;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;

/**
 *
 * @author gordonp
 */
public class RemoteHostInfoProvider extends HostInfoProvider {
    
    @Override
    public PathMap getMapper(String key) {
        return RemotePathMap.getMapper(key);
    }

    // TODO: can be wrong in imaginary situations user has some tool in PATH he forgot to add and want's
    // to fix this on the fly. He must rerun IDE in this case. And we save 5-10 ssh calls at each routine.
    private final Map<String, Map<String, String>> envCache = new HashMap<String, Map<String, String>>();

    @Override
    public synchronized Map<String, String> getEnv(String key) {
        Map<String, String> map = envCache.get(key);
        if (map == null) {
            map = new HashMap<String, String>();
            RemoteCommandSupport support = new RemoteCommandSupport(key, "PATH=/bin:/usr/bin; env"); // NOI18N
            if (support.getExitStatus() == 0) {
                String val = support.toString();
                String[] lines = val.split("\n"); // NOI18N
                for (int i = 0; i < lines.length; i++) {
                    int pos = lines[i].indexOf('=');
                    if (pos > 0) {
                        map.put(lines[i].substring(0, pos), lines[i].substring(pos+1));
                    }
                }
            }
            envCache.put(key, map);
        }
        
        return map;
    }

    @Override
    public boolean fileExists(String key, String path) {
        RemoteCommandSupport support = new RemoteCommandSupport(key, 
                "/usr/bin/test -d \"" + path + "\" -o -f \"" + path + "\""); // NOI18N
        return support.getExitStatus() == 0;
    }
}
