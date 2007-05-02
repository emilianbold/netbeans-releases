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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.exceptions.ReportPanel;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsTest extends NbTestCase {
    
    public ExceptionsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
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
        LogRecord log4 = new LogRecord(Level.INFO, "INFO");
        Throwable t1 = new NullPointerException("TESTING THROWABLE");
        Throwable t2 = new UnknownError("TESTING ERROR");
        log1.setThrown(t1);
        log2.setThrown(t2);
        log4.setThrown(t2);
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.restored();
        uiLogger.log(log1);
        uiLogger.log(log2);
        uiLogger.log(log3);
        assertEquals(3, installer.getLogsSize());
        if (installer.getThrown().getMessage().indexOf("TESTING ERROR") == -1) {
            fail("Wrong message " + installer.getThrown().getMessage());
        }
        log1 = new LogRecord(Level.SEVERE, "TESTING 2");
        log1.setThrown(t1);
        uiLogger.log(log1);
        assertEquals(4, installer.getLogsSize());
        List<LogRecord> arr = installer.getLogs();
        assertEquals("The same amount of logs is loaded: " + arr, 4, arr.size());
        if (installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1) {
            fail("Wrong message " + installer.getThrown().getMessage());
        }
        for (int i= 0; i < 10; i++){
            uiLogger.warning("MESSAGE "+Integer.toString(i));
        }
        assertEquals(14, installer.getLogsSize());
        if (installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1) {
            fail("Wrong message " + installer.getThrown().getMessage());
        }
        uiLogger.log(log4);
        assertEquals(15, installer.getLogsSize());
        if (installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1){
            fail("Wrong message " + installer.getThrown().getMessage());
        }
        if (installer.getThrown().getMessage().contains("WARNING")){
            fail("Message should not contain warnings" + installer.getThrown().getMessage());
        }
    }
}
