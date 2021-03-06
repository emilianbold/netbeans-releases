/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.NbPreferences;

/**
 *
 * @author vk155633
 */
/*package*/ class HostInfoCache implements ConnectionListener {

    private static final HostInfoCache  INSTANCE = new HostInfoCache();

    private static final String KEY_USERID = "userId"; //NOI18N
    private static final String KEY_GRPID = "groupId"; //NOI18N
    private static final String KEY_GROUPS = "allGroups"; //NOI18N

    private final Preferences preferences;

    public static HostInfoCache getInstance() {
        return INSTANCE;
    }

    /*package*/ static void initializeIfNeeded() {
        getInstance();
    }

    private HostInfoCache() {
        this.preferences = NbPreferences.forModule(HostInfoCache.class);
        ConnectionManager.getInstance().addConnectionListener(this);
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                preferences.putInt(getKey(KEY_USERID, env), hostInfo.getUserId());
                preferences.putInt(getKey(KEY_GRPID, env), hostInfo.getGroupId());
                preferences.put(getKey(KEY_GROUPS, env), toString(hostInfo.getAllGroupIDs()));

            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } catch (CancellationException ex) {
                // never report CancellationException
            }
        } else {
            Logger.getInstance().log(Level.WARNING,
                    "HostInfo should be available for {0} at this point", //NOI18N
                    new Object[] {env}); 
        }
    }

    private String toString(int[] array) {
        StringBuilder sb = new StringBuilder();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    sb.append(','); //NOI18N
                }
                sb.append(array[i]);
            }
        }
        return sb.toString();
    }

    private int[] fromString(String text) {
        if (text != null) {
            String[] split = text.trim().split(","); //NOI18N
            int[] tmp = new int[split.length];
            int cnt = 0;
            for (int i = 0; i < split.length; i++) {
                if (split[i].length() > 0 && Character.isDigit(split[i].charAt(0))) {
                    try {
                        tmp[cnt++] = Integer.parseInt(split[i]);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                }
            }
            if (cnt == tmp.length) {
                return tmp;
            } else {
                int[] result = new int[cnt];
                System.arraycopy(tmp, 0, result, 0, cnt);
                return result;
            }
        }
        return new int[0];
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }

    private String getKey(String key, ExecutionEnvironment env) {
        return ExecutionEnvironmentFactory.toUniqueID(env) + '_' + key;
    }

    public int getUserId(ExecutionEnvironment env) {
        return preferences.getInt(getKey(KEY_USERID, env), -1);
    }

    public int getGroupId(ExecutionEnvironment env) {
        return preferences.getInt(getKey(KEY_GRPID, env), -1);
    }

    public int[] getAllGroupIDs(ExecutionEnvironment env) {
        return fromString(preferences.get(getKey(KEY_GROUPS, env), "")); //NOI18N
    }
}
