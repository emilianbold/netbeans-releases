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

import java.awt.Dialog;
import java.awt.Frame;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.junit.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import org.openide.windows.WindowManager;

/**
 * Test NotifyException class.
 * @author Stanislav Aubrecht
 */
public class NotifyExceptionTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    public NotifyExceptionTest(String name) {
        super(name);
    }

    private static void waitEQ() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        // initialize the logging
        new TopLogging();

        DD.lastDescriptor = null;
        DD.toReturn = null;

        System.getProperties().remove("netbeans.exception.alert.min.level");
        System.getProperties().remove("netbeans.exception.report.min.level");

        NotifyException.cleanInstance();
    }
    
    /**
     * A simple test to ensure that error dialog window is not created modal
     * until the MainWindow is visible.
     */
    public void testNoModalErrorDialog() throws Exception {
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog modalDialog = new HiddenDialog( mainWindow, true );
        DD.toReturn = modalDialog;

        Logger.global.log(Level.WARNING, "Something is wrong", new NullPointerException("npe"));
        waitEQ();
        assertNotNull("Really returned", DD.lastDescriptor);
        assertEquals("It is DialogDescriptor", DialogDescriptor.class, DD.lastDescriptor.getClass());
        DialogDescriptor dd = (DialogDescriptor)DD.lastDescriptor;
        assertFalse( "The request is for non-modal dialog", dd.isModal());
        assertFalse("Main window is not visible", mainWindow.isVisible());
    }

    public void testExceptionWillGetTheLevelFromAnnoatation() throws Exception {
        NullPointerException npe = new NullPointerException("npe");
        ErrorManager.getDefault().annotate(npe, ErrorManager.WARNING, null, null, null, null);

        DD.toReturn = new HiddenDialog();
        Exceptions.printStackTrace(npe);

        waitEQ();
        assertNotNull("We are going to display a warning", DD.lastDescriptor);

    }

    public void testDirectlyLoggingAnExceptionWithALocalizedMessageAndTheRightLevelShowsItInADialog() throws Exception {
        NullPointerException npe = new NullPointerException("npe");

        LogRecord rec = new LogRecord(OwnLevel.UI, "MSG_KEY");
        rec.setThrown(npe);
        ResourceBundle b = ResourceBundle.getBundle("org/netbeans/core/NotifyExceptionBundle");
        rec.setResourceBundle(b);
        DD.toReturn = new HiddenDialog();
        Logger.global.log(rec);
        waitEQ();
        assertNotNull("We are going to display a warning", DD.lastDescriptor);
        assertTrue("We want message: " + DD.lastDescriptor, DD.lastDescriptor instanceof NotifyDescriptor.Message);
        NotifyDescriptor.Message msg = (NotifyDescriptor.Message)DD.lastDescriptor;
        assertEquals("Info msg", NotifyDescriptor.INFORMATION_MESSAGE, msg.getMessageType());
        assertEquals("Msg is localized", b.getString("MSG_KEY"), msg.getMessage());
    }

    public void testYesDialogShown() throws Exception {
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog modalDialog = new HiddenDialog( mainWindow, true );
        DD.toReturn = modalDialog;

        Logger l = Logger.getLogger(getName());
        l.setLevel(Level.ALL);
        System.setProperty("netbeans.exception.report.min.level", "200");
        l.log(Level.CONFIG, "Something is wrong", new NullPointerException("npe"));
        waitEQ();
        assertNotNull("Really returned", DD.lastDescriptor);
        assertEquals("It is DialogDescriptor", DialogDescriptor.class, DD.lastDescriptor.getClass());
        DialogDescriptor dd = (DialogDescriptor)DD.lastDescriptor;
        assertFalse( "The request is for non-modal dialog", dd.isModal());
        assertFalse("Main window is not visible", mainWindow.isVisible());
    }
    public void testNoDialogShownJustFlashing() throws Exception {
        class MockFlashingIcon extends FlashingIcon {
            public int cnt;

            public MockFlashingIcon() {
                super(new ImageIcon());
            }
            protected void onMouseClick() {
            }

            protected void timeout() {
            }

            public void startFlashing() {
                cnt++;
            }
        }
        MockFlashingIcon mock = new MockFlashingIcon();

        NotifyException.flasher = mock;

        Logger l = Logger.getLogger(getName());
        l.setLevel(Level.ALL);
        System.setProperty("netbeans.exception.alert.min.level", "200");
        l.log(Level.CONFIG, "Something is wrong", new NullPointerException("npe"));
        waitEQ();
        assertNull("Really returned", DD.lastDescriptor);

        assertEquals("Flasher flashing", 1, mock.cnt);
    }

    private static final class OwnLevel extends Level {
        public static final Level UI = new OwnLevel("UI", 1973);

        private OwnLevel(String n, int i) {
            super(n, i);
        }
    }

    private static final class DD extends DialogDisplayer {
        public static NotifyDescriptor lastDescriptor;
        public static Object toReturn;

        public Object notify(NotifyDescriptor descriptor) {
            Object t = toReturn;
            toReturn = null;
            assertNotNull("There is something to return", t);
            lastDescriptor = descriptor;
            return t;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            return (Dialog)notify(descriptor);
        }
    } // end of DD

    public static final class Lkp extends AbstractLookup {
        public static InstanceContent IC;

        public Lkp() {
            this(new InstanceContent());
        }
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new DD());
            ic.add(new NbErrorManager());
        }
    }

    private static final class HiddenDialog extends JDialog {
        private boolean v;

        public HiddenDialog() {
        }

        public HiddenDialog(Frame p, boolean b) {
            super(p, b);
        }

        public void setVisible(boolean b) {
            v = b;
        }
        public boolean isVisible() {
            return v;
        }
    }
}
