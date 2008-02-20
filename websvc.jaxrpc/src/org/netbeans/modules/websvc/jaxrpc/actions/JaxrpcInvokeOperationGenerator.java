/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.jaxrpc.actions;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.lang.model.element.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.IndentEngine;
import org.openide.text.NbDocument;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.jaxrpc.ServiceInformation;
import org.netbeans.modules.websvc.jaxrpc.client.wizard.ClientBuilder;
import org.netbeans.modules.websvc.wsdl.config.PortInformationHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Peter Williams
 */
public class JaxrpcInvokeOperationGenerator {

    // {0} = service name (as type, e.g. "FooSeWebService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    private static final String SERVICE_DELEGATE_BODY =
            //        "if({1} == null) '{'\n" +
            "{2} {1} = null;" +
            "\ntry '{'\n" +
            "\t\tjavax.naming.InitialContext ic = new javax.naming.InitialContext();\n" +
            "\t\t{1} = ({2}) ic.lookup(\"java:comp/env/service/{0}\");\n" +
            "\t'}' catch(javax.naming.NamingException ex) '{'\n" +
            "\t\tjava.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "\t'}'\n" +
            //        "'}'\n\n" +
            "return {1};\n";
    private static final String SERVICE_DELEGATE_METHOD_JSP_TEST = "private {0} {1}() '{'";
    // {0} = service name (as type, e.g. "FooSeWebService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    // {3} = service delegate name
    private static final String SERVICE_DELEGATE_METHOD_JSP =
            "<%!\n" +
            "private {2} {3}() '{'\n" +
            "    {2} {1} = null;\n" +
            "    try '{'\n" +
            "        javax.naming.InitialContext ic = new javax.naming.InitialContext();\n" +
            "        {1} = ({2}) ic.lookup(\"java:comp/env/service/{0}\");\n" +
            "    '}' catch(javax.naming.NamingException ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}'" +
            "    return {1};\n" +
            "'}'\n" +
            "%>\n";
    // {0} = port name (as variable, e.g. "fooPort")
    // {1} = true port name (e.g. "FooPort")
    // {2} = service delegate name (e.g. "getFooService")
    // {3} = fully qualified port name (as type, e.g. com.service.FooPortType)
    private static final String PORT_DELEGATE_BODY =
            //        "if({0} == null) '{'\n" +
            "{3} {0} = null;" +
            "\ntry '{'\n" +
            "\t\t{0} = {2}().get{1}();\n" +
            "\t'}' catch(javax.xml.rpc.ServiceException ex) '{'\n" +
            "\t\tjava.util.logging.Logger.getLogger({3}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "\t'}'\n" +
            //        "'}'\n\n" +
            "return {0};\n";
    private static final String PORT_DELEGATE_METHOD_JSP_TEST = "private {0} {1}() '{'";
    
    // {0} = port name (as variable, e.g. "fooPort")
    // {1} = true port name (e.g. "FooPort")
    // {2} = service delegate name (e.g. "getFooService")
    // {3} = fully qualified port name (as type, e.g. com.service.FooPortType)
    // {4} = port delegate name
    private static final String PORT_DELEGATE_METHOD_JSP =
            "<%!\n" +
            "private {3} {4}() '{'\n" +
            "    {3} {0} = null;\n" +
            "    try '{'\n" +
            "        {0} = {2}().get{1}();\n" +
            "    '}' catch(javax.xml.rpc.ServiceException ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({3}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}'\n" +
            "    return {0};\n" +
            "'}'\n" +
            "%>\n";
    // {0} = service operation name (e.g. "getFoo")
    // {1} = port delegate name (e.g. "getFooPort")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    private static final String OPERATION_INVOCATION_BODY =
            "\ntry '{' // This code block invokes the {0} operation on web service\n" +
            "\t{1}().{0}(/* TODO enter operation arguments */);\n" +
            "'}' catch(java.rmi.RemoteException ex) '{'\n" +
            "\tjava.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "'}' catch(Exception ex) '{'\n" +
            "\tjava.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "'}'\n";
    // {0} = service operation name (e.g. "getFoo")
    // {1} = port delegate name (e.g. "getFooPort")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    private static final String OPERATION_INVOCATION_BODY_JSP =
            "    try '{'\n" +
            "        out.println(\"result = \"+\n" +
            "            {1}().{0}(/* TODO enter operation arguments */));\n" +
            "    '}' catch(java.rmi.RemoteException ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}' catch(Exception ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}'\n";
    // {0} = service name (as type, e.g. "FooService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    // {3} = port name (as variable, e.g. "fooPort")
    // {4} = true port name (e.g. "FooPort")
    // {5} = fully qualified port name (as type, e.g. com.service.FooPortType)
    // {6} = service operation name (e.g. "getFoo")
    private static final String OPERATION_INVOCATION_NO_DELEGATES_BODY =
            "\ntry '{' // This code block invokes the {4}:{6} operation on web service\n" +
            // get service
            "\tjavax.naming.InitialContext ic = new javax.naming.InitialContext();\n" +
            "\t{2} {1} = ({2}) ic.lookup(\"java:comp/env/service/{0}\");\n" +
            // get port
            "\t{5} {3} = {1}.get{4}();\n" +
            // invoke operation
            "\t{3}.{6}(/* TODO enter operation arguments */);\n" +
            "'}' catch(javax.naming.NamingException ex) '{'\n" +
            "\t// TODO handle JNDI naming exception\n" +
            "'}' catch(javax.xml.rpc.ServiceException ex) '{'\n" +
            "\t// TODO handle service exception\n" +
            "'}' catch(java.rmi.RemoteException ex) '{'\n" +
            "\t// TODO handle remote exception\n" +
            "'}' catch(Exception ex) '{'\n" +
            "\t// TODO handle custom exceptions here\n" +
            "'}'\n";
    // {0} = service name (as type, e.g. "FooService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    // {3} = fully qualified service stub name (as type, e.g. com.service.FooService_Impl)
    // {4} = port name (as variable, e.g. "fooPort")
    // {5} = true port name (e.g. "FooPort")
    // {6} = fully qualified port name (as type, e.g. com.service.FooPortType)
    // {7} = service operation name (e.g. "getFoo")
    private static final String OPERATION_INVOCATION_JAXRPC_BODY =
            "\ntry '{' // This code block invokes the {5}:{7} operation on web service\n" +
            "\t{2} {1} = new {3}();\n" +
            "\t{6} {4} = {1}.get{5}();\n" +
            "\t{4}.{7}(/* TODO enter operation arguments*/);\n" +
            "'}' catch(javax.xml.rpc.ServiceException ex) '{'\n" +
            "\tjava.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "'}' catch(java.rmi.RemoteException ex) '{'\n" +
            "\tjava.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "'}' catch(Exception ex) '{'\n" +
            "\tjava.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "'}'\n";
    // {0} = service name (as type, e.g. "FooService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    // {3} = fully qualified service stub name (as type, e.g. com.service.FooService_Impl)
    // {4} = port name (as variable, e.g. "fooPort")
    // {5} = true port name (e.g. "FooPort")
    // {6} = fully qualified port name (as type, e.g. com.service.FooPortType)
    // {7} = service operation name (e.g. "getFoo")
    private static final String OPERATION_INVOCATION_JAXRPC_BODY_JSP =
            "    try '{'\n" +
            "        {2} {1} = new {3}();\n" +
            "        {6} {4} = {1}.get{5}();\n" +
            "        out.println(\"result = \"+\n" +
            "            {4}.{7}(/* TODO enter operation arguments*/));\n" +
            "    '}' catch(javax.xml.rpc.ServiceException ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}' catch(java.rmi.RemoteException ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}' catch(Exception ex) '{'\n" +
            "        java.util.logging.Logger.getLogger({2}.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);\n" +
            "    '}'\n";

    private static String varFromName(final String name) {
        if (name.length() > 0) {
            StringBuffer buf = new StringBuffer(name);

            // If the first character is uppercase, make it lowercase for the variable name,
            // otherwise, prefix an underscore.
            if (Character.isUpperCase(buf.charAt(0))) {
                buf.setCharAt(0, Character.toLowerCase(buf.charAt(0)));
            } else {
                buf.insert(0, '_');
            }
            return removeDots(buf).toString();
        } else {
            return "unknown"; // NOI18N
        }
    }

    private static String classFromName(final String name) {
        if (name.length() > 0) {
            StringBuffer result = new StringBuffer(name);

            if (result.length() > 0 && !Character.isUpperCase(result.charAt(0))) {
                result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
            }

            return removeDots(result).toString();
        } else {
            return "unknown"; // NOI18N
        }
    }
    // replace dots in a class/var name
    private static StringBuffer removeDots(final StringBuffer name) {
        int dotIndex;
        while ((dotIndex = name.indexOf(".")) > -1) { //NOI18N
            name.deleteCharAt(dotIndex); //delete the dot
            name.setCharAt(dotIndex, Character.toUpperCase(name.charAt(dotIndex))); // make the letter after dot uppercase
        }
        return name;
    }

    private static ClientStubDescriptor getStub(DataObject wsdlDobj) {
        ClientStubDescriptor result = null;
        FileObject wsdlFo = wsdlDobj.getPrimaryFile();

        WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(wsdlFo);
        if (clientSupport != null) {
            List clients = clientSupport.getServiceClients();
            for (Iterator iter = clients.iterator(); iter.hasNext();) {
                WsCompileClientEditorSupport.ServiceSettings settings = (WsCompileClientEditorSupport.ServiceSettings) iter.next();
                if (settings.getServiceName().equals(wsdlFo.getName())) {
                    result = settings.getClientStubDescriptor();
                    break;
                }
            }
        } else {
            // !PW Doh!  how did this happen?
        }
        return result;
    }

    private static List<ExecutableElement> getMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> pMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method : methods) {
            pMethods.add(method);

        }
        return pMethods;
    }

    private static void parse(FileObject fo, PortInformationHandler handler) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(fo.getInputStream(), handler);
    }

    private static void parse(URL url, PortInformationHandler handler) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(url.openConnection().getInputStream(), handler);
    }

    public static void insertMethodCall(final DataObject dataObj, final Node sourceNode, final Node serviceOperationNode) {

        // First, collect name of method, port, and service:
        Node serviceNode, servicePortNode;

        String wsdlName;
        String serviceName, serviceClassName, serviceVarName;
        String servicePortName, servicePortJaxRpcName, servicePortVarName, servicePortTypeName = null;
        String serviceOperationName;
        DataObject wsdlObj = null;

        try {
            servicePortNode = serviceOperationNode.getParentNode();
            serviceNode = servicePortNode.getParentNode();

            wsdlObj = serviceNode.getLookup().lookup(DataObject.class);
            serviceOperationName = serviceOperationNode.getName();
            servicePortName = servicePortNode.getName();
            servicePortJaxRpcName = classFromName(servicePortName);
            servicePortVarName = varFromName(servicePortName);
            serviceName = serviceNode.getName();
            serviceClassName = classFromName(serviceName);
            serviceVarName = varFromName(serviceName);

        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            String message = NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }

        EditorCookie ec = dataObj.getCookie(EditorCookie.class);
        JEditorPane pane = ec.getOpenedPanes()[0];
        // Collect up any and all errors for display in case of problem
        ArrayList errors = new ArrayList();

        String servicePackageName;
        ServiceInformation serviceInfo = serviceNode.getCookie(ServiceInformation.class);
        PortInformationHandler portInformation = (PortInformationHandler) serviceInfo.getPortInformation();
        List portInfoList = null;
        List wsdlLocationsList = serviceInfo.getPortInformation().getImportedSchemas();
        if (wsdlLocationsList != null && wsdlLocationsList.size() > 0) {
            PortInformationHandler handler = new PortInformationHandler(portInformation.getTargetNamespace(),
                    portInformation.getServices(),
                    portInformation.getEntirePortList(),
                    portInformation.getBindings(),
                    wsdlLocationsList);
            Iterator it = wsdlLocationsList.iterator();
            while (it.hasNext()) {
                String wsdlLocation = (String) it.next();
                try {
                    if (wsdlLocation.indexOf("/") < 0) { //local
                        dataObj.getPrimaryFile();
                        WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(dataObj.getPrimaryFile());
                        FileObject wsdlFo = clientSupport.getWsdlFolder().getFileObject(wsdlLocation);
                        if (wsdlFo != null) {
                            parse(wsdlFo, handler);
                        }
                    } else { // remote
                        URL wsdlURL = new URL(wsdlLocation);
                        try {
                            parse(wsdlURL, handler);
                        } catch (java.net.UnknownHostException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                            return;
                        }
                    }
                } catch (ParserConfigurationException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsdlParseFailure", ex.getMessage()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                //return result;
                } catch (SAXException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsdlParseFailure", ex.getMessage()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                //return result;
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_ClientIOError", wsdlLocation, ex.getMessage()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
            portInfoList = handler.getEntirePortList();
        } else {
            portInfoList = serviceInfo.getServicePorts(serviceName);
        }
        if (serviceInfo != null) {
            servicePackageName = serviceInfo.getServicePackageName();
            for (Iterator iter = portInfoList.iterator(); iter.hasNext();) {
                PortInformationHandler.PortInfo portInfo = (PortInformationHandler.PortInfo) iter.next();
                if (servicePortName.equals(portInfo.getPort())) {
                    servicePortTypeName = classFromName(portInfo.getPortType());
                    break;
                }
            }
            if (servicePortTypeName == null) {
                servicePortTypeName = servicePortJaxRpcName + "UnknownType"; // NOI18N
                errors.add(NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_CannotDetermineWebServicePortTypeName")); // NOI18N
            }
        } else {
            servicePackageName = "unknown.service.package"; // NOI18N
            servicePortTypeName = servicePortJaxRpcName + "UnknownType"; // NOI18N
            errors.add(NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_CannotLocateWebServiceInterfaces")); // NOI18N
        }

        String fqServiceClassName = servicePackageName + "." + serviceClassName;
        String fqPortTypeName = servicePackageName + "." + servicePortTypeName;

        String serviceDelegateName = "get" + serviceClassName; //NOI18N
        String portDelegateName = "get" + servicePortJaxRpcName; //NOI18N
        ClientStubDescriptor stubType = getStub(wsdlObj);
        EditorCookie cookie = sourceNode.getCookie(EditorCookie.class);

        addProjectReference(wsdlObj, sourceNode);

        // including code to JSP
        if (cookie != null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            if (stubType == null) {
                //errors.add(NbBundle.getMessage(InvokeOperationAction.class, "ERR_CannotDeterminedStubType", serviceName)); // NOI1N
                StringBuffer buf = new StringBuffer();
                buf.append(NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_FailedWebServiceInvocationCreation")); // NOI18N
                buf.append("\n"); // NOI18N
                buf.append(NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_CannotDeterminedStubType", serviceName));
                NotifyDescriptor desc = new NotifyDescriptor.Message(buf.toString(), NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return;
            }
            final javax.swing.text.StyledDocument document = cookie.getDocument();
            final StringBuffer buf = new StringBuffer();
            if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())) {
                try {
                    String testService = MessageFormat.format(SERVICE_DELEGATE_METHOD_JSP_TEST, new Object[]{fqServiceClassName, serviceDelegateName});
                    String testString = document.getText(0, document.getLength());
                    boolean serviceExists = testString.contains(testService);
                    String testPort = MessageFormat.format(PORT_DELEGATE_METHOD_JSP_TEST, new Object[]{fqPortTypeName, portDelegateName});
                    boolean portExists = testString.contains(testPort);
                    if (!serviceExists) {
                        Object[] args = new Object[]{serviceName, serviceVarName, fqServiceClassName, serviceDelegateName};
                        String method1 = MessageFormat.format(SERVICE_DELEGATE_METHOD_JSP, args);
                        buf.append("\n"); //NOI18N
                        buf.append(method1);
                    }
                    if (!portExists) {
                        Object[] args = new Object[]{servicePortVarName, servicePortJaxRpcName, serviceDelegateName, fqPortTypeName, portDelegateName};
                        String method2 = MessageFormat.format(PORT_DELEGATE_METHOD_JSP, args);
                        buf.append("\n");
                        buf.append(method2);
                    }

                } catch (BadLocationException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            String invocationBody = "";
            if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())) {
                // create the inserted text
                Object[] args = new Object[]{serviceOperationName, portDelegateName, fqServiceClassName                };
                invocationBody = MessageFormat.format(OPERATION_INVOCATION_BODY_JSP, args);

            } else if (ClientStubDescriptor.JAXRPC_CLIENT_STUB.equals(stubType.getName())) { // JAXRPC static stub
                // create the inserted text
                Object[] args = new Object[]{ //serviceOperationName, portDelegateName.getName() };
                    serviceName, serviceVarName, fqServiceClassName,
                    fqServiceClassName + "_Impl", // NOI18N // !PW Note this classname is JAXRPC implementation dependent.
                    servicePortVarName, servicePortJaxRpcName, fqPortTypeName,
                    serviceOperationName
                };
                invocationBody = MessageFormat.format(OPERATION_INVOCATION_JAXRPC_BODY_JSP, args);
            }
            final StringBuffer buf1 = new StringBuffer();
            // invocation
            buf1.append("    <%-- start web service invocation --%>\n"); //NOI18N
            buf1.append("    <h4>" + serviceName + " invocation:</h4>\n"); //NOI18N
            buf1.append("    <hr/>\n"); //NOI18N
            buf1.append("    <%\n"); //NOI18N
            buf1.append("    // TODO compute web service operation arguments here, e.g.:\n"); //NOI18N
            buf1.append("    // String arg0 = request.getParameter(\"arg0\");\n"); //NOI18N
            buf1.append(invocationBody);
            buf1.append("    %>\n"); //NOI18N
            buf1.append("    <hr/><%-- end web service invocation--%>\n"); //NOI18N
            // insert 2 parts in one atomic action
            NbDocument.runAtomic(document, new Runnable() {

                public void run() {
                    try {
                        if (buf.length() > 0) {
                            document.insertString(document.getLength(), buf.toString(), null);
                        }
                        if (buf1.length() > 0) {
                            String content = document.getText(0, document.getLength());
                            int pos = content.lastIndexOf("</body>"); //NOI18N
                            if (pos < 0) {
                                pos = content.lastIndexOf("</html>");
                            } //NOI18N
                            if (pos >= 0) { //find where line begins
                                while (pos > 0 && content.charAt(pos - 1) != '\n' && content.charAt(pos - 1) != '\r') {
                                    pos--;
                                }
                            } else {
                                pos = document.getLength();
                            }
                            document.insertString(pos, buf1.toString(), null);
                        }
                    } catch (javax.swing.text.BadLocationException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
            return;
        }
        //code for Java Class
        generateJavaClientCode(dataObj,
                stubType,
                serviceDelegateName,
                serviceName,
                fqServiceClassName,
                portDelegateName,
                fqPortTypeName,
                serviceVarName,
                servicePortVarName,
                servicePortJaxRpcName,
                serviceOperationName,
                errors);

    }

    private static void addProjectReference(DataObject wsdlDobj, Node sourceNode) {
        if (wsdlDobj != null) {
            Project clientProject = FileOwnerQuery.getOwner(wsdlDobj.getPrimaryFile());
            DataObject dObj = sourceNode.getCookie(DataObject.class);
            if (dObj != null) {
                JaxWsUtils.addProjectReference(clientProject, dObj.getPrimaryFile());
            }
        }
    }

    private static void generateJavaClientCode(final DataObject dataObj,
            final ClientStubDescriptor stubType,
            final String iServiceDelegateName,
            final String iServiceName,
            final String ifqServiceClassName,
            final String iPortDelegateName,
            final String ifqPortTypeName,
            final String iserviceVarName,
            final String iservicePortVarName,
            final String iservicePortJaxRpcName,
            final String iserviceOperationName,
            final ArrayList ierrors) {

        EditorCookie ec = dataObj.getCookie(EditorCookie.class);
        JEditorPane pane = ec.getOpenedPanes()[0];
        int caretOffset = pane.getCaretPosition();

        // create & format inserted text
        Document document = pane.getDocument();
        IndentEngine eng = IndentEngine.find(document);
        StringWriter textWriter = new StringWriter();
        // find the place where to insert the code
        int targetOffset = caretOffset; // target insertion point
        Writer indentWriter = eng.createWriter(document, targetOffset, textWriter);

        String invocationBody = "";
        if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())) {
            // create the inserted text
            Object[] args = new Object[]{iserviceOperationName, iPortDelegateName, ifqServiceClassName            };
            invocationBody = MessageFormat.format(OPERATION_INVOCATION_BODY, args);
        } else if (ClientStubDescriptor.JAXRPC_CLIENT_STUB.equals(stubType.getName())) { // JAXRPC static stub
            // create the inserted text
            Object[] args = new Object[]{ //serviceOperationName, portDelegateName.getName() };
                iServiceName, iserviceVarName, ifqServiceClassName,
                ifqServiceClassName + "_Impl", // NOI18N // !PW Note this classname is JAXRPC implementation dependent.
                iservicePortVarName, iservicePortJaxRpcName, ifqPortTypeName,
                iserviceOperationName
            };
            invocationBody = MessageFormat.format(OPERATION_INVOCATION_JAXRPC_BODY, args);
        }

        String textToInsert = "";
        try {
            indentWriter.write(invocationBody);
            indentWriter.close();
            textToInsert = textWriter.toString();
            document.insertString(targetOffset, textToInsert, null);
        } catch (BadLocationException badLoc) {
            try {
                document.insertString(targetOffset + 1, textToInsert, null);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }

        JavaSource targetSource = JavaSource.forFileObject(dataObj.getPrimaryFile());
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                String serviceDelegateName = iServiceDelegateName;
                String serviceName = iServiceName;
                String fqServiceClassName = ifqServiceClassName;
                String portDelegateName = iPortDelegateName;
                String fqPortTypeName = ifqPortTypeName;
                String serviceVarName = iserviceVarName;
                String servicePortVarName = iservicePortVarName;
                String servicePortJaxRpcName = iservicePortJaxRpcName;
                String serviceOperationName = iserviceOperationName;
                ArrayList errors = new ArrayList();
                errors.addAll(ierrors);

                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                if (typeElement != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);

                    if (stubType == null) {
                        errors.add(NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_CannotDeterminedStubType", serviceName)); // NOI18N
                    } else if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())) {        // add service and port delegate methods
                        boolean createServiceDelegate = true;
                        boolean createPortDelegate = true;

                        // find methods
                        List<ExecutableElement> allMethods = getMethods(workingCopy, typeElement);
                        for (ExecutableElement method : allMethods) {
                            if (method.getSimpleName().toString().equals(serviceDelegateName) && method.getParameters().size() == 0) {
                                if (method.getReturnType().toString().equals(fqServiceClassName)) {
                                    createServiceDelegate = false;
                                } else {
                                    serviceDelegateName += "_1";
                                }
                            }
                            if (method.getSimpleName().toString().equals(portDelegateName) && method.getParameters().size() == 0) {
                                if (method.getReturnType().toString().equals(fqPortTypeName)) {
                                    createPortDelegate = false;
                                } else {
                                    portDelegateName += "_1";
                                }
                            }
                        }


                        ClassTree modifiedClass = null;
                        int methodIndex = javaClass.getMembers().size() - 1;
                        if (createServiceDelegate) {
                            // Add service delegate
                            ModifiersTree modifiersTree = make.Modifiers(
                                    Collections.<Modifier>singleton(Modifier.PRIVATE),
                                    Collections.<AnnotationTree>emptyList());
                            Object[] args = new Object[]{serviceName, serviceVarName, fqServiceClassName                            };
                            String delegateBody = "{" + MessageFormat.format(SERVICE_DELEGATE_BODY, args) + "}";
                            MethodTree serviceDelegate = make.Method( //Not a statement block
                                    modifiersTree, // private
                                    serviceDelegateName, // operation name
                                    make.Identifier(fqServiceClassName), // return type
                                    Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                    Collections.<VariableTree>emptyList(),
                                    Collections.<ExpressionTree>emptyList(), // throws
                                    delegateBody, // body text
                                    null // default value - not applicable here, used by annotations
                                    );
                            modifiedClass = make.insertClassMember(javaClass, ++methodIndex, serviceDelegate);
                        }


                        if (createPortDelegate) {
                            // Add port delegate
                            ModifiersTree modifiersTree = make.Modifiers(
                                    Collections.<Modifier>singleton(Modifier.PRIVATE),
                                    Collections.<AnnotationTree>emptyList());
                            Object[] args = new Object[]{servicePortVarName, servicePortJaxRpcName, serviceDelegateName, fqPortTypeName                            };
                            String delegateBody = "{" + MessageFormat.format(PORT_DELEGATE_BODY, args) + "}";
                            MethodTree serviceDelegate = make.Method(
                                    modifiersTree, // public
                                    portDelegateName, // operation name
                                    make.Identifier(fqPortTypeName), // return type
                                    Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                    Collections.<VariableTree>emptyList(),
                                    Collections.<ExpressionTree>emptyList(), // throws
                                    delegateBody, // body text
                                    null // default value - not applicable here, used by annotations
                                    );
                            if (modifiedClass != null) {
                                modifiedClass = make.insertClassMember(modifiedClass, ++methodIndex, serviceDelegate);
                            } else {
                                modifiedClass = make.insertClassMember(javaClass, +methodIndex, serviceDelegate);
                            }

                            if (modifiedClass != null) {
                                workingCopy.rewrite(javaClass, modifiedClass);
                            }

                        }
                    }


                    if (errors.size() > 0) {
                        // At least one error was encountered during code insertion.  Display the list of messages.
                        StringBuffer buf = new StringBuffer(errors.size() * 100);
                        buf.append(NbBundle.getMessage(JaxrpcInvokeOperationGenerator.class, "ERR_FailedWebServiceInvocationCreation")); // NOI18N
                        buf.append("\n"); // NOI18N
                        for (Iterator iter = errors.iterator(); iter.hasNext();) {
                            buf.append(iter.next().toString());
                            buf.append("\n"); // NOI18N
                        }
                        NotifyDescriptor desc = new NotifyDescriptor.Message(buf.toString(), NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                }
            }

            public void cancel() {
            }
        };
        try {
            targetSource.runModificationTask(task).commit();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
}
