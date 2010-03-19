/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby.platform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;

public final class Util {

    /**
     * Regexp for matching version number in gem packages:  name-x.y.z (we need
     * to pull out x,y,z such that we can do numeric comparisons on them)
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+)\\.?(\\w+)?(-\\S+)?)?"); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    // FIXME: get rid of those proxy constants as soon as some NB Proxy API is available
    private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"; // NOI18N
    private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"; // NOI18N
    private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"; // NOI18N

    private static final String FIRST_TIME_KEY = "platform-manager-called-first-time"; // NOI18N
    private static final String FETCH_ALL_VERSIONS = "gem-manager-fetch-all-versions"; // NOI18N
    private static final String FETCH_GEM_DESCRIPTIONS = "gem-manager-fetch-descriptions"; // NOI18N

    public static final Comparator<String> VERSION_COMPARATOR = new Comparator<String>() {
        public int compare(String v1, String v2) {
            return Util.compareVersions(v1, v2);
        }
    };

    private Util() {
    }

    /** Return true iff the given line seems to be colored using ANSI terminal escape codes */
    public static boolean containsAnsiColors(String line) {
        // RSpec will color output with ANSI color sequence terminal escapes
        return line.indexOf("\033[") != -1; // NOI18N
    }

    /**
     * Remove ANSI terminal escape codes from a line.
     */
    public static String stripAnsiColors(String line) {
        StringBuilder sb = new StringBuilder(line.length());
        int index = 0;
        int max = line.length();
        while (index < max) {
            int nextEscape = line.indexOf("\033[", index); // NOI18N
            if (nextEscape == -1) {
                nextEscape = line.length();
            }

            for (int n = (nextEscape == -1) ? max : nextEscape; index < n; index++) {
                sb.append(line.charAt(index));
            }

            if (nextEscape != -1) {
                for (; index < max; index++) {
                    char c = line.charAt(index);
                    if (c == 'm') {
                        index++;
                        break;
                    }
                }
            }
        }

        return sb.toString();
    }

    public static void adjustProxy(final ProcessBuilder pb) {
        String proxy = Util.getNetBeansHttpProxy();
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

    /**
     * Returns an {@link Iterable} which will uniquely traverse all valid
     * elements on the <em>PATH</em> environment variables. That means,
     * duplicates and elements which are not valid, existing directories are
     * skipped.
     *
     * @return an {@link Iterable} which will traverse all valid elements on the
     * <em>PATH</em> environment variables.
     */
    public static Iterable<String> dirsOnPath() {
        String rawPath = System.getenv("PATH"); // NOI18N
        if (rawPath == null) {
            rawPath = System.getenv("Path"); // NOI18N
        }
        if (rawPath == null) {
            return Collections.emptyList();
        }
        Set<String> candidates = new LinkedHashSet<String>(Arrays.asList(rawPath.split(File.pathSeparator)));
        for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
            String dir = it.next();
            if (!new File(dir).isDirectory()) { // remove non-existing directories (#124562)
                LOGGER.fine(dir + " found in the PATH environment variable. But is not a valid directory. Ignoring...");
                it.remove();
            }
        }
        return NbCollections.iterable(candidates.iterator());
    }

    public static String findOnPath(final String toFind) {
        for (String path : Util.dirsOnPath()) {
            String result = path + File.separator + toFind;
            if (new File(result).isFile()) {
                return result;
            }
        }
        return null;
    }

    public static void preselectPlatform(final JComboBox platforms, final String preferencePlatformIDKey) {
        String lastPlatformID = RubyPreferences.getPreferences().get(preferencePlatformIDKey, null);
        if (lastPlatformID != null) {
            RubyPlatform platform = RubyPlatformManager.getPlatformByID(lastPlatformID);
            if (platform != null) {
                platforms.setSelectedItem(platform);
            }
        }
    }

    public static void notifyLocalized(Class aClass, String resName, int type, Object... params) {
        String message = NbBundle.getMessage(aClass, resName, params);
        if (type == NotifyDescriptor.ERROR_MESSAGE) {
            LOGGER.severe(message);
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, type));
    }

    public static void notifyLocalized(Class aClass, String resName, Object... params) {
        notifyLocalized(aClass, resName, NotifyDescriptor.INFORMATION_MESSAGE, params);
    }

    /** Returns whether the user confirmed the question or not. */
    public static boolean confirmLocalized(Class aClass, String resName, Object... params) {
        String message = NbBundle.getMessage(aClass, resName, params);
        Object result = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, NotifyDescriptor.Confirmation.OK_CANCEL_OPTION));
        return result.equals(NotifyDescriptor.OK_OPTION);
    }

    public static String readAsString(final InputStream is) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8"); // NOI18N
        } finally {
            is.close();
        }
    }

    /**
     * Return &gt; 1 if <code>version1</code> is greater than
     * <code>version2</code>, 0 if equal and -1 otherwise.
     */
    public static int compareVersions(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }

        Matcher matcher1 = VERSION_PATTERN.matcher(version1);

        if (matcher1.matches()) {
            int major1 = Integer.parseInt(matcher1.group(1));
            int minor1 = Integer.parseInt(matcher1.group(2));
            int micro1 = matcher1.group(4) == null ? 0 : Integer.parseInt(matcher1.group(4));
            // e.g. beta, as in rails-3.0.0.beta
            String suffix1 = matcher1.group(5);

            Matcher matcher2 = VERSION_PATTERN.matcher(version2);

            if (matcher2.matches()) {
                int major2 = Integer.parseInt(matcher2.group(1));
                int minor2 = Integer.parseInt(matcher2.group(2));
                int micro2 = matcher2.group(4) == null ? 0 : Integer.parseInt(matcher2.group(4));
                String suffix2 = matcher2.group(5);


                if (major1 != major2) {
                    return major1 - major2;
                }

                if (minor1 != minor2) {
                    return minor1 - minor2;
                }

                if (micro1 != micro2) {
                    return micro1 - micro2;
                }
                if (suffix1 == null) {
                    return 1;
                }
                if (suffix2 == null) {
                    return -1;
                }
                //  do just alphabetical comparison on suffix, stupid but
                // covers the most common cases, e.g. alpha < beta
                return suffix1.compareTo(suffix2);
            } else {
                // TODO uh oh
                //assert false : "no version match on " + version2;
            }
        } else {
            // TODO assert false : "no version match on " + version1;
        }

        // Just do silly alphabetical comparison
        return version1.compareTo(version2);
    }
}
