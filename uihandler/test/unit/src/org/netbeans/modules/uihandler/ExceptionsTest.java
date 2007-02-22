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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.exceptions.ReportPanel;

/**
 *
 * @author jindra
 */
public class ExceptionsTest extends NbTestCase {
    
    public ExceptionsTest(String testName) {
        super(testName);
    }
    
    public void testSetReportPanelSummary(){
        String str = "RETEZEC SUMMARY";
        ReportPanel panel = new ReportPanel();
        panel.setSummary(str);
        assertEquals(str, panel.getSummary());
    }
    
    public void testExceptionThrown() throws Exception{
        Logger uiLogger = Logger.getLogger("org.netbeans.ui");
        LogRecord log1 = new LogRecord(Level.SEVERE, "TESTING MESSAGE");
        LogRecord log2 = new LogRecord(Level.SEVERE, "TESTING MESSAGE");
        LogRecord log3 = new LogRecord(Level.SEVERE, "NO EXCEPTION LOG");
        Throwable t1 = new NullPointerException("TESTING THROWABLE");
        Throwable t2 = new UnknownError("TESTING ERROR");
        log1.setThrown(t1);
        log2.setThrown(t2);
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.restored();
        uiLogger.log(log1);
        uiLogger.log(log2);
        uiLogger.log(log3);
        assertEquals(3, installer.getLogsSize());
        assertEquals(t2, installer.getThrown());
        log1 = new LogRecord(Level.SEVERE, "TESTING 2");
        log1.setThrown(t1);
        uiLogger.log(log1);
        assertEquals(4, installer.getLogsSize());
        assertEquals(t1, installer.getThrown());
        for (int i= 0; i < 10; i++){
            uiLogger.warning("MESSAGE "+Integer.toString(i));
        }
        assertEquals(14, installer.getLogsSize());
        assertEquals(t1, installer.getThrown());
    }
}
