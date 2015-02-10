/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.common.ant;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.Project;

/**
 *
 * @author rsvitanic
 */
public class LibletUtils {

    /**
     * Gets all attributes from JAR's manifest.
     *
     * @param path the path to the JAR file.
     * @return a map of all attributes in the JAR's manifest.
     */
    public static Map<Object, Object> getJarManifestAttributes(String path) {
        JarFile jar = null;
        try {
            jar = new JarFile(path);
            Attributes manifestAttributes = jar.getManifest().getMainAttributes();
            return manifestAttributes;
        } catch (IOException ex) {
            Logger.getLogger(ExtractTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ex) {
                    Logger.getLogger(ExtractTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * Determines whether the given JAR is a LIBlet.
     *
     * @param manifestAttributes the map of JAR file's attributes.
     * @return {@code true} if the JAR file is a LIBlet, {@code false}
     * otherwise.
     */
    public static boolean isJarLiblet(Map<Object, Object> manifestAttributes) {
        return manifestAttributes.containsKey(new Attributes.Name("LIBlet-Name")); //NOI18N            
    }

    /**
     * Gets the string containing LIBlet attributes. These attributes are
     * LIBlet-Name, LIBlet-Vendor and LIBlet-Version. Attributes are separated
     * by semicolons.
     *
     * @param manifestAttributes the map of JAR file's attributes.
     * @return a string with information about the LIBlet.
     */
    public static String getLibletDetails(Map<Object, Object> manifestAttributes) {
        return (String) manifestAttributes.get(new Attributes.Name("LIBlet-Name")) //NOI18N            
                + ";"
                + (String) manifestAttributes.get(new Attributes.Name("LIBlet-Vendor")) //NOI18N            
                + ";"
                + (String) manifestAttributes.get(new Attributes.Name("LIBlet-Version")); //NOI18N
    }

    /**
     * Gets all LIBlets from the project properties. Returns a map where key is
     * the LIBlet information string and value is {@code true} if LIBlet classes
     * should be extracted in the Java ME application's JAR or {@code false}
     * otherwise.
     *
     * @param p the project to load LIBlets for.
     * @return a map of LIBlets in the project.
     */
    public static Map<String, Boolean> loadLibletsInProject(Project p) {
        final Map<String, Boolean> libletsInProject = new HashMap<String, Boolean>();
        for (int i = 0;; i++) {
            final String val = p.getProperty("liblets." + i + ".dependency"); //NOI18N
            if (val == null) {
                break;
            }
            final String[] fields = val.split(";"); //NOI18N
            if (fields.length >= 5 && fields[0].equalsIgnoreCase("liblet")) { //NOI18N
                final String extractVal = p.getProperty("liblets." + i + ".extract"); //NOI18N
                libletsInProject.put(fields[2] + ";" + fields[3] + ";" + fields[4], Boolean.parseBoolean(extractVal)); //NOI18N
            }
        }
        return libletsInProject;
    }
}
