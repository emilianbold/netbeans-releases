/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
/**
 *
 * @author  Winston Prakash
 */

package org.netbeans.modules.visualweb.dataconnectivity;

import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DerbyWaiter;
import org.openide.modules.ModuleInstall;

/**
 * Initialization code for dataconnectivity module.
 * Copies various files from application bundle into userdir.
 * Defines initial drivers.
 */
public class DataconnectivityModuleInstaller extends ModuleInstall {

    private static String JSFCL_DATA_BEANINFO_PATH = "com.sun.jsfcl.data"; //NOI18N
    private static String DATACONNECTIVITY_BEANINFO_PATH 
        = "org.netbeans.modules.visualweb.dataconnectivity.designtime"; // NOI18N

    @Override
    public void restored() {
        // initialize settings for data source naming option
        DataconnectivitySettings.getInstance() ;
        
        //!JK Temporary place to register the rowset customizer.  This call may change.
        //!JK See Carl for details.
        //!JK Also, temporary place to add JSFCL_DATA_BEANINFO_PATH to the beanInfoSearchPath
        org.netbeans.modules.visualweb.insync.live.LiveUnit.registerCustomizer(
                com.sun.sql.rowset.CachedRowSetXImpl.class.getName(),
                new org.netbeans.modules.visualweb.dataconnectivity.customizers.SqlCommandCustomizer(com.sun.sql.rowset.CachedRowSetXImpl.class.getName()));
              
        // data source tracking for NB4 JsfProjects - do prelim setup.
        ProjectDataSourceTracker.getInstance() ;
        
        List bisp = Arrays.asList(Introspector.getBeanInfoSearchPath());
        if (!bisp.contains(JSFCL_DATA_BEANINFO_PATH)) {
            bisp = new ArrayList(bisp);
            bisp.add(JSFCL_DATA_BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath((String[])bisp.toArray(new String[0]));
        }

        // Register the designtime directory for dataconnectivity in the search path
        if (!bisp.contains(DATACONNECTIVITY_BEANINFO_PATH)) {
            bisp = new ArrayList(bisp);
            bisp.add(DATACONNECTIVITY_BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath((String[])bisp.toArray(new String[0]));
        }                                      
        
        // Create sample database and connections if needed
        new DerbyWaiter(false);  
    }   
}
