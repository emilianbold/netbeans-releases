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
package org.netbeans.modules.visualweb.websvcmgr.consumer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceDescriptor;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceDescriptor.JarEntry;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceManager;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceManagerExt;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderBeanInfoWriter;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderDesignInfoWriter;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderInfo;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderWriter;
import org.netbeans.modules.visualweb.websvcmgr.codegen.WrapperClientBeanInfoWriter;
import org.netbeans.modules.visualweb.websvcmgr.codegen.WrapperClientWriter;
import org.netbeans.modules.visualweb.websvcmgr.codegen.WrapperClientWriter;
import org.netbeans.modules.visualweb.websvcmgr.util.Util;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * WebServiceManagerExt implementation for the Visualweb page designer
 * 
 * @author quynguyen
 */
public class DesignerWebServiceExtImpl implements WebServiceManagerExt {
    protected static final String VW_DESIGNTIME_JAR = "vw-dt";
    
    protected static final String CONSUMER_ID = DesignerWebServiceExtImpl.class.getName();    
    private static final String WEBSVC_HOME_PROP = "websvc.home";
    private static final String USER_FILE_PROP = "user.properties.file";
    private static final String WSDL_NAME_PROP = "serviceName";
    private static final String WSDL_FILE_NAME_PROP = "wsdlFileName";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PACKAGE_DIR = "packageDir";
    private static final String DESIGNTIME_CLASSPATH = "designtime.classpath";
    private static final String wsImportCompileScriptName = "ws_import_compile.xml";
    
    private static File wsImportCompileScript;
    
    private final String userDir = System.getProperty("netbeans.user");
    
    public DesignerWebServiceExtImpl() {
    }
    
    private void notifyError(int wsType) {
        String bundleKey = (wsType == WebServiceDescriptor.JAX_WS_TYPE) ? 
            "CODEGEN_ERROR_JAXWS" : "CODEGEN_ERROR_JAXRPC"; // NOI18N

        String errorMessage = NbBundle.getMessage(DesignerWebServiceExtImpl.class, bundleKey);
        NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
        DialogDisplayer.getDefault().notify(d);
    }
    
    public boolean wsServiceAddedExt(WebServiceDescriptor wsMetadataDesc) {
        boolean result = createClientClasses(wsMetadataDesc);
        
        if (!result) {
            notifyError(wsMetadataDesc.getWsType());
            return false;
        }

        boolean jarResult = jarGeneratedClasses(wsMetadataDesc);
        if (!jarResult) {            
            removeBuildFiles(wsMetadataDesc.getXmlDescriptorFile().getParentFile());
            wsMetadataDesc.removeConsumerData(CONSUMER_ID);
            notifyError(wsMetadataDesc.getWsType());
        }else {
            wsMetadataDesc.addJar(wsMetadataDesc.getName() + "-dt.jar", VW_DESIGNTIME_JAR);
            wsMetadataDesc.addJar(wsMetadataDesc.getName() + "-dt-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);
        }
        
        return jarResult;
    }
    
    public boolean wsServiceRemovedExt(WebServiceDescriptor wsMetadataDesc) {
        removeBuildFiles(wsMetadataDesc.getXmlDescriptorFile().getParentFile());
        wsMetadataDesc.removeConsumerData(CONSUMER_ID);
        return true;
    }
    
    private boolean jarGeneratedClasses(WebServiceDescriptor wsMetadataDesc) {
        try {
            Properties antProps = createAntProperties(wsMetadataDesc);
            String antTarget = (wsMetadataDesc.getWsType() == WebServiceDescriptor.JAX_WS_TYPE) ? 
                "jaxws-dt-compile" : 
                "jaxrpc-dt-compile";
            
            ExecutorTask executorTask = ActionUtils.runTarget(FileUtil.toFileObject(getAntScript()),
                    new String[] { antTarget }, antProps);

            executorTask.waitFinished();
            return executorTask.result() == 0;
        }catch (Exception ex) {
            return false;
        }
    }
    
    private File getAntScript() {
        if (wsImportCompileScript == null) {
            wsImportCompileScript = InstalledFileLocator.getDefault().locate(
                    wsImportCompileScriptName,
                    "", // NOI18N
                    false);
        }
        
        return wsImportCompileScript;
    }
    
    private void removeBuildFiles(File directory) {
        rmDir(new File(directory, "build")); // NOI18N
        rmDir(new File(directory, "dt")); // NOI18N
        rmDir(new File(directory, "src")); // NOI18N
    }
    
    private Properties createAntProperties(WebServiceDescriptor wsMetadataDesc) throws URISyntaxException {
        String wsdlFileName = new File(wsMetadataDesc.getWsdlUrl().toURI()).getAbsolutePath();
        String serviceName = wsMetadataDesc.getName();
        
        Properties properties = new Properties();
        
        properties.put(WEBSVC_HOME_PROP, WebServiceManager.WEBSVC_HOME);
        // INFO - This build properties file contains the classpath information
        // about all the library reference in the IDE
        properties.put(USER_FILE_PROP, userDir+"/build.properties");
        properties.put(WSDL_NAME_PROP, serviceName);
        properties.put(WSDL_FILE_NAME_PROP, wsdlFileName);
        properties.put(PACKAGE_NAME, wsMetadataDesc.getPackageName());
        properties.put(PACKAGE_DIR, wsMetadataDesc.getPackageName().replace('.', '/'));
        
        // TODO: provide the dataprovider classpath also form here
        // Currently obtained by adding ${libs.jsf12-support.classpath} from USER_FILE_PROP
        File designtimeJarFile = InstalledFileLocator.getDefault().locate("modules/ext/designtime.jar", null, true);
        
        properties.put(DESIGNTIME_CLASSPATH, designtimeJarFile.getAbsolutePath());
        
        return properties;
    }
    
    private boolean createClientClasses(WebServiceDescriptor wsMetadataDesc) {
        boolean isJaxRpc = wsMetadataDesc.getWsType() == wsMetadataDesc.JAX_RPC_TYPE;
        String serviceHome = wsMetadataDesc.getXmlDescriptorFile().getParent();
        String pkgNameDir = wsMetadataDesc.getPackageName().replace('.', '/');
        
        File sourceDir = new File(serviceHome + "/src/" + pkgNameDir);
        File dtSourceDir = new File(serviceHome + "/dt/src/" + pkgNameDir);
        sourceDir.mkdirs();
        dtSourceDir.mkdirs();
        
        Map<String, String> proxyBeanNames = new HashMap<String, String>();
        Map<String, Map<String, String>> portDataProviderMap = new HashMap<String, Map<String,String>>();
        DesignerWebServiceExtData data = new DesignerWebServiceExtData();
        
        data.setPortToDataProviderMap(portDataProviderMap);
        data.setPortToProxyBeanNameMap(proxyBeanNames);
        
        int portsCreated = 0;
        List<WsdlPort> ports = wsMetadataDesc.getModel().getPorts();
        
        for(WsdlPort port: ports) {
            // There will be one client wrapper class per web service port. All the classes (client wrapper class,
            // data provider classes) for the port will live in a sub-package with the port display name (in lower cases)
            Map<String, String> methodToDataProviderClassMap = new HashMap<String, String>();
            String className = port.getName() + "Client";
            String serviceClassName = wsMetadataDesc.getPackageName() + "." + wsMetadataDesc.getName();
            String javaName = port.getJavaName();
            
            File webserviceClient = new File(sourceDir, className + ".java"); // NOI18N
            File webserviceClientBeanInfo = new File(dtSourceDir, className + "BeanInfo.java"); // NOI18N
            
            WrapperClientWriter beanWriter = null;
            WrapperClientBeanInfoWriter beanInfoWriter = null;
            
            try {
                File proxyJar = null;
                for (JarEntry entry : wsMetadataDesc.getJars()) {
                    if (entry.getType().equals(JarEntry.PROXY_JAR_TYPE)) {
                        proxyJar = new File(serviceHome, entry.getName());
                    }
                }
                
                if (proxyJar == null) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "Could not find proxy jar for port: " + port.getName());
                    continue;
                }
                
                String libProperty = 
                        (wsMetadataDesc.getWsType() == wsMetadataDesc.JAX_WS_TYPE) ? "libs.jaxws21.classpath" : "libs.jaxrpc16.classpath";
                
                // Create a temporary jar so the proxy jar is not locked (for further updates + deletions)
                File tmpProxy = createTempCopy(proxyJar);
                tmpProxy.deleteOnExit();
                
                URLClassLoader classLoader = new URLClassLoader(Util.buildClasspath(tmpProxy, libProperty).toArray(new URL[0]));
                
                
                // Verify that the port getter method exists in the Service class, otherwise
                // the code generation in WrapperClientWriter will fail
                try {
                    String portImplMethod = "get" + Util.getProperPortName(port.getName());
                    Class serviceClass = classLoader.loadClass(serviceClassName);
                    serviceClass.getMethod(portImplMethod);
                }catch (Exception ex) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Invalid port: " + port.getName());
                    continue;
                }

                // Use reflection to get the proxy class methods; needed because the JAX-WS model is not
                // valid for JAX-RPC clients
                Class proxyClass = null;
                try {
                    proxyClass = classLoader.loadClass(javaName);
                }catch (ClassNotFoundException cnfe) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Could not load class: " + javaName);
                    continue;
                }
                
                java.lang.reflect.Method[] methods = proxyClass.getDeclaredMethods();
                ArrayList<java.lang.reflect.Method> methodList = new ArrayList<java.lang.reflect.Method>();
                for (int index = 0; index < methods.length; index++) {
                    methodList.add(methods[index]);
                }

                beanWriter = new WrapperClientWriter(new FileWriter(webserviceClient), wsMetadataDesc, isJaxRpc, methodList, port.getOperations());
                
                beanInfoWriter = new WrapperClientBeanInfoWriter(new FileWriter(webserviceClientBeanInfo));
                
                beanWriter.setPackage(wsMetadataDesc.getPackageName());
                beanWriter.setClassName(className);
                beanWriter.setContainedClassInfo(serviceClassName);
                beanWriter.addImport(wsMetadataDesc.getPackageName() + ".*");
                beanWriter.setPort(port);
                beanWriter.writeClass();
                beanWriter.flush();
                beanWriter.close();
                
                beanInfoWriter.setPackage(wsMetadataDesc.getPackageName());
                beanInfoWriter.setClassName(className);
                beanInfoWriter.writeBeanInfo();
                beanInfoWriter.flush();
                beanInfoWriter.close();
                
                // Now generate the data provider classes
                HashMap<String, String> methodToDataProviderTempMap = new HashMap<String,String>();
                for( Iterator iter = beanWriter.getDataProviders().iterator(); iter.hasNext(); ) {
                    DataProviderInfo dp = (DataProviderInfo)iter.next();
                    
                    // update the Port method name to dpClassName mapping
                    methodToDataProviderTempMap.put(Util.getMethodSignatureAsString(dp.getMethod()),
                            wsMetadataDesc.getPackageName() + "." + dp.getClassName());
                    
                    try {
                        // DataProvider.java
                        File dataProviderFile = new File(sourceDir, dp.getClassName() + ".java" ); // NOI18N
                        DataProviderWriter dpWriter = new DataProviderWriter( new FileWriter(dataProviderFile), dp, !isJaxRpc );
                        dpWriter.addImport( wsMetadataDesc.getPackageName() + ".*" );
                        dpWriter.writeClass();
                        dpWriter.flush();
                        dpWriter.close();
                        
                        // DataProviderBeanInfo.java
                        File dataProviderBeanInfoFile = new File( dtSourceDir, dp.getClassName() + "BeanInfo.java" ); // NOI18N
                        DataProviderBeanInfoWriter dpBeanInfoWriter = new DataProviderBeanInfoWriter( new FileWriter(dataProviderBeanInfoFile), dp );
                        dpBeanInfoWriter.writeClass();
                        dpBeanInfoWriter.flush();
                        dpBeanInfoWriter.close();
                        
                        // DataProviderDesignInfo.java
                        // Since the DesignInfo classes are going to be jarred into a separate jar, use a different dir for the source code
                        File dataProviderDesignInfoFile = new File( dtSourceDir, dp.getClassName() + "DesignInfo.java" ); // NOI18N
                        DataProviderDesignInfoWriter dpDesignInfoWriter = new DataProviderDesignInfoWriter( new FileWriter(dataProviderDesignInfoFile), dp );
                        dpDesignInfoWriter.writeClass();
                        dpDesignInfoWriter.flush();
                        dpDesignInfoWriter.close();
                        
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        return false;
                    }
                }
                
                copyIcons(dtSourceDir);
                
                // This will be persisted for next IDE session (all the persisted data
                // needs to be recorded last to prevent errors from corrupting the data structure
                portDataProviderMap.put(port.getName(), methodToDataProviderClassMap);
                proxyBeanNames.put(port.getName(), wsMetadataDesc.getPackageName() + "." + className);
                
                for (String key : methodToDataProviderTempMap.keySet()) {
                    String value = methodToDataProviderTempMap.get(key);
                    methodToDataProviderClassMap.put(key, value);
                }
                
                portsCreated++;
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return false;
            }
        }
        
        boolean result = portsCreated > 0;
        if (result) {
            wsMetadataDesc.addConsumerData(CONSUMER_ID, data);
        }
        
        return result;
    }
    
    private void copyIcons(File dtSourceDir){
        
        /**
         * Now copy the web service icon image to the folder that will be jarred up.
         */
        try {
            // Copy the Image contents from the URL  into the new file craeted in the backing folder.
            URL[] imageUrls = new URL[] {
                Util.class.getResource("/org/netbeans/modules/visualweb/websvcmgr/resources/webservice.png"),
                Util.class.getResource("/org/netbeans/modules/visualweb/websvcmgr/resources/methodicon.png"),
                Util.class.getResource("/org/netbeans/modules/visualweb/websvcmgr/resources/table_dp_badge.png")
            };
            String[] imageFileNames = new String[] { Util.getFileName(WrapperClientBeanInfoWriter.WEBSERVICE_ICON_FILENAME),
            Util.getFileName(DataProviderBeanInfoWriter.DATA_PROVIDER_ICON_FILE_NAME),
            Util.getFileName(DataProviderBeanInfoWriter.DATA_PROVIDER_ICON_FILE_NAME2)
            };
            
            for( int i = 0; i < imageUrls.length; i ++ ) {
                DataInputStream in = new DataInputStream(imageUrls[i].openStream());
                File outputFile = new File(dtSourceDir,imageFileNames[i]);
                DataOutputStream outImage = new DataOutputStream(new FileOutputStream(outputFile));
                
                byte[] bytes = new byte[1024];
                int byteCount = in.read(bytes);
                
                while ( byteCount > -1 ) {
                    outImage.write( bytes );
                    byteCount = in.read(bytes);
                }
                outImage.flush();
                outImage.close();
                in.close();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    private File createTempCopy(File src) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("proxyjar", "jar");
            java.nio.channels.FileChannel inChannel = new java.io.FileInputStream(src).getChannel();
            java.nio.channels.FileChannel outChannel = new java.io.FileOutputStream(tempFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            
            inChannel.close();
            outChannel.close();
            return tempFile;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            return null;
        }
    }
    
    private void rmDir(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory()) {
                rmDir(files[i]);
            }
            files[i].delete();
        }
        dir.delete();
    }
}
