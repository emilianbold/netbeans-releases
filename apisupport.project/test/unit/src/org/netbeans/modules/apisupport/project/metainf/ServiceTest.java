/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.metainf;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author pzajac
 */
public class ServiceTest extends NbTestCase {
    
    public ServiceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ServiceTest.class);
        
        return suite;
    }

    /**
     * Test of ahoj method, of class org.netbeans.modules.apisupport.project.metainf.Service.
     */
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
    
}
