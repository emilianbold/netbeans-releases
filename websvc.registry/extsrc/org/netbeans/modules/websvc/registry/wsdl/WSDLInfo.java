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

package org.netbeans.modules.websvc.registry.wsdl;

import org.openide.ErrorManager;
import org.netbeans.modules.websvc.registry.jaxrpc.Wsdl2Java;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.nodes.WebServicesPortNode;
import org.netbeans.modules.websvc.registry.util.Util;
import com.sun.xml.rpc.processor.ProcessorOptions;
import com.sun.xml.rpc.processor.config.Configuration;
import com.sun.xml.rpc.processor.config.WSDLModelInfo;
import com.sun.xml.rpc.processor.model.Operation;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import com.sun.xml.rpc.processor.modeler.ModelerException;
import com.sun.xml.rpc.processor.modeler.wsdl.WSDLModelerBase;
import com.sun.xml.rpc.processor.util.ClientProcessorEnvironment;
import com.sun.xml.rpc.spi.model.JavaInterface;
import com.sun.xml.rpc.spi.model.Model;
import com.sun.xml.rpc.spi.model.Service;
import com.sun.xml.rpc.util.JAXRPCClassFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.openide.util.NbBundle;






/**
 *
 * @author  Winston Prakash
 */
public class WSDLInfo {
    
    File errorFile;
    private String program ="wscompile";
    private String outputDir;
    private String nonClassOutputDir;
    private String sourceOutputDir;
    private URL wsdlUrl;
    private String packageName="";
    private String errorMessage;
    private boolean hasOperations;
    
    /**
     * The following is taken from com.sun.xml.rpc.processor.modeler.wsdl.WSDLModelerBase where it's used to set
     * the documentation property on Model.
     */
    private static final String WSDL_DOCUMENTATION = "com.sun.xml.rpc.processor.modeler.wsdl.documentation";
    
    public static final String SERVICE_DISPLAYNAME_TOKEN = "%%DISPLAY_NAME%%";
    public static final String TOKEN_SEPARATOR = "@";
    public static final String SIG_SEPARATOR = "|";
    public static final String RETURN_SEPARATOR = "!";
    public static final String PARAM_SEPARATOR = ":";
    
    private Model wsdlModel;
    Wsdl2Java wsdl2Java;
    
    boolean removeGeneratedFiles = true;
    
    public WSDLInfo() {
        try{
            errorFile = File.createTempFile("wscompile","error");
        }catch(IOException exc){
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
        }
    }
    
    public boolean hasOperations() {
        return hasOperations;
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
            packageName = Wsdl2Java.DEFAULT_TARGET_PACKAGE;
        }
        return packageName;
    }
    
    public String getErrorMessage(){
        StringWriter errorWriter = new StringWriter();
        StringBuffer strBuffer = new StringBuffer();
        errorWriter.write("<B>" + NbBundle.getMessage(WSDLInfo.class, "WSDLINFO_ERROR_PARSING") + " </B>");
        errorWriter.write("<BR><B><I>" + errorMessage + "</I></B>");
        return errorWriter.toString();
    }
    
    public void setOutputDirectory(String dir){
        outputDir = dir;
    }
    
    public String getOutputDirectory(){
        if (outputDir == null){
            File outDir = new File(System.getProperty("user.home"));
            return outDir.getAbsolutePath();
        }
        return outputDir;
    }
    
    public void setRemoveGeneratedFiles(boolean remove){
        removeGeneratedFiles = remove;
    }
    
    public boolean create() {
        /*
        wsdl2Java = new Wsdl2Java();
        wsdl2Java.setOutputDirectory(getOutputDirectory());
        wsdl2Java.setWsdlUrl( getWsdlUrl());
        if(!wsdl2Java.execute(errorFile)) {
            return false;
        }
        //if (removeGeneratedFiles) removeTemporaryFiles()
         
         */
        
        /**
         * First we need to get create a JAXRPC WSDLModeler
         */
        
        WSDLModelInfo modelInfo = new WSDLModelInfo();
        ClientProcessorEnvironment env = new ClientProcessorEnvironment(new ByteArrayOutputStream(), null, null);
        
        Configuration config = new Configuration(env);
        
        modelInfo = new WSDLModelInfo();
        
        config.setModelInfo(modelInfo);
        modelInfo.setParent(config);
        
        
        modelInfo.setLocation(getWsdlUrl().toExternalForm());
        Properties options = new Properties();
        options.put(ProcessorOptions.VALIDATE_WSDL_PROPERTY, "true");
        options.put(ProcessorOptions.SEARCH_SCHEMA_FOR_SUBTYPES, "true");
        options.put(ProcessorOptions.USE_WSI_BASIC_PROFILE, "false");
        
        
        WSDLModelerBase modeler = JAXRPCClassFactory.newInstance().createWSDLModeler(modelInfo, options);
        
        /**
         * Now that we have the modeler, we need to build a model.
         */
        Model tmpModel = null;
        try {
            tmpModel = modeler.buildModel();
        } catch(/*Modeler*/Exception me) {
            /**
             * try to find a ParseException in the nested exceptions to get the cause
             */
            errorMessage = " Exception = " + me.getLocalizedMessage();;
            return false;
        }
        
        this.setWSDLModel(tmpModel);
        
        return true;
    }
    
    private void setWSDLModel(Model inModel) {
        wsdlModel = inModel;
    }
    
    public Set getWebServices(){
        Set webServices = new HashSet();
        Model model = getWSDLModel();
        if(null != model) {
            for (Iterator services = model.getServices(); services.hasNext(); ) {
                WebServiceData wsData = new  WebServiceData() ;
                wsData.setURL(getWsdlUrl().toString());
                Service service = (Service) services.next();
                
                String serviceName = service.getJavaIntf().getName();
                wsData.setName(serviceName);
                serviceName = serviceName.substring(serviceName.lastIndexOf('.') + 1, serviceName.length());
                String hotsName = getWsdlUrl().getHost();
                String displayName = serviceName;
                wsData.setDisplayName(displayName);
                
                Iterator ports = service.getPorts();
                while (ports.hasNext()) {
                    Port port = (Port)ports.next();
                    /**
                     * We need to set a property on the Port for the name because QNAME is not a persistable
                     * as a JavaBean.
                     */
                    port.setProperty(WebServiceData.PORT_PROPERTY_NAME, port.getName().getLocalPart());
                    wsData.addPort(port);
                    wsData.setWebServiceAddress(port.getAddress());
                    String portName = port.getJavaInterface().getRealName();
                    portName = portName.substring(portName.lastIndexOf('.') + 1, portName.length());
                    wsData.setProxy(portName);
                    /**
                     * Iterator operations = port.getOperations();
                     * int methodCount=0;
                     * while (operations.hasNext()) {
                     * Operation operation = (Operation) operations.next();
                     * JavaMethod method = operation.getJavaMethod();
                     * wsData.addMethod(method.getName());
                     * }
                     */
                }
                webServices.add(wsData);
            }
        }
        
        return webServices;
    }
    
    private  Model getWSDLModel() {
        return wsdlModel;
    }
    /**
     * This method will use the Model to generated service information HTML.  The service DISPLAY_NAME
     * will be a token that expected to ge set by the calling class to allow the caller of this method to
     * control the display name.
     */
    public String getServiceInfo(String inServiceName) {
        StringWriter strWriter = new StringWriter();
        
        
        Model model = getWSDLModel();
        if(null != model) {
            Iterator services = model.getServices();
            for (int ii=0; services.hasNext();ii++ ) {
                Service service = (Service) services.next();
                String serviceName = service.getJavaIntf().getName();
                if(serviceName.equals(inServiceName)) {
                    serviceName = serviceName.substring(serviceName.lastIndexOf('.') + 1, serviceName.length());
                    strWriter.write("<a name=\"TOPOFPAGE\">&nbsp</a>");
// !HIE                   strWriter.write("<FORM NAME=\"service_form." + serviceName + "\" BGCOLOR=\"#e6e6ff\"><P>");
                    strWriter.write("<FORM NAME=\"service_form." + serviceName + "\">");
                    strWriter.write("<TABLE><TR>");
                    strWriter.write("<TD  VALIGN=TOP><B>" + NbBundle.getMessage(WSDLInfo.class, "SERVICE_WSDL_NAME") + "</B></TD>");
                    strWriter.write("<TD  VALIGN=TOP>" + serviceName + "</TD>");
                    strWriter.write("</TR><TR>");
                    strWriter.write("<TD  VALIGN=TOP><LABEL FOR=\"service_name." + serviceName +"\" ><B>"
                    + NbBundle.getMessage(WSDLInfo.class, "SERVICE_DISPLAY_NAME") + "</B></LABEL></TD>");
                    strWriter.write("<TD  VALIGN=TOP><INPUT TYPE=TEXT ID=\"service_name." + serviceName + "\" NAME=\"service_name."
                    + serviceName + "\" VALUE=\"" +
                    this.SERVICE_DISPLAYNAME_TOKEN +
                    "\" STYLE=\"background: #ade7ff\"></TD>");
                    strWriter.write("</TR><TR>");
                    strWriter.write("<TD  VALIGN=TOP><LABEL FOR=\"package_name." + serviceName +"\" ><B>"
                    + NbBundle.getMessage(WSDLInfo.class, "CLIENT_PACKAGE_NAME") + "</B></LABEL></TD>");
                    strWriter.write("<TD  VALIGN=TOP><INPUT TYPE=TEXT SIZE=30 ID=\"package_name." + serviceName
                    + "\" NAME=\"package_name." + serviceName + "\" VALUE=\"" +
                    Wsdl2Java.DEFAULT_TARGET_PACKAGE + "." + serviceName.toLowerCase() +
                    "\" STYLE=\"background: #ade7ff\"></TD>");
                    strWriter.write("</TR>");
                    String serviceDocumentationContent = (String)service.getProperty(this.WSDL_DOCUMENTATION);
                    
                    if(null != serviceDocumentationContent) {
                        strWriter.write("<TR>");
                        strWriter.write("<TD  VALIGN=TOP><LABEL><B>" + NbBundle.getMessage(WSDLInfo.class, "SERVICE_DOCUMENTATION") + "</B></LABEL></TD>");
                        strWriter.write("<TD  VALIGN=TOP>" + serviceDocumentationContent + "</TD>");
                        strWriter.write("</TR>");
                    }
                    Iterator ports = service.getPorts();
                    if(!ports.hasNext()) {
                        strWriter.write("</TABLE>");
                    }
                    while (ports.hasNext()) {
                        Port port = (Port)ports.next();
                        JavaInterface javaInterface = port.getJavaInterface();
                        //strWriter.write("Port Name: " + javaInterface.getName() + "\n");
                        String proxy = javaInterface.getName();
                        proxy = proxy.substring(proxy.lastIndexOf('.') + 1, proxy.length());
                        //strWriter.write("Proxy: " + proxy + "_Stub" + "\n");
                        strWriter.write("<TR>");
                        strWriter.write("<TD  VALIGN=TOP><B>" + NbBundle.getMessage(WSDLInfo.class, "WEB_SERVICE_PORTNNAME") + "</B></TD>");
                        strWriter.write("<TD  VALIGN=TOP>"  +port.getName().toString() +"</TD>");
                        strWriter.write("</TR>");
                        strWriter.write("<TR>");
                        strWriter.write("<TD  VALIGN=TOP><B>" + NbBundle.getMessage(WSDLInfo.class, "WEB_SERVICE_ADDRESS") + "</B></TD>");
                        strWriter.write("<TD  VALIGN=TOP>"  +port.getAddress() +"</TD>");
                        strWriter.write("</TR>");
                        Iterator operations = port.getOperations();
                        strWriter.write("<TR>");
                        strWriter.write("<TD  VALIGN=TOP><B>" + NbBundle.getMessage(WSDLInfo.class, "METHODS") + "</B></TD>");
                        strWriter.write("</TR>");
                        int methodCount=0;
                        while (operations.hasNext()) {
                            hasOperations=true;
                            Operation operation = (Operation) operations.next();
                            JavaMethod method = operation.getJavaMethod();
                            strWriter.write("<TR>");
                            strWriter.write("<TD VALIGN=TOP>");
                            /**
                             * Since the neither JavaParameter nor Operation override "equals()", we need to jump through
                             * some hoops to compare the method signatures for methods with the same name. The pattern is
                             *
                             * actionTOKEN_SEPARATORserviceTOKEN_SEPARATORportTOKEN_SEPARATORmethodTOKEN_SEPARATORSIG_SEPARATORreturntypePARAM_SEPARATORparametertypePARAM_SEPARATORparametertype:..:
                             *
                             * -David Botterill 4/22/2004
                             */
                            String signatureString = method.getReturnType().getRealName() + RETURN_SEPARATOR;
                            Iterator paramameters = method.getParameters();
                            while (paramameters.hasNext()) {
                                JavaParameter currentParam = (JavaParameter)paramameters.next();
                                signatureString += currentParam.getType().getRealName() + PARAM_SEPARATOR;
                            }
                            
                            strWriter.write("<A HREF=\"" + "test" + TOKEN_SEPARATOR +
                            serviceName + TOKEN_SEPARATOR +
                            port.getName().getLocalPart() + TOKEN_SEPARATOR +
                            method.getName() + TOKEN_SEPARATOR +
                            SIG_SEPARATOR +
                            signatureString +
                            "\">" + NbBundle.getMessage(WSDLInfo.class, "TEST_METHOD")+ "</A>");
                            strWriter.write("</TD>");
                            //                            strWriter.write("<TD COLSPAN=2  VALIGN=TOP>");
                            strWriter.write("<TD VALIGN=TOP>");
// !HIE                            strWriter.write("<UL><LI>");
                            strWriter.write("<I>" + method.getReturnType().getRealName() + "</I>&nbsp");
                            strWriter.write("<B>" + method.getName() + "</B>(");
                            Iterator params = method.getParameters();
                            while (params.hasNext()) {
                                strWriter.write("<I>");
                                JavaParameter param = (JavaParameter)params.next();
                                String parameterType = Util.getParameterType(port,param);
                                strWriter.write(parameterType);
                                if(params.hasNext()) strWriter.write(", ");
                                strWriter.write("</I>");
                            }
                            strWriter.write(")");
                            Iterator exceptions = method.getExceptionsList().iterator();
                            while (exceptions.hasNext()) {
                                /**
                                 * Bugid: 4970323 - The return type of an exceptions is a String not a JavaException.  This
                                 * can only be know for sure by reading the current JavaException code since the "JavaMethod.getExceptions()"
                                 * method returns an Iterator and the javadoc says nothing of it being a String.
                                 */
                                //JavaException exc = (JavaException)exceptions.next();
                                //strWriter.write(exc.getName());
                                /**
                                 * Make sure we don't get back a null or an empty String.  Check to make sure
                                 * the Object is a string in case the API matures and JavaException turns back into a real object with
                                 * a "getName" method.
                                 */
                                Object currentException = exceptions.next();
                                if(null != currentException &&
                                currentException instanceof String &&
                                ((String)currentException).length() > 0) {
                                    strWriter.write("<I> &nbsp throws &nbsp ");
                                    strWriter.write((String)currentException);
                                    if(params.hasNext()) strWriter.write(", &nbsp");
                                    strWriter.write("</I>");
                                }
                            }
                            //strWriter.write("<BR>");
                            String operationDocumentationContent = (String)operation.getProperty(this.WSDL_DOCUMENTATION);
                            
                            if(null != operationDocumentationContent) {
// !HIE                                strWriter.write("<BR><B>" + NbBundle.getMessage(WSDLInfo.class, "METHOD_DOCUMENTATION") + "</B>");
                                strWriter.write("<BR>" + operationDocumentationContent);
                            }
// !HIE                            strWriter.write("</LI></UL>");
                            strWriter.write("</TD>");
                            strWriter.write("</TR>");
                        }
                    }
                    strWriter.write("</TABLE>");
                    strWriter.write("</FORM>");
                    if(services.hasNext()) {
                        strWriter.write("<HR>");
                    }
                    /**
                     * we've processed the requested service so break
                     */
                    break;
                }
                
                
            }
        } else {
            strWriter.write("<BR><B>" +  NbBundle.getMessage(WSDLInfo.class, "WSDLINFO_ERROR_PARSING") +"</B><BR> ");
            
        }
        
        return strWriter.toString();
    }
    
    public static void main(String[] args){
        System.setProperty("http.proxyHost", "webcache.sfbay.sun.com");
        System.setProperty("http.proxyPort", "8080");
        
        WSDLInfo wsdlInfo = new WSDLInfo();
        File wsdlFile = new File("D:\\realtimeservice.wsdl");
        
        try{
            wsdlInfo.setWsdlUrl(wsdlFile.toURL());
            //wsdlInfo.setWsdlUrl(new URL("http://www.lixusnet.com/lixusnet/AddFinder.jws?wsdl"));
        }catch (Exception exc){
            exc.printStackTrace();
            System.exit(-1);
        }
        
        if(wsdlInfo.create()) {
            wsdlInfo.getServiceInfo("RealTimeService");
        }else{
            System.out.println(wsdlInfo.getErrorMessage());
        }
    }
}
