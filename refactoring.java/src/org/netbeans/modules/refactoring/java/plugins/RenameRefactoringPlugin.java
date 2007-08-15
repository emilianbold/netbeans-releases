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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Jan Becicka
 * @author Martin Matula
 * @author Pavel Flaska
 * @author Daniel Prusa
 */
public class RenameRefactoringPlugin extends JavaRefactoringPlugin {
    
    private TreePathHandle treePathHandle = null;
    private Collection<ExecutableElement> overriddenByMethods = null; // methods that override the method to be renamed
    private Collection<ExecutableElement> overridesMethods = null; // methods that are overridden by the method to be renamed
    private boolean doCheckName = true;
    
    private RenameRefactoring refactoring;
    
    /** Creates a new instance of RenameRefactoring */
    public RenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        TreePathHandle tph = rename.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph!=null) {
            treePathHandle = tph;
        } else {
            JavaSource source = JavaSource.forFileObject(rename.getRefactoringSource().lookup(FileObject.class));
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }
                    
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(JavaSource.Phase.RESOLVED);
                        CompilationUnitTree cut = co.getCompilationUnit();
                        for (Tree t: cut.getTypeDecls()) {
                            Element e = co.getTrees().getElement(TreePath.getPath(cut, t));
                            if (e.getSimpleName().toString().equals(co.getFileObject().getName())) {
                                treePathHandle = TreePathHandle.create(TreePath.getPath(cut, t), co);
                                refactoring.getContext().add(co);
                                break;
                            }
                        }
                    }
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected JavaSource getJavaSource(Phase p) {
        if (treePathHandle == null) {
            return null;
        }
        switch (p) {
            case PRECHECK:
            case FASTCHECKPARAMETERS:
                return JavaSource.forFileObject(treePathHandle.getFileObject());
            case CHECKPARAMETERS:    
                if (treePathHandle==null) {
                    return null;
                }
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                JavaSource source = JavaSource.create(cpInfo, treePathHandle.getFileObject());
                return source;

        }
        throw new IllegalStateException();
    }
    
    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        Problem preCheckProblem = null;
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        info.toPhase(JavaSource.Phase.RESOLVED);
        Element el = treePathHandle.resolveElement(info);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        FileObject file = SourceUtils.getFile(el, info.getClasspathInfo());
        if (file!=null && FileUtil.getArchiveFile(file)!= null) { //NOI18N
            preCheckProblem = createProblem(preCheckProblem, true, getCannotRename(file));
            return preCheckProblem;
        }
        
        if (file==null || !RetoucheUtils.isElementInOpenProject(file)) {
            preCheckProblem = new Problem(true, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_ProjectNotOpened"));
            return preCheckProblem;
        }
        
        switch(el.getKind()) {
        case METHOD:
            fireProgressListenerStep();
            fireProgressListenerStep();
            overriddenByMethods = RetoucheUtils.getOverridingMethods((ExecutableElement)el, info);
            fireProgressListenerStep();
            if (el.getModifiers().contains(Modifier.NATIVE)) {
                preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", el));
            }
            if (!overriddenByMethods.isEmpty()) {
                String msg = new MessageFormat(getString("ERR_IsOverridden")).format(
                        new Object[] {SourceUtils.getEnclosingTypeElement(el).getSimpleName().toString()});
                preCheckProblem = createProblem(preCheckProblem, false, msg);
            }
            for (ExecutableElement e : overriddenByMethods) {
                if (e.getModifiers().contains(Modifier.NATIVE)) {
                    preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", e));
                }
            }
            overridesMethods = RetoucheUtils.getOverridenMethods((ExecutableElement)el, info);
            fireProgressListenerStep();
            if (!overridesMethods.isEmpty()) {
                boolean fatal = false;
                for (Iterator iter = overridesMethods.iterator();iter.hasNext();) {
                    ExecutableElement method = (ExecutableElement) iter.next();
                    if (method.getModifiers().contains(Modifier.NATIVE)) {
                        preCheckProblem = createProblem(preCheckProblem, false, NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_RenameNative", method));
                    }
                    if (RetoucheUtils.isFromLibrary(method, info.getClasspathInfo())) {
                        fatal = true;
                        break;
                    }
                }
                String msg = fatal?getString("ERR_Overrides_Fatal"):getString("ERR_Overrides");
                preCheckProblem = createProblem(preCheckProblem, fatal, msg);
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
                        new Object[] {SourceUtils.getEnclosingTypeElement(hiddenField)}
                );
                preCheckProblem = createProblem(preCheckProblem, false, msg);
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
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    private static final String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_CannotRenameFile")).format(new Object[] {r.getNameExt()});
    }
    
    @Override
    protected Problem fastCheckParameters(CompilationController info) throws IOException {
        Problem fastCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        TreePath treePath = treePathHandle.resolve(info);
        Element element = treePathHandle.resolveElement(info);
        ElementKind kind = element.getKind();
        
        String newName = refactoring.getNewName();
        String oldName = element.getSimpleName().toString();
        
        if (oldName.equals(newName)) {
            boolean nameNotChanged = true;
            if (kind.isClass()) {
                if (!((TypeElement) element).getNestingKind().isNested()) {
                    nameNotChanged = info.getFileObject().getName().equals(element);
                }
            }
            if (nameNotChanged) {
                fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged"));
                return fastCheckProblem;
            }
            
        }
        
        if (!Utilities.isJavaIdentifier(newName)) {
            String s = kind == ElementKind.PACKAGE? getString("ERR_InvalidPackage"):getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        
        if (kind.isClass() && !((TypeElement) element).getNestingKind().isNested()) {
            if (doCheckName) {
                String oldfqn = RetoucheUtils.getQualifiedName(treePathHandle);
                String newFqn = oldfqn.substring(0, oldfqn.lastIndexOf(oldName));
                
                String pkgname = oldfqn;
                int i = pkgname.indexOf('.');
                if (i>=0)
                    pkgname = pkgname.substring(0,i);
                else
                    pkgname = "";
                
                String fqn = "".equals(pkgname) ? newName : pkgname + '.' + newName;
                FileObject fo = treePathHandle.getFileObject();
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (RetoucheUtils.typeExist(treePathHandle, newFqn)) {
                    String msg = new MessageFormat(getString("ERR_ClassClash")).format(
                            new Object[] {newName, pkgname}
                    );
                    fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                    return fastCheckProblem;
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
                    fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                    break;
                }
            } // for
        } else if (kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER) {
            String msg = variableClashes(newName,treePath, info);
            if (msg != null) {
                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                return fastCheckProblem;
            }
        } else {
            String msg = clashes(element, newName, info);
            if (msg != null) {
                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
                return fastCheckProblem;
            }
        }
        return fastCheckProblem;
    }
    
    @Override
    protected Problem checkParameters(CompilationController info) throws IOException {
        
        Problem checkProblem = null;
        int steps = 0;
        if (overriddenByMethods != null)
            steps += overriddenByMethods.size();
        if (overridesMethods != null)
            steps += overridesMethods.size();
        
        fireProgressListenerStart(refactoring.PARAMETERS_CHECK, 8 + 3*steps);
        
        info.toPhase(JavaSource.Phase.RESOLVED);
        Element element = treePathHandle.resolveElement(info);
        
        fireProgressListenerStep();
        fireProgressListenerStep();
        String msg;
        if (element.getKind() == ElementKind.METHOD) {
            checkProblem = checkMethodForOverriding((ExecutableElement)element, refactoring.getNewName(), checkProblem, info);
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
                        new Object[] {SourceUtils.getEnclosingTypeElement(hiddenField).toString()}
                );
                checkProblem = createProblem(checkProblem, false, msg);
            }
        }
        fireProgressListenerStop();
        return checkProblem;
    }
    
        private Problem checkMethodForOverriding(ExecutableElement m, String newName, Problem problem, CompilationInfo info) {
            ElementUtilities ut = info.getElementUtilities();
            //problem = willBeOverridden(m, newName, argTypes, problem);
            fireProgressListenerStep();
            problem = willOverride(m, newName, problem, info);
            fireProgressListenerStep();
            return problem;
        }
    
    private Set<ElementHandle<ExecutableElement>> allMethods;
    
    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, treePathHandle.getFileObject());
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    Element el = treePathHandle.resolveElement(info);
                    ElementKind kind = el.getKind();
                    ElementHandle<TypeElement> enclosingType;
                    if (el instanceof TypeElement) {
                         enclosingType = ElementHandle.create((TypeElement)el);
                    } else {
                         enclosingType = ElementHandle.create(SourceUtils.getEnclosingTypeElement(el));
                    }
                    set.add(SourceUtils.getFile(el, info.getClasspathInfo()));
                    if (kind.isField()) {
                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    } else if (el instanceof TypeElement) {
                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    } else if (kind == ElementKind.METHOD) {
                        //add all references of overriding methods
                        allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                        allMethods.add(ElementHandle.create((ExecutableElement)el));
                        for (ExecutableElement e:RetoucheUtils.getOverridingMethods((ExecutableElement)el, info)) {
                            addMethods(e, set, info, idx);
                        }
                        //add all references of overriden methods
                        for (ExecutableElement ov: RetoucheUtils.getOverridenMethods((ExecutableElement)el, info)) {
                            addMethods(ov, set, info, idx);
                            for (ExecutableElement e:RetoucheUtils.getOverridingMethods( ov,info)) {
                                addMethods(e, set, info, idx);
                            }
                        }
                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
                    }
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    
    private void addMethods(ExecutableElement e, Set set, CompilationInfo info, ClassIndex idx) {
        set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
        ElementHandle<TypeElement> encl = ElementHandle.create(SourceUtils.getEnclosingTypeElement(e));
        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        allMethods.add(ElementHandle.create(e));
    }
    
    
    public Problem prepare(RefactoringElementsBag elements) {
        if (treePathHandle == null)
            return null;
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        TransformTask transform = new TransformTask(new RenameTransformer(refactoring.getNewName(), allMethods), treePathHandle);
        createAndAddElements(a, transform, elements, refactoring);
        fireProgressListenerStop();
        return null;
    }

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
    private static int getAccessLevel(Element e) {
        Set<Modifier> access = e.getModifiers();
        if (access.contains(Modifier.PUBLIC)) {
            return 3;
        } else if (access.contains(Modifier.PROTECTED)) {
            return 2;
        } else if (!access.contains(Modifier.PRIVATE)) {
            return 1;
        } else {
            return 0;
        }
    }
    
    private Problem willOverride(ExecutableElement method, String name, Problem problem, CompilationInfo info) {
        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
        TypeElement jc = (TypeElement) method.getEnclosingElement();
        LinkedList supertypes = new LinkedList();
        
        ElementUtilities ut = info.getElementUtilities();
        //TODO:
        //ExecutableElement m = ut.getOverriddenMethod(method, name);
        ExecutableElement m = null;
        if (m!=null) {
            if (m.getModifiers().contains(Modifier.FINAL)) {
                String msg = new MessageFormat(getString("ERR_WillOverride_final")).format(
                        new Object[] {
                    method.getSimpleName(),
                    method.getEnclosingElement().getSimpleName(),
                    m.getSimpleName(),
                    m.getEnclosingElement().getSimpleName()
                }
                );
                return createProblem(problem, true, msg);
            } else if (getAccessLevel(m) > getAccessLevel(method)) {
                String msg = new MessageFormat(getString("ERR_WillOverride_access")).format(
                        new Object[] {
                    method.getSimpleName(),
                    method.getEnclosingElement().getSimpleName(),
                    m.getSimpleName(),
                    m.getEnclosingElement().getSimpleName()
                }
                );
                return createProblem(problem, true, msg);
            } else if (m.getModifiers().contains(Modifier.STATIC)!= method.getModifiers().contains(Modifier.STATIC)) {
                String msg = new MessageFormat(getString("ERR_WillOverride_static")).format(
                        new Object[] {
                    isStatic ? getString("LBL_static") : getString("LBL_instance"),
                    method.getSimpleName(),
                    method.getEnclosingElement().getSimpleName(),
                    m.getModifiers().contains(Modifier.STATIC) ? getString("LBL_static") : getString("LBL_instance"),
                    m.getSimpleName(),
                    m.getEnclosingElement().getSimpleName()
                }
                );
                return createProblem(problem, true, msg);
            } else {
                String msg = new MessageFormat(getString("ERR_WillOverride")).format(
                        new Object[] {
                    method.getSimpleName(),
                    method.getEnclosingElement().getSimpleName(),
                    m.getSimpleName(),
                    m.getEnclosingElement().getSimpleName()
                }
                );
                return createProblem(problem, false, msg);
            }
        } else {
            return problem;
        }
    }
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
        Elements elements = info.getElements();
        ElementUtilities utils = info.getElementUtilities();
        for (Element el:elements.getAllMembers(jc)) {
//TODO:
//            if (utils.willHide(el, field, name)) {
//                return el;
//            }
            if (el.getKind().isField()) {
                if (el.getSimpleName().toString().equals(name)) {
                    if (!el.getEnclosingElement().equals(field.getEnclosingElement())) {
                        return el;
                    }
                }
            }
        }
        return null;
    }
    
    private String variableClashes(String newName, TreePath tp, CompilationInfo info) {
        LocalVarScanner lookup = new LocalVarScanner(info, newName);
        TreePath scopeBlok = tp;
        EnumSet set = EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.FOR_LOOP, Tree.Kind.METHOD);
        while (!set.contains(scopeBlok.getLeaf().getKind())) {
            scopeBlok = scopeBlok.getParentPath();
        }
        Element var = info.getTrees().getElement(tp);
        lookup.scan(scopeBlok, var);

        if (lookup.hasRefernces())
            return new MessageFormat(getString("ERR_LocVariableClash")).format(
                new Object[] {newName}
            );
        
        TreePath temp = tp;
        while (temp.getLeaf().getKind() != Tree.Kind.METHOD) {
            Scope scope = info.getTrees().getScope(temp);
            for (Element el:scope.getLocalElements()) {
                if (el.getSimpleName().toString().equals(newName)) {
                    return new MessageFormat(getString("ERR_LocVariableClash")).format(
                            new Object[] {newName}
                    );
                }
            }
            temp = temp.getParentPath();
        }
        return null;
    }
    
    private String clashes(Element feature, String newName, CompilationInfo info) {
        ElementUtilities utils = info.getElementUtilities();
        Element dc = feature.getEnclosingElement();
        ElementKind kind = feature.getKind();
        if (kind.isClass() || kind.isInterface()) {
            for (Element current:ElementFilter.typesIn(dc.getEnclosedElements())) {
                if (current.getSimpleName().toString().equals(newName)) {
                    return new MessageFormat(getString("ERR_InnerClassClash")).format(
                            new Object[] {newName, dc.getSimpleName()}
                    );
                }
            }
        } else if (kind==ElementKind.METHOD) {
            if (utils.alreadyDefinedIn((CharSequence) newName, (ExecutableType) feature.asType(), (TypeElement) dc)) {
                return new MessageFormat(getString("ERR_MethodClash")).format(
                        new Object[] {newName, dc.getSimpleName()}
                );
            }
        } else if (kind.isField()) {
            for (Element current:ElementFilter.fieldsIn(dc.getEnclosedElements())) {
                if (current.getSimpleName().toString().equals(newName)) {
                    return new MessageFormat(getString("ERR_FieldClash")).format(
                            new Object[] {newName, dc.getSimpleName()}
                    );
                }
            }
        }
        return null;
    }
    
    
    private static final String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringPlugin.class, key);
    }
}
