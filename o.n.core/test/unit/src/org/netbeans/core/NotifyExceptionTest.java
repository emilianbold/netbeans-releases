/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

import org.openide.windows.WindowManager;

/**
 * Test NotifyException class.
 * @author Stanislav Aubrecht
 */
public class NotifyExceptionTest extends NbTestCase {
    
    public NotifyExceptionTest(String name) {
        super(name);
    }

    protected boolean runInEQ() {
        return true;
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NotifyExceptionTest.class));
    }
    
    protected void setUp() throws Exception {
    }
    
    static boolean errorDialogIsModal;
    /**
     * A simple test to ensure that error dialog window is not created modal
     * until the MainWindow is visible.
     */
    public void testNoModalErrorDialog() throws Exception {
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog modalDialog = new JDialog( mainWindow, true );
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                NotifyException ne = NotifyException.newInstance();
                errorDialogIsModal = ne.dialog.isModal();
                modalDialog.dispose();
            }
        } );

        modalDialog.show();
        
        assertFalse( "Error dialog is not modal", errorDialogIsModal );
    }
}
