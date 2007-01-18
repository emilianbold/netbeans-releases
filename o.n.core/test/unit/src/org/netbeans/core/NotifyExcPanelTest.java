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

package org.netbeans.core;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class NotifyExcPanelTest extends NbTestCase {
    Logger main;
    
    public NotifyExcPanelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        main = Logger.getLogger("");
        for (Handler h : main.getHandlers()) {
            main.removeHandler(h);
        }
    }
    
    public void testHandlesThatImplementCallableForJButtonAreIncluded() throws Exception {
        class H extends Handler 
        implements Callable<JButton> {
            public JButton button = new JButton("Extra");
        
            public void publish(LogRecord arg0) {
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

            public JButton call() throws Exception {
                return button;
            }
        } // end of H
        
        H handler = new H();
        
        main.addHandler(handler);
        
        List<Object> options = Arrays.asList(NotifyExcPanel.computeOptions("prev", "next"));
        
        assertTrue("Contains our button: " + options, options.contains(handler.button));
    }

    public void testHandlesThatImplementCallableForOtherObjectsAreNotIncluded() throws Exception {
        class H extends Handler 
        implements Callable<Object> {
            public JButton button = new JButton("Extra");
        
            public void publish(LogRecord arg0) {
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

            public JButton call() throws Exception {
                return button;
            }
        } // end of H
        
        H handler = new H();
        
        main.addHandler(handler);
        
        List<Object> options = Arrays.asList(NotifyExcPanel.computeOptions("prev", "next"));
        
        assertFalse("Does not contain our button: " + options, options.contains(handler.button));
    }
}
