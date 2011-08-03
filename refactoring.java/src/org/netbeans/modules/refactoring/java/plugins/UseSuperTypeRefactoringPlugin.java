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

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/*
 * UseSuperTypeRefactoringPlugin.java
 *
 * Created on June 22, 2005
 *
 * @author Bharath Ravi Kumar
 */
/**
 * The plugin that performs the actual work on
 * behalf of the use super type refactoring
 */
public class UseSuperTypeRefactoringPlugin extends JavaRefactoringPlugin {

    private final UseSuperTypeRefactoring refactoring;

    /**
     * Creates a new instance of UseSuperTypeRefactoringPlugin
     * @param refactoring The refactoring to be used by this plugin
     */
    public UseSuperTypeRefactoringPlugin(UseSuperTypeRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    /**
     * Prepares the underlying where used query & checks
     * for the visibility of the target type.
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle subClassHandle = refactoring.getTypeElement();
        replaceSubtypeUsages(subClassHandle, refactoringElements);
        return null;
    }

    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
            default:
                return JavaSource.forFileObject(refactoring.getTypeElement().getFileObject());
        }
    }

    /**
     *Checks whether the candidate element is a valid Type.
     *@return Problem The problem instance indicating that an invalid element was selected.
     */
    @Override
    public Problem preCheck() {
        cancelRequest = false;
        //        Element subType = refactoring.getTypeElement();
        //        if(!(subType instanceof JavaClass)){
        //            String errMsg = NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class,
        //                    "ERR_UseSuperType_InvalidElement"); // NOI18N
        //            return new Problem(true, errMsg);
        //        }
        return null;
    }

    /**
     * @return A problem indicating that no super type was selected.
     */
    @Override
    public Problem fastCheckParameters() {
        if (refactoring.getTargetSuperType() == null) {
            return new Problem(true, NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class, "ERR_UseSuperTypeNoSuperType"));
        }
        return null;
    }

    /**
     * A no op. Returns null
     */
    @Override
    public Problem checkParameters() {
        return null;
    }

    //---------private  methods follow--------

    private void replaceSubtypeUsages(final TreePathHandle subClassHandle, final RefactoringElementsBag elemsBag) {
        JavaSource javaSrc = JavaSource.forFileObject(subClassHandle.getFileObject());


        try {
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController complController) throws IOException {
                    complController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                    FileObject fo = subClassHandle.getFileObject();
                    ClasspathInfo classpathInfo = JavaRefactoringUtils.getClasspathInfoFor(fo);

                    ClassIndex clsIndx = classpathInfo.getClassIndex();
                    TypeElement javaClassElement = (TypeElement) subClassHandle.
                            resolveElement(complController);
                    EnumSet<ClassIndex.SearchKind> typeRefSearch = EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES);
                    Set<FileObject> refFileObjSet = clsIndx.getResources(ElementHandle.create(javaClassElement), typeRefSearch, EnumSet.of(ClassIndex.SearchScope.SOURCE));


                    if (!refFileObjSet.isEmpty()) {
                        fireProgressListenerStart(AbstractRefactoring.PREPARE, refFileObjSet.size());
                        try{
                            Collection<ModificationResult> results = processFiles(refFileObjSet, new FindRefTask(subClassHandle, refactoring.getTargetSuperType()));
                            elemsBag.registerTransaction(createTransaction(results));
                            for (ModificationResult result : results) {
                                for (FileObject fileObj : result.getModifiedFileObjects()) {
                                    for (Difference diff : result.getDifferences(fileObj)) {
                                        String old = diff.getOldText();
                                        if (old != null) {
                                            elemsBag.add(refactoring, DiffElement.create(diff, fileObj, result));
                                        }
                                    }
                                }
                            }
                        }finally{
                            fireProgressListenerStop();
                        }
                    }
                }
            }, false);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return;
    }

    private final class FindRefTask implements CancellableTask<WorkingCopy> {

        private final TreePathHandle subClassHandle;
        private final ElementHandle superClassHandle;

        private FindRefTask(TreePathHandle subClassHandle, ElementHandle superClassHandle) {
            this.subClassHandle = subClassHandle;
            this.superClassHandle = superClassHandle;
        }

        public void cancel() {
        }

        public void run(WorkingCopy compiler) throws Exception {
            try {
                if (compiler.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                    return;
                }
                CompilationUnitTree cu = compiler.getCompilationUnit();
                if (cu == null) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                    return;
                }
                Element subClassElement = subClassHandle.resolveElement(compiler);
                Element superClassElement = superClassHandle.resolve(compiler);
                assert subClassElement != null;
                ReferencesVisitor findRefVisitor = new ReferencesVisitor(compiler, subClassElement, superClassElement);
                findRefVisitor.scan(compiler.getCompilationUnit(), subClassElement);
            } finally {
                fireProgressListenerStep();
            }
        }

    }

    private static class ReferencesVisitor extends RefactoringVisitor {

        private final TypeElement superTypeElement;
        private final TypeElement subTypeElement;

        private ReferencesVisitor(WorkingCopy workingCopy, Element subClassElement, Element superClassElement) {
            try {
                setWorkingCopy(workingCopy);
            } catch (ToPhaseException phase) {
                //should never be thrown;
                Exceptions.printStackTrace(phase);
            }
            this.superTypeElement = (TypeElement) superClassElement;
            this.subTypeElement = (TypeElement) subClassElement;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree memSelTree, Element elemToFind) {
            Element elem = asElement(memSelTree);
            
            if ((elem != null) && isStatic(elem)) {
                Element expreElem = asElement(memSelTree.getExpression());
                //If a static member was referenced using the object instead 
                //of the class, don't handle it here.
                if(expreElem == null || ! (ElementKind.CLASS.equals(expreElem.getKind()) || 
                        ElementKind.INTERFACE.equals(expreElem.getKind()))){
                    return super.visitMemberSelect(memSelTree, elemToFind);
                }
                TypeElement type = (TypeElement) expreElem;
                if (!subTypeElement.equals(type)) {
                    return super.visitMemberSelect(memSelTree, elemToFind);
                }
                if (hidesSupTypeMember(elem, superTypeElement)) {
                    replaceType(memSelTree, superTypeElement);
                }
            }
            return super.visitMemberSelect(memSelTree, elemToFind);
        }

        
        @Override
        public Tree visitVariable(VariableTree varTree, Element elementToMatch) {
            TreePath treePath = getCurrentPath();
            VariableElement varElement = (VariableElement) workingCopy.
                    getTrees().getElement(treePath);


            //This check shouldn't be needed (ideally).
            if (varElement == null) {
                return super.visitVariable(varTree, elementToMatch);
            }

            Types types = workingCopy.getTypes();
            TypeMirror varTypeErasure = erasureOf(varElement.asType());
            TypeMirror elToMatchErasure = erasureOf(elementToMatch.asType());
        
            if (types.isSameType(varTypeErasure, elToMatchErasure)) {
                if (isReplaceCandidate(varElement)) {
                    replaceWithSuperType(varTree, superTypeElement);
                }
            }
            return super.visitVariable(varTree, elementToMatch);
        }

        // handles only simple situations like
        // ArrayList list = (ArrayList) getList();
        // where ArrayList is being substituted by a supertype
        @Override
        public Tree visitTypeCast(TypeCastTree castTree, Element elementToMatch) {
            TreePath path = getCurrentPath();
            Types types = workingCopy.getTypes();
            TypeMirror castTypeErasure = erasureOf(workingCopy.getTrees().getTypeMirror(path));
            TypeMirror elToMatchErasure = erasureOf(elementToMatch.asType());
            path = path.getParentPath();
            Element element = workingCopy.getTrees().getElement(path);
            if (element instanceof VariableElement && types.isSameType(castTypeErasure, elToMatchErasure)) {
                VariableElement varElement = (VariableElement)element;
                TypeMirror varTypeErasure = erasureOf(varElement.asType());
                if (types.isSameType(varTypeErasure, elToMatchErasure) && isReplaceCandidate(varElement)) {
                    TypeCastTree newTree = make.TypeCast(
                        make.Identifier(superTypeElement), castTree.getExpression());
                    rewrite(castTree, newTree);
                }
            }
            return super.visitTypeCast(castTree, element);
        }
        
        private boolean hidesSupTypeMember(Element methElement, TypeElement superTypeElement) {
            Elements elements = workingCopy.getElements();
            List<? extends Element> containedElements = elements.getAllMembers(superTypeElement);
            for (Element elem : containedElements) {
                boolean isPresentInSuperType = methElement.equals(elem) || 
                        elements.hides(methElement, elem);
                if ((elem != null) && isStatic(elem) && isPresentInSuperType) {
                    return true;
                }
            }
            return false;
        }

        private boolean isReplaceCandidate(VariableElement varElement) {
            VarUsageVisitor varUsagesVisitor = new VarUsageVisitor(subTypeElement,
                    workingCopy, superTypeElement);
            varUsagesVisitor.scan(workingCopy.getCompilationUnit(), varElement);
            return varUsagesVisitor.isReplaceCandidate();
        }

        private boolean isStatic(Element element) {
            Set<Modifier> modifiers = element.getModifiers();
            return modifiers.contains(Modifier.STATIC);
        }

/*        private boolean isWildCardType(Tree typeTree) {
            Tree.Kind treeKind = typeTree.getKind();
            return (Tree.Kind.EXTENDS_WILDCARD == treeKind || 
                    Tree.Kind.SUPER_WILDCARD == treeKind) ;
        }
*/
        private void replaceType(MemberSelectTree memSelTree, Element superTypeElement) {
            MemberSelectTree newTree = make.MemberSelect(
                    make.Identifier(superTypeElement), memSelTree.getIdentifier());
            rewrite(memSelTree, newTree);
        }

        private void replaceWithSuperType(VariableTree oldVarTree, Element superTypeElement) {
            TypeMirror supTypeErasure = erasureOf(superTypeElement.asType());
            Tree superTypeTree = make.Type(supTypeErasure);
  
            //TODO:The following code was an initial attempt at having intelligent
            //substitution of the correct parameter in the super type reference.
            //If the supertype is generic, the subtype's parameter (on the RHS of
            //and expression) is used in the super type's reference as well (on the 
            //LHS of an expression. But this gets complex when the subtype extends
            //an "instance" of a generic super type. Consider for example,
            //"SubType <T> extends Supertype<Number>" where Supertype is a generic type.
            //Now, if we have an expression of the form:
            //Subtype<Serializable> obj = new Subtype<Serializable>();
            //We cannot replace the LHS with Supertype<Serializable>
            //More work is needed for that. Putting it off till later and using
            //only the erasure of the super type for now. :( 
            //The commented section of code will be revived then.

/*            Tree typeTree = oldVarTree.getType();
            if(Tree.Kind.PARAMETERIZED_TYPE == typeTree.getKind()){
                ParameterizedTypeTree paramTypeTree = (ParameterizedTypeTree) typeTree;
                List<? extends Tree> typeArgTreeList = paramTypeTree.getTypeArguments();
                List<ExpressionTree> typeParamTrees = new ArrayList<ExpressionTree>(typeArgTreeList.size());
                for (Tree tree : typeArgTreeList) {
                    if(isWildCardType(tree)){
                        //Clear the list of type params to be included in the 
                        //super type's reference. We'll restrict the super type's
                        //occurence to its erasure in that case.
                        typeParamTrees.clear();
                        break;
                    }
                    typeParamTrees.add((ExpressionTree) tree);
                }
                if(! typeParamTrees.isEmpty()){
                    superTypeTree = make.ParameterizedType(
                                make.Identifier(superTypeElement.getSimpleName()), 
                                typeParamTrees);
                }
            }
*/            
            ExpressionTree oldInitTree = oldVarTree.getInitializer();
            ModifiersTree oldModifiers = oldVarTree.getModifiers();
            Tree newTree = make.Variable(oldModifiers, oldVarTree.getName(), 
                    superTypeTree, oldInitTree);
            rewrite(oldVarTree, newTree);
        }

        private Element asElement(Tree tree) {
            Trees treeUtil = workingCopy.getTrees();
            TreePath treePath = treeUtil.getPath(workingCopy.getCompilationUnit(), tree);
            Element element = treeUtil.getElement(treePath);
            return element;
        }

        private TypeMirror erasureOf(TypeMirror type) {
            Types types = workingCopy.getTypes();
            return types.erasure(type);
        }

    }
}
