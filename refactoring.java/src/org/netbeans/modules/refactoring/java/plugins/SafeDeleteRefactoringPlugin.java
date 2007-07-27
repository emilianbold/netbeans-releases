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

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.swing.Action;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.java.ui.SafeDeleteUI;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGrip;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;


/**
 * The plugin that carries out Safe Delete refactoring.
 * @author Bharath Ravikumar
 * @author Jan Becicka
 */
public class SafeDeleteRefactoringPlugin extends JavaRefactoringPlugin {
    private SafeDeleteRefactoring refactoring;
    private WhereUsedQuery[] whereUsedQueries;
    
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
        Set<Object> refactoredObjects = new HashSet<Object>();
        Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, whereUsedQueries.length + 1);
        for(int i = 0;i < whereUsedQueries.length; ++i) {
            Object refactoredObject = whereUsedQueries[i].getRefactoringSource().lookup(Object.class);
            refactoredObjects.add(refactoredObject);
            
            whereUsedQueries[i].prepare(inner);
            
            if (!files.contains(grips.get(i).getFileObject())) {
                TransformTask task = new TransformTask(new DeleteTransformer(), grips.get(i));
                createAndAddElements(Collections.singleton(grips.get(i).getFileObject()), task, refactoringElements, refactoring);
            }
            fireProgressListenerStep();
        }
        
        Collection importStmts = new ArrayList();
        
        for (Iterator<RefactoringElement> iter = inner.getRefactoringElements().iterator(); iter.hasNext(); ) {
            ElementGrip elem = iter.next().getLookup().lookup(ElementGrip.class);
            boolean isOuterRef = true;
            ElementGrip parent = elem;
            if (parent!=null) {
                do {
                    if (refactoredObjects.contains(parent.getHandle())) {
                        isOuterRef = false;
                        break;
                    }
                    parent = parent.getParent();
                } while (parent!=null);
            }
            //ElementGrip comp = elem;
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
                return new Problem(false, getString("ERR_ReferencesFound"), ProblemDetailsFactory.createProblemDetails(new ProblemDetailsImplemen(new WhereUsedQueryUI(elem!=null?elem.getHandle():null, "!!!TODO!!!", refactoring), inner)));
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
    @Override
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
//                return new Problem(true, NbBundle.getMessage(SafeDeleteRefactoringPlugin.class, "ERR_ProjectNotOpened"));
//            }
//        }
        return null;
    }
    
    /**
     * A No-op for this particular refactoring.
     */
    @Override
    public Problem fastCheckParameters() {
        //Nothing to be done for Safe Delete
        return null;
    }
    
    private ArrayList<TreePathHandle> grips = new ArrayList<TreePathHandle>();
    /**
     * Invokes the checkParameters of each of the underlying
     * WhereUsed refactorings and returns a Problem (if any)
     * returned by any of these queries.
     */
    @Override
    public Problem checkParameters() {
        //This class expects too many details from SafeDeleteRefactoring
        //But there's no other go I guess.
        grips.clear();
        for (final FileObject f:refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
            JavaSource source = JavaSource.forFileObject(f);
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                        
                    }
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        CompilationUnitTree cut = co.getCompilationUnit();
                        for (Tree t: cut.getTypeDecls()) {
                            TreePathHandle handle = TreePathHandle.create(TreePath.getPath(cut, t), co);
                            if (!containsHandle(handle, co))
                                grips.add(handle);
                        }
                    }
                }, true);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        grips.addAll(refactoring.getRefactoringSource().lookupAll(TreePathHandle.class));

        whereUsedQueries = new WhereUsedQuery[grips.size()];
        for(int i = 0;i <  whereUsedQueries.length; ++i) {
            whereUsedQueries[i] = createQuery(grips.get(i));
            
            whereUsedQueries[i].putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, refactoring.isCheckInComments());
            if(Tree.Kind.METHOD.equals(grips.get(i).getKind())){
                whereUsedQueries[i].putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS,true);
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
    
    private boolean containsHandle(TreePathHandle handle, CompilationInfo info) {
        for (TreePathHandle current : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            if (current.resolveElement(info).equals(handle.resolveElement(info))) {
                return true;
            }
        }
        return false;
    }
    
    private WhereUsedQuery createQuery(TreePathHandle tph) {
        WhereUsedQuery q = new WhereUsedQuery(Lookups.singleton(tph));
        for (Object o:refactoring.getContext().lookupAll(Object.class)) {
            q.getContext().add(o);
        }
        q.getContext().add(refactoring);
        q.getContext().add(this);
        return q;
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
 
    private static class ImportRefDeleteElement extends SimpleRefactoringElementImplementation {
        
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

        public Lookup getLookup() {
//            return importStmt.getResource();
            return Lookup.EMPTY;
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

    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
}
