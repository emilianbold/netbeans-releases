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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
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
                "operation", //NOI18N
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
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Adding operation");
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
                            MethodModel methodModel = MethodModelSupport.createMethodModel(controller, method);
                            wsOperations.add(methodModel);
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
