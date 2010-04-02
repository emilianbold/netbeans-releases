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
package org.netbeans.modules.nativeexecution.api.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class PasswordManager {

    private static final boolean keepPasswordsInMemory = true;
    private static final String KEY_PREFIX = "remote.user.info.password."; // NOI18N
    private static final String STORE_PREFIX = "remote.user.info.store."; // NOI18N
    private final Map<String, String> cache = Collections.synchronizedMap(new HashMap<String, String>());

    private static PasswordManager instance = new PasswordManager();

    private PasswordManager() {
    }

    public static PasswordManager getInstance() {
        return instance;
    }

    /**
     *
     * @param execEnv
     * @return password from memory or from Keyring if user selected "remember password" in previous IDE invocation
     */
    public char[] get(ExecutionEnvironment execEnv) {
        String key = execEnv.toString();
        if (keepPasswordsInMemory) {
            String cachedPassword = cache.get(key);
            if (cachedPassword != null) {
                return cachedPassword.toCharArray();
            }
        }
        boolean stored = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        if (stored) {
            char[] keyringPassword = Keyring.read(KEY_PREFIX + key);
            if (keepPasswordsInMemory && keyringPassword != null) {
                 cache.put(key, String.valueOf(keyringPassword));
            }
            return keyringPassword;
        }
        return null;
    }

    /**
     * Store password in memory. If user select "remember password" option password is stored in Keyring.
     *
     * @param execEnv
     * @param password
     * @param rememberPassword
     */
    public void storePassword(ExecutionEnvironment execEnv, char[] password, boolean rememberPassword) {
        setRememberPassword(execEnv, rememberPassword);
        put(execEnv, password);
    }

    /**
     * Update password in memory. If user selected "remember password" option before password is updated in Keyring.
     *
     * @param execEnv
     * @param password
     */
    private void put(ExecutionEnvironment execEnv, char[] password) {
        String key = execEnv.toString();
        if (keepPasswordsInMemory) {
            if (password != null) {
                cache.put(key, String.valueOf(password));
            } else {
                cache.put(key, null);
            }
        }
        boolean store = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        if (store) {
            Keyring.save(KEY_PREFIX + key, password,
                    NbBundle.getMessage(PasswordManager.class, "PasswordManagerPasswordFor",execEnv.getDisplayName())); // NOI18N
        }
    }

    /**
     * Remove password from memory and Keyring
     *
     * @param execEnv
     */
    public void clearPassword(ExecutionEnvironment execEnv) {
        String key = execEnv.toString();
        if (keepPasswordsInMemory) {
            cache.remove(key);
        }
        NbPreferences.forModule(PasswordManager.class).remove(STORE_PREFIX + key);
        Keyring.delete(KEY_PREFIX + key);
    }

    /**
     * Remove passwords for hosts that are not in list.
     *
     * @param envs
     */
    public void setServerList(List<ExecutionEnvironment> envs) {
        Set<String> keys = new HashSet<String>();
        for(ExecutionEnvironment env : envs) {
            String key = env.toString();
            keys.add(KEY_PREFIX+key);
            keys.add(STORE_PREFIX+key);
        }
        try {
            String[] allKeys = NbPreferences.forModule(PasswordManager.class).keys();
            for (String aKey : allKeys) {
                if (!keys.contains(aKey)){
                    if (aKey.startsWith(STORE_PREFIX)) {
                        Keyring.delete(KEY_PREFIX+aKey.substring(STORE_PREFIX.length()));
                        if (keepPasswordsInMemory) {
                            cache.remove(aKey.substring(STORE_PREFIX.length()));
                        }
                    }
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * User intention of "remember password"
     * @param execEnv
     * @return true if user checked "remember password" option.
     */
    public boolean isRememberPassword(ExecutionEnvironment execEnv){
        String key = execEnv.toString();
        boolean stored = NbPreferences.forModule(PasswordManager.class).getBoolean(STORE_PREFIX + key, false);
        return stored;
    }

    /**
     * Store user intention of "remember password"
     * @param execEnv
     * @param rememberPassword
     */
    public void setRememberPassword(ExecutionEnvironment execEnv, boolean rememberPassword) {
        String key = execEnv.toString();
        if (!rememberPassword) {
            Keyring.delete(KEY_PREFIX + key);
        }
        NbPreferences.forModule(PasswordManager.class).putBoolean(STORE_PREFIX + key, rememberPassword);
    }
}
