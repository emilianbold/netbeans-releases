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
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class BugTriggersTest extends TestCase {
    private static Installer o;
    
    static {
        Locale.setDefault(new Locale("te", "ST"));
        o = Installer.findObject(Installer.class, true);
        o.restored();
    }
    
    public BugTriggersTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        assertNotNull("Installer created", o);
        MockServices.setServices(DD.class);
        
        MemoryURL.registerURL("memory://error.html", getClass().getResourceAsStream("error.html"));
    }

    protected void tearDown() throws Exception {
    }

    public void testErrorMgrShowsDialogOnException() throws Exception {
        DD.toReturn = 0;
        IOException ex = new IOException("Chyba");
        ErrorManager.getDefault().notify(ex);
        
        assertNotNull("Descriptor called", DD.d);
    }
    
    public void testLoggerShowsDialogOnException() throws Exception {
        DD.toReturn = 0;
        IOException ex = new IOException("Chyba");
        Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
        
        assertNotNull("Descriptor called", DD.d);
    }
    
    public static final class DD extends DialogDisplayer {
        public static int toReturn = -1;
        public static NotifyDescriptor d;
        
        public Object notify(NotifyDescriptor descriptor) {
            fail("Not implemented");
            return null;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            d = descriptor;
            if (toReturn == -1) {
                fail("There should be something to return");
            }
            Object r = descriptor.getOptions()[toReturn];
            toReturn = -1;
            return new JDialog() {
                @Deprecated
                public void show() {
                }
            };
        }
        
    }
}
