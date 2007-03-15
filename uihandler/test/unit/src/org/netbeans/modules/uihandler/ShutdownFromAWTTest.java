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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach
 */
public class ShutdownFromAWTTest extends NbTestCase {
    Installer inst;
    
    public ShutdownFromAWTTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    

    protected void setUp() throws Exception {
        inst = Installer.findObject(Installer.class, true);
        inst.restored();

        MockServices.setServices(DD.class);
        Logger.getLogger("org.netbeans.ui").warning("ONE_LOG");
    }

    protected void tearDown() throws Exception {
    }

    public void testShutdown() throws Exception {
        assertTrue("In EQ", EventQueue.isDispatchThread());
    
        assertTrue("Ok to close", inst.closing());
        inst.close();
    }
    public static final class DD extends DialogDisplayer implements Mutex.Action<Integer> {
        private int cnt;
        
        private void assertAWT() {
            int cnt = this.cnt;
            int ret = Mutex.EVENT.readAccess(this);
            assertEquals("Incremented", cnt + 1, this.cnt);
            assertEquals("Incremented2", cnt + 1, ret);
        }
        
        public Object notify(NotifyDescriptor descriptor) {
            assertAWT();
            
            // last options allows to close usually
            return descriptor.getOptions()[descriptor.getOptions().length - 1];
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            assertAWT();
            
            return new JDialog() {
                public void setVisible(boolean v) {
                }
            };
        }
        
        public Integer run() {
            cnt++;
            assertTrue(EventQueue.isDispatchThread());
            return cnt;
        }
    }
    
}
