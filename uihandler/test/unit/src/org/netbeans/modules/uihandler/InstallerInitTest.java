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

import java.awt.Dialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import junit.framework.*;
import java.util.Locale;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerInitTest extends NbTestCase {
    private Installer installer;
    
    public InstallerInitTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        DD.d = null;
        MockServices.setServices(DD.class);

        // setup the listing
        installer.restored();

        assertNull("No dialog as there are no records", DD.d);
        
        Locale.setDefault(new Locale("in", "IT"));
        
    }

    @Override
    protected void tearDown() throws Exception {
        assertNotNull(installer);
        installer.close();
    }

    /* XXX: implement by beta1
    public void testWhatIfTheURLIsDown() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
        r.setLoggerName("org.netbeans.ui.anything");

        String utf8 = "";
        
        MemoryURL.registerURL("memory://someunknownURL.html", utf8);
        
        for (int i = 0; i < 1500; i++) {
            Logger.getLogger("org.netbeans.ui.anything").log(r);
        }
        assertEquals("full buffer", 1000, Installer.getLogsSize());
        
        assertNull("No dialogs so far", DD.d);
        
        installer.close();
        waitAWT();
        
        assertNull("No dialogs at close", DD.d);
        
        installer.restored();
        
        waitAWT();

        assertNull("No dialog if the URL does not live", DD.d);
    }
     */

    public void testGenerateEnoughLogsExit() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
        r.setLoggerName("org.netbeans.ui.anything");

        String utf8 = 
            "<html><head>" +
            "</head>" +
            "<body>" +
            "<form action='http://anna.nbextras.org/analytics/upload.jsp' method='post'>" +
            "  <input name='submit' value='&amp;Fill Survey' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes("utf-8"));
        
        MemoryURL.registerURL("memory://start.html", is);
        
        for (int i = 0; i < 1500; i++) {
            Logger.getLogger("org.netbeans.ui.anything").log(r);
        }
        assertEquals("full buffer", 1000, Installer.getLogsSize());
        
        assertNull("No dialogs so far", DD.d);
        
        installer.close();
        waitAWT();
        
        assertNull("No dialogs at close", DD.d);
        
        installer.restored();
        
        waitAWT();

        assertNotNull("A dialog shown at begining", DD.d);
    }

    public void testGenerateTooLittleLogs() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
        r.setLoggerName("org.netbeans.ui.anything");

        String utf8 = 
            "<html><head>" +
            "</head>" +
            "<body>" +
            "<form action='http://anna.nbextras.org/analytics/upload.jsp' method='post'>" +
            "  <input name='submit' value='&amp;Fill Survey' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes("utf-8"));
        
        MemoryURL.registerURL("memory://start.html", is);
        
        for (int i = 0; i < 500; i++) {
            Logger.getLogger("org.netbeans.ui.anything").log(r);
        }
        assertEquals("not full buffer", 500, Installer.getLogsSize());
        
        assertNull("No dialogs so far", DD.d);
        
        installer.close();
        waitAWT();
        
        assertNull("No dialogs at close", DD.d);
        
        installer.restored();
        
        waitAWT();

        assertNull("No dialog shown at begining", DD.d);
    }
    
    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor d;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            return NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            
            return new DialogImpl(d, new Frame());
        }

        private static class DialogImpl extends Dialog 
        implements PropertyChangeListener {
            NotifyDescriptor d;
            
            private DialogImpl(NotifyDescriptor d, Frame owner) {
                super(owner);
                this.d = d;
            }

            @java.lang.Override
            public synchronized void setVisible(boolean b) {
                assertFalse(isModal());
            }

            public synchronized void propertyChange(PropertyChangeEvent evt) {
                if (d != null && d.getOptions().length == 2) {
                    d.setValue(NotifyDescriptor.CLOSED_OPTION);
                    d = null;
                    notifyAll();
                }
            }
        }
        
    }

    private void waitAWT() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }
}
