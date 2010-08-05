/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.awt;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public final class ActionProcessor extends LayerGeneratingProcessor {
    private static final String IDENTIFIER = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"; // NOI18N
    private static final Pattern FQN = Pattern.compile(IDENTIFIER + "(?:[.]" + IDENTIFIER + ")*"); // NOI18N

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<String>();
        hash.add(ActionRegistration.class.getCanonicalName());
        hash.add(ActionID.class.getCanonicalName());
        return hash;
    }
    
    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) throws LayerGenerationException {
        TypeMirror actionListener = type(ActionListener.class);
        TypeMirror p1 = type(Presenter.Menu.class);
        TypeMirror p2 = type(Presenter.Toolbar.class);
        TypeMirror p3 = type(Presenter.Popup.class);
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionRegistration.class)) {
            ActionRegistration ar = e.getAnnotation(ActionRegistration.class);
            ActionID aid = e.getAnnotation(ActionID.class);
            if (aid == null) {
                throw new LayerGenerationException("@ActionRegistration can only be used together with @ActionID annotation", e);
            }
            if (aid.category().contains("/")) {
                throw new LayerGenerationException("@ActionID category() cannot contain /", e);
            }
            if (!FQN.matcher(aid.id()).matches()) {
                throw new LayerGenerationException("@ActionID id() must be valid fully qualified name", e);
            }
            String id = aid.id().replace('.', '-');
            File f = layer(e).file("Actions/" + aid.category() + "/" + id + ".instance");
            f.bundlevalue("displayName", ar.displayName());
            
            String key;
            boolean createDelegate = true;
            if (e.getKind() == ElementKind.FIELD) {
                VariableElement var = (VariableElement)e;
                TypeMirror stringType = type(String.class);
                if (
                    e.asType() != stringType || 
                    !e.getModifiers().contains(Modifier.PUBLIC) || 
                    !e.getModifiers().contains(Modifier.STATIC) ||
                    !e.getModifiers().contains(Modifier.FINAL)
                ) {
                    throw new LayerGenerationException("Only static string constant fields can be annotated", e);
                }
                if (ar.key().length() != 0) {
                    throw new LayerGenerationException("When annotating field, one cannot define key()", e);
                }
                
                createDelegate = false;
                key = var.getConstantValue().toString();
            } else {
                if (!isAssignable(e.asType(), actionListener)) {
                    throw new LayerGenerationException("Class annotated with @ActionRegistration must implement java.awt.event.ActionListener!", e);
                }
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException("Class has to be public", e);
                }
                key = ar.key();
            }
            
            boolean direct = 
                isAssignable(e.asType(), p1) ||
                isAssignable(e.asType(), p2) ||
                isAssignable(e.asType(), p3);
            
            
            if (direct) {
                if (key.length() != 0) {
                    throw new LayerGenerationException("Cannot specify key and implement Presenter interface", e);
                }
                f.instanceAttribute("instanceCreate", Action.class);
            } else {
                if (key.length() == 0) {
                    f.methodvalue("instanceCreate", "org.openide.awt.Actions", "alwaysEnabled");
                } else {
                    f.methodvalue("instanceCreate", "org.openide.awt.Actions", "callback");
                    if (createDelegate) {
                        f.methodvalue("fallback", "org.openide.awt.Actions", "alwaysEnabled");
                    }
                    f.stringvalue("key", key);
                }
                if (createDelegate) {
                    try {
                        f.instanceAttribute("delegate", ActionListener.class);
                    } catch (LayerGenerationException ex) {
                        generateContext(e, f);
                    }
                }
                f.boolvalue("noIconInMenu", !ar.iconInMenu());
                if (ar.asynchronous()) {
                    f.boolvalue("asynchronous", true);
                }
                if (ar.surviveFocusChange()) {
                    f.boolvalue("surviveFocusChange", true);
                }
            }
            f.write();
        }
        return true;
    }

    private TypeMirror type(Class<?> type) {
        final TypeElement e = processingEnv.getElementUtils().getTypeElement(type.getName().replace('$', '.'));
        return e == null ? null : e.asType();
    }

    private void generateContext(Element e, File f) throws LayerGenerationException {
        ExecutableElement ee = null;
        ExecutableElement candidate = null;
        for (ExecutableElement element : ElementFilter.constructorsIn(e.getEnclosedElements())) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                candidate = element;
                if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                if (ee != null) {
                    throw new LayerGenerationException("Only one public constructor allowed in ", e); // NOI18N
                }
                ee = element;
            }

        }
        
        if (ee == null || ee.getParameters().size() != 1) {
            if (candidate != null) {
                throw new LayerGenerationException("Constructor has to be public with one argument", candidate);
            }
            throw new LayerGenerationException("Constructor must have one argument", ee);
        }

        VariableElement ve = (VariableElement)ee.getParameters().get(0);
        DeclaredType dt = (DeclaredType)ve.asType();
        String dtName = processingEnv.getElementUtils().getBinaryName((TypeElement)dt.asElement()).toString();
        if ("java.util.List".equals(dtName)) {
            f.stringvalue("type", dt.getTypeArguments().get(0).toString());
            f.methodvalue("delegate", "org.openide.awt.Actions", "inject");
            f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString());
            f.stringvalue("selectionType", "ANY");
            f.methodvalue("instanceCreate", "org.openide.awt.Actions", "context");
            return;
        }
        if (!dt.getTypeArguments().isEmpty()) {
            throw new LayerGenerationException("No type parameters allowed in ", ee);
        }

        f.stringvalue("type", ve.asType().toString());
        f.methodvalue("delegate", "org.openide.awt.Actions", "inject");
        f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString());
        f.stringvalue("selectionType", "EXACTLY_ONE");
        f.methodvalue("instanceCreate", "org.openide.awt.Actions", "context");
    }

    private boolean isAssignable(TypeMirror first, TypeMirror snd) {
        if (snd == null) {
            return false;
        } else {
            return processingEnv.getTypeUtils().isAssignable(first, snd);
        }
    }
}
