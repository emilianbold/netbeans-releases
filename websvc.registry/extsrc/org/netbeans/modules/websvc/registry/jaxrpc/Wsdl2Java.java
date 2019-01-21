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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.registry.jaxrpc;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.registry.util.Util;
import org.netbeans.modules.websvc.registry.util.WebProxySetter;
import org.netbeans.modules.websvc.registry.model.WebServiceData;

import com.sun.xml.rpc.spi.JaxRpcObjectFactory;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.spi.model.Model;
import com.sun.xml.rpc.spi.model.Service;
import com.sun.xml.rpc.spi.tools.CompileTool;
import com.sun.xml.rpc.spi.tools.GeneratedFileInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.awt.StatusDisplayer;



/**
 * @author  Winston Prakash
 */
public class Wsdl2Java {
    
    private String program ="wscompile";
    private String outputDir;
    private String nonClassOutputDir;
    private String sourceOutputDir;
    private URL wsdlUrl;
    private String packageName;
    private JaxRpcObjectFactory jaxprObjectFactory ;
    private CompileTool compTool;
    private File webserviceClient;
    private File webserviceClientBeanInfo;
    
    
    public final static String DEFAULT_TARGET_PACKAGE = "webservice"; // the default location to generate the java source
    
    public Wsdl2Java() {
        
        System.setProperty("http.proxyHost", WebProxySetter.getInstance().getProxyHost());
        System.setProperty("http.proxyPort", WebProxySetter.getInstance().getProxyPort());
        
        jaxprObjectFactory = JaxRpcObjectFactory.newInstance();
    }
    
    public void setOutputDirectory(String path){
        outputDir = path;
    }
    
    public String getOutputDirectory(){
        if(outputDir == null) {
			outputDir = System.getProperty("user.home");
		}
        return outputDir;
    }
    
    public void setNonClassOutputDirectory(String path){
        nonClassOutputDir = path;
    }
    
    public String getNonClassOutputDirectory(){
        if(nonClassOutputDir == null) {
			nonClassOutputDir = getOutputDirectory();
		}
        return nonClassOutputDir;
    }
    
    public void setSourceOutputDirectory(String path){
        sourceOutputDir = path;
    }
    
    public String getSourceOutputDirectory(){
        if(sourceOutputDir == null) {
			sourceOutputDir = getOutputDirectory();
		}
        return sourceOutputDir;
    }
    
    public File getWebserviceClient() {
        return webserviceClient;
    }
    public File getWebserviceClientBeanInfo() {
        return webserviceClientBeanInfo;
    }
    
    public void setWsdlUrl(URL url){
        wsdlUrl = url;
    }
    
    public URL getWsdlUrl(){
        return wsdlUrl;
    }
    
    public void setPackageName(String pkgName){
        packageName = pkgName;
    }
    
    public String getPackageName(){
        if(packageName == null) {
            packageName = this.DEFAULT_TARGET_PACKAGE;
        }
        return packageName;
    }
    
    public boolean execute(WebServiceData inWSData, File errorFile) {
        PrintStream ps = null;
        try{
            FileOutputStream fdout = new FileOutputStream(errorFile);
            BufferedOutputStream bos = new BufferedOutputStream(fdout, 2048);
            ps = new PrintStream(bos, false);
        }catch(Exception exc){
            ps = System.out;
        }
        return execute(inWSData,ps);
    }
    
    public boolean execute(WebServiceData inWSData,OutputStream outputStream) {
        jaxprObjectFactory = JaxRpcObjectFactory.newInstance();
        compTool = jaxprObjectFactory.createCompileTool(outputStream, program);
        WSCompileArguments wsCompileArgs = new WSCompileArguments();
        
        wsCompileArgs.setClasspath(Util.getRuntimeClassPath());
        File outDir = new File(getOutputDirectory());
        if(!outDir.exists()) {
            outDir.mkdirs();
        }
        wsCompileArgs.setOutputDirectory(outDir.getAbsolutePath());
        wsCompileArgs.setGen("client");
        //wsCompileArgs.setImportGen(true);
        wsCompileArgs.setVerbose(false);
        wsCompileArgs.setKeep(false);
        /**
         * If the wsi feature is turned on, I've found some of the client code to be genereated incorrectly
         * with JAX-RPC 1.1.
         * -David Botterill 4/22/2004
         */
        //wsCompileArgs.addFeature("wsi");
        wsCompileArgs.addFeature("searchschema");
        File nonClassDir = new File(getNonClassOutputDirectory());
        if(!nonClassDir.exists()) nonClassDir.mkdirs();
        wsCompileArgs.setNonclassOutputDirectory(nonClassDir.getAbsolutePath());
        
        File srcDir = new File(getSourceOutputDirectory());
        if(!srcDir.exists()) srcDir.mkdirs();
        wsCompileArgs.setSourceOutputDirectory(srcDir.getAbsolutePath());
        
        Configuration config = new Configuration();
        WsdlType wsdl = new WsdlType();
        config.setWsdl(wsdl);
        try{
            wsdl.setLocation( getWsdlUrl());
        }catch(Exception exc){
            exc.printStackTrace();
            return false;
        }
        wsdl.setPackageName(getPackageName());
        wsCompileArgs.setConfiguration(config);

        boolean ret = false;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Wsdl2Java.class.getClassLoader());
        try {
            ret = compTool.run(wsCompileArgs.toArgs());
            if(ret) {
                createWrapperClients(inWSData);
            } else {
                ErrorManager.getDefault().log(Wsdl2Java.class.getName() + NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_INTERNAL"));
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_EXTERNAL"));
            }
        } catch (Exception exc) {
            Logger.getLogger(Wsdl2Java.class.getName()).log(Level.WARNING, NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_INTERNAL"), exc);
//            ErrorManager.getDefault().notify(exc);
//            ErrorManager.getDefault().log(Wsdl2Java.class.getName() + NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_INTERNAL"));
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_EXTERNAL"));
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }

        return ret;
    }
    
    /**
     * Create client wrapper for all the ports.
     */
    public void createWrapperClients(WebServiceData inWSData){
        File sourceDir = new File(getSourceOutputDirectory());
        String pkgName = getPackageName();
        StringTokenizer strTokenizer = new StringTokenizer(getPackageName(),".");
        while(strTokenizer.hasMoreTokens()) {
            sourceDir = new File(sourceDir,strTokenizer.nextToken());
        }
        if(!sourceDir.exists()) {
            ErrorManager.getDefault().log(Wsdl2Java.class.getName() + NbBundle.getMessage(Wsdl2Java.class, "ERROR_CREATING_SOURCEDIR"));
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Wsdl2Java.class, "ERROR_CREATING_SOURCEDIR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Wsdl2Java.class, "ERROR_CREATING_SOURCEDIR"));
        }
        Model model = compTool.getProcessor().getModel();
        if(model == null) {
            ErrorManager.getDefault().log(Wsdl2Java.class.getName() + NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_INTERNAL"));
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_EXTERNAL"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_EXTERNAL"));
            return;
        }
        for (Iterator services = model.getServices(); services.hasNext(); ) {
            Service service = (Service) services.next();
            String serviceFullName = service.getJavaIntf().getName();
            /**
             * We only want the name, not the fully qualified name.
             */
            String serviceName = serviceFullName.substring(serviceFullName.lastIndexOf(".") + 1);
            Iterator ports = service.getPorts();
            /**
             * Since there will be one jar per web service client, even for a multi-service WSDL, we want the
             * display name of the WebServiceData passed in to be used to name the client.
             */
            String displayName = inWSData.getDisplayName();
            String clientNameRoot = displayName.substring(displayName.lastIndexOf('.') + 1, displayName.length());
            
            webserviceClient = new File(sourceDir, clientNameRoot + "Client.java");
            webserviceClientBeanInfo = new File(sourceDir, clientNameRoot + "ClientBeanInfo.java");
            WrapperClientWriter beanWriter = null;
            WrapperClientBeanInfoWriter beanInfoWriter = null;
            try {
                beanWriter = new WrapperClientWriter(new FileWriter(webserviceClient));
                beanInfoWriter = new WrapperClientBeanInfoWriter(new FileWriter(webserviceClientBeanInfo));
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
//                StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_EXTERNAL"),2);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Wsdl2Java.class, "ERROR_WSCOMPILE_EXTERNAL"));
                return;
            }
            
            beanWriter.setPackage(getPackageName());
            beanWriter.setName(clientNameRoot + "Client");
            beanWriter.setContainedClassInfo(serviceName);
            beanWriter.addImport(inWSData.getPackageName() + ".*");
            beanInfoWriter.setPackage(getPackageName());
            beanInfoWriter.setName(clientNameRoot + "Client");
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                beanWriter.addPort(port);
            }
            beanWriter.writeClass();
            beanWriter.flush();
            beanWriter.close();
            beanInfoWriter.writeBeanInfo();
            beanInfoWriter.flush();
            beanInfoWriter.close();
            
        }
    }
    
    public Model getModel(){
        return compTool.getProcessor().getModel();
    }
    
    
    public File[] getGeneratedFiles(){
        return getGeneratedFiles(false);
    }
    
    public File[] getGeneratedJavaFiles(){
        return getGeneratedFiles(true);
    }
    
    public File[] getGeneratedFiles(boolean onlyJava){
        Set generatedFiles = new HashSet();
        // I had to compile JAXRPC-RI from source file and add it to external - WJP
        // com.sun.xml.rpc.processor.util.ClientProcessorEnvironment,shutDown sets
        // generatedfiles to null. So compTool.getEnvironment().getGeneratedFiles()
        // fails with NPE
        for (Iterator iter = compTool.getEnvironment().getGeneratedFiles(); iter.hasNext();) {
            Object obj = iter.next();
            File file;
            if (obj instanceof File) {
                file = (File) obj;
            }else if (obj instanceof GeneratedFileInfo) {
                file = ((GeneratedFileInfo)obj).getFile();
            }else {
                //System.out.println("Wsdl2Java.getGeneratedFile(): Found a nonFile: "+obj);
                continue;
            }
            
            if (file.exists()){
                if(onlyJava){
                    if(file.getName().endsWith(".java")) generatedFiles.add(file);
                }else{
                    generatedFiles.add(file);
                }
            }
        }
        return (File[]) generatedFiles.toArray(new File[generatedFiles.size()]);
    }
    
    public static void main(String[] args){
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "8080");
        
        Wsdl2Java wsdl2java = new Wsdl2Java();
        wsdl2java.setOutputDirectory("D:\\wsdl2java\\classes");
        //wsdl2java.setNonClassOutputDirectory("D:\\wsdl2java\\non-classes");
        //wsdl2java.setSourceOutputDirectory("D:\\wsdl2java\\sources");
        File wsdlFile = new File("D:\\wsdl2java\\JavaTravellerService.wsdl");
        //File wsdlFile = new File("D:\\wsdl2java\\AddressBook.wsdl");
        
        try{
            wsdl2java.setWsdlUrl(wsdlFile.toURI().toURL());
            //wsdl2java.setWsdlUrl(new URL("http://www.lixusnet.com/lixusnet/AddFinder.jws?wsdl"));
            //wsdl2java.setWsdlUrl(new URL("http://ws.digiposs.com/WorldTime.jws?wsdl"));
            //wsdl2java.setWsdlUrl(new URL("http://www.xmethods.net/sd/2001/TemperatureService.wsdl"));
        }catch (Exception exc){
            exc.printStackTrace();
            System.exit(-1);
        }
        
        //wsdl2java.setPackageName("wsdl2java_pkg");
        WebServiceData wsData = new WebServiceData();
        wsData.setDisplayName("JavaTraveller_service");
        if(wsdl2java.execute(wsData,System.out)) {
            System.out.println("Client Proxy Successfully created");
        }else{
            System.out.println("Client Proxy creation failed");
        }
        
        /*File[] generatedFiles = wsdl2java.getGeneratedJavaFiles();
        for(int i=0; i< generatedFiles.length; i++){
            System.out.println(generatedFiles[i].getAbsolutePath());
        }*/
    }
    
}
