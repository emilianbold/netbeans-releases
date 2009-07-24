/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.spi;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

/**
 *
 * @author ak119685
 */
public class MonitorsUpdateServiceTest {
    final Set<CollectedInfo> collectedInfo = EnumSet.of(
            SunStudioDCConfiguration.CollectedInfo.MEMSUMMARY,
            SunStudioDCConfiguration.CollectedInfo.SYNCSUMMARY);
    
    public MonitorsUpdateServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String dirs = System.getProperty("netbeans.dirs", ""); // NOI18N
        System.setProperty("netbeans.dirs", "/export/home/ak119685/netbeans-src/main/nbbuild/netbeans/dlight1:" + dirs);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of start method, of class MonitorsUpdateService.
     */
    @Test
    public void testStart() {
        MonitorsUpdateService service = new MonitorsUpdateService(
                new Collector(),
                ExecutionEnvironmentFactory.getLocal(),
                "/shared/dp/sstrunk/intel-S2/",
                "/var/tmp/dlightExperiment_20.er/",
                collectedInfo);

        service.start();
        service.stop();
        service.start();
        service.stop();
    }

    private class Collector extends SunStudioDataCollector {

        public Collector() {
            super(collectedInfo);
        }

        @Override
        protected void updateIndicators(List<DataRow> data) {
            for (DataRow d : data) {
                for (String cn : d.getColumnNames()) {
                    System.out.println(cn + ": " + d.getData(cn).toString());
                }
            }
        }

    }


}