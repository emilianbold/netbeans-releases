/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): Martin Schovanek.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */
package org.netbeans.jemmy;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A JUnit test for Waiter.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class TimeoutsTest extends TestCase {
    private String originalScale = null;

    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public TimeoutsTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        originalScale = System.getProperty("jemmy.timeouts.scale");
    }
    
    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void tearDown() throws Exception {
        // clean up
        if (originalScale == null) {
            System.getProperties().remove("jemmy.timeouts.scale");
        } else {
            System.setProperty("jemmy.timeouts.scale", originalScale);
        }
        Timeouts.resetTimeoutScale();
    }
    
    /**
     * Suite method.
     *
     * @return a test suite.
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(TimeoutsTest.class);
        return suite;
    }
    
    /**
     * Tests getTimeoutsScale() and getTimeout() methods.
     */
    public void testGetTimeout() {
        // the default scale should be 1.0
        Timeouts.initDefault("TimeoutsTest.TestTimeout", 1000);
        Timeouts ts = new Timeouts();
        assertEquals(1000L, ts.getTimeout("TimeoutsTest.TestTimeout"));
        Timeouts.resetTimeoutScale();
        // set the scale to 2.5
        System.setProperty("jemmy.timeouts.scale", "2.5");
        assertEquals(2500L, ts.getTimeout("TimeoutsTest.TestTimeout"));
    }
}
