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

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.util.NbBundle;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldsPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    
    private List<EncapsulateFieldRefactoring> refactorings;
    private final EncapsulateFieldsRefactoring refactoring;
    private boolean cancelRequest = false;
    
    private ProgressListener listener = new ProgressListener() {
        public void start(ProgressEvent event) {
            fireProgressListenerStart(event.getOperationType(),event.getCount());
        }

        public void step(ProgressEvent event) {
            fireProgressListenerStep();
        }

       public void stop(ProgressEvent event) {
            fireProgressListenerStop();
        }
    };

    /** Creates a new instance of EcapsulateFields.
     * @param selectedObjects Array of objects (fields) that should be encapsulated.
     */
    public EncapsulateFieldsPlugin(EncapsulateFieldsRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem checkParameters() {
        return validation(2);
    }
    
    public Problem fastCheckParameters() {
        Collection<EncapsulateFieldInfo> fields = refactoring.getRefactorFields();
        if (fields.isEmpty()) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateNothingSelected"));
        }
        initRefactorings(fields,
                refactoring.getMethodModifiers(),
                refactoring.getFieldModifiers(),
                refactoring.isAlwaysUseAccessors());
        return validation(1);
    }

    public Problem preCheck() {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        try {
            fireProgressListenerStep();
            
            final Problem[] result = new Problem[1];
            final TreePathHandle selectedObject = refactoring.getSelectedObject();
            JavaSource js = JavaSource.forFileObject(selectedObject.getFileObject());
            
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController javac) throws Exception {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    result[0] = preCheck(selectedObject.resolve(javac), javac);
                }
            }, true);
            
            return result[0];
            
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            fireProgressListenerStop();
        }
    }

    private  Problem preCheck(TreePath selectedField, CompilationController javac) throws IOException {
        if (selectedField == null) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "DSC_ElNotAvail"));
        }

        Element elm = javac.getTrees().getElement(selectedField);
        if (elm != null && ElementKind.FIELD == elm.getKind()) {
            TreePath source = javac.getTrees().getPath(elm);
            if (source == null) {
                // missing sources with field declaration
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "DSC_ElNotAvail"));
            }
            
            TypeElement encloser = (TypeElement) elm.getEnclosingElement();
            if (ElementKind.INTERFACE == encloser.getKind() || NestingKind.ANONYMOUS == encloser.getNestingKind()) {
                // interface constants, local variables and annonymous declarations are unsupported
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateInIntf"));
            }
            return null;
        }

        TreePath clazz = RetoucheUtils.findEnclosingClass(javac, selectedField, true, false, true, false, false);
        TypeElement clazzElm = (TypeElement) javac.getTrees().getElement(clazz);
        if (elm != clazzElm || clazzElm == null) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateWrongType"));
        }
        if (ElementKind.INTERFACE == clazzElm.getKind()
                || ElementKind.ANNOTATION_TYPE == clazzElm.getKind()
                || NestingKind.ANONYMOUS == clazzElm.getNestingKind()) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateInIntf"));
        }

        for (Element member : clazzElm.getEnclosedElements()) {
            if (ElementKind.FIELD == member.getKind()) { // no enum constant
                return null;
            }
        }
        return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateNoFields", clazzElm.getQualifiedName()));
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        Problem problem = null;
        RefactoringSession session = elements.getSession();
        for (EncapsulateFieldRefactoring ref : refactorings) {
            if (cancelRequest) {
                return null;
            }
            Problem lastProblem = ref.prepare(session);
            if (lastProblem != null) {
                if (problem == null) {
                    problem = lastProblem;
                } else {
                    problem.setNext(lastProblem);
                    problem = lastProblem;
                }
            }
        }
        return problem;
    }
    
    public void cancelRequest() {
        if (refactorings != null) {
            // if there were initialized refactorings for some fields,
            // cancel their prepare phase
            for (EncapsulateFieldRefactoring ref : refactorings) {
                ref.cancelRequest();
            }
        }
    }
    
    private void initRefactorings(Collection<EncapsulateFieldInfo> refactorFields, Set<Modifier> methodModifier, Set<Modifier> fieldModifier, boolean alwaysUseAccessors) {
        refactorings = new ArrayList<EncapsulateFieldRefactoring>(refactorFields.size());
        for (EncapsulateFieldInfo info: refactorFields) {
            EncapsulateFieldRefactoring ref = new EncapsulateFieldRefactoring(info.getField());
            ref.setGetterName(info.getGetterName());
            ref.setSetterName(info.getSetterName());
            ref.setMethodModifiers(methodModifier);
            ref.setFieldModifiers(fieldModifier);
            ref.setAlwaysUseAccessors(alwaysUseAccessors);
            refactorings.add(ref);
        }
    }
    
    private Problem validation(int phase) {
        Problem result = null;
        for (EncapsulateFieldRefactoring ref : refactorings) {
            Problem lastresult = null;
            switch (phase) {
            case 0: lastresult = ref.preCheck(); break;
            case 1: lastresult = ref.fastCheckParameters(); break;
            case 2:
                lastresult = ref.checkParameters();
                ref.addProgressListener(listener);
                break;
            }
            if (lastresult != null) {
                if (result != null) {
                    result.setNext(lastresult);
                } else {
                    result = lastresult;
                }

                if (lastresult.isFatal()) {
                    return result;
                }
            }
        }

        return null;
    }

}    
