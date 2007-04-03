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

package org.netbeans.modules.websvc.design.javamodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.jws.WebParam.Mode;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.design.util.SourceUtils;
import org.openide.ErrorManager;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class Utils {

    public static boolean isEqualTo(String str1, String str2) {
        if (str1==null) return str2==null;
        else return str1.equals(str2);
    }
    
    public static void populateModel(final FileObject implClass, final ServiceModel serviceModel) {
        
        JavaSource javaSource = JavaSource.forFileObject(implClass);
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                //CompilationUnitTree cut = controller.getCompilationUnit();
                
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                if (srcUtils!=null) {
                    //ClassTree javaClass = srcUtils.getClassTree();
                    // find if class is Injection Target
                    TypeElement classEl = srcUtils.getTypeElement();
                    TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                    if (wsElement!=null) {
                        List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
                        AnnotationMirror webServiceAn=null;
                        for (AnnotationMirror anMirror : annotations) {
                            if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                                webServiceAn = anMirror;
                                break;
                            }
                        }
                        if (webServiceAn==null) {
                            serviceModel.setStatus(ServiceModel.STATUS_NOT_SERVICE);
                            return;
                        }
                        
                        Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = webServiceAn.getElementValues();
                        boolean nameFound=false;
                        boolean serviceNameFound=false;
                        boolean portNameFound=false;                
                        for(ExecutableElement ex:expressions.keySet()) {
                            if (ex.getSimpleName().contentEquals("serviceName")) { //NOI18N
                                serviceModel.serviceName = (String)expressions.get(ex).getValue();
                                serviceNameFound=true;
                            } else if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                                serviceModel.name = (String)expressions.get(ex).getValue();
                                nameFound=true;
                            } else if (ex.getSimpleName().contentEquals("portName")) { //NOI18N
                                serviceModel.portName = (String)expressions.get(ex).getValue();
                                portNameFound=true;
                            } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                                serviceModel.setTargetNamespace((String)expressions.get(ex).getValue());
                            } else if (ex.getSimpleName().contentEquals("endpointInterface")) { //NOI18N
                                serviceModel.setEndpointInterface((String)expressions.get(ex).getValue());
                            } else if (ex.getSimpleName().contentEquals("wsdlLocation")) { //NOI18N
                                serviceModel.setWsdlLocation((String)expressions.get(ex).getValue());
                            }
                        }
                        // set default names
                        if (!nameFound) serviceModel.name=implClass.getName();
                        if (!portNameFound) serviceModel.portName = serviceModel.getName()+"Port"; //NOI18N
                        if (!serviceNameFound) serviceModel.serviceName=implClass.getName()+"Service"; //NOI18N                
                    }
                    
                    boolean foundWebMethodAnnotation=false;
                    TypeElement methodAnotationEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                    List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
                    for (Element member : classEl.getEnclosedElements()) {
                        if (member.getKind() == ElementKind.METHOD/* && member.getSimpleName().contentEquals("min")*/) {
                            ExecutableElement methodEl = (ExecutableElement) member;                            
                            if (methodEl.getModifiers().contains(Modifier.PUBLIC)) {
                                List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
                                if (foundWebMethodAnnotation) {
                                    for (AnnotationMirror anMirror : methodAnnotations) {
                                        if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                            methods.add(methodEl);
                                            break;
                                        }
                                    }
                                } else { // until now no @WebMethod annotations
                                    for (AnnotationMirror anMirror : methodAnnotations) {
                                        if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                            // found first @WebMethod annotation
                                            // need to remove all, previously found, public methods
                                            methods.clear();
                                            foundWebMethodAnnotation=true;
                                            methods.add(methodEl);
                                            break;
                                        }
                                    }
                                    if (!foundWebMethodAnnotation) {
                                        // all methods are supposed to be web methods when missing @WebMethod annotation
                                        methods.add(methodEl);
                                    }
                                }
                            }
                        }
                    }
                    // populate methods
                    
                    List<MethodModel> operations = new ArrayList<MethodModel>();
                    if (methods.size()==0) {
                        serviceModel.setStatus(ServiceModel.STATUS_INCORRECT_SERVICE);
                        return;
                    }
                    
                    for (int i=0;i<methods.size();i++) {
                        MethodModel operation = new MethodModel();
                        Utils.populateOperation(controller, methods.get(i), operation);
                        operations.add(operation);
                    }
                    serviceModel.operations=operations;
                }
            }
            public void cancel() {}
        };
        
        try {
            javaSource.runUserActionTask(task, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static void populateOperation(CompilationController controller, ExecutableElement methodEl, MethodModel methodModel) {
        TypeElement methodAnotationEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        TypeElement onewayAnotationEl = controller.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
        boolean nameFound=false;
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                               
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("operationName")) { //NOI18N
                        methodModel.setOperationName((String)expressions.get(ex).getValue());
                        nameFound=true;
                    } else if (ex.getSimpleName().contentEquals("action")) { //NOI18N
                        methodModel.setAction((String)expressions.get(ex).getValue());
                    }
                }
                
            } else if (controller.getTypes().isSameType(onewayAnotationEl.asType(), anMirror.getAnnotationType())) {
                methodModel.setOneWay(true);
            }
        }
        if (!nameFound) methodModel.setOperationName(methodEl.getSimpleName().toString());
        
        // Return type
        TypeMirror returnType = methodEl.getReturnType();
        if (returnType.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement)((DeclaredType)returnType).asElement();
            methodModel.setReturnType(element.getQualifiedName().toString());
        } else { // for primitive types
            methodModel.setReturnType(returnType.toString());
        }
        
        // populate faults
        List<? extends TypeMirror> faultTypes = methodEl.getThrownTypes();
        List<FaultModel> faults = new ArrayList<FaultModel>();
        for (TypeMirror faultType:faultTypes) {
            if (faultType.getKind() == TypeKind.DECLARED) {
                TypeElement faultEl = (TypeElement)((DeclaredType)faultType).asElement();
                faults.add(new FaultModel(faultEl.getQualifiedName().toString()));
            } else {
                faults.add(new FaultModel(faultType.toString()));
            }
        }
        methodModel.setFaults(faults);
        
        // populate params
        List<? extends VariableElement> paramElements = methodEl.getParameters();
        List<ParamModel> params = new ArrayList<ParamModel>();
        for (int i=0;i<paramElements.size();i++) {
            VariableElement paramEl = paramElements.get(i);
            ParamModel param = new ParamModel("arg"+String.valueOf(i));
            populateParam(controller, paramEl, param);
            params.add(param);
        }
        methodModel.setParams(params);
    }
    
    private static void populateParam(CompilationController controller, VariableElement paramEl, ParamModel paramModel) {
        TypeMirror type = paramEl.asType();
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement)((DeclaredType)type).asElement();
            paramModel.setParamType(element.getQualifiedName().toString());
        } else { // for primitive type
            paramModel.setParamType(type.toString());
        }
        TypeElement paramAnotationEl = controller.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = paramEl.getAnnotationMirrors();
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (controller.getTypes().isSameType(paramAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();             
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                        paramModel.setName((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("partName")) { //NOI18N
                        paramModel.setPartName((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                        paramModel.setTargetNamespace((String)expressions.get(ex).getValue());
                    } else if (ex.getSimpleName().contentEquals("mode")) { //NOI18N
                        paramModel.setMode((Mode)expressions.get(ex).getValue());
                    }
                }
            }
        }
    }
}
