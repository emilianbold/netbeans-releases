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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import junit.framework.*;
import java.util.Locale;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerReadPageTest extends NbTestCase {
    
    public InstallerReadPageTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        // setup the listing
        installer.restored();
        
        Locale.setDefault(new Locale("te", "ST"));
        
        DD.d = null;
        MockServices.setServices(DD.class);
    }

    @Override
    protected void tearDown() throws Exception {
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.close();
    }

    public void testURLInCzechEncoding() throws Exception {
        doEncodingTest("iso-8859-2", "<meta http-equiv='Content-Type' content='text/html; charset=iso-8859-2'></meta>");
    }
 
    public void testURLInNoEncoding() throws Exception {
        doEncodingTest("utf-8", "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'></meta>");
    }

    public void testURLInUTF8Encoding() throws Exception {
        doEncodingTest("UTF-8", "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'></meta>");
    }
    
    private void doEncodingTest(String encoding, String metaTag) throws Exception {
        //String kun = "Žluťoučký kůň";
        String kun = "\u017Dlu\u0165ou\u010Dky k\u016F\u0148";
        String utf8 = 
            "<html><head>" +
            metaTag +
            "</head>" +
            "<body>" +
            "<form action='http://anna.nbextras.org/analytics/upload.jsp' method='post'>" +
            "  <input name='submit' value='" + kun + "' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes(encoding));
        
        MemoryURL.registerURL("memory://kun.html", is);
        
        boolean res = Installer.displaySummary("KUN", true, false,true);
        assertFalse("Close options was pressed", res);
        assertNotNull("DD.d assigned", DD.d);
        
        List<Object> data = Arrays.asList(DD.d.getOptions());
        assertEquals("Two objects: " + data, 2, DD.d.getOptions().length);
        assertEquals("First is jbutton", JButton.class, DD.d.getOptions()[0].getClass());
        JButton b = (JButton)DD.d.getOptions()[0];
        
        assertEquals("It has the right localized text", kun, b.getText());
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
}
