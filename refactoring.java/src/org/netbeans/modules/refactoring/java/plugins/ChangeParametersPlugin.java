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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers).
 *
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 */
public class ChangeParametersPlugin extends JavaRefactoringPlugin {
    
    private ChangeParametersRefactoring refactoring;
    private TreePathHandle treePathHandle;
    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public ChangeParametersPlugin(ChangeParametersRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }
    
    @Override
    public Problem checkParameters() {
        //TODO:
        return null;
    }

    @Override
    public Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        ParameterInfo paramTable[] = refactoring.getParameterInfo();
        Problem p=null;
        for (int i = 0; i< paramTable.length; i++) {
            int origIndex = paramTable[i].getOriginalIndex();
     
            if (origIndex==-1) {
            // check parameter name
            String s;
            s = paramTable[i].getName();
            if ((s == null || s.length() < 1))
                p = createProblem(p, true, newParMessage("ERR_parname")); // NOI18N
            else {
                if (!Utilities.isJavaIdentifier(s)) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier",s)); // NOI18N
                }
            }

            // check parameter type
            String t = paramTable[i].getType();
            if (t == null)
                p = createProblem(p, true, newParMessage("ERR_partype")); // NOI18N

            // check the default value
            s = paramTable[i].getDefaultValue();
            if ((s == null || s.length() < 1))
                p = createProblem(p, true, newParMessage("ERR_pardefv")); // NOI18N

            }
            ParameterInfo in = paramTable[i];

            if (in.getType() != null && in.getType().endsWith("...") && i!=paramTable.length-1) {//NOI18N
                    p = createProblem(p, true, org.openide.util.NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_VarargsFinalPosition", new Object[] {}));
                }
        }
        return p;    
    }

    private static String newParMessage(String par) {
        return new MessageFormat(
                getString("ERR_newpar")).format(new Object[] { getString(par) } // NOI18N
            );
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPlugin.class, key);
    }

    private Set<ElementHandle<ExecutableElement>> allMethods;
    
    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    final ElementUtilities elmUtils = info.getElementUtilities();

                    //add all references of overriding methods
                        TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
                        Element el = treePathHandle.resolveElement(info);
                    ElementHandle<TypeElement>  enclosingType = ElementHandle.create(elmUtils.enclosingTypeElement(el));
                        allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                        allMethods.add(ElementHandle.create((ExecutableElement)el));
                        for (ExecutableElement e:RetoucheUtils.getOverridingMethods((ExecutableElement)el, info)) {
                            set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        //add all references of overriden methods
                        for (ExecutableElement e:RetoucheUtils.getOverridenMethods((ExecutableElement)el, info)) {
                            set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        set.add(SourceUtils.getFile(el, info.getClasspathInfo()));
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    
    
    public Problem prepare(RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            TransformTask transform = new TransformTask(new ChangeParamsTransformer(refactoring, allMethods), treePathHandle);
            Problem p = createAndAddElements(a, transform, elements, refactoring);
            if (p != null) {
                fireProgressListenerStop();
                return p;
            }
        }
        fireProgressListenerStop();
        return null;
//        JavaMetamodel.getManager().getProgressSupport().addProgressListener(this);
//        Problem problem = null;
//        try {
//            // get the original access modifier and check, if the original
//            // modifier is weaker than the new modifier. If so, set checkMod
//            // to true and in following code check, if all usages will have 
//            // sufficient access privileges.
//            int origAccessMods = method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
//            boolean checkMod = compareModifiers(origAccessMods, modifier) == -1;
//            // get all the callers and usages of the callable and add them
//            // the the collection of refactored elements
//            referencesIterator = ((CallableFeatureImpl) method).findDependencies(true, true, true).iterator();
//            elements.add(refactoring, new SignatureElement(method, paramTable, modifier));
//            int parNum = ((CallableFeature) refactoring.getRefactoredObject()).getParameters().size();
//            while (referencesIterator.hasNext()) {
//                if (cancelRequest) {
//                    return null;
//                }
//                Object ref = referencesIterator.next();
//                if (ref instanceof Invocation) {
//                    // Callers. Refactored method has to have the same number
//                    // of parameters as its caller. (see issue #53041 for details)
//                    if (((Invocation) ref).getParameters().size() == parNum) {
//                        if (problem == null && checkMod) {
//                            // check the access to refactored method
//                            Feature f = JavaModelUtil.getDeclaringFeature((Invocation)ref);
//                            if (Modifier.isPrivate(modifier)) { // changing to private
//                                if (!Utilities.compareObjects(getOutermostClass(f), getOutermostClass(method))) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "private" })); // NOI18N
//                                }
//                            } else if (Modifier.isProtected(modifier)) { // changing to protected
//                                if (!method.getResource().getPackageName().equals(f.getResource().getPackageName())) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "protected" })); // NOI18N
//                                }
//                            } else if (modifier == 0) { // default modifier check
//                                if (!f.getResource().getPackageName().equals(method.getResource().getPackageName())) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "default" })); // NOI18N
//                                }
//                            }
//                        }
//                        elements.add(refactoring, new CallerElement((Invocation) ref, paramTable));
//                    }
//                } else {
//                    // declaration/declarations (in case of overriden or overrides)
//                    elements.add(refactoring, new SignatureElement((CallableFeature) ref, paramTable, modifier));
//                }
//            }
//            return problem;
//        } 
//        finally {
//            referencesIterator = null;
//            JavaMetamodel.getManager().getProgressSupport().removeProgressListener(this);
//        }
    }
    
    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        switch(p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:    
            case PRECHECK:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
        return null;
    }
    /**
     * Returns list of problems. For the change method signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     *
     * @return  overrides or overriden problem or both
     */
    @Override
    public Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        Element el = treePathHandle.resolveElement(info);
        if (!(el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType"));
            return preCheckProblem;
        }
        
        FileObject fo = SourceUtils.getFile(el,info.getClasspathInfo());
        if (RetoucheUtils.isFromLibrary(el, info.getClasspathInfo())) { //NOI18N
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                    ChangeParametersPlugin.class, "ERR_CannotRefactorLibraryClass",
                    el.getEnclosingElement()
                    ));
            return preCheckProblem;
        }
        
        if (!RetoucheUtils.isElementInOpenProject(fo)) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(
                    ChangeParametersPlugin.class,
                    "ERR_ProjectNotOpened",
                    FileUtil.getFileDisplayName(fo)));
            return preCheckProblem;
        }
        
        if (info.getElementUtilities().enclosingTypeElement(el).getKind() == ElementKind.ANNOTATION_TYPE) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_MethodsInAnnotationsNotSupported"));
            return preCheckProblem;
        }
        
        for (ExecutableElement e : RetoucheUtils.getOverridenMethods((ExecutableElement) el, info)) {
            if (RetoucheUtils.isFromLibrary(e, info.getClasspathInfo())) { //NOI18N
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_CannnotRefactorLibrary", el));
            }
        }
                    
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
}
