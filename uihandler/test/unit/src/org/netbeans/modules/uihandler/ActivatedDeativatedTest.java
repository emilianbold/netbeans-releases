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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class ActivatedDeativatedTest extends NbTestCase {
    private Installer o;
    
    public ActivatedDeativatedTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        Locale.setDefault(new Locale("te", "ST"));
        o = Installer.findObject(Installer.class, true);
        assertNotNull("Installer created", o);
        MockServices.setServices(A.class, D.class/*); //*/, DD.class);
    }

    protected void tearDown() throws Exception {
    }
    
    public void testActivatedAndDeativated() {
        CharSequence log = Log.enable("org.netbeans.ui", Level.ALL);
        
        o.restored();
        if (log.toString().indexOf("A start") == -1) {
            fail("A shall start: " + log);
        }
        
        assertTrue("Allowed to close", o.closing());
        if (log.toString().indexOf("D stop") == -1) {
            fail("D shall stop: " + log);
        }
    }
    
    
    public static final class A implements Activated {
        public void activated(Logger uiLogger) {
            uiLogger.config("A started");
        }
    }
    public static final class D implements Deactivated {
        public void deactivated(Logger uiLogger) {
            uiLogger.config("D stopped");
        }
    }
    public static final class DD extends DialogDisplayer {
        public Object notify(NotifyDescriptor descriptor) {
            // last options allows to close usually
            return descriptor.getOptions()[descriptor.getOptions().length - 1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            return new JDialog() {
                public void setVisible(boolean v) {
                }
            };
        }
        
    }
}
