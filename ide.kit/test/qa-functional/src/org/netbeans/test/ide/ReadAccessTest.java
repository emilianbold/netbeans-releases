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

package org.netbeans.test.ide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 * Whitelist test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 *
 * To run this test do the following:
 * 1. execute test/whitelist/prepare.bat to prepare LimeWare project
 * 2. execute test/whitelist/test.bat to do the measurement
 * 3. execute test/whitelist/unprepare.bat to restore the environment
 *
 * @author mrkam@netbeans.org
 */
public class ReadAccessTest extends JellyTestCase {

    private static int stage;

    private static void initCheckReadAccess() throws IOException {
        if (stage == 2) {
            Set<String> allowedFiles = new HashSet<String>();
            InputStream is = WhitelistTest.class.getResourceAsStream("allowed-file-reads.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            for (;;) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                allowedFiles.add(line);
            }
            CountingSecurityManager.initialize(null, CountingSecurityManager.Mode.CHECK_READ, allowedFiles);
        }
    }
    
    public ReadAccessTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {

        /**
         * Specify:
         * stage = 1 to initialize userdir
         * stage = 2 to perform measurement
         */
        stage = 2;
        //stage = Integer.getInteger("test.whitelist.stage", 1);
        
        initCheckReadAccess();
        
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            ReadAccessTest.class
        ).clusters(".*").enableModules(".*").reuseUserDir(stage > 1);
        

        conf = conf.addTest("testReadAccess");
        
        return NbModuleSuite.create(conf);
    }

    public void testReadAccess() throws Exception {
        try {
            if (CountingSecurityManager.isEnabled()) {
                CountingSecurityManager.assertCounts("No reads during startup", 0);
            } else {
                System.out.println("Initialization mode, counting is disabled");
            }
        } catch (Error e) {
            e.printStackTrace(getLog("report.txt"));
            throw e;
        }
    }

}
