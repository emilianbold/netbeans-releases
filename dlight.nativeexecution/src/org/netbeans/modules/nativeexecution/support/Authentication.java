/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.util.prefs.Preferences;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbPreferences;

/**
 *
 * @author ak119685
 */
public final class Authentication {

    private static final Preferences prefs = NbPreferences.forModule(Authentication.class);
    private static final boolean isUnitTest = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    private static final String knownHostsFile;
    private static String lastSSHKeyFile;
    private final ExecutionEnvironment env;
    private final String pref_key;
    private String sshKeyFile;
    private Type type = Type.UNDEFINED;

    static {
        String hosts = System.getProperty("ssh.knonwhosts.file", null); // NOI18N

        if (hosts == null || !isValidKnownHostsFile(hosts)) {
            hosts = System.getProperty("user.home") + "/.ssh/known_hosts"; // NOI18N
            if (!isValidKnownHostsFile(hosts)) {
                hosts = System.getProperty("netbeans.user") + "/.ssh/known_hosts"; // NOI18N
                if (!isValidKnownHostsFile(hosts)) {
                    hosts = null;
                }
            }
        }

        knownHostsFile = hosts;

        String key = System.getProperty("user.home") + "/.ssh/id_dsa"; // NOI18N

        if (!isValidSSHKeyFile(key)) {
            key = System.getProperty("user.home") + "/.ssh/id_rsa"; // NOI18N
            if (!isValidSSHKeyFile(key)) {
                key = null;
            }
        }

        lastSSHKeyFile = key;
    }

    private Authentication(ExecutionEnvironment env) {
        pref_key = env == null ? null : Authentication.class.getName() + '_' + ExecutionEnvironmentFactory.toUniqueID(env);
        this.env = env;
    }

    public static Authentication getFor(ExecutionEnvironment env) {
        Authentication result = new Authentication(env);
        result.restore();

        if (isUnitTest) {
            result.setPassword();
        } else {
            if (result.sshKeyFile == null || result.sshKeyFile.trim().length() == 0) {
                result.sshKeyFile = lastSSHKeyFile;
            }
        }

        return result;
    }

    public boolean isDefined() {
        return type != Type.UNDEFINED;
    }

    public void setPassword() {
        if (type == Type.PASSWORD) {
            return;
        }

        type = Type.PASSWORD;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public void setSSHKeyFile(String filename) throws IllegalArgumentException {
        if (!isValidSSHKeyFile(filename)) {
            throw new IllegalArgumentException("Invalid ssh key file " + filename); // NOI18N
        }

        type = type.SSH_KEY;
        sshKeyFile = filename;
    }

    public static boolean isValidSSHKeyFile(String filename) {
        JSch test = new JSch();

        try {
            test.addIdentity(filename);
        } catch (JSchException ex) {
            return false;
        }

        return true;
    }

    private static boolean isValidKnownHostsFile(String knownHostsFile) {
        JSch test = new JSch();

        try {
            test.setKnownHosts(knownHostsFile);
        } catch (JSchException ex) {
            return false;
        }

        return true;
    }

    public Type getType() {
        return type;
    }

    public void store() {
        if (env == null) {
            return;
        }

        if (type == Type.SSH_KEY) {
            prefs.put(pref_key, sshKeyFile);
            lastSSHKeyFile = sshKeyFile;
        } else {
            prefs.put(pref_key, type.name());
        }
    }

    private void restore() {
        if (env == null) {
            return;
        }

        String typeOrKey = prefs.get(pref_key, Type.UNDEFINED.name());

        if (Type.UNDEFINED.name().equals(typeOrKey)) {
            type = Type.UNDEFINED;
        } else if (Type.PASSWORD.name().equals(typeOrKey)) {
            type = Type.PASSWORD;
        } else {
            if (isValidSSHKeyFile(typeOrKey)) {
                type = Type.SSH_KEY;
                sshKeyFile = typeOrKey;
            } else {
                type = Type.UNDEFINED;
            }
        }

    }

    public ExecutionEnvironment getEnv() {
        return env;
    }

    public void remove() {
        if (pref_key != null) {
            prefs.remove(pref_key);
        }
    }

    public void apply() {
        if (env == null) {
            return;
        }

        store();
        ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();
        access.changeAuth(env, this);
    }

    public String getKey() {
        return getSSHKeyFile();
    }

    public String getSSHKeyFile() {
        return sshKeyFile;
    }

    public enum Type {

        UNDEFINED,
        PASSWORD,
        SSH_KEY
    }
}
