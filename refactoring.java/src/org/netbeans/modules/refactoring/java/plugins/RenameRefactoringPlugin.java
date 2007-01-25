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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.java.plugins.RetoucheCommit;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.java.DiffElement;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.classpath.RefactoringClassPathImplementation;
import org.netbeans.modules.refactoring.java.plugins.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka, Martin Matula, Pavel Flaska, Daniel Prusa
 */
public class RenameRefactoringPlugin extends JavaRefactoringPlugin {
    
    private TreePathHandle treePathHandle = null;
    private Collection overriddenByMethods = null; // methods that override the method to be renamed
    private Collection overridesMethods = null; // methods that are overridden by the method to be renamed
    private boolean doCheckName = true;
    private FileObject originalFolder = null;
    private Set varNames;
    
    private RenameRefactoring refactoring;
    
    /** Creates a new instance of RenameRefactoring */
    public RenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        Object o = rename.getRefactoredObject();
        if (o instanceof TreePathHandle) {
            treePathHandle = (TreePathHandle) o;
        } else {
            JavaSource source = JavaSource.forFileObject((FileObject) o);
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }
                    
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(JavaSource.Phase.RESOLVED);
                        CompilationUnitTree cut = co.getCompilationUnit();
                        treePathHandle = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                        refactoring.getContext().add(co);
                    }
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public Problem preCheck() {
        CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
        Element el = treePathHandle.resolveElement(info);
        
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        try {
            Problem result = isElementAvail(treePathHandle, info);
            if (result != null) {
                return result;
            }
            FileObject file = SourceUtils.getFile(el, info.getClasspathInfo());
            if (FileUtil.getArchiveFile(file)!= null) { //NOI18N
                return createProblem(result, true, getCannotRename(file));
            }
            
            if (!RetoucheUtils.isElementInOpenProject(file)) {
                return new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ProjectNotOpened"));
            }
            
            switch(el.getKind()) {
                case METHOD:
                    fireProgressListenerStep();
                    fireProgressListenerStep();
                    overriddenByMethods = RetoucheUtils.getOverridingMethods((ExecutableElement)el, info);
                    fireProgressListenerStep();
                    if (!overriddenByMethods.isEmpty()) {
                        String msg = new MessageFormat(getString("ERR_IsOverridden")).format(
                                new Object[] {SourceUtils.getEnclosingTypeElement(el).getSimpleName().toString()});
                        result = createProblem(result, false, msg);
                    }
                    overridesMethods = RetoucheUtils.getOverridingMethods((ExecutableElement) treePathHandle.resolveElement(info), info);
                    fireProgressListenerStep();
                    if (!overridesMethods.isEmpty()) {
                        boolean fatal = false;
                        for (Iterator iter = overridesMethods.iterator();iter.hasNext();) {
                            ExecutableElement method = (ExecutableElement) iter.next();
                            if (RetoucheUtils.isFromLibrary(method, info.getClasspathInfo())) {
                                fatal = true;
                                break;
                            }
                        }
                        String msg = fatal?getString("ERR_Overrides_Fatal"):getString("ERR_Overrides");
                        result = createProblem(result, fatal, msg);
                    }
                    break;
                case FIELD:
                case ENUM_CONSTANT:
                    fireProgressListenerStep();
                    fireProgressListenerStep();
                    Element hiddenField = hides(el, el.getSimpleName().toString(), info);
                    fireProgressListenerStep();
                    fireProgressListenerStep();
                    if (hiddenField != null) {
                        String msg = new MessageFormat(getString("ERR_Hides")).format(
                                new Object[] {SourceUtils.getEnclosingTypeElement(el)}
                        );
                        result = createProblem(result, false, msg);
                    }
                    break;
                case PACKAGE:
                    //TODO: any prechecks?
                    break;
                case LOCAL_VARIABLE:
                    //TODO: any prechecks for formal parametr or local variable?
                    break;
                case CLASS:
                case INTERFACE:
                case ANNOTATION_TYPE:
                case ENUM:
                    //TODO: any prechecks for JavaClass?
                    break;
                default:
                    //                if (!((jmiObject instanceof Resource) && ((Resource)jmiObject).getClassifiers().isEmpty()))
                    //                    result = createProblem(result, true, NbBundle.getMessage(RenameRefactoring.class, "ERR_RenameWrongType"));
            }
            
            return result;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    private static final String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_CannotRenameFile")).format(new Object[] {r.getNameExt()});
    }
    
    public Problem fastCheckParameters() {
        CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
        Element el = treePathHandle.resolveElement(info);
        ElementKind kind = el.getKind();
        
        String newName = refactoring.getNewName();
        Problem result = null;
        String oldName = el.getSimpleName().toString();
        
        if (oldName.equals(newName)) {
            boolean nameNotChanged = true;
            if (el.getKind().isClass()) {
                //                Object comp = jmiObject.refImmediateComposite();
                //                if (comp instanceof Resource && isResourceClass((Resource)comp, jmiObject)) {
                //                    String dobjName = JavaMetamodel.getManager().getDataObject((Resource)comp).getName();
                //                    nameNotChanged = dobjName.equals(newName);
                //                }
            }
            if (nameNotChanged)
                return createProblem(result, true, getString("ERR_NameNotChanged"));
        }
        
        if (!Utilities.isJavaIdentifier(newName)) {
            String s = kind == ElementKind.PACKAGE? getString("ERR_InvalidPackage"):getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            result = createProblem(result, true, msg);
            return result;
        }
        
        if (kind.isClass()) {
            if (doCheckName) {
                TypeElement type = (TypeElement) el;
                String oldfqn = type.getQualifiedName().toString();
                String newFqn = oldfqn.substring(0, oldfqn.lastIndexOf(type.getSimpleName().toString()));
                
                String pkgname = type.getQualifiedName().toString();
                int i = pkgname.indexOf('.');
                if (i>=0)
                    pkgname = pkgname.substring(0,i);
                else
                    pkgname = "";
                
                String fqn = "".equals(pkgname) ? newName : pkgname + '.' + newName;
                FileObject fo = treePathHandle.getFileObject();
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (info.getElements().getTypeElement(newFqn)!=null) {
                    String msg = new MessageFormat(getString("ERR_ClassClash")).format(
                            new Object[] {newName, pkgname}
                    );
                    return createProblem(result, true, msg);
                }
            }
            FileObject primFile = treePathHandle.getFileObject();
            FileObject folder = primFile.getParent();
            FileObject[] children = folder.getChildren();
            for (int x = 0; x < children.length; x++) {
                if (children[x] != primFile && !children[x].isVirtual() && children[x].getName().equals(newName) && "java".equals(children[x].getExt())) { //NOI18N
                    String msg = new MessageFormat(getString("ERR_ClassClash")).format(
                            new Object[] {newName, folder.getPath()}
                    );
                    result = createProblem(result, true, msg);
                    break;
                }
            } // for
            
            if (kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER) {
                //            String msg = variableClashes(newName,JavaModelUtil.getDeclaringFeature((Variable) jmiObject));
                //            if (msg != null) {
                //                result = createProblem(result, true, msg);
                //                return result;
                //            }
            }
            if (kind.isField() || kind == kind.METHOD) {
                //            String msg = clashes((Feature) jmiObject, newName);
                //            if (msg != null) {
                //                result = createProblem(result, true, msg);
                //                return result;
                //            }
            }
        }
        return result;
    }
    
    public Problem checkParameters() {
        int steps = 0;
        if (overriddenByMethods != null)
            steps += overriddenByMethods.size();
        if (overridesMethods != null)
            steps += overridesMethods.size();
        
        CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
        Element element = treePathHandle.resolveElement(info);
        
        Problem result = null;
        fireProgressListenerStart(refactoring.PARAMETERS_CHECK, 8 + 3*steps);
        
        try {
            fireProgressListenerStep();
            fireProgressListenerStep();
            String msg;
            if (element.getKind() == ElementKind.METHOD) {
                //                result = checkMethodForOverriding(m, newName, result);
                //                for (Iterator iter = overridesMethods.iterator(); iter.hasNext();) {
                //                    m = (Method) iter.next();
                //                    msg = clashes(m, newName);
                //                    if (msg != null) {
                //                        result = createProblem(result, true, msg);
                //                    }
                //                    result = checkMethodForOverriding(m, newName, result);
                //                }
                //                for (Iterator iter = overriddenByMethods.iterator(); iter.hasNext();) {
                //                    m = (Method) iter.next();
                //                    msg = clashes(m, newName);
                //                    if (msg != null) {
                //                        result = createProblem(result, true, msg);
                //                    }
                //                    result = checkMethodForOverriding(m, newName, result);
                //                }
                fireProgressListenerStep();
                fireProgressListenerStep();
            } else if (element.getKind().isField()) {
                fireProgressListenerStep();
                fireProgressListenerStep();
                Element hiddenField = hides(element, refactoring.getNewName(), info);
                fireProgressListenerStep();
                fireProgressListenerStep();
                fireProgressListenerStep();
                if (hiddenField != null) {
                    msg = new MessageFormat(getString("ERR_WillHide")).format(
                            new Object[] {SourceUtils.getEnclosingTypeElement(element).toString()}
                    );
                    result = createProblem(result, false, msg);
                }
            }
            
            return result;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    //    private Problem checkMethodForOverriding(ExecutableElement m, String newName, Problem problem) {
    //        List argTypes = getParamTypes(m);
    //        fireProgressListenerStep();
    //        problem = willBeOverridden(m, newName, argTypes, problem);
    //        fireProgressListenerStep();
    //        problem = willOverride(m, newName, argTypes, problem);
    //        fireProgressListenerStep();
    //        return problem;
    //    }
    
    private Set<FileObject> getRelevantFiles(CompilationInfo info, Element el) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> set = new HashSet<FileObject>();
        set.add(SourceUtils.getFile(el, cpInfo));
        if (el.getKind().isField()) {
            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        } else if (el instanceof TypeElement) {
            set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        } else if (el.getKind() == ElementKind.METHOD) {
            //XXX: IMPLEMENTORS_RECURSIVE was removed
            Set<ElementHandle<TypeElement>> s = idx.getElements(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
            for (ElementHandle<TypeElement> eh:s) {
                TypeElement te = eh.resolve(info);
                if (te==null) {
                    continue;
                }
                for (Element e:te.getEnclosedElements()) {
                    if (e instanceof ExecutableElement) {
                        if (info.getElements().overrides((ExecutableElement)e, (ExecutableElement)el, te)) {
                            set.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        }
                    }
                }
            }
            set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
        }
        return set;
    }
    
    private ClasspathInfo getClasspathInfo(CompilationInfo info) {
        ClassPath boot = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
        FileObject fo = treePathHandle.getFileObject();
        ClassPath rcp = RefactoringClassPathImplementation.getCustom(Collections.singleton(fo));
        ClasspathInfo cpi = ClasspathInfo.create(boot, rcp, rcp);
        return cpi;
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        final CompilationInfo mainInfo = refactoring.getContext().lookup(CompilationInfo.class);
        final Element element = treePathHandle.resolveElement(mainInfo);
        
        if (cpInfo==null) {
            cpInfo = getClasspathInfo(mainInfo);
            refactoring.getContext().add(cpInfo);
        }
        
        Set<FileObject> a = getRelevantFiles(mainInfo, element);
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            final Collection<ModificationResult> results = processFiles(a, new FindTask(elements, element));
            elements.registerTransaction(new RetoucheCommit(results));
            for (ModificationResult result:results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (Difference dif: result.getDifferences(jfo)) {
                        String old = dif.getOldText();
                        if (old!=null) {
                            //TODO: workaround
                            //generator issue?
                            elements.add(refactoring,DiffElement.create(dif, jfo, result));
                        }
                    }
                }
            }
        }
        fireProgressListenerStop();
        return null;
        //        varNames = null;
        //        CommentRenameFinder docFinder=null;
        //
        //        if (newName == null) {
        //            return new Problem(true, getString("ERR_NameNotSet"));
        //        }
        //
        //        int steps = 9;
        //        if (overriddenByMethods != null)
        //            steps += overriddenByMethods.size();
        //        if (overridesMethods != null)
        //            steps += overridesMethods.size();
        //
        //        JavaMetamodel.getManager().getProgressSupport().addProgressListener(this);
        //
        //
        //        //fireProgressListenerStart(rename.PREPARE, steps);
        //
        //        try {
        //            if (refactoring.isSearchInComments()) {
        //                docFinder=new CommentRenameFinder((Element)jmiObject,newName);
        //                elements.addAll(refactoring, docFinder.searchCommentsInResource(((Element)jmiObject).getResource()));
        //            }
        //            if (jmiObject instanceof JavaPackage) {
        //                //fireProgressListenerStep();
        //
        //                referencesIterator = ((NamedElement) jmiObject).getReferences().iterator();
        //                while (referencesIterator.hasNext()) {
        //                    Element element = (Element) referencesIterator.next();
        //
        //                    if (cancelRequest) {
        //                        return null;
        //                    }
        //                    if (refactoring.isSearchInComments()) {
        //                        elements.addAll(refactoring, docFinder.searchCommentsInResource(element.getResource()));
        //                    }
        //                    elements.add(refactoring, new RenamePackageElement(jmiObject, element, newName));
        //                }
        //                DataFolder folder = originalFolder!=null ? DataFolder.findFolder(originalFolder) : getFolder(((JavaPackage)jmiObject).getName());
        //                if (folder != null) {
        //                    elements.add(refactoring, new RenameDataFolder(folder, newName));
        //                }
        //                return null;
        //            } else {
        //                //fireProgressListenerStep();
        //                addElementsForJmiObject(elements, jmiObject, docFinder);
        //
        //
        //                if (overridesMethods != null) {
        //                    for (Iterator iter = overridesMethods.iterator(); iter.hasNext();) {
        //                        if (cancelRequest) {
        //                            return null;
        //                        }
        //                        //fireProgressListenerStep();
        //                        Method m = (Method) iter.next();
        //                        if (m.getResource().getName().endsWith(".class")) {
        //                            return resourceNotAvailable(m);
        //                        }
        //                        elements.add(refactoring, new RenameDOElement(m));
        //                        //addElementsForJmiObject(elements, (RefObject) iter.next());
        //                    }
        //                }
        //                if (overriddenByMethods != null) {
        //                    for (Iterator iter = overriddenByMethods.iterator(); iter.hasNext();) {
        //                        if (cancelRequest) {
        //                            return null;
        //                        }
        //                        Method m = (Method) iter.next();
        //                        if (m.getResource().getName().endsWith(".class")) {
        //                            return resourceNotAvailable(m);
        //                        }
        //                        //fireProgressListenerStep();
        //                        elements.add(refactoring, new RenameDOElement(m));
        //                        //addElementsForJmiObject(elements, (RefObject) iter.next());
        //                    }
        //                }
        //                return null;
        //            }
        //        } finally {
        //            referencesIterator = null;
        //            JavaMetamodel.getManager().getProgressSupport().removeProgressListener(this);
        //        }
    }
    
    //    private static Problem resourceNotAvailable(Method m) {
    //        String resourceName = Utilities.replaceString(m.getResource().getName(), ".class", ".java"); //NOI18N
    //        return new Problem(true, new MessageFormat(getString("ERR_ResourceUnavailable")).format (new Object[] {m.getName(), resourceName}));
    //    }
    //
    //    private void addElementsForJmiObject(RefactoringElementsBag elements, RefObject refObject, CommentRenameFinder docFinder) {
    //        elements.add(refactoring, new RenameDOElement(refObject));
    //        if (refObject instanceof Method) {
    //            referencesIterator = ((MethodImpl) refObject).findDependencies(true, true, false).iterator();
    //        } else {
    //            referencesIterator = ((NamedElement) refObject).getReferences().iterator();
    //        }
    //        while (referencesIterator.hasNext()) {
    //            Element element = (Element) referencesIterator.next();
    //
    //            if (cancelRequest) {
    //                return;
    //            }
    //            if (refactoring.isSearchInComments()) {
    //                elements.addAll(refactoring, docFinder.searchCommentsInResource(element.getResource()));
    //            }
    //            String name = newName;
    //            if (jmiObject instanceof Field) {
    //                Feature f = JavaModelUtil.getDeclaringFeature(element);
    //                if (((VariableAccess)element).getParentClass()==null && variableClashes(newName,f)!=null) {
    //                    ClassDefinition decl = ((Field)jmiObject).getDeclaringClass() ;
    //                    if (f.getDeclaringClass().equals(decl)) {
    //                        name = "this." + newName; //NOI18N
    //                    } else {
    //                        if (decl instanceof NamedElement)
    //                            name = ((NamedElement) decl).getName() + ".this." + newName; //NOI18N
    //                    }
    //                }
    //            }
    //            elements.add(refactoring, new RenameUsageElement(refObject, element, name));
    //        }
    //    }
    //
    //    private DataFolder getFolder(String name) {
    //        FileObject fo = RefactoringClassPathImplementation.getDefault().findResource(name.replace('.','/'));
    //        if (fo == null)
    //            return null;
    //        return DataFolder.findFolder(fo);
    //    }
    //
    //    private Collection overriddenBy(Method method, String name, List argTypes) {
    //        if (!CheckUtils.isVirtual(method))
    //            return Collections.EMPTY_LIST;
    //
    //        return ((MethodImpl) method).findDependencies(false, false, true);
    //    }
    //
    //    private Collection overrides(Method method, String name, List argTypes, ClassDefinition[] cls_par) {
    //        if (!CheckUtils.isVirtual(method))
    //            return Collections.EMPTY_LIST;
    //
    //        ClassDefinition javaClass = method.getDeclaringClass ();
    //        LinkedList supertypes = new LinkedList ();
    //        Collection result = new HashSet();
    //        Method last = null;
    //
    //        supertypes.addAll (javaClass.getInterfaces());
    //        ClassDefinition jc = javaClass.getSuperClass ();
    //        if (jc != null)
    //            supertypes.add (jc);
    //        while (supertypes.size () > 0) {
    //            jc = (ClassDefinition) supertypes.removeFirst ();
    //            if (jc instanceof UnresolvedClass) {
    //                continue;
    //            }
    //            Method m = jc.getMethod (name, argTypes, false);
    //            if ((m != null) && CheckUtils.isVirtual(m)) {
    //                result.add(m);
    //                last = m;
    //            }
    //            supertypes.addAll (jc.getInterfaces ());
    //            jc = jc.getSuperClass ();
    //            if (jc != null) {
    //                supertypes.add (jc);
    //            }
    //        }
    //
    //        if (last != null) {
    //            ClassDefinition cd = last.getDeclaringClass();
    //            ClassDefinition implementor = findDifferentSubtype((JavaClass) cd, name, argTypes, javaClass);
    //            if (implementor != null) {
    //                cls_par[0] = cd;
    //                cls_par[1] = implementor;
    //            }
    //        }
    //        return result;
    //    }
    //
    //    private ClassDefinition findDifferentSubtype(JavaClass baseClass, String name, List argTypes, ClassDefinition subtype) {
    //        Set supertypes = new HashSet();
    //        LinkedList subtypes = new LinkedList();
    //        ClassDefinition jc = baseClass;
    //
    //        collectSupertypes(supertypes, subtype);
    //        addSubtypes(jc, subtypes);
    //        while (subtypes.size() > 0) {
    //            jc = (ClassDefinition) subtypes.removeFirst();
    //            if (jc instanceof UnresolvedClass) {
    //                continue;
    //            }
    //            if (supertypes.contains(jc)) {
    //                continue;
    //            } else if (jc.getMethod(name, argTypes, false) != null) {
    //                return jc;
    //            }
    //            addSubtypes(jc, subtypes);
    //        }
    //        return null;
    //    }
    //
    //    private void collectSupertypes(Set supertypes, ClassDefinition jc) {
    //        if (jc == null)
    //            return;
    //        supertypes.add(jc);
    //        collectSupertypes(supertypes, jc.getSuperClass());
    //        for (Iterator iter = jc.getInterfaces().iterator(); iter.hasNext();) {
    //            collectSupertypes(supertypes, (ClassDefinition)iter.next());
    //        }
    //    }
    //
    //    private boolean isStatic(Feature feature) {
    //        return (feature.getModifiers () & Modifier.STATIC) > 0;
    //    }
    //
    //    private int getAccessLevel(Feature f) {
    //        int mod = f.getModifiers();
    //        if ((mod & Modifier.PUBLIC) > 0)
    //            return 3;
    //        if ((mod & Modifier.PROTECTED) > 0)
    //            return 2;
    //        if ((mod & Modifier.PRIVATE) > 0)
    //            return 0;
    //        return 1;
    //    }
    //
    //    private Method isOverridden(Method method, String name, List argTypes) {
    //        if (!CheckUtils.isVirtual(method))
    //            return null;
    //
    //        ClassDefinition jc = method.getDeclaringClass();
    //        LinkedList subtypes = new LinkedList();
    //        addSubtypes(jc, subtypes);
    //        while (subtypes.size() > 0) {
    //            jc = (ClassDefinition) subtypes.removeFirst();
    //            if (jc instanceof UnresolvedClass) {
    //                continue;
    //            }
    //            Method m = jc.getMethod(name, argTypes, false);
    //            if ((m != null) && CheckUtils.isVirtual(m))
    //                return m;
    //            addSubtypes(jc, subtypes);
    //        }
    //        return null;
    //    }
    //
    //    private Problem willBeOverridden(Method method, String name, List argTypes, Problem problem) {
    //        int accessLevel = getAccessLevel(method);
    //        if (accessLevel == 0)
    //            return null;
    //
    //        boolean isStatic = isStatic(method);
    //        boolean isFinal = (method.getModifiers() & Modifier.FINAL) > 0;
    //        Method temp = null;
    //        ClassDefinition jc = method.getDeclaringClass();
    //        LinkedList subtypes = new LinkedList();
    //        addSubtypes(jc, subtypes);
    //        while (subtypes.size() > 0) {
    //            jc = (ClassDefinition) subtypes.removeFirst();
    //            if (jc instanceof UnresolvedClass) {
    //                continue;
    //            }
    //            Method m = jc.getMethod(name, argTypes, false);
    //            if (m != null) {
    //                if (temp == null)
    //                    temp = m;
    //                if (isFinal) {
    //                    String msg = new MessageFormat(getString("ERR_WillBeOverridden_final")).format(
    //                        new Object[] {
    //                            method.getName(),
    //                            getDefClassName(method.getDeclaringClass()),
    //                            m.getName(),
    //                            getDefClassName(m.getDeclaringClass())
    //                        }
    //                    );
    //                    return createProblem(problem, true, msg);
    //                }
    //                if (getAccessLevel(m) < accessLevel) {
    //                    String msg = new MessageFormat(getString("ERR_WillBeOverridden_access")).format(
    //                        new Object[] {
    //                            method.getName(),
    //                            getDefClassName(method.getDeclaringClass()),
    //                            m.getName(),
    //                            getDefClassName(m.getDeclaringClass())
    //                        }
    //                    );
    //                    return createProblem(problem, true, msg);
    //                }
    //                if (isStatic != isStatic(m)) {
    //                    String msg = new MessageFormat(getString("ERR_WillBeOverridden_static")).format(
    //                        new Object[] {
    //                            isStatic ? getString("LBL_static") : getString("LBL_instance"),
    //                            method.getName(),
    //                            getDefClassName(method.getDeclaringClass()),
    //                            isStatic(m) ? getString("LBL_static") : getString("LBL_instance"),
    //                            m.getName(),
    //                            getDefClassName(m.getDeclaringClass())
    //                        }
    //                    );
    //                    return createProblem(problem, true, msg);
    //                }
    //            } else {
    //                addSubtypes(jc, subtypes);
    //            }
    //        }
    //        if (temp != null) {
    //            String msg = new MessageFormat(getString("ERR_WillBeOverridden")).format(
    //                new Object[] {
    //                    method.getName(),
    //                    getDefClassName(method.getDeclaringClass()),
    //                    temp.getName(),
    //                    getDefClassName(temp.getDeclaringClass())
    //                }
    //            );
    //            return createProblem(problem, false, msg);
    //        } else {
    //            return problem;
    //        }
    //    }
    //
    //    private Problem willOverride(Method method, String name, List argTypes, Problem problem) {
    //        int accessLevel = getAccessLevel(method);
    //        boolean isStatic = isStatic(method);
    //        Method temp = null;
    //        ClassDefinition jc = method.getDeclaringClass ();
    //        LinkedList supertypes = new LinkedList ();
    //
    //        supertypes.addAll (jc.getInterfaces());
    //        jc = jc.getSuperClass ();
    //        if (jc != null)
    //            supertypes.add (jc);
    //        while (supertypes.size () > 0) {
    //            jc = (ClassDefinition) supertypes.removeFirst ();
    //            if (jc instanceof UnresolvedClass) {
    //                continue;
    //            }
    //            Method m = jc.getMethod (name, argTypes, false);
    //            if (m != null) {
    //                if (temp == null)
    //                    temp = m;
    //                if ((m.getModifiers() & Modifier.FINAL) > 0) {
    //                    String msg = new MessageFormat(getString("ERR_WillOverride_final")).format(
    //                        new Object[] {
    //                            method.getName(),
    //                            getDefClassName(method.getDeclaringClass()),
    //                            m.getName(),
    //                            getDefClassName(m.getDeclaringClass())
    //                        }
    //                    );
    //                    return createProblem(problem, true, msg);
    //                }
    //                if (getAccessLevel(m) > accessLevel) {
    //                    String msg = new MessageFormat(getString("ERR_WillOverride_access")).format(
    //                        new Object[] {
    //                            method.getName(),
    //                            getDefClassName(method.getDeclaringClass()),
    //                            m.getName(),
    //                            getDefClassName(m.getDeclaringClass())
    //                        }
    //                    );
    //                    return createProblem(problem, true, msg);
    //                }
    //                if (isStatic != isStatic(m)) {
    //                    String msg = new MessageFormat(getString("ERR_WillOverride_static")).format(
    //                        new Object[] {
    //                            isStatic ? getString("LBL_static") : getString("LBL_instance"),
    //                            method.getName(),
    //                            getDefClassName(method.getDeclaringClass()),
    //                            isStatic(m) ? getString("LBL_static") : getString("LBL_instance"),
    //                            m.getName(),
    //                            getDefClassName(m.getDeclaringClass())
    //                        }
    //                    );
    //                    return createProblem(problem, true, msg);
    //                }
    //            } else {
    //                supertypes.addAll (jc.getInterfaces ());
    //                jc = jc.getSuperClass ();
    //                if (jc != null)
    //                    supertypes.add (jc);
    //            }
    //        } // while
    //        if (temp != null) {
    //            String msg = new MessageFormat(getString("ERR_WillOverride")).format(
    //                new Object[] {
    //                    method.getName(),
    //                    getDefClassName(method.getDeclaringClass()),
    //                    temp.getName(),
    //                    getDefClassName(temp.getDeclaringClass())
    //                }
    //            );
    //            return createProblem(problem, false, msg);
    //        } else {
    //            return problem;
    //        }
    //    }
    //
    //    private Method overrides(Method method, String name, List argTypes, boolean findFinal) {
    //        Method res = null;
    //        if (!CheckUtils.isVirtual(method))
    //            return null;
    //
    //        ClassDefinition jc = method.getDeclaringClass ();
    //        LinkedList supertypes = new LinkedList ();
    //
    //        supertypes.addAll (jc.getInterfaces());
    //        jc = jc.getSuperClass ();
    //        if (jc != null)
    //            supertypes.add (jc);
    //        while (supertypes.size () > 0) {
    //            jc = (ClassDefinition) supertypes.removeFirst ();
    //            if (jc instanceof UnresolvedClass) {
    //                continue;
    //            }
    //            Method m = jc.getMethod (name, argTypes, false);
    //            if ((m != null) && CheckUtils.isVirtual(m)) {
    //                if ((m.getModifiers () & Modifier.FINAL) > 0) {
    //                    res = m;
    //                    break;
    //                } else if (res == null) {
    //                    res = m;
    //                    if (!findFinal)
    //                        break;
    //                }
    //            }
    //            supertypes.addAll (jc.getInterfaces ());
    //            jc = jc.getSuperClass ();
    //            if (jc != null)
    //                supertypes.add (jc);
    //        }
    //        return res;
    //    }
    //
    private Element hides(Element field, String name, CompilationInfo info) {
        TypeElement jc = SourceUtils.getEnclosingTypeElement(field);
        Types types = info.getTypes();
        Elements elements = info.getElements();
        jc =(TypeElement) types.asElement(jc.getSuperclass());
        while (jc != null) {
            for (Element el : info.getElements().getAllMembers(jc)) {
                if (elements.hides(el, field)) {
                    return el;
                }
            }
            jc =(TypeElement) types.asElement(jc.getSuperclass());
        }
        return null;
    }
    
    //    private String variableClashes(String newName, Feature scope) {
    //        if (varNames==null)
    //            varNames=CheckUtils.getAllVariableNames(scope);
    //        if (varNames.contains(newName)) {
    //                return new MessageFormat (getString ("ERR_LocVariableClash")).format (
    //                    new Object[] {newName}
    //                );
    //        }
    //        return null;
    //    }
    //
    //    private String clashes(Feature feature, String newName) {
    //        ClassDefinition dc = feature.getDeclaringClass ();
    //        if (feature instanceof TypeParameter) {
    //            // TODO: any check?
    //        } else if (feature instanceof JavaClass) {
    //            if (dc != null) {
    //                String result = checkInnersForClash(newName, dc);
    //                if (result != null)
    //                    return result;
    //            } else {
    //                Element composite = (Element) feature.refImmediateComposite();
    //                if (composite instanceof Resource) {
    //                    Resource resource = (Resource)composite;
    //                    DataObject dobj = JavaMetamodel.getManager().getDataObject(resource);
    //                    FileObject primFile = dobj.getPrimaryFile();
    //                    FileObject folder = primFile.getParent();
    //                    FileObject[] children = folder.getChildren();
    //                    for (int x = 0; x < children.length; x++) {
    //                        if (children[x] != primFile && !children[x].isVirtual() && children[x].getName().equals(newName) && "java".equals(children[x].getExt())) { //NOI18N
    //                            return new MessageFormat(getString("ERR_ClassClash")).format(
    //                                    new Object[] {newName, resource.getPackageName()}
    //                            );
    //                        }
    //                    }
    //                }
    //            }
    //        } else if (feature instanceof Method) {
    //            List params = getParamTypes ((Method) feature);
    //            if (dc.getMethod(newName, params, false) != null) {
    //                return new MessageFormat (getString ("ERR_MethodClash")).format (
    //                    new Object[] {newName, getDefClassName(dc)}
    //                );
    //            } // if
    //        } else if (feature instanceof Field) {
    //            if (dc.getField(newName, false) != null) {
    //                return new MessageFormat (getString ("ERR_FieldClash")).format (
    //                    new Object[] {newName, getDefClassName(dc)}
    //                );
    //            } // if
    //        }
    //        return null;
    //    }
    //
    //    private String checkInnersForClash(final String newName, final ClassDefinition dc) {
    //        Iterator iter = dc.getFeatures ().iterator ();
    //        while (iter.hasNext ()) {
    //            Object obj = iter.next();
    //            if (!(obj instanceof JavaClass))
    //                continue;
    //            JavaClass nestedClass = (JavaClass) obj;
    //            if (nestedClass.getSimpleName ().equals (newName)) {
    //                return new MessageFormat (getString ("ERR_InnerClassClash")).format (
    //                    new Object[] {newName, getDefClassName(dc)}
    //                );
    //            }
    //        } // while
    //        return null;
    //    }
    //
    //    String getElementName(NamedElement elem) {
    //        if (elem instanceof JavaClass) {
    //            return ((JavaClass) elem).getSimpleName();
    //        } else {
    //            return elem.getName();
    //        }
    //    }
    //
    //    String getDefClassName(ClassDefinition cd) {
    //        if (cd instanceof JavaClass) {
    //            return ((JavaClass) cd).getName();
    //        } else {
    //            return "";
    //        }
    //    }
    //
    //    private List getParamTypes(Method method) {
    //        List types = new LinkedList ();
    //        Iterator iter = method.getParameters ().iterator ();
    //        while (iter.hasNext ())
    //            types.add (getRealType(((Parameter) iter.next ()).getType ()));
    //        return types;
    //    }
    //
    //    private static Type getRealType(Type type) {
    //        if (type instanceof ParameterizedType) {
    //            return ((ParameterizedType) type).getDefinition();
    //        }
    //        return type;
    //    }
    //
    //    /**
    //     * Tests, if the renamed object should cause resource rename. Checks
    //     * if the object is java class. Then it checks for resource. If the
    //     * resource exists and resource name is the same as the name of class
    //     * is the same as the name of resource, it returns true. In all other
    //     * cases it returns false.
    //     *
    //     * @return  true, if the renamed object should cause resource rename
    //     */
    //    static boolean isResourceClass(Resource res, RefObject refObject) { //todo (#pf): try to find out better name for this method.
    //        if (res == null || !(refObject instanceof JavaClass))
    //            return false;
    //        int classCount = 0;
    //        for (Iterator iter = res.getClassifiers().iterator(); iter.hasNext(); ) {
    //            if (iter.next() instanceof JavaClass) {
    //                classCount++;
    //            }
    //        }
    //        if (classCount == 1) {
    //            return true;
    //        }
    //        String relativeResName = res.getName();
    //        String javaClassName = ((JavaClass) refObject).getSimpleName();
    //        int begin = relativeResName.lastIndexOf('/') + 1;
    //        if (begin < 0) begin = 0;
    //        int end = relativeResName.lastIndexOf('.');
    //        if (javaClassName.equals(relativeResName.substring(begin, end)))
    //            return true;
    //        else
    //            return false;
    //    }
    //
    //    private void addSubtypes(ClassDefinition cd, List list) {
    //        if (!(cd instanceof JavaClass)) {
    //            return;
    //        }
    //        JavaClass jc = (JavaClass) cd;
    //        Collection subtypes = null;
    //        if (jc.isInterface()) {
    //            subtypes = jc.getImplementors();
    //        } else {
    //            subtypes = jc.getSubClasses();
    //        }
    //        list.addAll(subtypes);
    //    }
    //
    private static final String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringPlugin.class, key);
    }
    
    //    public void start(org.netbeans.modules.javacore.internalapi.ProgressEvent event) {
    //        fireProgressListenerStart(event.getOperationType(), event.getCount());
    //    }
    //
    //    public void step(org.netbeans.modules.javacore.internalapi.ProgressEvent event) {
    //        fireProgressListenerStep();
    //    }
    //
    //    public void stop(org.netbeans.modules.javacore.internalapi.ProgressEvent event) {
    //        fireProgressListenerStop();
    //    }
    
    //    public void setClassPath() {
    //        if (NbAbstractRefactoring.isElementAvail((Element) jmiObject) == null && (jmiObject instanceof Method)) {
    //            Collection c = ((MethodImpl) jmiObject).getOverridenMethods();
    //            if (!c.isEmpty()) {
    //                setClassPath(c);
    //                return;
    //            }
    //        }
    //        setClassPath((Element) jmiObject);
    //    }
    
    // RenameDOElement ..........................................................
    //    private class RenameDOElement extends SimpleRefactoringElementImpl implements ExternalChange {
    //        private final String text;
    //        private PositionBounds bounds = null;
    //        private String oldName = null;
    //        private Resource res = null;
    //        private RefObject refObject;
    //
    //        public RenameDOElement(RefObject refObject) {
    //            this.refObject = refObject;
    //            Object o;
    //            if (refObject instanceof Resource) {
    //                o = refObject;
    //            } else {
    //                o = refObject.refImmediateComposite();
    //            }
    //            if (o instanceof Resource) res = (Resource) o;
    //            String bundleName = null;
    //            if (refObject instanceof Resource) {
    //                bundleName = "LBL_RenameClassDO";          //NOI18N
    //            } else if (refObject instanceof JavaEnum) {
    //                bundleName = "LBL_RenameEnum"; // NOI18N
    //            } else if (refObject instanceof AnnotationType) {
    //                bundleName = "LBL_RenameAnnotationType"; // NOI18N
    //            } else if (refObject instanceof TypeParameter) {
    //                bundleName = "LBL_RenameTypeParameter"; // NOI18N
    //            } else if (refObject instanceof JavaClass) {
    //                bundleName = isResourceClass() ? "LBL_RenameClassDO" : "LBL_RenameClass";          //NOI18N
    //            } else if (refObject instanceof Method) {
    //                bundleName = "LBL_RenameMethod";          //NOI18N
    //            } else if (refObject instanceof Field) {
    //                bundleName ="LBL_RenameField";          //NOI18N
    //            } else if (refObject instanceof Parameter) {
    //                bundleName = "LBL_RenameParameter"; // NOI18N
    //            } else if (refObject instanceof LocalVariable) {
    //                bundleName = "LBL_RenameLocVariable"; // NOI18N
    //            } else if (refObject instanceof Attribute) {
    //                bundleName = "LBL_RenameAttribute"; // NOI18N
    //            } else {
    //                assert false:"Invalid type "+refObject.getClass(); // NOI18N
    //            }
    //            text = MessageFormat.format(NbBundle.getMessage(RenameRefactoring.class, bundleName), new Object[] {newName});
    //        }
    //
    //        private boolean isResourceClass() {
    //            return RenameRefactoringPlugin.isResourceClass(res, refObject);
    //        }
    //
    //        public String getDisplayText() {
    //            return text;
    //        }
    //
    //        public Element getJavaElement() {
    //            return (Element) refObject;
    //        }
    //
    //        public PositionBounds getPosition() {
    //            if (bounds == null) {
    //                if (!(refObject instanceof Resource)) {
    //                    bounds = JavaMetamodel.getManager().getElementPosition((Element)refObject);
    //                }
    //            }
    //            return bounds;
    //        }
    //
    //        public String getText() {
    //            return getDisplayText();
    //        }
    //
    //        public void performChange() {
    //            if (refObject instanceof Resource) {
    //                 JavaMetamodel.getManager().registerExtChange(this);
    //            } else {
    //                NamedElement obj = (NamedElement) refObject;
    //                if (obj instanceof JavaClass) {
    //                    oldName = ((JavaClass) obj).getSimpleName();
    //                    if (isResourceClass()) {
    //                        JavaMetamodel.getManager().registerExtChange(this);
    //                    }
    //                    ((JavaClass) obj).setSimpleName(newName);
    //                } else {
    //                    oldName = obj.getName();
    //                    obj.setName(newName);
    //                }
    //            }
    //        }
    //
    //        private void doRename() {
    //            try {
    //                DataObject dobj = JavaMetamodel.getManager().getDataObject(res);
    //                oldName = dobj.getName();
    //                dobj.rename(newName);
    //            } catch (DataObjectNotFoundException e) {
    //                throw (RuntimeException) new RuntimeException().initCause(e);
    //            } catch (IOException e) {
    //                throw (RuntimeException) new RuntimeException().initCause(e);
    //            }
    //        }
    //
    //        public void performExternalChange () {
    //            doRename();
    //        }
    //
    //        public void undoExternalChange() {
    //           if (oldName == null) return;
    //            String temp = newName;
    //            newName = oldName;
    //            oldName = temp;
    //            doRename();
    //            newName = temp;
    //        }
    //
    //        public FileObject getParentFile() {
    //            return null;
    //        }
    //    } // RenameDOElement
    //
    //    // RenamePackageElement ..........................................................
    //    private class RenamePackageElement extends RenameUsageElement {
    //        private String oldName = null;
    //
    //        public RenamePackageElement(RefObject jmiObject, Element feature, String newName) {
    //                super(jmiObject, feature, newName);
    //        }
    //
    //        public void performChange() {
    //            MultipartId mpi = (MultipartId) feature;
    //            oldName = mpi.getName();
    //            mpi.setName(newName);
    //        }
    //
    //    } // RenamePackageElement
    //
    //    // RenameDataFolder ..........................................................
    //    private class RenameDataFolder extends SimpleRefactoringElementImpl implements RefactoringElementImplementation, ExternalChange {
    //        private final String text;
    //        private PositionBounds bounds;
    //        private String oldName, newName;
    //        private DataFolder folder;
    //
    //        public RenameDataFolder(DataFolder folder, String name) {
    //            newName = name;
    //            this.folder = folder;
    //            text = MessageFormat.format(NbBundle.getMessage(RenameRefactoring.class, "LBL_RenameFolder"), new Object[] {folder.getName(), newName});
    //        }
    //
    //        public String getDisplayText() {
    //            return text;
    //        }
    //
    //        public Element getJavaElement() {
    //            return (Element) jmiObject;
    //        }
    //
    //        public PositionBounds getPosition() {
    //            if (bounds == null) {
    //                bounds = JavaMetamodel.getManager().getElementPosition((Element)jmiObject);
    //            }
    //            return bounds;
    //        }
    //
    //
    //        public String getText() {
    //            return getDisplayText();
    //        }
    //
    //        public void performChange() {
    //             JavaMetamodel.getManager().registerExtChange(this);
    //        }
    //
    //        private void doRename() {
    //            oldName = folder.getName();
    //            try {
    //                folder.rename(newName);
    //            } catch (java.io.IOException e) {
    //                throw (RuntimeException) new RuntimeException().initCause(e);
    //            }
    //        }
    //
    //        public void performExternalChange () {
    //            doRename();
    //        }
    //
    //        public void undoExternalChange() {
    //            if (oldName == null) return;
    //            String temp = newName;
    //            newName = oldName;
    //            oldName = temp;
    //            doRename();
    //            newName = temp;
    //        }
    //
    //        public FileObject getParentFile() {
    //            return null;
    //        }
    //
    //    } // RenameDataFolder
    
    private class FindTask implements CancellableTask<WorkingCopy> {
        
        private RefactoringElementsBag elements;
        private Element element;
        
        public FindTask(RefactoringElementsBag elements, Element element) {
            super();
            this.elements = elements;
            this.element = element;
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
            Element el = treePathHandle.resolveElement(compiler);
            assert el != null;
            
            RenameTransformer findVisitor = new RenameTransformer(refactoring.getNewName(), compiler);
            findVisitor.scan(compiler.getCompilationUnit(), el);
            
            for (TreePath tree : findVisitor.getUsages()) {
                ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
            }
            fireProgressListenerStep();
        }
    }
}
