/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.refactoring.java.plugins;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.DiffElement;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.java.classpath.RefactoringClassPathImplementation;
import org.netbeans.modules.refactoring.java.plugins.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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
    private final RenameRefactoring renameRefactoring = null;
    private final Logger log = Logger.getLogger("org.netbeans.modules.refactoring.plugins"); // NOI18N
    private ErrorManager errMngr =  ErrorManager.getDefault();
    private static final float ONE_DOT_FIVE = 1.5f;
    
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
        
        ElementHandle superType = refactoring.getTargetSuperType();
        TreePathHandle subClassHandle = refactoring.getTypeElement();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, 1);
        
        try{
            getClasspathInfo(subClassHandle, refactoringElements);
        }finally{
            fireProgressListenerStop();
        }
        return null;
    }
    
    /**
     *Checks whether the candidate element is a valid Type.
     *@return Problem The problem instance indicating that an invalid element was selected.
     */
    public org.netbeans.modules.refactoring.api.Problem preCheck() {
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
    public org.netbeans.modules.refactoring.api.Problem fastCheckParameters() {
        if (refactoring.getTargetSuperType() == null) {
            return new Problem(true, NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class, "ERR_UseSuperTypeNoSuperType"));
        }
        return null;
    }
    
    /**
     * A no op. Returns null
     */
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        return null;
    }
    
    //---------private  methods follow--------
    
    private void getClasspathInfo(final TreePathHandle subClassHandle,
            final RefactoringElementsBag elemsBag){
        JavaSource javaSrc = JavaSource.forFileObject(subClassHandle.getFileObject());
        
        
        try{
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                }
                
                public void run(CompilationController complController) throws IOException {
                    complController.toPhase(Phase.ELEMENTS_RESOLVED);
                    
                    ClassPath bootClassPath = complController.getClasspathInfo().
                            getClassPath(ClasspathInfo.PathKind.BOOT);
                    ClassPath srcClassPath = complController.getClasspathInfo().
                            getClassPath(ClasspathInfo.PathKind.SOURCE);
                    FileObject fo = subClassHandle.getFileObject();
                    ClassPath refacClassPath = RefactoringClassPathImplementation.
                            getCustom(Collections.singleton(fo));
                    ClasspathInfo classpathInfo = ClasspathInfo.
                            create(bootClassPath, srcClassPath, refacClassPath);
                    
                    ClassIndex clsIndx = classpathInfo.getClassIndex();
                    TypeElement javaClassElement = (TypeElement) subClassHandle.
                            resolveElement(complController);
                    EnumSet<SearchKind> typeRefSearch = EnumSet.of(ClassIndex.SearchKind.
                            TYPE_REFERENCES);
                    Set<FileObject> refFileObjSet = clsIndx.getResources(ElementHandle.
                            create(javaClassElement), typeRefSearch,
                            EnumSet.of(ClassIndex.SearchScope.SOURCE));
                    
                    
                    if(! refFileObjSet.isEmpty()){
                        fireProgressListenerStart(AbstractRefactoring.PREPARE,
                                refFileObjSet.size());
                        
                        Collection<ModificationResult> results =
                                processFiles(refFileObjSet,
                                new FindRefTask(subClassHandle,
                                refactoring.getTargetSuperType()));
                        elemsBag.registerTransaction(new RetoucheCommit(results));
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
                    }
                }
            }, false);
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
        return;
    }
    
    private final class FindRefTask implements CancellableTask<WorkingCopy>{
        
        private final TreePathHandle subClassHandle;
        private final ElementHandle superClassHandle;
        private FindRefTask(TreePathHandle subClassHandle, ElementHandle
                superClassHandle){
            this.subClassHandle = subClassHandle;
            this.superClassHandle = superClassHandle;
        }
        
        public void cancel() {
        }
        
        public void run(WorkingCopy compiler) throws Exception {
            compiler.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
            Element subClassElement = subClassHandle.resolveElement(compiler);
            Element superClassElement = superClassHandle.resolve(compiler);
            assert subClassElement != null;
            ReferencesVisitor findRefVisitor = new ReferencesVisitor(compiler,
                    subClassElement, superClassElement);
            findRefVisitor.scan(compiler.getCompilationUnit(), subClassElement);
            for (TreePath tree : findRefVisitor.getUsages()) {
                ElementGripFactory.getDefault().put(compiler.getFileObject(),
                        tree, compiler);
            }
            fireProgressListenerStep();
        }
    }
    
    private static class ReferencesVisitor extends SearchVisitor{
        private final Element superTypeElement;
        private final Element subTypeElement;
        private ReferencesVisitor(WorkingCopy workingCopy, Element subClassElement,
                Element superClassElement){
            super(workingCopy);
            this.superTypeElement = superClassElement;
            this.subTypeElement = subClassElement;
        }
        
        @Override
        public Tree visitVariable(VariableTree varTree, Element elementToMatch) {
            Element typeElement = workingCopy.getTrees().getElement(getCurrentPath());
            TreePath treePath = getCurrentPath();
            VariableElement varElement = (VariableElement) workingCopy.
                    getTrees().getElement(treePath);
            TypeMirror varType = varElement.asType();
            if(varType.equals(elementToMatch.asType())){
                if(isReplaceCandidate(varElement)){
                    replaceWithSuperType(varTree, superTypeElement);
                    addUsage(treePath);
                }
            }
            return super.visitVariable(varTree, elementToMatch);
        }
        
        private boolean isReplaceCandidate(VariableElement varElement){
            VarUsageVisitor varUsagesVisitor = new VarUsageVisitor(workingCopy,
                    (TypeElement) superTypeElement);
            varUsagesVisitor.scan(workingCopy.getCompilationUnit(),
                    varElement);
            return varUsagesVisitor.isReplaceCandidate();
        }
        
        private void replaceWithSuperType(VariableTree oldVarTree, Element superClassElement){
            Tree superTypeTree = make.Type(superClassElement.asType());
            ExpressionTree oldInitTree = oldVarTree.getInitializer();
            ModifiersTree oldModifiers = oldVarTree.getModifiers();
            Tree newTree = make.Variable(oldModifiers, oldVarTree.getName(),
                    superTypeTree, oldInitTree);
            workingCopy.rewrite(oldVarTree, newTree);
        }
        
    }
}
