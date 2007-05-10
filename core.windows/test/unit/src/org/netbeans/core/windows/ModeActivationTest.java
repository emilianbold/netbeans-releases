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

package org.netbeans.core.windows;

import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/** 
 * Test Mode activation behavior.
 * 
 * @author Marek Slama
 * 
 */
public class ModeActivationTest extends NbTestCase {

    public ModeActivationTest (String name) {
        super (name);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ModeActivationTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    /**
     * Test basic behavior when Mode is activated. TC is docked into Mode, opened, activated,
     * closed. During this activation state of Mode is tested.
     */
    public void testActivate () throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        PersistenceHandler.getDefault().load();
        
        //This must be unit test as we need minimum winsys config
        //if default minimum winsys config is changed this test must be changed too.
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        Set <? extends Mode> s = wmi.getModes();
        assertEquals("There must be only one mode",s.size(),1);
        
        Mode editor = null;
        for (Mode m : s) {
            assertEquals("There must be only editor mode",m.getName(),"editor");
            editor = m;
        }
        Mode activeMode = wmi.getActiveMode();
        assertNull("No mode is activated ie. active mode must be null",activeMode);
        
        //Mode cannot be activated when it is empty
        wmi.setActiveMode((ModeImpl) editor);
        activeMode = wmi.getActiveMode();
        assertNull("Ignore mode activation when mode is empty",activeMode);
        
        //Editor mode must be empty
        TopComponent [] tcs = editor.getTopComponents();
        assertEquals("Mode editor must be empty",tcs.length,0);

        //Dock TC into mode
        TopComponent tc = new TopComponent();
        
        //As tc is not yet docked into any mode this must return null
        Mode m = wmi.findMode(tc);
        assertNull("No mode for TC",m);
        
        editor.dockInto(tc);
        //Editor mode must contain one TC
        tcs = editor.getTopComponents();
        assertEquals("Mode editor must contain one TC",tcs.length,1);
        
        //Mode cannot be activated when it does not contain opened TC
        wmi.setActiveMode((ModeImpl) editor);
        activeMode = wmi.getActiveMode();
        assertNull("Mode cannot be activated when it does not contain opened TC",activeMode);
        
        m = wmi.findMode(tc);
        assertEquals("Mode editor must be found for TC",m,editor);
        
        //TC is closed
        assertFalse("TC is closed",tc.isOpened());
        
        tc.open();
        //TC is opened
        assertTrue("TC is opened",tc.isOpened());
        tc.requestActive();
        
        //Editor mode is now activated
        activeMode = wmi.getActiveMode();
        assertEquals("Editor mode is now activated",editor,activeMode);
        
        //Check active tc
        TopComponent activeTC = wmi.getRegistry().getActivated();
        assertEquals("TC is now active",tc,activeTC);
        
        tc.close();
        //TC is closed
        assertFalse("TC is closed",tc.isOpened());
        
        //No mode is now activated
        activeMode = wmi.getActiveMode();
        assertNull("No mode is activated ie. active mode must be null", activeMode);
    }
    
}
