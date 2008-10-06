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
package org.netbeans.performance.enterprise;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.actions.AddNewBpelProcess;
import org.netbeans.performance.enterprise.actions.CreateCompositeApplication;
import org.netbeans.performance.enterprise.actions.WatchProjects;

public class MeasureEnterpriseGCTest extends NbTestCase {

    public MeasureEnterpriseGCTest(String name) {
        super(name);
    }

    public static Test suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("Enterprise Performance GC suite");
        System.setProperty("suitename", "org.netbeans.performance.enterprise.MeasureEnterpriseGCTest");


        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(WatchProjects.class)
                .addTest(WatchProjects.class, "testInitGCProjects")
                .addTest(AddNewBpelProcess.class, "measureTime")

                // TODO: Uncomment once issue 138456 is fixed
                //.addTest(CreateBPELmodule.class, "measureTime")
                
                .addTest(CreateCompositeApplication.class, "measureTime")    
//                .addTest(OpenSchemaView.class, "testGCwithOpenComplexSchemaView")    
                .addTest(WatchProjects.class, "testGCProjects")
                .enableModules(".*").clusters(".*").reuseUserDir(true)));    
/*
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(AddNewBpelProcess.class)
                .addTest("measureTime").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateBPELmodule.class)
                .addTest("measureTime").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateCompositeApplication.class)
                .addTest("measureTime").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(WatchProjects.class)
                .addTest("testGCProjects").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(OpenSchemaView.class)
                .addTest("testGCwithOpenComplexSchemaView").enableModules(".*").clusters(".*")));    
*/
        return suite;
    }
}