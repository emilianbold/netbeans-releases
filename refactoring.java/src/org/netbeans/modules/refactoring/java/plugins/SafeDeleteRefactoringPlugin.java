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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.swing.Action;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.DiffElement;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.ui.UI;
import org.netbeans.modules.refactoring.java.api.JavaWhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ProblemDetailsFactory;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.java.ui.SafeDeleteUI;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGrip;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;


/**
 * The plugin that carries out Safe Delete refactoring.
 * @author Bharath Ravikumar, Jan Becicka
 */
public class SafeDeleteRefactoringPlugin extends JavaRefactoringPlugin {
    private SafeDeleteRefactoring refactoring;
    private JavaWhereUsedQuery[] whereUsedQueries;
    
    /**
     * Creates the a new instance of the Safe Delete refactoring
     * plugin.
     * @param refactoring The refactoring to be used by this plugin
     */
    public SafeDeleteRefactoringPlugin(SafeDeleteRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    /**
     * For each element to be refactored, the corresponding
     * prepare method of the underlying WhereUsed query is
     * invoked to check for usages. If none is present, a
     * <CODE>SafeDeleteRefactoringElement</CODE> is created
     * with the corresponding element.
     * @param refactoringElements
     * @return
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        RefactoringSession inner = RefactoringSession.create("delete"); // NOI18N
        Set refactoredObjects = new HashSet();
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, whereUsedQueries.length + 1);
        for(int i = 0;i < whereUsedQueries.length; ++i) {
            Object refactoredObject = whereUsedQueries[i].getRefactoredObject();
            refactoredObjects.add(refactoredObject);

            whereUsedQueries[i].prepare(inner);
            
            JavaSource javaSource = JavaSource.forFileObject(grips.get(i).getFileObject());
            try {
                final ModificationResult result = javaSource.runModificationTask(new FindTask(refactoringElements, grips.get(i)));
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (ModificationResult.Difference dif: result.getDifferences(jfo)) {
                        refactoringElements.add(refactoring,DiffElement.create(dif, jfo));
                    }
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            fireProgressListenerStep();
        }
        
        Collection importStmts = new ArrayList();
        
        for (Iterator iter = inner.getRefactoringElements().iterator(); iter.hasNext(); ) {
            ElementGrip elem = (ElementGrip) ((RefactoringElement) iter.next()).getComposite();
            ElementGrip comp = elem;
            boolean isOuterRef = true;
//            //Check if this usage is an import statement & ignore it if so.
//            boolean isUsageImport = false;
//            Import importStmt = null;
//            if(comp instanceof Resource){
//                //An Import shows up as a Resource - not as an Import object.
//                //Hence, we go through the hassle of checking the set of import 
//                //statements. This is not a reliable way of verifying if the import is 
//                //accounting for a usage. But there's no better option currently.
//                Iterator importIterator = ((Resource)comp).getImports().iterator();
//                while(importIterator.hasNext()){
//                    importStmt = (Import) importIterator.next();
//                    if(refactoredObjects.contains(importStmt.getImportedNamespace())){
//                        isUsageImport = true;
//                        break;
//                    }
//                }
//            }
//            
//            //If the usage is an import, we'd have to store away the standalone 
//            //import to be deleted later
//            if(isUsageImport){
//                importStmts.add(new ImportRefDeleteElement(importStmt));
//                //Go no further in this iteration. Move on to the next usage.
//                continue;
//            }
//            
//            while (comp != null && !(comp instanceof Resource)) {
//                if (refactoredObjects.contains(comp)) {
//                    isOuterRef = false;
//                    break;
//                }
//                comp = (Element) comp.refImmediateComposite();
//            }
            if (isOuterRef) {
                fireProgressListenerStop();
                return new Problem(false, getString("ERR_ReferencesFound"), ProblemDetailsFactory.createProblemDetails(new ProblemDetailsImplemen(new WhereUsedQueryUI(elem.getHandle(), "!!!TODO!!!"), inner)));
            }
        }
        
        //If there we no non-import usages, delete the import statements as well.
        if(importStmts.size() > 0){
            for (Iterator it = importStmts.iterator(); it.hasNext();) {
                RefactoringElementImplementation refacElem = 
                        (RefactoringElementImplementation) it.next();
                refactoringElements.add(refactoring, refacElem);
            }
        }
        
        fireProgressListenerStop();
        return null;
    }
    
    private class ProblemDetailsImplemen implements ProblemDetailsImplementation {
        
        private RefactoringUI ui;
        private RefactoringSession rs;
        
        public ProblemDetailsImplemen(RefactoringUI ui, RefactoringSession rs) {
            this.ui = ui;
            this.rs = rs;
        }
        
        public void showDetails(Action callback, Cancellable parent) {
            parent.cancel();
            UI.openRefactoringUI(ui, rs, callback);
        }
        
        public String getDetailsHint() {
            return getString("LBL_ShowUsages");
        }
        
    }
    
    /**
     * Checks whether the element being refactored is a valid Method/Field/Class
     * @return Problem returns a generic problem message if the check fails
     */
    public Problem preCheck() {
//        Element[] refElements = refactoring.getRefactoredObjects();
//        for(int i = 0;i < refElements.length; ++i) {
//            Element refactoredObject = refElements[i];
//            boolean validType = refactoredObject instanceof ClassMember
//                    || refactoredObject instanceof LocalVariable
//                    || refactoredObject instanceof Resource;
//            if(!validType) {
//                String errMsg = NbBundle.getMessage(SafeDeleteRefactoringPlugin.class,
//                        "ERR_SafeDel_InvalidType"); // NOI18N
//                return new Problem(true,errMsg);
//            }
//            
//            if (!CheckUtils.isElementInOpenProject(refactoredObject)) {
//                return new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ProjectNotOpened"));
//            }
//        }
        return null;
    }
    
    /**
     * A No-op for this particular refactoring.
     */
    public Problem fastCheckParameters() {
        //Nothing to be done for Safe Delete
        return null;
    }
    
    private ArrayList<TreePathHandle> grips = new ArrayList();
    /**
     * Invokes the checkParameters of each of the underlying
     * WhereUsed refactorings and returns a Problem (if any)
     * returned by any of these queries.
     */
    public Problem checkParameters() {
        //This class expects too many details from SafeDeleteRefactoring
        //But there's no other go I guess.
        grips.clear();
        Object[] object = refactoring.getRefactoredObjects();
        final ArrayList<ClasspathInfo> controllers = new ArrayList();
        if (object.getClass().isAssignableFrom(FileObject[].class)) {
            for (final FileObject f:(FileObject[])object) {
                JavaSource source = JavaSource.forFileObject(f);
                try {
                    source.runUserActionTask(new CancellableTask<CompilationController>() {
                        public void cancel() {
                            
                        }
                        public void run(CompilationController co) throws Exception {
                            co.toPhase(Phase.ELEMENTS_RESOLVED);
                            CompilationUnitTree cut = co.getCompilationUnit();
                            for (Tree t: cut.getTypeDecls()) {
                                grips.add(TreePathHandle.create(TreePath.getPath(cut, t), co));
                            }
                            controllers.add(co.getClasspathInfo());                            
                        }
                    }, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            grips.addAll(Arrays.asList((TreePathHandle[])refactoring.getRefactoredObjects()));
        }

        whereUsedQueries = new JavaWhereUsedQuery[grips.size()];
        for(int i = 0;i <  whereUsedQueries.length; ++i) {
            if (!controllers.isEmpty()) {
                refactoring.getContext().add(controllers.get(i));
            }
            whereUsedQueries[i] = new JavaWhereUsedQuery(grips.get(i), refactoring);
            whereUsedQueries[i].setSearchInComments(refactoring.isCheckInComments());
            if(Tree.Kind.METHOD.equals(grips.get(i).getKind())){
                whereUsedQueries[i].setFindOverridingMethods(true);
            }
        }
        
        Problem problemFromUsage = null;
        for(int i = 0;i < whereUsedQueries.length; ++i) {
//          Fix for issue 63050. Doesn't make sense to check usages of a Resource.Ignore it.
//            if(whereUsedQueries[i].getRefactoredObject() instanceof Resource)
//                continue;
            if((problemFromUsage = whereUsedQueries[i].checkParameters()) != null)
                return problemFromUsage;
        }
        return null;
    }
    
    
    //Private Helper methods
    private static String getString(String key) {
        return NbBundle.getMessage(SafeDeleteRefactoringPlugin.class, key);
    }
    
    /**
     *Returns the default critical error message for this refactoring
     *
     */
    private Problem getProblemMessage(Object refactoredObject) {
        String errorMsg = NbBundle.getMessage(SafeDeleteUI.class,
                "DSC_SafeDelProblem", refactoredObject);// NOI18N
        return new Problem(true,errorMsg);
    }
 
    private static class ImportRefDeleteElement extends SimpleRefactoringElementImpl{
        
//        private final Import importStmt;
        private final String text = "todo";
        
//        private ImportRefDeleteElement(Import importStmt){
//            this.importStmt = importStmt;
//            text = NbBundle.getMessage(ImportRefDeleteElement.class, 
//                    "TXT_SafeDel_Delete_Ref_Import", importStmt.getName());
//        }
        
        public String getText() {
            return text;
        }

        public String getDisplayText() {
            return getText();
        }

        public void performChange() {
//            importStmt.refDelete();
        }

        public Element getComposite() {
//            return importStmt.getResource();
            return null;
        }

        public FileObject getParentFile() {
            //return JavaMetamodel.getManager().getFileObject(importStmt.getResource());
            return null;
        }

        public PositionBounds getPosition() {
            //return JavaMetamodel.getManager().getElementPosition(importStmt);
            return null;
        }
    }
    
    private class FindTask implements CancellableTask<WorkingCopy> {

        private RefactoringElementsBag elements;
        private TreePathHandle jmiObject;

        public FindTask(RefactoringElementsBag elements, TreePathHandle element) {
            super();
            this.elements = elements;
            this.jmiObject = element;
        }

        public void cancel() {
        }

        public void run(WorkingCopy compiler) throws IOException {
            compiler.toPhase(Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
            Element el = jmiObject.resolveElement(compiler);
            assert el != null;

            DeleteTransformer findVisitor = new DeleteTransformer(compiler);
            findVisitor.scan(compiler.getCompilationUnit(), el);

            for (TreePath tree : findVisitor.getUsages()) {
                ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
            }
        }
    }        
}
