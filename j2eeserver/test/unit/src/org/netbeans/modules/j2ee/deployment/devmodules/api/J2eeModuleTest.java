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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.tests.j2eeserver.devmodule.TestJ2eeModuleImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class J2eeModuleTest extends NbTestCase {

    private J2eeModule j2eeModule;
    private TestJ2eeModuleImpl j2eeModuleImpl;
    
    /** Creates a new instance of J2eeModuleTest */
    public J2eeModuleTest(String testName) {
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
    
    public void testCreateJ2eeModule() {
        assertNotNull(j2eeModule);
        assertNotNull(j2eeModuleImpl);
    }
    
    public void testPropertyChangeListener() {
        final Set propChanged = new HashSet();
        PropertyChangeListener p = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                propChanged.add(evt.getPropertyName());
            }
        };
        // check that event comes
        j2eeModule.addPropertyChangeListener(p);
        j2eeModuleImpl.firePropertyChange(J2eeModule.PROP_MODULE_VERSION, null, null);
        assertTrue(propChanged.contains(J2eeModule.PROP_MODULE_VERSION));
        // check that event does not come
        j2eeModule.removePropertyChangeListener(p);
        j2eeModuleImpl.firePropertyChange(J2eeModule.PROP_RESOURCE_DIRECTORY, null, null);
        assertFalse(propChanged.contains(J2eeModule.PROP_RESOURCE_DIRECTORY));
    }
    
    public void testGetDeploymentDescriptor() throws Exception {
        // check non-existing DDs
        assertNull(j2eeModule.getMetadataModel(EjbJarMetadata.class));
        
        // check existing DDs
        assertNotNull(j2eeModule.getMetadataModel(WebAppMetadata.class));
    }
}
