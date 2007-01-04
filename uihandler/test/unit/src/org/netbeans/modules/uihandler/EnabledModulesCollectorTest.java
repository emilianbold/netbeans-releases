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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 */
public class EnabledModulesCollectorTest extends NbTestCase {
    private Installer installer;
    
    public EnabledModulesCollectorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Locale.setDefault(new Locale("te", "ST"));
        MockServices.setServices(EnabledModulesCollector.class, MyModule.class, MyModule2.class, ActivatedDeativatedTest.DD.class);
        installer = Installer.findObject(Installer.class, true);
        installer.restored();
    }

    protected void tearDown() throws Exception {
    }
    
    public void testSetOfEnabledModulesIsListed() {
        // just log something
        Logger.getLogger("org.netbeans.ui.empty").warning("say anything");
        
        assertTrue("ok", installer.closing());
        
        List<LogRecord> rec = Installer.getLogs();
        if (rec.get(0).getMessage().equals("say anything")) {
            rec.remove(0);
        }
        
        assertEquals("One record for disabled and one for enabled: " + rec, 2, rec.size());
        
        assertEquals("UI_ENABLED_MODULES", rec.get(0).getMessage());
        assertEquals("UI_DISABLED_MODULES", rec.get(1).getMessage());
        assertEquals("one enabled", 1, rec.get(0).getParameters().length);
        assertEquals("one disabled", 1, rec.get(1).getParameters().length);
        assertEquals("the one enabled", MyModule.INSTANCE.getCodeNameBase(), rec.get(0).getParameters()[0]);
        assertEquals("the one disabled", MyModule2.INSTANCE2.getCodeNameBase(), rec.get(1).getParameters()[0]);

        assertNotNull("Localized msg0", rec.get(0).getResourceBundle().getString(rec.get(0).getMessage()));
        assertNotNull("Localized msg1", rec.get(1).getResourceBundle().getString(rec.get(1).getMessage()));
    }
    
    public static class MyModule extends ModuleInfo {
        static MyModule INSTANCE;
        
        public MyModule() {
            if (MyModule.class == getClass()) {
                INSTANCE = this;
            }
        }
        
        public String getCodeNameBase() {
            return "my.module";
        }

        public int getCodeNameRelease() {
            return -1;
        }

        public String getCodeName() {
            return getCodeNameBase();
        }

        public SpecificationVersion getSpecificationVersion() {
            return new SpecificationVersion("1.2");
        }

        public boolean isEnabled() {
            return true;
        }

        public Object getAttribute(String attr) {
            return null;
        }

        public Object getLocalizedAttribute(String attr) {
            return null;
        }

        public Set<org.openide.modules.Dependency> getDependencies() {
            return Collections.emptySet();
        }

        public boolean owns(Class clazz) {
            return false;
        }
    } // end of MyModule
    
    public static final class MyModule2 extends MyModule {
        static MyModule2 INSTANCE2;
        
        public MyModule2() {
            INSTANCE2 = this;
        }
        public String getCodeNameBase() {
            return "my.module2";
        }
        
        public boolean isEnabled() {
            return false;
        }
    }
}
