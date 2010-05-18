/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.websvcmgr.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.websvcmgr.test.RunCommand;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.test.SetupData;
import org.netbeans.modules.websvc.manager.test.SetupUtil;
import org.openide.modules.InstalledFileLocator;


/**
 *
 * @author quynguyen
 */
public class DesignerWebServiceExtImplTest extends NbTestCase {
    static final String CLIENT_JAR_PATH = "/org/netbeans/modules/visualweb/websvcmgr/test/resources/USZip.jar";
    static final String SRC_JAR_PATH = "/org/netbeans/modules/visualweb/websvcmgr/test/resources/USZip-src.jar";
    
    private SetupData data;
    private WebServiceData wsData;
    
    
    public DesignerWebServiceExtImplTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        String javaHome = System.getProperty("java.home");
        assertTrue("This unit test will not complete successfully without the Java SDK", 
                new File(javaHome, "../lib/tools.jar").exists());
        
        data = SetupUtil.commonSetUp(getWorkDir());
        wsData = new WebServiceData(
                    data.getLocalWsdlFile().getAbsolutePath(),
                    data.getLocalOriginalWsdl().toURI().toURL().toExternalForm(),
                    WebServiceListModel.DEFAULT_GROUP);
        
        wsData.setPackageName("websvc");
        wsData.setCatalog(data.getLocalCatalogFile().getAbsolutePath());
        WebServiceManager.getInstance().addWebService(wsData, false);
        
        wsData.setCompiled(true);
        wsData.setState(WebServiceData.State.WSDL_SERVICE_COMPILED);
        
        File jarsDir = new File(data.getWebsvcHome(), "USZip/jaxws");
        jarsDir.mkdirs();
        
        File clientJar = new File(jarsDir, "USZip.jar");
        File srcJar = new File(jarsDir, "USZip-src.jar");
        
        SetupUtil.retrieveURL(clientJar, DesignerWebServiceExtImplTest.class.getResource(CLIENT_JAR_PATH));
        SetupUtil.retrieveURL(srcJar, DesignerWebServiceExtImplTest.class.getResource(SRC_JAR_PATH));

        assertTrue("JAX-WS proxy jar was not copied", clientJar.exists());
        assertTrue("JAX-WS source jar was not copied", srcJar.exists());
        
        WebServiceDescriptor descriptor = new WebServiceDescriptor(
                wsData.getName(), wsData.getPackageName(), 
                WebServiceDescriptor.JAX_WS_TYPE, 
                new File(wsData.getWsdlFile()).toURI().toURL(), 
                new File(jarsDir, "descriptor.xml"), wsData.getWsdlService());
        
        descriptor.addJar("USZip.jar", WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE);
        descriptor.addJar("USZip-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);
        
        wsData.setJaxWsDescriptor(descriptor);
        wsData.setJaxWsDescriptorPath(descriptor.getXmlDescriptor());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        WebServiceManager.getInstance().removeWebService(wsData);
        SetupUtil.commonTearDown();
    }
    
    public void testCreateClientClasses() throws Exception {
       DesignerWebServiceExtImpl impl = new DesignerWebServiceExtImpl();
        impl.createClientClasses(wsData.getJaxWsDescriptor());
        
        Properties antProps = impl.createAntProperties(wsData.getJaxWsDescriptor());
        Properties buildProps = new Properties();
        buildProps.load(new FileInputStream(new File(System.getProperty("netbeans.user"), "build.properties")));
        
        File proxyJar = new File(data.getWebsvcHome(), "USZip/jaxws/USZip.jar");
        List<String> cp = buildCompileClasspath(proxyJar, buildProps, antProps);
        File endorsedDir = new File(System.getProperty("java.endorsed.dirs"));
        File srcDir = new File(data.getWebsvcHome(), "USZip/jaxws/src");
        
        boolean result = compile(srcDir, cp, endorsedDir);
        assertTrue("Web service client and designtime classes failed to compile", result);
        
        File soap12Client = new File(srcDir, "websvc/USZipSoap12Client.class");
        File validateZip12 = new File(srcDir, "websvc/USZipSoap12ValidateZip.class");
        
        File soapClient = new File(srcDir, "websvc/USZipSoapClient.class");
        File validateZip = new File(srcDir, "websvc/USZipSoapValidateZip.class");
        
        assertTrue("USZipSoap12Client.java not compiled", soap12Client.exists());
        assertTrue("USZipSoapClient.java not compiled", soapClient.exists());
        assertTrue("USZipSoap12ValidateZip.java not compiled", validateZip12.exists());
        assertTrue("USZipSoapValidateZip.java not compiled", validateZip.exists());
    }
    
    private List<String> buildCompileClasspath(File proxyJar, Properties... props) {
        ArrayList<String> cp = new ArrayList<String>();
        
        // ${java.home}/../lib/tools.jar
        cp.add(new File(System.getProperty("java.home"), "../lib/tools.jar").getAbsolutePath());
        cp.add(getProperty("libs.jaxws21.classpath", props));
        cp.add(getProperty("designtime.classpath", props));
        
        File dp = InstalledFileLocator.getDefault().locate("modules/ext/dataprovider.jar", null, false);
        cp.add(dp.getAbsolutePath());
        cp.add(proxyJar.getAbsolutePath());
        return cp;
        
    }
    
    private String getProperty(String propName, Properties... props) {
        for (int i = 0; i < props.length; i++) {
            String prop = props[i].getProperty(propName);
            if (prop != null && prop.length() > 0) {
                return prop;
            }
        }
        
        throw new IllegalArgumentException("Property " + propName + " not found");
    }
    
    private boolean compile(File srcPath, List<String> classpath, File endorsedDir) {
        ArrayList<String> cmdArray = new ArrayList<String>();
        
        cmdArray.add(new File(System.getProperty("java.home"), "../bin/javac").getAbsolutePath());
        cmdArray.add("-endorseddirs");
        cmdArray.add(endorsedDir.getAbsolutePath());
        
        cmdArray.add("-classpath");
        
        StringBuffer cpBuffer = new StringBuffer();
        String sep = System.getProperty("path.separator");
        for (String cpElement : classpath) {
            cpBuffer.append(cpElement + sep);
        }
        cpBuffer.append(srcPath.getAbsolutePath());
        
        cmdArray.add(cpBuffer.toString());
        
        List<String> javaFiles = new LinkedList<String>();
        getJavaFiles(srcPath, javaFiles);
        
        for (String javaFile : javaFiles) {
            cmdArray.add(javaFile);
        }
        
        try {
            RunCommand runCommand = new RunCommand();
            runCommand.execute(cmdArray.toArray(new String[cmdArray.size()]));
            int returnStatus = runCommand.getReturnStatus();
            return returnStatus == 0;
        }catch (IOException ex) {
            return false;
        }
    }
    
    private void getJavaFiles(File srcDir, List<String> files) {
        if (!srcDir.isDirectory()) {
            return;
        }else {
            File[] subFiles = srcDir.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                if (subFiles[i].isDirectory()) {
                    getJavaFiles(subFiles[i], files);
                }else if (subFiles[i].getName().endsWith(".java")) {
                    files.add(subFiles[i].getAbsolutePath());
                }
            }
        }
    }
    
}
