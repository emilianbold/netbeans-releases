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

package org.netbeans.modules.uihandler;

import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class BugTriggersTest extends NbTestCase {
    private static Installer o;
    
    public BugTriggersTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected void setUp() throws Exception {
        Installer.findObject(Installer.class, true).restored();
    }

    protected void tearDown() throws Exception {
    }
    
    public void testRootLoggerHasHandler() throws Exception {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            if (h instanceof Callable) {
                return;
            }
        }
        fail("No handler which implements Callable");
    }
 }
