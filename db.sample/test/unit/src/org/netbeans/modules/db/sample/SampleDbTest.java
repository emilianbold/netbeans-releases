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

package org.netbeans.modules.db.sample;

import org.netbeans.api.db.sample.SampleDbManager;
import org.netbeans.junit.NbTestCase;

/**
 * Test creating sample databases.
 * 
 * The system proper
 * @author David
 */
public class SampleDbTest extends NbTestCase {
    
    /**
     * Set these properties in test/unit/unit.properties.  There are settings
     * for each database vendor we want to test
     * 
     * YOU NEED TO COPY unit-template.properties to unit.properties and
     * set this with the values that are correct in your environment.  This
     * way local configuration of your database is not checked back into
     * the codeline.
     *
     * Note that the default settings in unit-template.properties
     * use servers on the Sun QE network, and will not work outside of the 
     * Sun network.  
     */
    String driverClass = System.getProperty("db.driverclass",
            "com.mysql.jdbc.Driver");
    String user = System.getProperty("db.user", "root");
    String password = System.getProperty("db.password", "durga12");
    String host = System.getProperty("db.host", "localhost");
    String port = System.getProperty("db.port", "3306");
    String[] samples = System.getProperty("db.samples",
            "sample").split(",");
    
    private SampleDbManager manager;
    
    public SampleDbTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = SampleDbManager.getController(driverClass);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testDropDatabase() throws Exception {
        for ( String sample : samples ) {
            testDropDatabase(sample);
        }
    }
    
    private void testDropDatabase(String sampleName) throws Exception {
        manager.dropDatabase(sampleName, host, port, user, password);
    }
}
