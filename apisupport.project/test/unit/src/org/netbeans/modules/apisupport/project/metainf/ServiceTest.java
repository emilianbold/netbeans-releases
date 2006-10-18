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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileUtil;

/**
 * @author pzajac
 */
public class ServiceTest extends TestBase {
    
    public ServiceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(ServiceTest.class);
    }
    
    public void testReadServices() {
        File jarFile = new File(getDataDir(),"ServiceTest.jar");
        List /*Service*/ services = Service.readServices(jarFile);
        
        Iterator it = services.iterator() ;
        
        Service service = (Service) it.next();
        assertEquals("testicek", service.getCodebase() );
        assertEquals("java.io.InputStream", service.getFileName());
        Iterator cIt = service.getClasses().iterator() ;
        assertEquals("java.io.FileInputStream",cIt.next());
        assertEquals("java.io.BufferedInputStream",cIt.next());
        assertFalse(cIt.hasNext());
        
        service = (Service) it.next();
        assertEquals("testicek", service.getCodebase() );
        assertEquals("java.util.Collection", service.getFileName());
        cIt = service.getClasses().iterator() ;
        assertEquals("java.util.ArrayList",cIt.next());
        assertEquals("java.util.Stack",cIt.next());
        assertFalse(cIt.hasNext());
    }
    
    public void testGetOnlyProjectServices() throws IOException {
        NbModuleProject module = generateStandaloneModule("module");
        FileUtil.createData(module.getSourceDirectory(), "META-INF/services/some.test.MyService");
        FileUtil.createData(module.getSourceDirectory(), "META-INF/services/.#some.test.MyService.1.2");
        assertEquals("one service", 1, Service.getOnlyProjectServices(module).size());
    }
    
}
