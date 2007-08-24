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
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.WsdlResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.JaxwsOperationInfo;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import static com.sun.source.tree.Tree.Kind.*;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class WsdlComponentGenerator extends RestComponentGenerator {
 
    public WsdlComponentGenerator(FileObject targetFile, RestComponentData data) {
        super(targetFile, new WsdlResourceBean(data, FileOwnerQuery.getOwner(targetFile)));
    }
    
    public Map<String,String> getInputParameterTypes() {
        Map<String,String> ret = new HashMap<String,String>();
        for(JaxwsOperationInfo info : ((WsdlResourceBean)bean).getOperationInfos()) {
            String[] names = info.getInputParameterNames();
            String[] types = info.getInputParameterTypes();
            for (int i=0; i<names.length; i++) {
                ret.put(names[i], types[i]);  //TODO duplicate param names b/w operations
            }
        }
        return ret;
    }
    
    public void setConstantInputValues(Map<String,Object> constantParamValues) {
        ((WsdlResourceBean)bean).setConstantParams(constantParamValues);
    }
    
    
    private void setupWebServiceClient() {
        for(JaxwsOperationInfo info : ((WsdlResourceBean)bean).getOperationInfos()) {
            info.setupWebServiceClient();
        }
    }
    
    /**
     *  Return target and generated file objects
     */
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);
        
        FileObject outputWrapperFO = generateJaxbOutputWrapper();
        jaxbOutputWrapperJS = JavaSource.forFileObject(outputWrapperFO);
        generateComponentResourceClass();
        addSubResourceMethod();
        FileObject refConverterFO = getOrCreateGenericRefConverter().getFileObjects().iterator().next();
        modifyTargetConverter();
        putFocusOnTargetFile();
        FileObject[] result = new FileObject[] { targetFile, wrapperResourceFile, refConverterFO, outputWrapperFO };
        JavaSourceHelper.saveSource(result);
        
        finishProgressReporting();
        
        return new HashSet<FileObject>(Arrays.asList(result));
    }
    
    public FileObject generateJaxbOutputWrapper() throws IOException {
        FileObject converterFolder = getConverterFolder();
        String packageName = SourceGroupSupport.packageForFolder(converterFolder);
        ((WsdlResourceBean)bean).setOutputWrapperPackageName(packageName);
        String[] returnTypeNames = ((WsdlResourceBean)bean).getOutputTypes();
        XmlOutputWrapperGenerator gen = new XmlOutputWrapperGenerator(
                converterFolder, ((WsdlResourceBean)bean).getOutputWrapperName(), packageName, returnTypeNames);
        return gen.generate();
    }
    
    public void generateComponentResourceClass() throws IOException {
        
        setupWebServiceClient();
        if (wrapperResourceFile == null) {
            GenericResourceGenerator delegate = new GenericResourceGenerator(destDir, bean);
            delegate.setTemplate(RESOURCE_TEMPLATE);
            Set<FileObject> files = delegate.generate(getProgressHandle());
            if (files == null || files.size() == 0) {
                return;
            }
            wrapperResourceFile = files.iterator().next();
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            modifyGETMethod();
        } else {
            wrapperResourceJS = JavaSource.forFileObject(wrapperResourceFile);
            ((WsdlResourceBean)bean).initConstantParams(wrapperResourceJS);
        }
    }
    
    public void modifyGETMethod() throws IOException {
        ModificationResult result = wrapperResourceJS.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                String converterType = JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
                JavaSourceHelper.addImports(copy, new String[] { converterType });

                String converterName = converterType.substring(converterType.lastIndexOf('.')+1);
                String methodBody = "{" + getOverridingStatements(); //NOI18N
                methodBody += "$CONVERTER$ representation = new $CONVERTER$(); ".replace("$CONVERTER$", converterName);
                
                for (JaxwsOperationInfo info : ((WsdlResourceBean)bean).getOperationInfos()) {
                    methodBody += getWSInvocationCode(info);
                }
                methodBody += "return representation; }";
                MethodTree methodTree = JavaSourceHelper.getMethodByName(copy, "getXml"); //NOI18
                JavaSourceHelper.replaceMethodBody(copy, methodTree, methodBody);
            }
        });
        result.commit();
    }
    
    private static final String HINT_INIT_ARGUMENTS=" // TODO initialize WS operation arguments here\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = service field name
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
            "'}'\n"; //NOI18N
    
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
    
    /**
     * Add JAXWS client code for invoking the given operation at current position.
     */
    private String getWSInvocationCode(JaxwsOperationInfo info) throws IOException {
        //Collect java names for invocation code
        String wsdlUrl = info.getWsdlURL();
        final String serviceJavaName = info.getService().getJavaName();
        String portJavaName = info.getPort().getJavaName();
        String operationJavaName = info.getOperation().getJavaName();
        String portGetterMethod = info.getPort().getPortGetter();
        String serviceFieldName = "service"; //NOI18N
        String returnTypeName = info.getOperation().getReturnTypeName();
        List<WsdlParameter> outArguments = info.getOutputParameters();
        String responseType="Object"; //NOI18N
        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N
        
        String argumentInitializationPart = "";
        String argumentDeclarationPart = "";
        try {
            StringBuffer argumentBuffer1=new StringBuffer();
            StringBuffer argumentBuffer2=new StringBuffer();
            for (int i=0;i<outArguments.size();i++) {
                String argumentTypeName = outArguments.get(i).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
                    responseType = resolveResponseType(argumentTypeName);
                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = outArguments.get(i).getName();
                argumentBuffer1.append("\t"+argumentTypeName+" "+argumentName+" = "+resolveInitValue(argumentTypeName)+"\n"); //NOI18N
            }
            List<WsdlParameter> parameters = info.getOperation().getParameters();
            Set<String> constants = ((WsdlResourceBean)bean).getConstantParamNames();
            for (int i=0; i<parameters.size(); i++) {
                String argument = parameters.get(i).getName();
                if (constants.contains(argument)) {
                    Object value = ((WsdlResourceBean)bean).getConstantParamValue(argument);
                    if (value instanceof Enum) {
                        argument = ((WsdlResourceBean)bean).getConstantParamType(argument) + "." + value;
                    } else {
                        argument = GenericResourceGenerator.toConstantName(argument);
                    }
                }
                argumentBuffer2.append(i>0 ? ", "+argument : argument); //NOI18N
            }
            argumentInitializationPart=(argumentBuffer1.length()>0?"\t"+HINT_INIT_ARGUMENTS+argumentBuffer1.toString():"");
            argumentDeclarationPart=argumentBuffer2.toString();
            
        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(WsdlComponentGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
        
        boolean success=false;
        
        final boolean[] insertServiceDef = {true};
        final String[] printerName = {"System.out"}; // NOI18N
        final String[] argumentInitPart = {argumentInitializationPart};
        final String[] argumentDeclPart = {argumentDeclarationPart};
        final String[] serviceFName = {serviceFieldName};
        final boolean[] generateWsRefInjection = {false};
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree cut = controller.getCompilationUnit();
                ClassTree classTree = JavaSourceHelper.findPublicTopLevelClass(controller);
                generateWsRefInjection[0] = JavaSourceHelper.isInjectionTarget(controller);
                insertServiceDef[0] = !generateWsRefInjection[0];
                
                // compute the service field name
                if (generateWsRefInjection[0]) {
                    Set<String> serviceFieldNames = new HashSet<String>();
                    boolean injectionExists=false;
                    int memberOrder=0;
                    for (Tree member : classTree.getMembers()) {
                        // for the first inner class in top level
                        ++memberOrder;
                        if (VARIABLE == member.getKind()) {
                            // get variable type
                            VariableTree var = (VariableTree)member;
                            Tree typeTree = var.getType();
                            TreePath typeTreePath = controller.getTrees().getPath(cut, typeTree);
                            TypeElement typeEl = JavaSourceHelper.getTypeElement(controller, typeTreePath);
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
            public void cancel() {}
        };
        
        // create the inserted text
        wrapperResourceJS.runUserActionTask(task, true);
        
        String invocationBody = getJavaInvocationBody(
                info.getOperation(),
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
                responseType);
        
        List<WsdlParameter> outParams = info.getOutputParameters();
        String outputClassName = SourceGroupSupport.getClassName(info.getOutputType());
        invocationBody += "representation.set"+outputClassName+"(";
        if (Constants.VOID.equals(returnTypeName) && outParams.size() > 0) {
            invocationBody += outParams.get(0).getName() + ".value);";
        } else if (! Constants.VOID.equals(returnTypeName)) {
            invocationBody += "result);";
        } else {
            throw new IllegalArgumentException("Unsupported return type "+returnTypeName);
        }
        
        if (generateWsRefInjection[0]) {
            insertServiceRef(wrapperResourceJS, info, serviceFieldName);
        }
        return invocationBody;
    }
    
    protected String getOverridingStatements() {
        String[] params = bean.getQueryParams();
        String[] paramTypes = bean.getQueryParamTypes();
        return JavaSourceHelper.getParamEqualThisFieldStatements(params, paramTypes);
    }
    
    private void insertServiceRef(JavaSource targetJS, JaxwsOperationInfo info, final String serviceFieldName) throws IOException {
        final String serviceJavaName = info.getService().getJavaName();
        String wsdlUrl = info.getWsdlURL();
        
        if (wsdlUrl.startsWith("file:")) { //NOI18N
            wsdlUrl = info.getWsdlLocation().toString();
        }
        final String localWsdlUrl = wsdlUrl;
        CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = JavaSourceHelper.getTopLevelClassElement(workingCopy);
                ClassTree javaClass = JavaSourceHelper.getClassTree(workingCopy, typeElement);
                VariableTree serviceRefInjection = generateServiceRefInjection(
                        workingCopy, make, serviceFieldName, serviceJavaName, localWsdlUrl);
                ClassTree modifiedClass = make.insertClassMember(javaClass, 0, serviceRefInjection);
                workingCopy.rewrite(javaClass, modifiedClass);
            }
            public void cancel() {}
        };
        targetJS.runModificationTask(modificationTask).commit();
    }
    
    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    private static String resolveInitValue(String type) {
        if (type.startsWith("javax.xml.ws.Holder")) { //NOI18N
            return "new "+type+"();";
        }
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
        
        return "null;"; //NOI18N
    }
    
    private static String resolveResponseType(String argumentType) {
        int start = argumentType.indexOf("<");
        int end = argumentType.indexOf(">");
        if (start>0 && end>0 && start<end) {
            return argumentType.substring(start+1,end);
        } else return "javax.xml.ws.Response"; //NOI18N
    }
    
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
                        (insertServiceDef?JAVA_SERVICE_DEF:"")+
                        JAVA_PORT_DEF+
                        JAVA_VOID;
                invocationBody = MessageFormat.format(body, args);
            } else {
                String body =
                        (insertServiceDef?JAVA_SERVICE_DEF:"")+
                        JAVA_PORT_DEF+
                        JAVA_RESULT+
                        JAVA_OUT;
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
        
        VariableTree var =  make.Variable(
                methodModifiers,
                fieldName,
                make.Type(typeElement.asType()),
                null
                );
        return make.Variable(
                make.Modifiers(var.getModifiers().getFlags(), var.getModifiers().getAnnotations()),
                var.getName(),
                var.getType(),
                var.getInitializer()
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
    
    private String getOutputWrapperQualifiedName() throws IOException {
        return JavaSourceHelper.getClassType(jaxbOutputWrapperJS);
    }
}
