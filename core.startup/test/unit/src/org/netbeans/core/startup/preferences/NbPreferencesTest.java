/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.preferences;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Radek Matous
 */
public class NbPreferencesTest extends NbTestCase {
    public NbPreferencesTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestPreferences.class);
        suite.addTestSuite(TestFileStorage.class);
        suite.addTestSuite(TestPropertiesStorage.class);
        suite.addTestSuite(TestNbPreferencesFactory.class);
        
        return suite;
    }
    
    public static class BasicSetupTest extends NbTestCase {
        public BasicSetupTest(String testName) {
            super(testName);                        
        }
        
        
        protected void tearDown() throws Exception {
            super.tearDown();
            /*Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.INFO  ,getName()+ "->" + Statistics.FLUSH.toString());//NOI18N
            logger.log(Level.INFO,getName()+ "->" + Statistics.LOAD.toString());//NOI18N
            logger.log(Level.INFO,getName()+ "->" + Statistics.REMOVE_NODE.toString());//NOI18N
            logger.log(Level.INFO,getName()+ "->" + Statistics.CHILDREN_NAMES.toString());//NOI18N
             **/
        }
        
        protected void setUp() throws Exception {
            super.setUp();
            NbPreferencesFactory.doRegistration();
            Statistics.CHILDREN_NAMES.reset();
            Statistics.FLUSH.reset();
            Statistics.LOAD.reset();
            Statistics.REMOVE_NODE.reset();
        }        
    }
}