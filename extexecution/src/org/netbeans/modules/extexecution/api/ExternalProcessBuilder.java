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
 * Utility class to make the external process creation easier.
 * <p>
 * Builder handle command, working directory, <code>PATH</code> variable and HTTP proxy.
 * <p>
 * This class is <i>not thread safe</i>.
 *
 * @author Petr Hejl
 * @see #create()
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

    /**
     * Creates the new builder that will create the process by executing
     * given commnad.
     *
     * @param command command to execute
     */
    public ExternalProcessBuilder(String command) {
        this.command = command;
    }

    /**
     * Sets this builder's working directory. Process subsequently created
     * by {@link #create()} method will be executed with this directory
     * as current working dir.
     * <p>
     * Note that each process has always working directory even when not
     * configured explicitly (the value of <code>user.dir</code> property).
     *
     * @param pwd working directory, <code>null</code> allowed
     * @return this process builder
     */
    public ExternalProcessBuilder pwd(File pwd) {
        this.pwd = pwd;
        return this;
    }

    /**
     * Configures whether the working directory should be part of
     * process's <code>PATH</code> environment variable.
     * <p>
     * If passed value is <code>true</code> working directory is added to the
     * <code>PATH</code> variable. For <code>PATH</code> variable construction
     * see {@link #create()}.
     *
     * @param pwdTopath if <code>true</code> working directory is added to the
     *             <code>PATH</code> variable
     * @return this process builder
     */
    public ExternalProcessBuilder pwdToPath(boolean pwdTopath) {
        this.pwdToPath = pwdTopath;
        return this;
    }

    /**
     * Configures the additional property where to find java installation
     * in order to add its bin to <code>PATH</code> (if configured to do so by
     * {@link #javaHomeToPath(boolean)}. <code>java.home</code> is always
     * searched as default fallback.
     *
     * @param javaHomeProperty name of the property where to search for
     *             java isntallation
     * @return this process builder
     * @see #javaHomeToPath(boolean)
     */
    public ExternalProcessBuilder addJavaHomeProperty(String javaHomeProperty) {
        Parameters.notNull("javaHomeProperty", javaHomeProperty);

        javaHomeProperties.add(javaHomeProperty);
        return this;
    }

    /**
     * Configures whether the java installation's bin dir should be part of
     * process's <code>PATH</code> environment variable.
     * <p>
     * If passed value is <code>true</code> the java installation's bin is
     * added to the <code>PATH</code> variable. For <code>PATH</code>
     * variable construction see {@link #create()}.
     *
     * @param javaHomeToPath if <code>true</code>the java installation's bin
     *             is added to the <code>PATH</code> variable
     * @return this process builder
     */
    public ExternalProcessBuilder javaHomeToPath(boolean javaHomeToPath) {
        this.javaHomeToPath = javaHomeToPath;
        return this;
    }

    /**
     * Configures the additional path to add to the <code>PATH</code> variable.
     * <p>
     * In the group of paths added by this call the last added path will
     * be the first one in the <code>PATH</code> variable.
     *
     * @param path path to add to <code>PATH</code> variable
     * @return this process builder
     */
    public ExternalProcessBuilder addPath(File path) {
        Parameters.notNull("path", path);

        paths.add(path);
        return this;
    }

    /**
     * Configures the additional argument for the command. Arguments are added
     * in the same order in which they are added.
     *
     * @param argument command argument to add
     * @return this process builder
     */
    public ExternalProcessBuilder addArgument(String argument) {
        Parameters.notNull("arg", argument);

        arguments.add(argument);
        return this;
    }

    /**
     * Configures the additional environment variable for the command.
     *
     * @param name name of the variable
     * @param value value of the variable
     * @return this process builder
     * @see #create()
     */
    public ExternalProcessBuilder addEnvironmentVariable(String name, String value) {
        Parameters.notNull("name", name);
        Parameters.notNull("value", value);

        envVariables.put(name, value);
        return this;
    }

    /**
     * Creates the new {@link Process} based on the properties configured
     * in this builder.
     * <p>
     * Process is created by executing the command with configured arguments.
     * If custom working directory is specified it is used otherwise value
     * of system property <code>user.dir</code> is used as working dir.
     * <p>
     * Environment variables are prepared in following way:
     * <ol>
     *   <li>Get table of system environment variables.
     *   <li>Put all environment variables configured by
     * {@link #addEnvironmentVariable(java.lang.String, java.lang.String)}.
     * This rewrites system variables if conflict occurs.
     *   <li>Get <code>PATH</code> variable and append all paths added
     * by {@link #addPath(java.io.File)}. The order of paths in <code>PATH</code>
     * variable is reversed to order of addition (the last added is the first
     * one in <code>PATH</code>). Original content of <code>PATH</code> follows
     * the added content.
     *   <li>If builder is configured to add working directory to <code>PATH</code>
     * the working directory is placed to the beginning of the <code>PATH</code>.
     *   <li>If builder is configured to add java installation bin directory to
     * <code>PATH</code>:
     * 
     *     <ol>
     *       <li>Ask system for value of each property configured by
     *     {@link #addJavaHomeProperty(java.lang.String)} (in order in which
     *     these were added).
     *       <li>If there is corresponding value this value with appended bin
     *     diretory is placed at the first place in <code>PATH</code> and no
     *     further values are investigated.
     *       <li>If no value for java installation is found <code>java.home</code>
     *     is used as a fallback. And the result (if any) is placed at the first
     *     place in <code>PATH</code>.
     *     </ol>
     *   <li>HTTP proxy settings are configured (http.proxyHost and http.proxyPort
     * variables).
     * </ol>
     * @return the new {@link Process} based on the properties configured
     *             in this builder
     */
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
        // FIXME use ProxySelector

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
