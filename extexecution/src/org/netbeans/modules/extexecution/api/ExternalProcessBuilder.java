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

package org.netbeans.modules.extexecution.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public final class ExternalProcessBuilder {

    // FIXME: get rid of those proxy constants as soon as some NB Proxy API is available
    private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"; // NOI18N

    private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"; // NOI18N

    private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"; // NOI18N

    private final String command;

    private File pwd;

    private boolean javaHomeToPath;

    private boolean pwdToPath;

    private final List<String> arguments = new ArrayList<String>();

    private final List<File> paths = new ArrayList<File>();

    private final List<String> javaHomeProperties = new ArrayList<String>();

    private final Map<String, String> envVariables = new HashMap<String, String>();

    public ExternalProcessBuilder(String command) {
        this.command = command;
    }

    public ExternalProcessBuilder pwd(File pwd) {
        Parameters.notNull("pwd", pwd);

        this.pwd = pwd;
        return this;
    }

    public ExternalProcessBuilder javaHomeToPath(boolean javaHomeToPath) {
        this.javaHomeToPath = javaHomeToPath;
        return this;
    }

    public ExternalProcessBuilder pwdToPath(boolean pwdTopath) {
        this.pwdToPath = pwdTopath;
        return this;
    }

    public ExternalProcessBuilder addArgument(String argument) {
        Parameters.notNull("arg", argument);

        arguments.add(argument);
        return this;
    }

    // last added is the first one in path
    public ExternalProcessBuilder addPath(File path) {
        Parameters.notNull("path", path);

        paths.add(path);
        return this;
    }

    public ExternalProcessBuilder addEnvironmentVariable(String name, String value) {
        Parameters.notNull("name", name);
        Parameters.notNull("value", value);

        envVariables.put(name, value);
        return this;
    }

    public ExternalProcessBuilder addJavaHomeProperty(String javaHomeProperty) {
        Parameters.notNull("javaHomeProperty", javaHomeProperty);

        javaHomeProperties.add(javaHomeProperty);
        return this;
    }

    public Process create() throws IOException {
        List<String> commandL = new ArrayList<String>();

        commandL.add(command);

        List<String> args = buildArguments();
        commandL.addAll(args);
        String[] command = commandL.toArray(new String[commandL.size()]);

        if ((command != null) && Utilities.isWindows()) {
            for (int i = 0; i < command.length; i++) {
                if ((command[i] != null) && (command[i].indexOf(' ') != -1) &&
                        (command[i].indexOf('"') == -1)) { // NOI18N
                    command[i] = '"' + command[i] + '"'; // NOI18N
                }
            }
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        if (pwd != null) {
            pb.directory(pwd);
        }

        Map<String, String> pbEnv = pb.environment();
        Map<String, String> env = buildEnvironment(pbEnv);
        pbEnv.putAll(env);
        adjustProxy(pb);
        return pb.start();
    }

    // package level for unit testing
    Map<String, String> buildEnvironment(Map<String, String> original) {
        Map<String, String> ret = new HashMap<String, String>(original);
        ret.putAll(envVariables);

        // Find PATH environment variable - on Windows it can be some other
        // case and we should use whatever it has.
        String pathName = "PATH"; // NOI18N

        if (Utilities.isWindows()) {
            pathName = "Path"; // NOI18N

            for (String key : ret.keySet()) {
                if ("PATH".equals(key.toUpperCase(Locale.ENGLISH))) { // NOI18N
                    pathName = key;

                    break;
                }
            }
        }

        // TODO use StringBuilder
        String currentPath = ret.get(pathName);

        if (currentPath == null) {
            currentPath = "";
        }

        for (File path : paths) {
            currentPath = path.getAbsolutePath().replace(" ", "\\ ") //NOI18N
                    + File.pathSeparator + currentPath;
        }

        if (pwdToPath) {
            File path = pwd;
            if (path == null) {
                String userDir = System.getProperty("user.dir");
                if (userDir != null) {
                    path = new File(userDir);
                }
            }
            if (path != null) {
                currentPath = path.getAbsolutePath().replace(" ", "\\ ") // NOI18N
                        + File.pathSeparator + currentPath;
            }
        }

        if (javaHomeToPath) {
            String javaHome = null;
            for (String property : javaHomeProperties) {
                javaHome = System.getProperty(property);
                if (javaHome != null) {
                    break;
                }
            }

            if (javaHome == null) {
                javaHome = System.getProperty("java.home"); // NOI18N
            }

            if (javaHome != null) {
                javaHome = javaHome + File.separator + "bin"; // NOI18N
                if (!Utilities.isWindows()) {
                    javaHome = javaHome.replace(" ", "\\ "); // NOI18N
                }
                currentPath = new File(javaHome).getAbsolutePath() + File.pathSeparator + currentPath;
            }
        }

        if (!"".equals(currentPath.trim())) {
            ret.put(pathName, currentPath);
        }
        return ret;
    }

    private List<String> buildArguments() {
        return new ArrayList<String>(arguments);
    }

    private void adjustProxy(ProcessBuilder pb) {
        String proxy = getNetBeansHttpProxy();
        if (proxy != null) {
            Map<String, String> env = pb.environment();
            if ((env.get("HTTP_PROXY") == null) && (env.get("http_proxy") == null)) { // NOI18N
                env.put("HTTP_PROXY", proxy); // NOI18N
                env.put("http_proxy", proxy); // NOI18N
            }
            // PENDING - what if proxy was null so the user has TURNED off
            // proxies while there is still an environment variable set - should
            // we honor their environment, or honor their NetBeans proxy
            // settings (e.g. unset HTTP_PROXY in the environment before
            // launching plugin?
        }
    }

    /**
     * FIXME: get rid of the whole method as soon as some NB Proxy API is
     * available.
     */
    private static String getNetBeansHttpProxy() {
        String host = System.getProperty("http.proxyHost"); // NOI18N

        if (host == null) {
            return null;
        }

        String portHttp = System.getProperty("http.proxyPort"); // NOI18N
        int port;

        try {
            port = Integer.parseInt(portHttp);
        } catch (NumberFormatException e) {
            port = 8080;
        }

        Preferences prefs = NbPreferences.root().node("org/netbeans/core"); // NOI18N
        boolean useAuth = prefs.getBoolean(USE_PROXY_AUTHENTICATION, false);
        String auth = "";
        if (useAuth) {
            auth = prefs.get(PROXY_AUTHENTICATION_USERNAME, "") + ":" + prefs.get(PROXY_AUTHENTICATION_PASSWORD, "") + '@'; // NOI18N
        }

        // Gem requires "http://" in front of the port name if it's not already there
        if (host.indexOf(':') == -1) {
            host = "http://" + auth + host; // NOI18N
        }

        return host + ":" + port; // NOI18N
    }
}
