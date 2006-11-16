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

package org.netbeans.modules.websvc.core.jaxws.actions;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import static org.netbeans.api.java.source.JavaSource.Phase;
import static com.sun.source.tree.Tree.Kind.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.jaxws.JaxWsUtils;
import org.netbeans.modules.websvc.core.jaxws.nodes.OperationNode;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.IndentEngine;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/** JaxWsCodeGenerator.java
 *
 * Created on March 2, 2006
 *
 * @author mkuchtiak
 */
public class JaxWsCodeGenerator {
    
    private static final List IMPLICIT_JSP_OBJECTS = Arrays.asList(new String[]{
        "request","response","session","out","page","config","application","pageContext" //NOI18N
    });
    
    private static final String HINT_INIT_ARGUMENTS=" // TODO initialize WS operation arguments here\n"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = service field name
    private static final String JAVA_TRY =
            "\ntry '{' // Call Web Service Operation\n"; //NOI18N
    private static final String JAVA_SERVICE_DEF =
            "   {0} {7} = new {0}();\n"; //NOI18N
    private static final String JAVA_PORT_DEF =
            "   {1} port = {7}.{2}();\n"; //NOI18N
    private static final String JAVA_RESULT =
            "   {3}" + //NOI18N
            "   // TODO process result here\n" +   //NOI18N
            "   {4} result = port.{5}({6});\n"; //NOI18N
    private static final String JAVA_VOID =
            "   {3}" + //NOI18N
            "   port.{5}({6});\n"; //NOI18N
    private static final String JAVA_OUT =
            "   {8}.println(\"Result = \"+result);\n"; //NOI18N
    private static final String JAVA_CATCH =
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JAVA_STATIC_STUB_ASYNC_POLLING =
            "\ntry '{' // Call Web Service Operation(async. polling)\n" + //NOI18N
            "   {0} service = new {0}();\n" + //NOI18N
            "   {1} port = service.{2}();\n" + //NOI18N
            "   {3}" + //NOI18N
            "   // TODO process asynchronous response here\n" +   //NOI18N
            "   {4} resp = port.{5}({6});\n" + //NOI18N
            "   while(!resp.isDone()) '{'\n" + //NOI18N
            "       // do something\n" + //NOI18N
            "       Thread.sleep(100);\n" + //NOI18N
            "   '}'\n" + //NOI18N
            "   System.out.println(\"Result = \"+resp.get());\n" + //NOI18N
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = response type (e.g. FooResponse)
    private static final String JAVA_STATIC_STUB_ASYNC_CALLBACK =
            "\ntry '{' // Call Web Service Operation(async. callback)\n" + //NOI18N
            "   {0} service = new {0}();\n" + //NOI18N
            "   {1} port = service.{2}();\n" + //NOI18N
            "   {3}" + //NOI18N
            "       public void handleResponse(javax.xml.ws.Response<{7}> response) '{'\n" + //NOI18N
            "           try '{'\n" + //NOI18N
            "               // TODO process asynchronous response here\n" + //NOI18N
            "               System.out.println(\"Result = \"+ response.get());\n" + //NOI18N
            "           '}' catch(Exception ex) '{'\n" + //NOI18N
            "               // TODO handle exception\n" + //NOI18N
            "           '}'\n" + //NOI18N
            "       '}'\n" + //NOI18N
            "   '}';\n" + //NOI18N
            "   {4} result = port.{5}({6});\n" + //NOI18N
            "   while(!result.isDone()) '{'\n" + //NOI18N
            "       // do something\n" + //NOI18N
            "       Thread.sleep(100);\n" + //NOI18N
            "   '}'\n" + //NOI18N
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'\n"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB =
            "    <%-- start web service invocation --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\t// TODO process result here\n" +   //NOI18N
            "\t{4} result = port.{5}({6});\n" + //NOI18N
            "\tout.println(\"Result = \"+result);\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation --%><hr/>\n"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB_VOID =
            "    <%-- start web service invocation --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\tport.{5}({6});\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation --%><hr/>\n"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB_ASYNC_POLLING =
            "    <%-- start web service invocation(async. polling) --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\t// TODO process asynchronous response here\n" +   //NOI18N
            "\t{4} resp = port.{5}({6});\n" + //NOI18N
            "\twhile(!resp.isDone()) '{'\n" + //NOI18N
            "\t\t// do something\n" + //NOI18N
            "\t\tThread.sleep(100);\n" + //NOI18N
            "\t'}'\n" + //NOI18N
            "\tout.println(\"Result = \"+resp.get());\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation(async. polling) --%><hr/>\n"; //NOI18N
    
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB_ASYNC_CALLBACK =
            "    <%-- start web service invocation(async. callback) --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\t// TODO process asynchronous response here\n" +   //NOI18N
            "\t{4} result = port.{5}({6});\n" + //NOI18N
            "\twhile(!result.isDone()) '{'\n" + //NOI18N
            "\t\t// do something\n" + //NOI18N
            "\t\tThread.sleep(100);\n" + //NOI18N
            "\t'}'\n" + //NOI18N
            "\tout.println(\"Result = \"+asyncHandler.getResponse());\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation(async. callback) --%><hr/>\n"; //NOI18N
    
    // {0} = handler name (as type, e.g. "FooCallbackHandler")
    // {1} = response type (e.g. FooResponse)
    private static final String JSP_CALLBACK_HANDLER =
            "<%!\n" + //NOI18N
            "class {0} implements javax.xml.ws.AsyncHandler<{1}> '{'\n" + //NOI18N
            "    private {1} output;\n" + //NOI18N
            "\n" + //NOI18N
            "    public void handleResponse(javax.xml.ws.Response<{1}> response) '{'\n" + //NOI18N
            "        try '{'\n" + //NOI18N
            "            output = response.get();\n" + //NOI18N
            "        '}' catch(Exception ex) '{'\n" + //NOI18N
            "            // TODO handle exception\n" + //NOI18N
            "        '}'\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "\n" + //NOI18N
            "    {1} getResponse() '{'\n" + //NOI18N
            "         return output;\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "'}'\n" + //NOI18N
            "%>\n"; //NOI18N
    
    public static void insertMethodCall(DataObject dataObj, Node sourceNode, Node operationNode) {
        EditorCookie cookie = (EditorCookie)sourceNode.getCookie(EditorCookie.class);
        boolean inJsp = cookie!=null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType")); //NOI18N
        // First, collect name of method, port, and service:
        
        Node serviceNode, portNode, wsdlNode;
        String wsdlUrl;
        String serviceFieldName;
        String serviceJavaName, portJavaName, portGetterMethod, operationJavaName, returnTypeName;
        String responseType="Object"; //NOI18N
        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N
        String argumentInitializationPart, argumentDeclarationPart;
        WsdlOperation operation;
        Client client;
        
        try {
            serviceFieldName="service"; //NOI18N
            portNode = operationNode.getParentNode();
            serviceNode = portNode.getParentNode();
            wsdlNode = serviceNode.getParentNode();
            operation = (WsdlOperation)operationNode.getLookup().lookup(WsdlOperation.class);
            WsdlPort port = (WsdlPort)portNode.getLookup().lookup(WsdlPort.class);
            WsdlService service = (WsdlService)serviceNode.getLookup().lookup(WsdlService.class);
            
            client = (Client)wsdlNode.getLookup().lookup(Client.class);
            wsdlUrl = client.getWsdlUrl();
            operationJavaName = operation.getJavaName();
            portJavaName = port.getJavaName();
            portGetterMethod = port.getPortGetter();
            serviceJavaName = service.getJavaName();
            List arguments = operation.getParameters();
            returnTypeName = operation.getReturnTypeName();
            StringBuffer argumentBuffer1=new StringBuffer();
            StringBuffer argumentBuffer2=new StringBuffer();
            for (int i=0;i<arguments.size();i++) {
                String argumentTypeName = ((WsdlParameter)arguments.get(i)).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
                    responseType = resolveResponseType(argumentTypeName);
                    if (inJsp) argumentTypeName = pureJavaName(portJavaName)+"CallbackHandler"; //NOI18N
                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = ((WsdlParameter)arguments.get(i)).getName();
                if (inJsp && IMPLICIT_JSP_OBJECTS.contains(argumentName)) {
                    argumentName=argumentName+"_1"; //NOI18N
                }
                String argumentDeclaration = argumentTypeName+" "+argumentName;
                argumentBuffer1.append("\t"+argumentTypeName+" "+argumentName+" = "+resolveInitValue(argumentTypeName, dataObj.getPrimaryFile())+"\n"); //NOI18N
                argumentBuffer2.append(i>0?", "+argumentName:argumentName); //NOI18N
            }
            argumentInitializationPart=(argumentBuffer1.length()>0?"\t"+HINT_INIT_ARGUMENTS+argumentBuffer1.toString():"");
            argumentDeclarationPart=argumentBuffer2.toString();
            
        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }
        
        EditorCookie ec = (EditorCookie)dataObj.getCookie(EditorCookie.class);
        JEditorPane pane = ec.getOpenedPanes()[0];
        int caretOffset = pane.getCaretPosition();
        
        // Collect up any and all errors for display in case of problem
        List errors = new ArrayList();
        
//        JMManager manager = (JMManager)JMManager.getManager();
//        MDRepository repository = JavaModel.getJavaRepository();
        
        boolean success=false;
        
        // including code to JSP
        if (inJsp) {
            final javax.swing.text.StyledDocument document = cookie.getDocument();
            String invocationBody = "";
            // invocation
            Object[] args = new Object [] {
                serviceJavaName,
                portJavaName,
                portGetterMethod,
                argumentInitializationPart,
                returnTypeName,
                operationJavaName,
                argumentDeclarationPart
            };
            switch (operation.getOperationType()) {
                case WsdlOperation.TYPE_NORMAL : {
                    if ("void".equals(returnTypeName))
                        invocationBody = MessageFormat.format(JSP_STATIC_STUB_VOID, args);
                    else
                        invocationBody = MessageFormat.format(JSP_STATIC_STUB, args);
                    break;
                }
                case WsdlOperation.TYPE_ASYNC_POLLING : {
                    invocationBody = MessageFormat.format(JSP_STATIC_STUB_ASYNC_POLLING, args);
                    break;
                }
                case WsdlOperation.TYPE_ASYNC_CALLBACK : {
                    invocationBody = MessageFormat.format(JSP_STATIC_STUB_ASYNC_CALLBACK, args);
                    break;
                }
            }
            
            try {
                String content = document.getText(0, document.getLength());
                int pos = content.lastIndexOf("</body>"); //NOI18N
                if (pos<0) pos = content.lastIndexOf("</html>"); //NOI18N
                if (pos>=0) { //find where line begins
                    while (pos>0 && content.charAt(pos-1)!='\n' && content.charAt(pos-1)!='\r') {
                        pos--;
                    }
                } else pos = document.getLength();
                
                if (WsdlOperation.TYPE_ASYNC_CALLBACK==operation.getOperationType()) {
                    Object[] args1 = new Object [] {
                        callbackHandlerName,
                        responseType
                    };
                    final String methodBody = MessageFormat.format(JSP_CALLBACK_HANDLER, args1);
                    final String invocationPart = invocationBody;
                    final int position = pos;
                    // insert 2 parts in one atomic action
                    NbDocument.runAtomic(document, new Runnable() {
                        public void run() {
                            try {
                                document.insertString(document.getLength(),methodBody,null);
                                document.insertString(position,invocationPart,null);
                            } catch (javax.swing.text.BadLocationException ex) {}
                        }
                    });
                } else {
                    document.insertString(pos,invocationBody,null);
                }
                success=true;
            } catch (javax.swing.text.BadLocationException ex) {}
        } else {
//            
//            // including code to java class
//            
//            boolean generateWsRefInjection=false;
//            boolean insertServiceDef=true;
//            String printerName="System.out"; //NOI18N
//            JavaClass javaClass=null;
//            
//            boolean rollbackFlag = true; // rollback the transaction by default
//            // insert call code
//            repository.beginTrans(true);    // this happens in a new transaction
//            try {
//                ClassMember cm = JMIUtils.getCallableFeatureFromNode(sourceNode);
//                if (cm.isValid()) {
//                    
//                    RefFeatured refFeatured = cm.refImmediateComposite();
//                    if (refFeatured instanceof JavaClass) {
//                        javaClass = (JavaClass)refFeatured;
//                        
//                        // test if target can accept injections
//                        generateWsRefInjection = InjectionTargetQuery.isInjectionTarget(javaClass);
//                        insertServiceDef=!generateWsRefInjection;
//                        
//                        // test if in servlet
//                        JavaClass superClass = javaClass.getSuperClass();
//                        
//                        if (cm instanceof Method) {
//                            Method m = (Method)cm;
//                            String printer = findPrinter((Method)cm);
//                            if (printer!=null) printerName=printer;
//                        }
//                        
//                        if ("javax.servlet.http.HttpServlet".equals(superClass.getName())) { //NOI18N
//                            argumentInitializationPart = fixNamesInInitializationPart(argumentInitializationPart);
//                            argumentDeclarationPart = fixNamesInDeclarationPart(argumentDeclarationPart);
//                        }
//                        // compute the service field name
//                        if (generateWsRefInjection) {
//                            Set serviceFieldNames = new HashSet();
//                            List contents = javaClass.getContents();
//                            boolean injectionExists=false;
//                            for (int i=0;i<contents.size();i++) {
//                                Object obj = contents.get(i);
//                                if (obj instanceof Field) {
//                                    String fieldTypeName = ((Field)obj).getType().getName();
//                                    if (serviceJavaName.equals(fieldTypeName)) {
//                                        serviceFieldName=((Field)obj).getName();
//                                        generateWsRefInjection=false;
//                                        injectionExists=true;
//                                        break;
//                                    } else {
//                                        serviceFieldNames.add(((Field)obj).getName());
//                                    }
//                                }
//                            }
//                            if (!injectionExists) {
//                                serviceFieldName = findProperServiceFieldName(serviceFieldNames);
//                            }
//                        }
//                    }
//                    
//                    ClassDefinition cd = cm.getDeclaringClass();
//                    JavaModelPackage modelPkg = JavaMetamodel.getManager().getJavaExtent(cd);
//                    
//                    // find the place where to insert the code
//                    int targetOffset = caretOffset; // target insertion point
//                    Element elem = manager.getElementByOffset(dataObj.getPrimaryFile(), caretOffset);
//                    if (elem instanceof BehavioralFeature) {
//                        elem = ((BehavioralFeature) elem).getBody();
//                    } else if (elem instanceof PrimaryExpression) {
//                        while (!(elem instanceof org.netbeans.jmi.javamodel.Statement)) {
//                            elem = (Element)elem.refImmediateComposite(); // go through parents
//                        }
//                    }
//                    if (elem instanceof StatementBlock) {
//                        List statements = ((StatementBlock)elem).getStatements();
//                        int i;
//                        if (statements.size() > 0) {
//                            for (i = 0; i < statements.size(); i++) {
//                                Statement s = (Statement)statements.get(i);
//                                if (JavaMetamodel.getManager().getElementPosition(s).getBegin().getOffset() > targetOffset) {
//                                    break;
//                                }
//                            }
//                            if (i>0) {
//                                elem = (Statement)statements.get(i-1);
//                                targetOffset = JavaMetamodel.getManager().getElementPosition(elem).getEnd().getOffset(); // statement boundary
//                            } else {
//                                elem = (Statement)statements.get(i);
//                                targetOffset = JavaMetamodel.getManager().getElementPosition(elem).getBegin().getOffset(); // statement boundary
//                            }
//                        } else {
//                            targetOffset = JavaMetamodel.getManager().getElementPosition(elem).getBegin().getOffset() + 1; // statement boundary
//                        }
//                    } else if (elem instanceof Statement) {
//                        targetOffset = JavaMetamodel.getManager().getElementPosition(elem).getEnd().getOffset(); // statement boundary
//                    }
//                    
//                    // create & format inserted text
//                    Document doc = pane.getDocument();
//                    IndentEngine eng = IndentEngine.find(doc);
//                    StringWriter textWriter = new StringWriter();
//                    Writer indentWriter = eng.createWriter(doc, targetOffset, textWriter);
//                    
//                    // create the inserted text
//                    String invocationBody = getJavaInvocationBody(
//                            operation,
//                            insertServiceDef,
//                            serviceJavaName,
//                            portJavaName,
//                            portGetterMethod,
//                            argumentInitializationPart,
//                            returnTypeName,
//                            operationJavaName,
//                            argumentDeclarationPart,
//                            serviceFieldName,
//                            printerName,
//                            responseType);
//                    
//                    indentWriter.write(invocationBody);
//                    indentWriter.close();
//                    String textToInsert = textWriter.toString();
//                    
//                    try {
//                        doc.insertString(targetOffset, textToInsert, null);
//                    } catch (BadLocationException badLoc) {
//                        doc.insertString(targetOffset + 1, textToInsert, null);
//                    }
//                    success=true;
//                    rollbackFlag = false;   // great, no exceptions so far! -> do not rollback
//                }
//            } catch (NullPointerException npe) {
//                // This could happen if we get a source exception attempting to create
//                // a base component, and a later step tries to use that (still null)
//                // component.  There should be an error in the log.
//                if (errors.size() == 0) {
//                    // If there isn't an error in the log already, put a general one there.
//                    errors.add(NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_UnexpectedNPE", npe.getMessage())); // NOI18N
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
//                }
//                
//            } catch (BadLocationException badLoc) {
//                errors.add(NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_UnexpectedBLE", badLoc.getMessage())); // NOI18N
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, badLoc);
//                
//            } catch (IOException ioe) {
//                errors.add(NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_UnexpectedIOE", ioe.getMessage())); // NOI18N
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
//                
//            } finally {
//                repository.endTrans(rollbackFlag);
//            }
//            
//            // generate WebServiceRef injection
//            if (generateWsRefInjection && success && javaClass!=null) {
//                if (wsdlUrl.startsWith("file:")) { //NOI18N
//                    DataObject dObj = (DataObject) sourceNode.getCookie(DataObject.class);
//                    if (dObj!=null)
//                        wsdlUrl = findWsdlLocation(client,dObj.getPrimaryFile());
//                }
//                generateServiceRefInjection(javaClass, serviceFieldName, serviceJavaName, wsdlUrl);
//            }
        }
        if (errors.size() > 0) {
            // At least one error was encountered during code insertion.  Display the list of messages.
            StringBuffer buf = new StringBuffer(errors.size() * 100);
            buf.append(NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_FailedWebServiceInvocationCreation")); // NOI18N
            buf.append("\n"); // NOI18N
            for(Iterator iter = errors.iterator(); iter.hasNext(); ) {
                buf.append(iter.next().toString());
                buf.append("\n"); // NOI18N
            }
            NotifyDescriptor desc = new NotifyDescriptor.Message(buf.toString(), NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
        
        if (success) { // managed to insert string
            Node clientNode = operationNode.getParentNode().getParentNode().getParentNode();
            FileObject srcRoot = (FileObject)clientNode.getLookup().lookup(FileObject.class);
            Project clientProject = FileOwnerQuery.getOwner(srcRoot);
            DataObject dObj = (DataObject) sourceNode.getCookie(DataObject.class);
            if (dObj!=null) {
                JaxWsUtils.addProjectReference(clientProject, FileOwnerQuery.getOwner(dObj.getPrimaryFile()));
            }
        }
    }
    
    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    private static String resolveInitValue(String type, FileObject targetFile) {
        if ("int".equals(type) || "long".equals(type) || "short".equals(type) || "byte".equals(type)) //NOI18N
            return "0;"; //NOI18N
        if ("boolean".equals(type)) //NOI18N
            return "false;"; //NOI18N
        if ("float".equals(type) || "double".equals(type)) //NOI18N
            return "0.0;"; //NOI18N
        if ("java.lang.String".equals(type)) //NOI18N
            return "\"\";"; //NOI18N
        if (type.endsWith("CallbackHandler")) { //NOI18N
            return "new "+type+"();"; //NOI18N
        }
        if (type.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
            return "new "+type+"() {"; //NOI18N
        }

// Retouche         
//        if(targetFile != null && !isEnum(type)){
//            JavaClass jc = JMIUtils.findClass(type, targetFile);
//            if(jc != null){
//                if(hasDefaultConstructor(jc) || !hasExplicitConstructor(jc)){
//                    return "new " + type+ "();";//NOI18N
//                }
//            }
//        }
        return "null;"; //NOI18N
    }
// Retouche    
//    private static boolean isEnum(String typeName){
//        Type type = JavaModel.getDefaultExtent().getType().resolve(typeName);
//        if(type != null){
//            return (type instanceof JavaEnum);
//        }
//        return false;
//    }
//    
//    /**
//     * Determines if a Java class has an explicit constructor definition
//     */
//    private static boolean hasExplicitConstructor(JavaClass jc){
//        List<ClassMember> members = jc.getContents();
//        for(ClassMember member: members){
//            if(member instanceof Constructor){
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    /**
//     * Determines if a Java class has a no-arg constructor
//     */
//    private static boolean hasDefaultConstructor(JavaClass jc){
//        return jc.getConstructor(Collections.emptyList(), false) != null;
//    }
    
    private static String resolveResponseType(String argumentType) {
        int start = argumentType.indexOf("<");
        int end = argumentType.indexOf(">");
        if (start>0 && end>0 && start<end) {
            return argumentType.substring(start+1,end);
        } else return "javax.xml.ws.Response"; //NOI18N
    }
    
    private static String pureJavaName(String javaNameWithPackage) {
        int index = javaNameWithPackage.lastIndexOf(".");
        return index>=0?javaNameWithPackage.substring(index+1):javaNameWithPackage;
    }
    
    public static void insertMethod(final Document document, final int pos, OperationNode operationNode) {
        
        boolean inJsp = "text/x-jsp".equals(document.getProperty("mimeType")); //NOI18N
        // First, collect name of method, port, and service:
        
        Node serviceNode, portNode, wsdlNode;
        final String wsdlUrl;
        final String serviceJavaName;
        String serviceFieldName;
        String portJavaName, portGetterMethod, operationJavaName, returnTypeName;
        String responseType="Object"; //NOI18N
        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N
        String argumentInitializationPart, argumentDeclarationPart;
        WsdlOperation operation;
        Client client;
        
        try {
            serviceFieldName="service"; //NOI18N
            portNode = operationNode.getParentNode();
            serviceNode = portNode.getParentNode();
            wsdlNode = serviceNode.getParentNode();
            operation = (WsdlOperation)operationNode.getLookup().lookup(WsdlOperation.class);
            WsdlPort port = (WsdlPort)portNode.getLookup().lookup(WsdlPort.class);
            WsdlService service = (WsdlService)serviceNode.getLookup().lookup(WsdlService.class);
            
            client = (Client)wsdlNode.getLookup().lookup(Client.class);
            wsdlUrl = client.getWsdlUrl();
            operationJavaName = operation.getJavaName();
            portJavaName = port.getJavaName();
            portGetterMethod = port.getPortGetter();
            serviceJavaName = service.getJavaName();
            List arguments = operation.getParameters();
            returnTypeName = operation.getReturnTypeName();
            StringBuffer argumentBuffer1=new StringBuffer();
            StringBuffer argumentBuffer2=new StringBuffer();
            for (int i=0;i<arguments.size();i++) {
                String argumentTypeName = ((WsdlParameter)arguments.get(i)).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
                    responseType = resolveResponseType(argumentTypeName);
                    if (inJsp) argumentTypeName = pureJavaName(portJavaName)+"CallbackHandler"; //NOI18N
                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = ((WsdlParameter)arguments.get(i)).getName();
                if (inJsp && IMPLICIT_JSP_OBJECTS.contains(argumentName)) {
                    argumentName=argumentName+"_1"; //NOI18N
                }
                String argumentDeclaration = argumentTypeName+" "+argumentName;
                argumentBuffer1.append("\t"+argumentTypeName+" "+argumentName+" = "+resolveInitValue(argumentTypeName,
                        NbEditorUtilities.getFileObject(document))+"\n"); //NOI18N
                argumentBuffer2.append(i>0?", "+argumentName:argumentName); //NOI18N
            }
            argumentInitializationPart=(argumentBuffer1.length()>0?"\t"+HINT_INIT_ARGUMENTS+argumentBuffer1.toString():"");
            argumentDeclarationPart=argumentBuffer2.toString();
            
        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }
        
        // including code to JSP
        if (inJsp) {
            String invocationBody = "";
            // invocation
            Object[] args = new Object [] {
                serviceJavaName,
                portJavaName,
                portGetterMethod,
                argumentInitializationPart,
                returnTypeName,
                operationJavaName,
                argumentDeclarationPart
            };
            switch (operation.getOperationType()) {
                case WsdlOperation.TYPE_NORMAL : {
                    if ("void".equals(returnTypeName))
                        invocationBody = MessageFormat.format(JSP_STATIC_STUB_VOID, args);
                    else
                        invocationBody = MessageFormat.format(JSP_STATIC_STUB, args);
                    break;
                }
                case WsdlOperation.TYPE_ASYNC_POLLING : {
                    invocationBody = MessageFormat.format(JSP_STATIC_STUB_ASYNC_POLLING, args);
                    break;
                }
                case WsdlOperation.TYPE_ASYNC_CALLBACK : {
                    invocationBody = MessageFormat.format(JSP_STATIC_STUB_ASYNC_CALLBACK, args);
                    break;
                }
            }
            
            try {
                if (WsdlOperation.TYPE_ASYNC_CALLBACK==operation.getOperationType()) {
                    Object[] args1 = new Object [] {
                        callbackHandlerName,
                        responseType
                    };
                    final String methodBody = MessageFormat.format(JSP_CALLBACK_HANDLER, args1);
                    final String invocationPart = invocationBody;
                    // insert 2 parts in one atomic action
                    NbDocument.runAtomic((StyledDocument)document, new Runnable() {
                        public void run() {
                            try {
                                document.insertString(document.getLength(),methodBody,null);
                                document.insertString(pos,invocationPart,null);
                            } catch (javax.swing.text.BadLocationException ex) {}
                        }
                    });
                } else {
                    document.insertString(pos,invocationBody,null);
                }
                
                
            } catch (javax.swing.text.BadLocationException ex) {}
            
            return;
        }
        
        // including code to java class
        FileObject targetFo = NbEditorUtilities.getFileObject(document);
        
        JavaSource targetSource = JavaSource.forFileObject(targetFo);
        final String respType = responseType;
        final boolean[] insertServiceDef = {true};
        final String[] printerName = {"System.out"}; // NOI18N
        final String[] argumentInitPart = {argumentInitializationPart};
        final String[] argumentDeclPart = {argumentDeclarationPart};
        final String[] serviceFName = {serviceFieldName};
        // PENDING compute this from using InjectionTargetQuery.isInjectionTarget 
        //System.out.println("Is InjectionTarget? " + InjectionTargetQuery.isInjectionTarget(targetFo, null));
        final boolean[] generateWsRefInjection = {false};
        CancellableTask task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree cut = controller.getCompilationUnit();
                
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                if (srcUtils!=null) {
                    ClassTree javaClass = srcUtils.getClassTree();
                    // find if class is Injection Target
                    TypeElement thisTypeEl = srcUtils.getTypeElement();
                    generateWsRefInjection[0] = InjectionTargetQuery.isInjectionTarget(controller, thisTypeEl);
                    insertServiceDef[0] = !generateWsRefInjection[0];
                    if (isServletClass(controller, javaClass)) {
                        printerName[0]="out";
                        argumentInitPart[0] = fixNamesInInitializationPart(argumentInitPart[0]);
                        argumentDeclPart[0] = fixNamesInDeclarationPart(argumentDeclPart[0]);
                    }
                    // compute the service field name
                    if (generateWsRefInjection[0]) {
                        Set serviceFieldNames = new HashSet();
                        boolean injectionExists=false;
                        int memberOrder=0;
                        for (Tree member : javaClass.getMembers()) {
                            // for the first inner class in top level
                            ++memberOrder;
                            if (VARIABLE == member.getKind()) {
                                // get variable type
                                VariableTree var = (VariableTree)member;
                                Tree typeTree = var.getType();
                                TreePath typeTreePath = controller.getTrees().getPath(cut, typeTree);
                                TypeElement typeEl = (TypeElement)controller.getTrees().getElement(typeTreePath);
                                if (typeEl!=null) {
                                    String variableType = typeEl.getQualifiedName().toString(); 
                                    if (serviceJavaName.equals(variableType)) {
                                        serviceFName[0]=var.getName().toString();
                                        generateWsRefInjection[0]=false;
                                        injectionExists=true;
                                        break;
                                    }
                                }
                                serviceFieldNames.add(var.getName().toString());
                            }
                        }
                        if (!injectionExists) {
                            serviceFName[0] = findProperServiceFieldName(serviceFieldNames);
                        }
                    }
                }
            }
            public void cancel() {}
        };
        


        try {
            // find proper values for 
            //   - printer name 
            //   - service variable name 
            //   - argument declaration part
            //   - argument initialization part
            targetSource.runUserActionTask(task, true);
            
            // create & format inserted text
            IndentEngine eng = IndentEngine.find(document);
            StringWriter textWriter = new StringWriter();
            Writer indentWriter = eng.createWriter(document, pos, textWriter);

            // create the inserted text
            String invocationBody = getJavaInvocationBody(
                    operation,
                    insertServiceDef[0],
                    serviceJavaName,
                    portJavaName,
                    portGetterMethod,
                    argumentInitPart[0],
                    returnTypeName,
                    operationJavaName,
                    argumentDeclPart[0],
                    serviceFName[0],
                    printerName[0],
                    respType);

            indentWriter.write(invocationBody);
            indentWriter.close();
            String textToInsert = textWriter.toString();

            try {
                document.insertString(pos, textToInsert, null);
            } catch (BadLocationException badLoc) {
                document.insertString(pos + 1, textToInsert, null);
            }
            
            // @insert WebServiceRef injection
            if (generateWsRefInjection[0]) {
                CancellableTask modificationTask = new CancellableTask<WorkingCopy>() {
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(Phase.RESOLVED);

                        TreeMaker make = workingCopy.getTreeMaker();
                        
                        SourceUtils srcUtils = SourceUtils.newInstance(workingCopy);
                        if (srcUtils!=null) {
                            ClassTree javaClass = srcUtils.getClassTree();
                            VariableTree serviceRefInjection = generateServiceRefInjection(workingCopy, make, serviceFName[0], serviceJavaName, wsdlUrl);
                            ClassTree modifiedClass = make.insertClassMember(javaClass, 0, serviceRefInjection);
                            workingCopy.rewrite(javaClass, modifiedClass);
                        }
                    }
                    public void cancel() {}
                };
                targetSource.runModificationTask(modificationTask).commit();
            }         
        } catch (BadLocationException badLoc) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, badLoc);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
    }
    
    private static VariableTree generateServiceRefInjection(
            WorkingCopy workingCopy,
            TreeMaker make,
            String fieldName,
            String fieldType,
            String wsdlUrl) {
        TypeElement wsRefElement = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceRef"); //NOI18N

        AnnotationTree wsRefAnnotation = make.Annotation(
                make.QualIdent(wsRefElement), 
                Collections.<ExpressionTree>singletonList(make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlUrl)))
        );
        // create method modifier: public and no annotation
        ModifiersTree methodModifiers = make.Modifiers(
            Collections.<Modifier>singleton(Modifier.PUBLIC),
            Collections.<AnnotationTree>singletonList(wsRefAnnotation)
        );
        TypeElement typeElement = workingCopy.getElements().getTypeElement(fieldType);
        return make.Variable(
            methodModifiers,
            fieldName,
            make.Type(typeElement.asType()),
            null
        );
    }
    
    private static String findProperServiceFieldName(Set serviceFieldNames) {
        String name="service";
        int i=0;
        while (serviceFieldNames.contains(name)) {
            name="service_"+String.valueOf(++i);
        }
        return name; //NOI18N
    }
    
    private static boolean isServletClass(CompilationController controller, ClassTree classTree) {
        SourceUtils srcUtils = SourceUtils.newInstance(controller, classTree);
        return srcUtils.isSubtype("javax.servlet.http.HttpServlet");
    }

// Retouche    
//    private static JavaClass getJavaClass(FileObject fo) {
//        JavaModel.getJavaRepository().beginTrans(false);
//        JavaClass jc = null;
//        try {
//            Resource res = JavaModel.getResource(fo);
//            if (res != null) {
//                JavaModel.setClassPath(res);
//                List/*<JavaClass>*/ classes = res.getClassifiers();
//                if (classes.size() == 1) {
//                    jc = (JavaClass)classes.get(0);
//                }
//            }
//            return jc;
//        } finally {
//            JavaModel.getJavaRepository().endTrans(false);
//        }
//    }
    
    private static String getJavaInvocationBody(
            WsdlOperation operation,
            boolean insertServiceDef,
            String serviceJavaName,
            String portJavaName,
            String portGetterMethod,
            String argumentInitializationPart,
            String returnTypeName,
            String operationJavaName,
            String argumentDeclarationPart,
            String serviceFieldName,
            String printerName,
            String responseType) {
        String invocationBody="";
        Object [] args = new Object [] {
            serviceJavaName,
            portJavaName,
            portGetterMethod,
            argumentInitializationPart,
            returnTypeName,
            operationJavaName,
            argumentDeclarationPart,
            serviceFieldName,
            printerName
        };
        switch (operation.getOperationType()) {
            case WsdlOperation.TYPE_NORMAL : {
                if ("void".equals(returnTypeName)) { //NOI18N
                    String body =
                            JAVA_TRY+
                            (insertServiceDef?JAVA_SERVICE_DEF:"")+
                            JAVA_PORT_DEF+
                            JAVA_VOID+
                            JAVA_CATCH;
                    invocationBody = MessageFormat.format(body, args);
                } else {
                    String body =
                            JAVA_TRY+
                            (insertServiceDef?JAVA_SERVICE_DEF:"")+
                            JAVA_PORT_DEF+
                            JAVA_RESULT+
                            JAVA_OUT+
                            JAVA_CATCH;
                    invocationBody = MessageFormat.format(body, args);
                } break;
            }
            case WsdlOperation.TYPE_ASYNC_POLLING : {
                invocationBody = MessageFormat.format(JAVA_STATIC_STUB_ASYNC_POLLING, args);
                break;
            }
            case WsdlOperation.TYPE_ASYNC_CALLBACK : {
                args[7] = responseType;
                invocationBody = MessageFormat.format(JAVA_STATIC_STUB_ASYNC_CALLBACK, args);
                break;
            }
        }
        return invocationBody;
    }
    
    private static String fixNamesInDeclarationPart(String argumentDeclarationPart) {
        StringTokenizer tok = new StringTokenizer(argumentDeclarationPart," ,"); //NOI18N
        StringBuffer buf = new StringBuffer();
        int i=0;
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            String newName=null;
            if ("request".equals(token)) newName="request_1"; //NOI18N
            else if ("response".equals(token)) newName="response_1"; //NOI18N
            else if ("out".equals(token)) newName="out_1"; //NOI18N
            else newName=token;
            buf.append(i>0?", "+newName:newName); //NOI18N
            i++;
        }
        return buf.toString();
    }
    
    private static String fixNamesInInitializationPart(String argumentInitializationPart) {
        return argumentInitializationPart.replaceFirst(" request ", //NOI18N
                " request_1 ").replaceFirst(" response ", //NOI18N
                " response_1 ").replaceFirst(" out "," out_1 "); //NOI18N
    }
// Retouche    
//    private static String findPrinter(Method m) {
//        List childrens = m.getChildren();
//        boolean foundPrinter=false;
//        for (int i=0;i<childrens.size();i++) {
//            Object o = childrens.get(i);
//            if (o instanceof Parameter) {
//                Parameter param = (Parameter)o;
//                if ("java.io.PrintWriter".equals(param.getType().getName())) { //NOI18N
//                    return param.getName();
//                }
//            }
//            if (o instanceof StatementBlock) {
//                List<Statement> statements = ((StatementBlock)o).getStatements();
//                for (Statement st:statements) {
//                    if (st instanceof LocalVarDeclaration) {
//                        List<Variable> variables = ((LocalVarDeclaration)st).getVariables();
//                        for (Variable var:variables) {
//                            if ("java.io.PrintWriter".equals(var.getType().getName())) { //NOI18N
//                                return var.getName();
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
    
    private static String findWsdlLocation(Client client, FileObject targetFo) {
        Project targetProject = FileOwnerQuery.getOwner(targetFo);
        J2eeModuleProvider moduleProvider = (J2eeModuleProvider)targetProject.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider!=null && J2eeModule.WAR.equals(moduleProvider.getJ2eeModule().getModuleType())) {
            return "WEB-INF/wsdl/client/"+client.getName()+"/"+client.getLocalWsdlFile(); //NOI18N
        } else {
            return "META-INF/wsdl/client/"+client.getName()+"/"+client.getLocalWsdlFile(); //NOI18N
        }
    }
}
