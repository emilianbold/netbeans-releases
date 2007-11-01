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

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Helper for adding WS Operation to Web Service.
 * @author Milan Kuchtiak
 */
public class AddWsOperationHelper {
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    
    private final String name;
    private final boolean createAnnotations;
    private MethodModel method;
    
    public AddWsOperationHelper(String name, boolean flag) {
        this.name = name;
        this.createAnnotations = flag;
    }
    
    public AddWsOperationHelper(String name) {
        this(name,true);
    }
    
    protected MethodModel getPrototypeMethod() {
        return MethodModel.create(
                NbBundle.getMessage(AddWsOperationHelper.class,"TXT_DefaultOperationName"), //NOI18N
                "java.lang.String", //NOI18N
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
    }
    
    public String getTitle() {
        return name;
    }
    
    protected MethodCustomizer createDialog(FileObject fileObject, MethodModel methodModel) throws IOException {
        
        return MethodCustomizerFactory.operationMethod(
                getTitle(),
                methodModel,
                ClasspathInfo.create(
                    EMPTY_PATH, // boot classpath
                    ClassPath.getClassPath(fileObject, ClassPath.COMPILE), // classpath from dependent projects and libraries
                    ClassPath.getClassPath(fileObject, ClassPath.SOURCE)), // source classpath
                getExistingMethods(fileObject));
    }
    
    public void addMethod(FileObject fileObject, String className) throws IOException {
        if (className == null) {
            return;
        }
        method = getPrototypeMethod();
        MethodCustomizer methodCustomizer = createDialog(fileObject, method);
        if (methodCustomizer.customizeMethod()) {
            try {
                
                method = methodCustomizer.getMethodModel();
                okButtonPressed(method, fileObject, className);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        else{  //user pressed cancel button
            method = null;
        }
    }
    
    /**
     *  Variant of addMethod(FileObject, String)which returns the final MethodModel.
     */ 
    public MethodModel getMethodModel(FileObject fileObject, String className) throws IOException{
        addMethod(fileObject, className);
        return method;
    }
    
    protected void okButtonPressed(MethodModel method, FileObject implClassFo, String className) throws IOException {
        addOperation(method, implClassFo);
    }
    
    protected FileObject getDDFile(FileObject fileObject) {
        return EjbJar.getEjbJar(fileObject).getDeploymentDescriptor();
    }
    
    /*
     * Adds a method definition to the the implementation class
     */
    private void addOperation(final MethodModel methodModel, final FileObject implClassFo) {
        final JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AddWsOperationHelper.class, "MSG_AddingNewOperation", methodModel.getName()));
        handle.start(100);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                MethodTree method = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                if (method!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    if (genUtils!=null) {
                        handle.progress(20);
                        ClassTree javaClass = genUtils.getClassTree();
                        TypeElement webMethodAn = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                        TypeElement webParamAn = workingCopy.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
                        
                        AssignmentTree opName = make.Assignment(make.Identifier("operationName"), make.Literal(method.getName().toString())); //NOI18N
                        
                        AnnotationTree webMethodAnnotation = make.Annotation(
                                make.QualIdent(webMethodAn),
                                Collections.<ExpressionTree>singletonList(opName)
                                );
                        // Public modifier
                        ModifiersTree modifiersTree = make.Modifiers(
                                Collections.<Modifier>singleton(Modifier.PUBLIC),
                                Collections.<AnnotationTree>emptyList()
                                );
                        // add @WebMethod annotation
                        if(createAnnotations)
                            modifiersTree = make.addModifiersAnnotation(modifiersTree, webMethodAnnotation);
                        
                        handle.progress(40);
                        // add @Oneway annotation
                        if (Kind.PRIMITIVE_TYPE == method.getReturnType().getKind()) {
                            PrimitiveTypeTree primitiveType = (PrimitiveTypeTree)method.getReturnType();
                            if (TypeKind.VOID == primitiveType.getPrimitiveTypeKind()) {
                                TypeElement oneWayAn = workingCopy.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
                                AnnotationTree oneWayAnnotation = make.Annotation(
                                        make.QualIdent(oneWayAn),
                                        Collections.<ExpressionTree>emptyList()
                                        );
                                if(createAnnotations)
                                    modifiersTree = make.addModifiersAnnotation(modifiersTree, oneWayAnnotation);
                            }
                        }
                        
                        // add @WebParam annotations
                        List<? extends VariableTree> parameters = method.getParameters();
                        List<VariableTree> newParameters = new ArrayList<VariableTree>();
                        if(createAnnotations) {
                            for (VariableTree param:parameters) {
                                AnnotationTree paramAnnotation = make.Annotation(
                                        make.QualIdent(webParamAn),
                                        Collections.<ExpressionTree>singletonList(
                                        make.Assignment(make.Identifier("name"), make.Literal(param.getName().toString()))) //NOI18N
                                        );
                                newParameters.add(genUtils.addAnnotation(param, paramAnnotation));
                            }
                        } else {
                            newParameters.addAll(parameters);
                        }
                        
                        handle.progress(70);
                        // create new (annotated) method
                        MethodTree  annotatedMethod = genUtils.getTypeElement().getKind() == ElementKind.CLASS ?
                            make.Method(
                                modifiersTree,
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                newParameters,
                                method.getThrows(),
                                getMethodBody(method.getReturnType()), //NOI18N
                                (ExpressionTree)method.getDefaultValue()) :
                            make.Method(
                                modifiersTree,
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                newParameters,
                                method.getThrows(),
                                (BlockTree)null,
                                (ExpressionTree)method.getDefaultValue());
                        Comment comment = Comment.create(Style.JAVADOC, 0,0,0,NbBundle.getMessage(AddWsOperationHelper.class, "TXT_WSOperation"));
                        make.addComment(annotatedMethod, comment, true);
                        
                        handle.progress(90);
                        ClassTree modifiedClass = make.addClassMember(javaClass,annotatedMethod);
                        workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
            }
            public void cancel() {}
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        targetSource.runModificationTask(modificationTask).commit();    
                        DataObject dataObject = DataObject.find(implClassFo);
                        if (dataObject!=null) {
                            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
                            if (cookie!=null) cookie.save();
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    } finally {
                        handle.finish();
                    }                
                }
            });
        } else {
            try {
                targetSource.runModificationTask(modificationTask).commit();    
                DataObject dataObject = DataObject.find(implClassFo);
                if (dataObject!=null) {
                    SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
                    if (cookie!=null) cookie.save();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } finally {
                handle.finish();
            }            
        }

    }
    
    private String getMethodBody(Tree returnType) {
        String body = null;
        if (Kind.PRIMITIVE_TYPE == returnType.getKind()) {
            TypeKind type = ((PrimitiveTypeTree)returnType).getPrimitiveTypeKind();
            if (TypeKind.VOID == type) body = ""; //NOI18N
            else if (TypeKind.BOOLEAN == type) body = "return false;"; // NOI18N
            else if (TypeKind.INT == type) body = "return 0;"; // NOI18N
            else if (TypeKind.LONG == type) body = "return 0;"; // NOI18N
            else if (TypeKind.FLOAT == type) body = "return 0.0;"; // NOI18N
            else if (TypeKind.DOUBLE == type) body = "return 0.0;"; // NOI18N
            else if (TypeKind.BYTE == type) body = "return 0;"; // NOI18N
            else if (TypeKind.SHORT == type) body = "return 0;"; // NOI18N
            else if (TypeKind.CHAR == type) body = "return ' ';"; // NOI18N
            else body = "return null"; //NOI18N
        } else
            body = "return null"; //NOI18N
        return "{\n\t\t"+NbBundle.getMessage(AddWsOperationHelper.class, "TXT_TodoComment")+"\n"+body+"\n}";
    }
    /*
    protected static MethodsNode getMethodsNode() {
        Node[] nodes = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]);
        if (nodes.length != 1) {
            return null;
        }
        return nodes[0].getLookup().lookup(MethodsNode.class);
    }
     */
    
    private Collection<MethodModel> getExistingMethods(FileObject implClass) {
        JavaSource javaSource = JavaSource.forFileObject(implClass);
        final ResultHolder<MethodModel> result = new ResultHolder<MethodModel>();
        if (javaSource!=null) {
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    SourceUtils srcUtils = SourceUtils.newInstance(controller);
                    if (srcUtils!=null) {
                        // find methods
                        List<ExecutableElement> allMethods = getMethods(controller, srcUtils.getTypeElement());
                        Collection<MethodModel> wsOperations = new ArrayList<MethodModel>();
                        boolean foundWebMethodAnnotation=false;
                        for(ExecutableElement method:allMethods) {
                            // check if return type is a valid type
                            if (method.getReturnType().getKind() == TypeKind.ERROR) break;
                            // check if param types are valid types
                            
                            boolean validParamTypes = true;
                            List<? extends VariableElement> params = method.getParameters();
                            for (VariableElement param:params) {
                                if (param.asType().getKind() == TypeKind.ERROR) {
                                    validParamTypes = false;
                                    break;
                                }
                            }
                            if (validParamTypes) {
                                MethodModel methodModel = MethodModelSupport.createMethodModel(controller, method);
                                wsOperations.add(methodModel);
                            }
                        } // for
                        result.setResult(wsOperations);
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
        return result.getResult();
    }
    
    private List<ExecutableElement> getMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method:methods) {
            //Set<Modifier> modifiers = method.getModifiers();
            //if (modifiers.contains(Modifier.PUBLIC)) {
            publicMethods.add(method);
            //}
        }
        return publicMethods;
    }
    
    /** Holder class for result
     */
    private class ResultHolder<E> {
        private Collection<E> result;
        
        public Collection<E> getResult() {
            return result;
        }
        
        public void setResult(Collection<E> result) {
            this.result=result;
        }
    }
}
