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

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandlerWhenInterruptedTest extends NbTestCase {
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions");

    
    public UIHandlerWhenInterruptedTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        assertNotNull("Installer created", o);
        o.restored();
    }

    protected void tearDown() throws Exception {
    }

    public void testPublishWhenInterupted() {
        LogRecord rec = new LogRecord(Level.FINER, "1"); // NOI18N
        UILOG.log(rec);        

        for (int i = 1; i < 800; i++) {
            LogRecord rec2 = new LogRecord(Level.FINER, "" + i); // NOI18N
            Thread.currentThread().interrupt();
            UILOG.log(rec2);        
        }

        int cnt = 50;
        while (cnt-- > 0 && Installer.getLogsSize() < 800) {
            // ok, repeat
        }
        List<LogRecord> logs = Installer.getLogs();
        assertEquals("One log: " + logs, 800, logs.size());
        
        for (int i = 1; i < 800; i++) {
            assertEquals("" + i, logs.get(i).getMessage());
        }
        
    }
    
    private static final class MyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
