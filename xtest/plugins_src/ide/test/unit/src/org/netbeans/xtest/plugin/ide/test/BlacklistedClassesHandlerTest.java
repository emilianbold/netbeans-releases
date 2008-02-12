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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.xtest.plugin.ide.test;

import org.netbeans.xtest.plugin.ide.BlacklistedClassesHandler;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import junit.framework.TestCase;

/**
 *
 * @author mrkam@netbeans.org
 */
public class BlacklistedClassesHandlerTest extends TestCase {
    
    public BlacklistedClassesHandlerTest(String testName) {
        super(testName);
    }            

    protected void setUp() throws Exception {
        super.setUp();
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        if (bcHandler != null) {
            bcHandler.resetViolations();
        } 
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Test of getBlacklistedClassesHandler method, of class BlacklistedClassesHandler.
     */
    public void testGetBlacklistedClassesHandler_1() {
        System.out.println("getBlacklistedClassesHandler_1");
        BlacklistedClassesHandler result = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        assertNull(result);
    }    
    public void testGetBlacklistedClassesHandler_2() {
        System.out.println("getBlacklistedClassesHandler_2");
        BlacklistedClassesHandler result = BlacklistedClassesHandler.getBlacklistedClassesHandler(
                System.getProperty("xtest.data") + System.getProperty("file.separator") + "blacklist.txt");
        assertNotNull(result);
    }
    public void testGetBlacklistedClassesHandler_3() {
        System.out.println("getBlacklistedClassesHandler_3");
        BlacklistedClassesHandler result = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        assertNotNull(result);
    }
    public void testGetBlacklistedClassesHandler_4() {
        System.out.println("getBlacklistedClassesHandler_4");
        Error result = null;
        try {
            BlacklistedClassesHandler.getBlacklistedClassesHandler(
                    System.getProperty("xtest.data") + System.getProperty("file.separator") + "blacklist.txt");
        } catch (Error err) {
            result = err;
            assertEquals(err.getMessage(), "BlacklistedClassesHandler shouldn't be initialized twice!");
        }
        assertNotNull(result);
    }
    
    /**
     * Test of publish method, of class BlacklistedClassesHandler.
     */
    public void testPublishAndNoViolations() {
        System.out.println("publish");
        BlacklistedClassesHandler instance = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        assertTrue(instance.noViolations());
        publish(instance, "org.netbeans.Test", "org.netbeans.Test1");        
        assertTrue(instance.noViolations());
        publish(instance);        
        assertFalse(instance.noViolations());        
    }

    /**
     * Test of logViolations method, of class BlacklistedClassesHandler.
     */
    public void testLogViolations() {
        System.out.println("logViolations");
        BlacklistedClassesHandler instance = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        
        class MyHandler extends Handler {
            
            public boolean published = false;

            public void publish(LogRecord record) {
                published = true;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
            
        }
        
        MyHandler handler = new MyHandler();
        Logger.getLogger(instance.getClass().getName()).addHandler(handler);
        
        publish(instance);
        instance.logViolations();
        
        assertTrue(handler.published);
    }

    /**
     * Test of listViolations method, of class BlacklistedClassesHandler.
     */
    public void testListViolations() {
        System.out.println("listViolations");
        BlacklistedClassesHandler instance = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        String result = instance.listViolations();
        assertNotNull(result);
        System.out.println(result);
    }

    /**
     * Test of resetViolations method, of class BlacklistedClassesHandler.
     */
    public void testResetViolations() {
        System.out.println("resetViolations");
        BlacklistedClassesHandler instance = BlacklistedClassesHandler.getBlacklistedClassesHandler();
        publish(instance);
        assertFalse(instance.noViolations());
        instance.resetViolations();
        assertTrue(instance.noViolations());
    }

    private void publish(BlacklistedClassesHandler instance) {
        publish(instance, "org.netbeans.Test1", "org.netbeans.Test");
    }
    
    private void publish(BlacklistedClassesHandler instance, String className, String instantiator) {
        LogRecord record = null;

        record = new LogRecord(Level.ALL, "{0} initiated loading of {1}");
        record.setParameters(new String[]{instantiator, className});
        instance.publish(record);

    }

}