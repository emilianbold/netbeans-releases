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
package org.netbeans.modules.coherence.server.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.server.CoherenceModuleProperties;
import org.netbeans.modules.coherence.server.CoherenceProperties;

/**
 * Holds helper methods for Coherence server classpath property.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ClasspathPropertyUtils {

    private ClasspathPropertyUtils() {
    }

    /**
     * Converts one long string which represents additional classpath into {@code
     * String[]}.
     *
     * @param classpath {@code String} consists from all classpaths
     * @return resulting {@code String[]}
     */
    public static String[] classpathFromStringToArray(String classpath) {
        if ("".equals(classpath.trim())) { //NOI18N
            return new String[0];
        }
        return classpath.split(CoherenceModuleProperties.CLASSPATH_SEPARATOR);

    }

    /**
     * Converts {@code List} of {@code Strings} into one long {@code String} for
     * storing that into {@link InstanceProperties}.
     *
     * @param classpaths {@code List} of all classpath entries
     * @return resulting {@code String}
     */
    public static String classpathFromListToString(List<String> classpaths) {
        StringBuilder sb = new StringBuilder();
        for (String cp : classpaths) {
            sb.append(cp).append(CoherenceModuleProperties.CLASSPATH_SEPARATOR);
        }
        String resultString = sb.toString();
        if (resultString.length() == 0) {
            return ""; //NOI18N
        }
        return resultString.substring(0, resultString.length() - CoherenceModuleProperties.CLASSPATH_SEPARATOR.length());
    }

    /**
     * Gets full absolute path to given Coherence library jar.
     * @param serverRoot root dir of Coherence server
     * @param jarName Coherence library jar
     * @return full path
     */
    public static String getAbsolutePath(String serverRoot, String jarName) {
        StringBuilder sb = new StringBuilder();
        sb.append(serverRoot);  //NOI18N
        sb.append(File.separator);
        sb.append(CoherenceProperties.PLATFORM_LIB_DIR);
        sb.append(File.separator);
        sb.append(jarName);
        return sb.toString();
    }

    /**
     * Gets whether given jar is one of Coherence server library jars.
     * @param jarPath absolute path to the jar
     * @param includeBaseJar whether to take coherence.jar as a library jar or not
     * @return {@code true} if the jar is one of Coherence server jars, {@code false} otherwise
     */
    public static  boolean isCoherenceServerJar(String jarPath, boolean includeBaseJar) {
        List<String> jars = new ArrayList<String>(ClasspathTable.COHERENCE_SERVER_JARS);
        if (includeBaseJar) {
            jars.add(CoherenceProperties.COHERENCE_JAR_NAME);
        }

        for (String serverJar : jars) {
            if (jarPath.contains(serverJar)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether given jar is one of Coherence server library jars.
     * @param jarPath absolute path to the jar
     * @return {@code true} if the jar is one of Coherence server jars, {@code false} otherwise
     */
    public static boolean isCoherenceServerJar(String jarPath) {
        return isCoherenceServerJar(jarPath, false);
    }

    /**
     * Saves a new classpath into properties.
     *
     * @param classpath original classpath property value
     * @param additionalCp jars from additional classpath (absolute path)
     * @param coreCp core Coherence libraries jars (absolute path)
     * @return classpath value updated about new CP entries
     */
    public static String getUpdatedClasspath(String classpath, String[] additionalCp, String[] coreCp) {
        assert additionalCp != null || coreCp != null;
        List<String> newCp = new ArrayList<String>();
        String[] oldCp = ClasspathPropertyUtils.classpathFromStringToArray(classpath);

        // core Coherence jars
        if (coreCp == null) {
            for (String cpEntry : oldCp) {
                if (ClasspathPropertyUtils.isCoherenceServerJar(cpEntry, true)) {
                    newCp.add(cpEntry);
                }
            }
        } else {
            newCp.addAll(Arrays.asList(coreCp));
        }

        // additional jars
        if (additionalCp == null) {
            for (String cpEntry : oldCp) {
                if (!ClasspathPropertyUtils.isCoherenceServerJar(cpEntry)) {
                    newCp.add(cpEntry);
                }
            }
        } else {
            newCp.addAll(Arrays.asList(additionalCp));
        }

        StringBuilder sb = new StringBuilder();
        for (String cpEntry : newCp) {
            if (sb.length() != 0) {
                sb.append(CoherenceModuleProperties.CLASSPATH_SEPARATOR);
            }
            sb.append(cpEntry);
        }
        return sb.toString();
    }

}
