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
package org.netbeans.modules.j2ee.persistence.jpqleditor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author sp153251
 */
public class Utils {

    /**
     * test persistence unit and produce properties to sunstitute ee environment
     * with se environment (to execut jpql on se level)
     *
     * @param pe
     * @param pu
     * @param props - map will be filled with properties
     * @return return possible problems (missed paths etc.)
     */
    public static List<String> substitutePersistenceProperties(PersistenceEnvironment pe, PersistenceUnit pu, DatabaseConnection dbconn, Map<String, String> props) {
        final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
        final Provider provider = ProviderUtil.getProvider(pu.getProvider(), pe.getProject());
        ArrayList<String> problems = new ArrayList<String>();
        if (containerManaged) {
            props.put("javax.persistence.provider", provider.getProviderClass());//NOI18N
            props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");//NOI18N
            if (dbconn != null) {
                props.put(provider.getJdbcUrl(), dbconn.getDatabaseURL());
                props.put(provider.getJdbcDriver(), dbconn.getDriverClass());
                props.put(provider.getJdbcUsername(), dbconn.getUser());
                props.put(provider.getJdbcPassword(), dbconn.getPassword());
            } 
        }
        return problems;
    }

    /**
     * create URLs from project base to use as classpath later
     * also adds jdbc and provider f necessary and if available
     * @param pe
     * @param localResourcesURLList
     * @return possible problems (missed paths etc.)
     */
    public static List<String> collectClassPathURLs(PersistenceEnvironment pe, PersistenceUnit pu, DatabaseConnection dbconn, List<URL> localResourcesURLList) {
        final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
        final Provider provider = ProviderUtil.getProvider(pu.getProvider(), pe.getProject());
        ArrayList<String> problems = new ArrayList<String>();
        // Construct custom classpath here.
        localResourcesURLList.addAll(pe.getProjectClassPath(pe.getLocation()));
        localResourcesURLList.add(pe.getLocation().getParent().toURL());
        localResourcesURLList.add(pe.getLocation().toURL());
        localResourcesURLList.add(pe.getLocation().getFileObject("persistence.xml").toURL());
        ClassPath cp = ClassPath.getClassPath(pe.getLocation(), ClassPath.EXECUTE);
        if (cp == null) {
            cp = ClassPath.getClassPath(pe.getLocation(), ClassPath.COMPILE);
        }
        if (containerManaged) {
            String providerClassName = provider.getProviderClass();
            String resourceName = providerClassName.replace('.', '/') + ".class"; // NOI18N
            if (cp != null) {
                FileObject fob = cp.findResource(resourceName); // NOI18N
                if (fob == null) {
                    Library library = PersistenceLibrarySupport.getLibrary(provider);
                    if (library != null) {
                        localResourcesURLList.addAll(library.getContent("classpath"));//NOI18N
                    } else {
                        problems.add(NbBundle.getMessage(Utils.class, "ProviderAbsent"));//NOI18N
                    }
                }
            }
        }
        if (dbconn != null) {
            ////autoadd driver classpath
            String driverClassName = dbconn.getDriverClass();
            String resourceName = driverClassName.replace('.', '/') + ".class"; // NOI18N
            if (cp != null) {
                FileObject fob = cp.findResource(resourceName); // NOI18N
                if (fob == null) {
                    JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(driverClassName);
                    if (driver != null && driver.length > 0) {
                        localResourcesURLList.addAll(Arrays.asList(driver[0].getURLs()));
                    } else {
                        problems.add(NbBundle.getMessage(Utils.class, "DriverAbsent"));//NOI18N
                    }
                }
            }
        } else {
            problems.add(NbBundle.getMessage(Utils.class, "DatabaseConnectionAbsent"));//NOI18N
        }
        return problems;
    }
}
