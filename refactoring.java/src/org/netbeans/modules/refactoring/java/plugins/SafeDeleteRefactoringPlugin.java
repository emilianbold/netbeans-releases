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

import javax.lang.model.element.Element;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGrip;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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
    
    private static final String DOT = "."; //NOI18N
    private static final String JAVA_EXTENSION = "java";
    
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
        Collection<ElementGrip> abstractMethHandles = new ArrayList<ElementGrip>();
        Set<Object> refactoredObjects = new HashSet<Object>();
        Collection<? extends FileObject> files = lookupJavaFileObjects();
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, whereUsedQueries.length + 1);
        for(int i = 0;i < whereUsedQueries.length; ++i) {
            Object refactoredObject = whereUsedQueries[i].getRefactoringSource().lookup(Object.class);
            refactoredObjects.add(refactoredObject);
            
            whereUsedQueries[i].prepare(inner);
            TreePathHandle treePathHandle = grips.get(i);
            if(Tree.Kind.METHOD == treePathHandle.getKind()){
                JavaSource javaSrc = JavaSource.forFileObject(treePathHandle.getFileObject());
                try {
                    javaSrc.runUserActionTask(new OverriddenAbsMethodFinder(treePathHandle, abstractMethHandles), cancelRequest);

                } catch (IOException ioException) {
                    ErrorManager.getDefault().notify(ioException);
                }
            }
            
            if (!files.contains(treePathHandle.getFileObject())) {
                TransformTask task = new TransformTask(new DeleteTransformer(), grips.get(i));
                Problem problem = createAndAddElements(Collections.singleton(grips.get(i).getFileObject()), task, refactoringElements, refactoring);
                if (problem != null) {
                    fireProgressListenerStop();
                    return problem;
                }
            }
            fireProgressListenerStep();
        }
        Problem problemFromWhereUsed = null;
        for (RefactoringElement refacElem : inner.getRefactoringElements()) {
            ElementGrip elem = refacElem.getLookup().lookup(ElementGrip.class);
            if (!isPendingDelete(elem, refactoredObjects)) {
                problemFromWhereUsed = new Problem(false, getString("ERR_ReferencesFound"), ProblemDetailsFactory.createProblemDetails(new ProblemDetailsImplemen(new WhereUsedQueryUI(elem!=null?elem.getHandle():null, elem!=null?elem.toString():"", refactoring), inner)));
                break;
            }
        }
        
        for(ElementGrip absMethodGrip : abstractMethHandles){
            if (!isPendingDelete(absMethodGrip, refactoredObjects)) {
                Problem probAbsMethod = new Problem(false,
                        getParameterizedString("ERR_OverridesAbstractMethod", 
                        getMethodName(absMethodGrip.getHandle())));
                if(problemFromWhereUsed != null){
                    problemFromWhereUsed.setNext(probAbsMethod);
                }else{
                    problemFromWhereUsed = probAbsMethod;
                }
                break;
            }
        }
        
        if(problemFromWhereUsed != null){
            fireProgressListenerStop();
            return problemFromWhereUsed;
        }
        
        Collection importStmts = new ArrayList();
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
        for (final FileObject f: lookupJavaFileObjects()) {
            JavaSource source = JavaSource.forFileObject(f);
            if (source == null) {
                continue;
            }
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
    
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    private boolean containsHandle(TreePathHandle handle, CompilationInfo info) {
        Element wanted = handle.resolveElement(info);
        for (TreePathHandle current : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            if (wanted == current.resolveElement(info)) {
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
    
    private Collection<? extends FileObject> lookupJavaFileObjects() {
        Lookup lkp = refactoring.getRefactoringSource();
        Collection<? extends FileObject> javaFiles = null;
        NonRecursiveFolder folder = lkp.lookup(NonRecursiveFolder.class);
        if (folder != null) {
            javaFiles = getJavaFileObjects(folder.getFolder(), false);
        } else {
            Collection<FileObject> javaFileObjects =  new ArrayList<FileObject>();
            for (FileObject fileObject : lkp.lookupAll(FileObject.class)) {
                if (fileObject.isFolder()) {
                    javaFileObjects.addAll(getJavaFileObjects(fileObject, true));
                }else if (RetoucheUtils.isRefactorable(fileObject)) {
                    javaFileObjects.add(fileObject);
                }
            }
            javaFiles = javaFileObjects;
        }
        return javaFiles;
    }
    
    //Private Helper methods
    private static String getString(String key) {
        return NbBundle.getMessage(SafeDeleteRefactoringPlugin.class, key);
    }
    
    private static boolean isPendingDelete(ElementGrip elementGrip, Set<Object> refactoredObjects){
        ElementGrip parent = elementGrip;
        if (parent!=null) {
            do {
                if (refactoredObjects.contains(parent.getHandle())) {
                    return true;
                }
                parent = parent.getParent();
            } while (parent!=null);
        }
        return false;
    }
            
    private static String getParameterizedString(String key, Object parameter){
        return NbBundle.getMessage(SafeDeleteRefactoringPlugin.class, key, parameter);
    }
    
    private static String getMethodName(final TreePathHandle methodHandle){
        JavaSource javaSrc = JavaSource.forFileObject(methodHandle.getFileObject());
        final String[] methodNameString = new String[1];
        //Ugly hack to return the method name from the anonymous inner class
        try {
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>(){

                public void cancel() {
                    //No op
                }

                public void run(CompilationController compilationController) throws Exception {
                    ExecutableElement execElem = (ExecutableElement) methodHandle.resolveElement(compilationController);
                    TypeElement type = (TypeElement) execElem.getEnclosingElement();
                    methodNameString[0] = type.getQualifiedName() + DOT + execElem.toString();
                }
                
            }, true);

        } catch (IOException ioException) {
            ErrorManager.getDefault().notify(ioException);
        }

        return methodNameString[0];
    }
    
    private static Collection<FileObject> getJavaFileObjects(FileObject dirFileObject, boolean isRecursive){
        Collection<FileObject> javaSrcFiles = new ArrayList<FileObject>();
        addSourcesInDir(dirFileObject, isRecursive, javaSrcFiles);
        return javaSrcFiles;
    }

    private static void addSourcesInDir(FileObject dirFileObject, boolean isRecursive, Collection<FileObject> javaSrcFiles) {
        for (FileObject childFileObject : dirFileObject.getChildren()) {
            if (childFileObject.isData() && JAVA_EXTENSION.equalsIgnoreCase(childFileObject.getExt())) {
                javaSrcFiles.add(childFileObject);
            }
            else if (childFileObject.isFolder() && isRecursive) {
                addSourcesInDir(childFileObject, true, javaSrcFiles);
            }
        }
    }
}
