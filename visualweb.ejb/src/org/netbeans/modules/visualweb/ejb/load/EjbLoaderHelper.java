/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
/*
 * EjbLoaderHelper.java
 *
 * Created on March 9, 2005, 10:10 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.util.JarExploder;

/**
 * 
 * @author cao
 */
public class EjbLoaderHelper {

    public EjbLoaderHelper() {
    }

    public static URLClassLoader getEjbGroupClassLoader(EjbGroup ejbGroup) {

        try {
            // The class path has the client jar files plus Java EE classes which contain the
            // classes/interfaces for EJBs

            ArrayList<URL> classPathUrls = new ArrayList<URL>();

            // Client jars
            List<String> jarFileNames = ejbGroup.getClientJarFiles();
            for (String fileName : jarFileNames) {
                URL url = new File(fileName).toURI().toURL();
                classPathUrls.add(url);
            }

            List<File> containerJars = getJavaEEClasspathEntries();
            for (File file : containerJars) {
                URL url = file.toURI().toURL();
                classPathUrls.add(url);
            }

            URL[] classPathArray = classPathUrls.toArray(new URL[classPathUrls.size()]);
            URLClassLoader classloader = URLClassLoader.newInstance(classPathArray);
            return classloader;
        } catch (java.net.MalformedURLException e) {
            // Log error
            String logMsg = "Error occurred when trying load classes from "
                    + ejbGroup.getClientJarFiles().toString();

            e.printStackTrace();

            return null;
        }
    }

    /**
     * Looks through all installed servers and looks for one that supports EJBs which typically is a
     * Java EE container. It then returns a list of classpath entries represented by File-s. A File
     * is typically a jar file but could also be a directory.
     * 
     * @return List of classpath entries
     */
    public static List<File> getJavaEEClasspathEntries() {
        for (String serverInstanceID : Deployment.getDefault().getServerInstanceIDs()) {
            String displayName = Deployment.getDefault().getServerInstanceDisplayName(
                    serverInstanceID);
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            if (displayName != null && j2eePlatform != null
                    && j2eePlatform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
                File[] classpath = j2eePlatform.getClasspathEntries();
                return Arrays.asList(classpath);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns true iff an action should be enabled if that action depends on access to EJB API
     * classes. In NB6, these classes come from an installed app server that is separate from the
     * IDE itself.
     * 
     * @return
     */
    public static boolean isEnableAction() {
        return !getJavaEEClasspathEntries().isEmpty();
    }

    public static ArrayList getAllClazz(EjbGroup ejbGroup) {

        try {

            ArrayList allClazz = new ArrayList();

            for (Iterator<String> iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext();) {
                String jarFile = iter.next();
                allClazz.addAll(JarExploder.getAllClasses(jarFile));
            }

            // Remove the invalid ones
            // URLClassLoader classloader = getEjbGroupClassLoader( ejbGroup );
            for (Iterator iter = allClazz.iterator(); iter.hasNext();) {
                String className = (String) iter.next();

                // No internal classes for now
                if (className.indexOf('$') != -1) {
                    iter.remove();
                    continue;
                }

                /*
                 * try { Class.forName( className, false, classloader ); } catch(
                 * java.lang.ClassNotFoundException e ) { iter.remove(); }
                 */
            }

            return allClazz;
        } catch (Exception e) {
            e.printStackTrace();

            return new ArrayList();
        }
    }
}
