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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author gpatil
 */
public class EARProjectTest extends NbTestCase {
    private File scratchDir = null;

    private static final String EJB_IN_EAR_JAR_FILE = "projects/earEJBWeb.ear"; // NOI18N
    private static final String EJB_IN_EAR = "!/ejbPortNameMissing.jar" ;
    private static final String EJB_IN_EAR_EPTS = "[EndPoint(Provider):service:{http://services/fm}AlarmIRP:port:AlarmIRPPortTypePort:portType:{http://services/fm}AlarmIRPPortType]" ; //NOI18N

    private static final String WEB_IN_EAR_JAR_FILE = "projects/earEJBWeb.ear"; // NOI18N
    private static final String WEB_IN_EAR = "!/webAppDefault.war" ;
    private static final String WEB_IN_EAR_EPTS = "[EndPoint(Provider):service:{http://alldef.corp.com/}WsAllDefaultService:port:WsAllDefaultPort:portType:{http://alldef.corp.com/}WsAllDefault]" ; //NOI18N
    
    private static final String EAR_WITH_EJB_WEB = "projects/earEJBWeb.ear"; // NOI18N
    private static final String EAR_SU_WITH_EJB_WEB = "projects/earEJBWeb_jbi.ear"; // NOI18N
    
    private static final Logger logger = Logger.getLogger(EARProjectTest.class.getName());

    public EARProjectTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        FileObject scratch = TestUtil.makeScratchDir(this);
        this.scratchDir = FileUtil.toFile(scratch);
    }

    @Override
    public void tearDown() throws Exception {
        TestUtil.deleteRec(scratchDir);
    }
    
    public void testEjbInEarEndpointScan() throws MalformedURLException, IOException{
        File f = new File(getDataDir().getAbsolutePath(), EJB_IN_EAR_JAR_FILE);
        URL url = f.toURL();
        url = new URL("jar:" + url.toString() + EJB_IN_EAR);
        JarInJarProject project = new JarInJarProject(url);
        List<Endpoint> list = project.getWebservicesEndpoints();
        assertEquals(EJB_IN_EAR_EPTS, list.toString());
    }

    public void testWebInEarEndpointScan() throws MalformedURLException, IOException{
        File f = new File(getDataDir().getAbsolutePath(), WEB_IN_EAR_JAR_FILE);
        URL url = f.toURL();
        url = new URL("jar:" + url.toString() + WEB_IN_EAR);
        JarInJarProject project = new JarInJarProject(url);
        List<Endpoint> list = project.getWebservicesEndpoints();
        assertEquals(WEB_IN_EAR_EPTS, list.toString());
    }
    
    public void testSUForEarWithWebEjb() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), EAR_WITH_EJB_WEB);
        File eF = new File(getDataDir().getAbsolutePath(), EAR_SU_WITH_EJB_WEB);
        EnterpriseAppProject earProject = new EnterpriseAppProject(f.getAbsolutePath());
        String ret = earProject.createJar(this.scratchDir.getAbsolutePath(), null);
        assertTrue(ret.endsWith("ear")); //NOI18N
        JarFile expected = new JarFile(eF);
        JarFile actual = new JarFile(ret);
        assertTrue("Generated jar with jbi.xml did not macth:", CompAppTestUtil.compareJar(expected, actual));
    }
}
