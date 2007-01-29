/*
 * JaxWsAddOperation.java
 *
 * Created on December 12, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.jaxws.actions;

import com.sun.source.tree.MethodTree;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.openide.filesystems.FileObject;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsAddOperation implements AddOperationCookie {
    
    private FileObject implClassFo;
    /** Creates a new instance of JaxWsAddOperation */
    public JaxWsAddOperation(FileObject implClassFo) {
        this.implClassFo=implClassFo;
    }
    
    /*
     * Adds a method definition to the the implementation class, possibly to SEI
     */
    public void addOperation(final MethodModel methodModel) {
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                MethodTree method = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                if (method!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    if (genUtils!=null) {
                        ClassTree javaClass = genUtils.getClassTree();
                        TypeElement webMethodAn = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                        TypeElement webParamAn = workingCopy.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N

                        AnnotationTree webMethodAnnotation = make.Annotation(
                            make.QualIdent(webMethodAn), 
                            Collections.<ExpressionTree>emptyList()
                        );
                        // add @WebMethod annotation
                        ModifiersTree modifiersTree = make.addModifiersAnnotation(method.getModifiers(), webMethodAnnotation);

                        // add @Oneway annotation
                        if (Kind.PRIMITIVE_TYPE == method.getReturnType().getKind()) {
                            PrimitiveTypeTree primitiveType = (PrimitiveTypeTree)method.getReturnType();
                            if (TypeKind.VOID == primitiveType.getPrimitiveTypeKind()) {
                                TypeElement oneWayAn = workingCopy.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
                                AnnotationTree oneWayAnnotation = make.Annotation(
                                    make.QualIdent(oneWayAn), 
                                    Collections.<ExpressionTree>emptyList()
                                );
                                modifiersTree = make.addModifiersAnnotation(modifiersTree, oneWayAnnotation);
                            }
                        }

                        // add @WebParam annotations 
                        List<? extends VariableTree> parameters = method.getParameters();
                        List<VariableTree> newParameters = new ArrayList<VariableTree>();
                        for (VariableTree param:parameters) {
                            AnnotationTree paramAnnotation = make.Annotation(
                                make.QualIdent(webParamAn), 
                                Collections.<ExpressionTree>singletonList(
                                    make.Assignment(make.Identifier("name"), make.Literal(param.getName().toString()))) //NOI18N
                            );
                            newParameters.add(genUtils.addAnnotation(param, paramAnnotation));
                        }
                        // create new (annotated) method
                        MethodTree  annotatedMethod = make.Method(
                                    modifiersTree,
                                    method.getName(),
                                    method.getReturnType(),
                                    method.getTypeParameters(),
                                    newParameters,
                                    method.getThrows(),
                                    method.getBody(),
                                    (ExpressionTree)method.getDefaultValue());
                        Comment comment = Comment.create(NbBundle.getMessage(JaxWsAddOperation.class, "TXT_WSOperation"));                    
                        make.addComment(annotatedMethod, comment, true);

                        ClassTree modifiedClass = make.addClassMember(javaClass,annotatedMethod);
                        workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
            }
            public void cancel() {}
        };
        try {
            targetSource.runModificationTask(modificationTask).commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }        
    }
    
}
