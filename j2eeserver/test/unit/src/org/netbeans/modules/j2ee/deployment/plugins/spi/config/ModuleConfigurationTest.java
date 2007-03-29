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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.tests.j2eeserver.devmodule.TestJ2eeModuleImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sherold
 */
public class ModuleConfigurationTest extends NbTestCase {

    private J2eeModule j2eeModule;
    private TestJ2eeModuleImpl j2eeModuleImpl;
    
    /** Creates a new instance of J2eeModuleTest */
    public ModuleConfigurationTest(String testName) {
        super(testName);
    }
        
    @Override
    protected void setUp() throws Exception {
        File dataDir = getDataDir();
        File rootFolder = new File(getDataDir(), "/sampleweb");
        FileObject samplewebRoot = FileUtil.toFileObject(rootFolder);
        j2eeModuleImpl = new TestJ2eeModuleImpl(samplewebRoot);
        j2eeModule = J2eeModuleFactory.createJ2eeModule(j2eeModuleImpl);
    }
    
    public void testCreateJ2eeModule() throws Exception {
        ModuleConfigurationImpl conf = ModuleConfigurationImpl.create(j2eeModule);
        ContextRootConfiguration contextRootConfiguration = conf.getLookup().lookup(ContextRootConfiguration.class);
        String contextRoot = "mycontext";
        contextRootConfiguration.setContextRoot(contextRoot);
        assertEquals(contextRoot, contextRootConfiguration.getContextRoot());
    }
    
    private static class ModuleConfigurationImpl implements ModuleConfiguration, ContextRootConfiguration, PropertyChangeListener {
        
        private final J2eeModule j2eeModule;
        private String context;
        
        private ModuleConfigurationImpl(J2eeModule j2eeModule) {
            this.j2eeModule = j2eeModule;
        }
        
        public static ModuleConfigurationImpl create(J2eeModule j2eeModule) {
            ModuleConfigurationImpl moduleConfigurationImpl = new ModuleConfigurationImpl(j2eeModule);
            j2eeModule.addPropertyChangeListener(moduleConfigurationImpl);
            return moduleConfigurationImpl;
        }
        
        public J2eeModule getJ2eeModule() {
            return j2eeModule;
        }

        public void dispose() {
            j2eeModule.removePropertyChangeListener(this);
        }

        public Lookup getLookup() {
            return Lookups.fixed(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
        }

        public String getContextRoot() throws ConfigurationException {
            return context;
        }

        public void setContextRoot(String contextRoot) throws ConfigurationException {
            context = contextRoot;
        }
    }
    
    
}
