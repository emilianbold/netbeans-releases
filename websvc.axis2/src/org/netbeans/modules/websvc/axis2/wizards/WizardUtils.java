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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.axis2.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.config.model.Axis2;
import org.netbeans.modules.websvc.axis2.config.model.Axis2ComponentFactory;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.GenerateWsdl;
import org.netbeans.modules.websvc.axis2.config.model.JavaGenerator;
import org.netbeans.modules.websvc.axis2.java.GenerationUtils;
import org.netbeans.modules.websvc.axis2.java.SourceUtils;
import org.netbeans.modules.websvc.axis2.services.model.MessageReceiver;
import org.netbeans.modules.websvc.axis2.services.model.MessageReceivers;
import org.netbeans.modules.websvc.axis2.services.model.Parameter;
import org.netbeans.modules.websvc.axis2.services.model.Service;
import org.netbeans.modules.websvc.axis2.services.model.ServiceGroup;
import org.netbeans.modules.websvc.axis2.services.model.Services;
import org.netbeans.modules.websvc.axis2.services.model.ServicesComponentFactory;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class WizardUtils {
    /** add service element to service group. used in "from java case"
     * 
     * @param servicesModel
     * @param serviceClass
     * @param serviceFo
     */
    static void addService(ServicesModel servicesModel, String serviceClass, FileObject serviceFo) {
        ServicesComponentFactory factory = servicesModel.getFactory();
        Parameter param = factory.createParameter();
        param.setNameAttr("ServiceClass"); //NOI18N
        param.setValue(serviceClass);
        MessageReceivers receivers = factory.createMessageReceivers();
        MessageReceiver receiver1 = factory.createMessageReceiver();
        MessageReceiver receiver2 = factory.createMessageReceiver();
        receiver1.setMepAttr("http://www.w3.org/2004/08/wsdl/in-only"); //NOI18N
        receiver1.setClassAttr("org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver"); //NOI18N
        receiver2.setMepAttr("http://www.w3.org/2004/08/wsdl/in-out"); //NOI18N
        receiver2.setClassAttr("org.apache.axis2.rpc.receivers.RPCMessageReceiver"); //NOI18N
        receivers.addMessageReceiver(receiver1);
        receivers.addMessageReceiver(receiver2);

        Services services = servicesModel.getRootComponent();
        ServiceGroup serviceGroup = (ServiceGroup)services;

        servicesModel.startTransaction();
        Service service = factory.createService();
        service.setNameAttr(serviceFo.getName());
        service.setScopeAttr("application"); //NOI18N
        service.setDescription(serviceFo.getName()+" service"); //NOI18N
        service.setMessageReceivers(receivers);
        service.addParameter(param);
        serviceGroup.addService(service);
        servicesModel.endTransaction();

    }
    /** add the clone of service element to serviceGroup, replace the serviceClass value
     * 
     * @param servicesModel
     * @param service
     * @param serviceClass
     */ 
    static void addService(ServicesModel servicesModel, Service service, String serviceClass) {
        ServicesComponentFactory factory = servicesModel.getFactory();
        Services services = servicesModel.getRootComponent();

        ServiceGroup serviceGroup = (ServiceGroup)services;

        servicesModel.startTransaction();

        Service newService = (Service)service.copy(serviceGroup);
        List<Parameter> params = newService.getParameters();
        for (Parameter param:newService.getParameters()) {
            if ("ServiceClass".equals(param.getNameAttr())) {
                param.setValue(serviceClass);
                break;
            }
        }
        serviceGroup.addService(newService);

        servicesModel.endTransaction();
    }
    /** add service element to axis2.xml, used in "from java" case
     * 
     * @param axis2Model
     * @param serviceClass
     * @param serviceFo
     * @param generateWsdl
     */
    static void addService(Axis2Model axis2Model, String serviceClass, FileObject serviceFo, boolean generateWsdl) {
        Axis2ComponentFactory factory = axis2Model.getFactory();

        Axis2 axis2 = axis2Model.getRootComponent();
        if (axis2 != null) {
            //List<org.netbeans.modules.websvc.axis2.config.model.Service> services = axis2.getServices();
            
            axis2Model.startTransaction();
            org.netbeans.modules.websvc.axis2.config.model.Service service = factory.createService();
            service.setNameAttr(serviceFo.getName());
            service.setServiceClass(serviceClass);
            if (generateWsdl) {
                GenerateWsdl genWsdl = factory.createGenerateWsdl();
                String defaultNs = AxisUtils.getNamespaceFromClassName(serviceClass);
                genWsdl.setTargetNamespaceAttr(defaultNs);
                genWsdl.setSchemaNamespaceAttr(defaultNs+"xsd");
                service.setGenerateWsdl(genWsdl);
            }
            axis2.addService(service);
            axis2Model.endTransaction();
        }
    }
    /** add service element to axis2.xml, used in "from wsdl" case
     * 
     * @param axis2Model
     * @param wsdlUrl
     * @param serviceClass
     * @param serviceName
     * @param portName
     * @param packageName
     * @param databinding
     * @param isSEI
     */
    static void addService(Axis2Model axis2Model, String wsdlUrl, String serviceClass, String serviceName, String portName, String packageName, String databinding, boolean isSEI, String moreOptions) {
        Axis2ComponentFactory factory = axis2Model.getFactory();

        Axis2 axis2 = axis2Model.getRootComponent();
        if (axis2 != null) {            
            axis2Model.startTransaction();
            org.netbeans.modules.websvc.axis2.config.model.Service service = factory.createService();
            service.setNameAttr(serviceName);factory.createService();
            service.setServiceClass(serviceClass);
            service.setWsdlUrl(wsdlUrl);
            JavaGenerator javaGenerator = factory.createJavaGenerator();
            javaGenerator.setDatabindingNameAttr(databinding);
            javaGenerator.setServiceNameAttr(serviceName);
            javaGenerator.setPortNameAttr(portName);
            javaGenerator.setPackageNameAttr(packageName);
            javaGenerator.setSEIAttr(isSEI);
            javaGenerator.setOptionsAttr(moreOptions);
            service.setJavaGenerator(javaGenerator);
            axis2.addService(service);
            axis2Model.endTransaction();
        }
    }
    
    static void generateSkeletonMethods(FileObject targetFile, final String sourceElement, final String interfaceName) 
        throws IOException {
        
        JavaSource targetJavaSource = JavaSource.forFileObject(targetFile);
        assert targetJavaSource != null && sourceElement != null;
        
        final List<ElementHandle> elementHandles = new ArrayList<ElementHandle>();

        CancellableTask<CompilationController> introspectTask = new CancellableTask<CompilationController>() {

            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeEl = controller.getElements().getTypeElement(sourceElement);
                if (typeEl != null) {
                    List<? extends Element> elements = typeEl.getEnclosedElements();
                    for (Element el:elements) {
                        elementHandles.add(ElementHandle.create(el));
                    }
                }
            }

            public void cancel() {
            }
        };       
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree targetClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (targetClass!=null) {
                      
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    
                    ClassTree modifiedClass = null;
                    // add implementation clause
                    if (interfaceName != null) {
                        modifiedClass = genUtils.addImplementsClause(targetClass, interfaceName);
                    }
                    for (ElementHandle elementHandle: elementHandles) {
                        Element element = elementHandle.resolve(workingCopy);
                        ExecutableElement methodElement = (ExecutableElement)element;
                        
                        List<? extends VariableElement> parameterElements = methodElement.getParameters();
                        List<VariableTree> parameters = new ArrayList<VariableTree>();
                        for (VariableElement param:parameterElements) {
                            parameters.add(make.Variable(param, null));
                        }
                        List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
                        List<ExpressionTree> thrownTrees = new ArrayList<ExpressionTree>();
                        for (TypeMirror thr:thrownTypes) {
                            thrownTrees.add((ExpressionTree)make.Type(thr));
                        }
                        
                        if (element.getKind() == ElementKind.METHOD) {
                            MethodTree method = make.Method (
                                    make.Modifiers(methodElement.getModifiers()),
                                    methodElement.getSimpleName(), // operation name
                                    make.Type(methodElement.getReturnType()), // return type
                                    Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                    parameters,
                                    thrownTrees, // throws
                                    "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Please implement "+targetClass.getSimpleName()+"#"+methodElement.getSimpleName()+" method.\") }", // body text
                                    null // default value - not applicable here, used by annotations
                                    );

                            if (modifiedClass == null) {
                                modifiedClass =  make.addClassMember(targetClass, method);
                            } else {
                                modifiedClass =  make.addClassMember(modifiedClass, method);
                            }
                        }
                    }
                    if (modifiedClass != null) workingCopy.rewrite(targetClass, modifiedClass);
                }
            }
            
            public void cancel() {
            }
        };
        targetJavaSource.runUserActionTask(introspectTask, true);
        targetJavaSource.runModificationTask(task).commit();
        
    }
    
    public static void addAxis2Library(Project project) {
        ClassPath classPath = null;
        FileObject srcRoot = null;
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups!=null && sourceGroups.length>0) {
            srcRoot = sourceGroups[0].getRootFolder();
            classPath = ClassPath.getClassPath(srcRoot,ClassPath.COMPILE);
        }
        FileObject wsdl2Java=null;
        if (classPath!=null) {
            wsdl2Java = classPath.findResource("org/apache/axis2/wsdl/WSDL2Javajava.class"); // NOI18N
        }
        if (wsdl2Java==null) {
            // add axis2 library
            Library axis2lib = LibraryManager.getDefault().getLibrary("axis2"); //NOI18N
            if (axis2lib != null) {
                try {
                    ProjectClassPathModifier.addLibraries(new Library[]{axis2lib}, srcRoot, ClassPath.COMPILE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
