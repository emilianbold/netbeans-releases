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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.*;
import org.netbeans.modules.refactoring.java.classpath.RefactoringClassPathImplementation;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MoveRefactoringPlugin extends JavaRefactoringPlugin {

    private Map packagePostfix = new HashMap();
    private AbstractRefactoring refactoring;
    ArrayList<FileObject> filesToMove = new ArrayList();
    HashMap<FileObject,ElementHandle> classes;
    Map<FileObject, Set<FileObject>> whoReferences = new HashMap();
    private FileObject[] origFilesToMove;
    public MoveRefactoringPlugin(MoveRefactoring move) {
        this.refactoring = move;
        setup(move.getRefactoringSource().lookupAll(FileObject.class), "", true);
    }
    
    public MoveRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        FileObject fo = rename.getRefactoringSource().lookup(FileObject.class);
        if (fo!=null) {
            setup(Collections.singletonList(fo), "", true);
        } else {
            setup(Collections.singletonList(((NonRecursiveFolder)rename.getRefactoringSource().lookup(NonRecursiveFolder.class)).getFolder()), "", false);
        }
    }
    
    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> set = new HashSet<FileObject>();
        for (Map.Entry<FileObject, ElementHandle> entry : classes.entrySet()) {
            //set.add(SourceUtils.getFile(el, cpInfo));
            Set<FileObject> files = idx.getResources(entry.getValue(), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
            set.addAll(files);
            whoReferences.put(entry.getKey(), files);
        }
        set.addAll(filesToMove);
        return set;
    }    

    private ClasspathInfo getClasspathInfo(CompilationInfo info) {
        //ClassPath boot = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
        ClassPath rcp = RefactoringClassPathImplementation.getCustom(filesToMove);
        ClasspathInfo cpi = ClasspathInfo.create(rcp, rcp, rcp);
        return cpi;
    }
    
    private void initClasses() {
        classes = new HashMap();
        for (int i=0;i<filesToMove.size();i++) {
            final int j = i;
            try {
                JavaSource source = JavaSource.forFileObject(filesToMove.get(i));
                
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    
                    public void cancel() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    
                    public void run(final CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        List<? extends Tree> trees= parameter.getCompilationUnit().getTypeDecls();
                        for (Tree t: trees) {
                            if (t.getKind() == Tree.Kind.CLASS) {
                                if (((ClassTree) t).getSimpleName().toString().equals(filesToMove.get(j).getName())) {
                                    classes.put(filesToMove.get(j), ElementHandle.create(parameter.getTrees().getElement(TreePath.getPath(parameter.getCompilationUnit(), t))));
                                    return ;
                                }
                            }
                        }
                              
                    }
                }, true);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            };
            
        }
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        final CompilationInfo mainInfo = refactoring.getContext().lookup(CompilationInfo.class);
        
        if (cpInfo==null) {
            cpInfo = getClasspathInfo(mainInfo);
            refactoring.getContext().add(cpInfo);
        }
        
        initClasses();
        
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            final Collection<ModificationResult> results = processFiles(a, new UpdateReferences());
            elements.registerTransaction(new RetoucheCommit(results));
            for (ModificationResult result:results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (ModificationResult.Difference dif: result.getDifferences(jfo)) {
                        elements.add(refactoring,DiffElement.create(dif, jfo,result));
                    }
                }
            }
            
        }
        fireProgressListenerStop();
        return null;
    }
    String getNewPackageName() {
        if (refactoring instanceof MoveRefactoring) {
            return RetoucheUtils.getPackageName(((MoveRefactoring) refactoring).getTarget().lookup(URL.class));        
        } else {
            return ((RenameRefactoring) refactoring).getNewName();
        }
    }
    
    String getTargetPackageName(FileObject fo) {
        if (refactoring instanceof RenameRefactoring) {
            if (refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class) !=null)
                //package rename
                return getNewPackageName();
            else {
                //folder rename
                FileObject folder = refactoring.getRefactoringSource().lookup(FileObject.class);
                ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
                FileObject root = cp.findOwnerRoot(folder);
                String prefix = FileUtil.getRelativePath(root, folder.getParent()).replace('/','.');
                String postfix = FileUtil.getRelativePath(folder, fo.getParent()).replace('/', '.');
                String t = concat(prefix, getNewPackageName(), postfix);
                return t;
            }
        } else if (packagePostfix != null) {
            String postfix = (String) packagePostfix.get(fo);
            String packageName = concat(null, getNewPackageName(), postfix);
            return packageName;
        } else
            return getNewPackageName();
    }
    
    private void setup(Collection fileObjects, String postfix, boolean recursively) {
        for (Iterator i = fileObjects.iterator(); i.hasNext(); ) {
            FileObject fo = (FileObject) i.next();
            if (RetoucheUtils.isJavaFile(fo)) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
                filesToMove.add(fo);
            } else if (!(fo.isFolder())) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
            } else if (VisibilityQuery.getDefault().isVisible(fo)) {
                //o instanceof DataFolder
                //CVS folders are ignored
                boolean addDot = !"".equals(postfix);
                Collection col = new ArrayList();
                for (FileObject fo2: fo.getChildren()) {
                    col.add(fo2);
                }
                if (recursively)
                    setup(col, postfix +(addDot?".":"") +fo.getName(), true); // NOI18N
            }
        }
    }
 
    private String concat(String s1, String s2, String s3) {
        String result = "";
        if (s1 != null && !"".equals(s1)) {
            result += s1 + "."; // NOI18N
        }
        result +=s2;
        if (s3 != null && !"".equals(s3)) {
            result += ("".equals(result)? "" : ".") + s3; // NOI18N
        }
        return result;
    }        
    private class UpdateReferences implements CancellableTask<WorkingCopy> {

        public UpdateReferences() {
        }

        public void cancel() {
        }

        public void run(WorkingCopy compiler) throws IOException {
            compiler.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
            
            MoveTransformer findVisitor = new MoveTransformer(compiler, MoveRefactoringPlugin.this);
            findVisitor.scan(compiler.getCompilationUnit(), null);

            for (TreePath tree : findVisitor.getUsages()) {
                    ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
          }
            fireProgressListenerStep();
        }
    }        
}    
