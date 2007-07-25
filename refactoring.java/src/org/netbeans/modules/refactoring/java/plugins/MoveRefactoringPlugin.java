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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.*;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class MoveRefactoringPlugin extends JavaRefactoringPlugin {

    private Map packagePostfix = new HashMap();
    AbstractRefactoring refactoring;
    ArrayList<FileObject> filesToMove = new ArrayList<FileObject>();
    HashMap<FileObject,ElementHandle> classes;
    Map<FileObject, Set<FileObject>> whoReferences = new HashMap<FileObject, Set<FileObject>>();
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
            setup(Collections.singletonList((rename.getRefactoringSource().lookup(NonRecursiveFolder.class)).getFolder()), "", false); // NOI18N
        }
    }
    
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        for (FileObject file:filesToMove) {
            if (!RetoucheUtils.isElementInOpenProject(file)) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(MoveRefactoringPlugin.class, "ERR_ProjectNotOpened"));
            }
        }
        return preCheckProblem;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        if (refactoring instanceof RenameRefactoring) {
            //folder rename
            FileObject f = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (f!=null) {
                String newName = ((RenameRefactoring) refactoring).getNewName();
                if (!RetoucheUtils.isValidPackageName(newName)) {
                    String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage")).format(
                            new Object[] {newName}
                    );
                    return new Problem(true, msg);
                }
                
                if (f.getParent().getFileObject(newName, f.getExt())!=null) {
                    String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class,"ERR_PackageExists")).format(
                            new Object[] {newName}
                    );
                    return new Problem(true, msg);
                }
            }
            return super.fastCheckParameters();
        }
        if (refactoring instanceof MoveRefactoring) {
            try {
                for (FileObject f: filesToMove) {
                    if (!RetoucheUtils.isJavaFile(f))
                        continue;
                    String targetPackageName = this.getTargetPackageName(f);
                    if (!RetoucheUtils.isValidPackageName(targetPackageName)) {
                        String s = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage"); //NOI18N
                        String msg = new MessageFormat(s).format(
                                new Object[] {targetPackageName}
                        );
                        return new Problem(true, msg);
                    }
                    FileObject targetRoot = RetoucheUtils.getClassPathRoot(((MoveRefactoring)refactoring).getTarget().lookup(URL.class));
                    FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));
                    
                    String pkgName = null;
                    if ((targetF!=null && !targetF.canWrite())) {
                        return new Problem(true, new MessageFormat(NbBundle.getMessage(MoveRefactoringPlugin.class,"ERR_PackageIsReadOnly")).format( // NOI18N
                                new Object[] {targetPackageName}
                        ));
                    }
                    
                    //                this.movingToDefaultPackageMap.put(r, Boolean.valueOf(targetF!= null && targetF.equals(classPath.findOwnerRoot(targetF))));
                    pkgName = targetPackageName;
                    
                    if (pkgName == null) {
                        pkgName = ""; // NOI18N
                    } else if (pkgName.length() > 0) {
                        pkgName = pkgName + '.';
                    }
                    //targetPrefix = pkgName;
                    
                    //                JavaClass[] sourceClasses = (JavaClass[]) sourceClassesMap.get(r);
                    //                String[] names = new String [sourceClasses.length];
                    //                for (int x = 0; x < names.length; x++) {
                    //                    names [x] = sourceClasses [x].getName();
                    //                }
                    //
                    //                FileObject movedFile = JavaMetamodel.getManager().getDataObject(r).getPrimaryFile();
                    String fileName = f.getName();
                    if (targetF!=null) {
                        FileObject[] children = targetF.getChildren();
                        for (int x = 0; x < children.length; x++) {
                            if (children[x].getName().equals(fileName) && "java".equals(children[x].getExt()) && !children[x].equals(f) && !children[x].isVirtual()) { //NOI18N
                                return new Problem(true, new MessageFormat(
                                        NbBundle.getMessage(MoveRefactoringPlugin.class,"ERR_ClassToMoveClashes")).format(new Object[] {fileName} // NOI18N
                                ));
                            }
                        } // for
                    }
                    
                    //                boolean accessedByOriginalPackage = ((Boolean) accessedByOriginalPackageMap.get(r)).booleanValue();
                    //                boolean movingToDefaultPackage = ((Boolean) movingToDefaultPackageMap.get(r)).booleanValue();
                    //                if (p==null && accessedByOriginalPackage && movingToDefaultPackage) {
                    //                    p= new Problem(false, getString("ERR_MovingClassToDefaultPackage")); // NOI18N
                    //                }
                    
                    //                if (f.getFolder().getPrimaryFile().equals(targetF) && isPackageCorrect(r)) {
                    //                    return new Problem(true, getString("ERR_CannotMoveIntoSamePackage"));
                    //                }
                }
            } catch (IOException ioe) {
                //do nothing
            }
        }
        return super.fastCheckParameters();
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
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
    
    private void initClasses() {
        classes = new HashMap<FileObject,ElementHandle>();
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
            }
            
        }
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        initClasses();
        
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        MoveTransformer t;
        TransformTask task = new TransformTask(t=new MoveTransformer(this), null);
        createAndAddElements(a, task, elements, refactoring);
        fireProgressListenerStop();
        return t.getProblem();
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
                    if (!fo2.isFolder() || (fo2.isFolder() && recursively)) 
                        col.add(fo2);
                }
                setup(col, postfix +(addDot?".":"") +fo.getName(), recursively); // NOI18N
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

    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
}    
