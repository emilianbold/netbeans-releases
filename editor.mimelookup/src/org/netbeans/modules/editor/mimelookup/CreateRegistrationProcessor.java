/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.mimelookup;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
@SupportedAnnotationTypes({"org.netbeans.api.editor.mimelookup.MimeRegistration", "org.netbeans.api.editor.mimelookup.MimeRegistrations"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@ServiceProvider(service=Processor.class)
public class CreateRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        TypeElement mimeRegistration = processingEnv.getElementUtils().getTypeElement("org.netbeans.api.editor.mimelookup.MimeRegistration");

        for (Element el : roundEnv.getElementsAnnotatedWith(mimeRegistration)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!mimeRegistration.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                process(el, am);
            }
        }

        TypeElement mimeRegistrations = processingEnv.getElementUtils().getTypeElement("org.netbeans.api.editor.mimelookup.MimeRegistrations");

        for (Element el : roundEnv.getElementsAnnotatedWith(mimeRegistrations)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!mimeRegistrations.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (!e.getKey().getSimpleName().contentEquals("value")) continue;

                    for (AnnotationMirror r : (AnnotationMirror[]) e.getValue().getValue()) {
                        process(el, r);
                    }
                }
            }
        }

        return true;
    }

    private void process(Element toRegister, AnnotationMirror mimeRegistration) throws LayerGenerationException {
        TypeMirror service = null;
        String mimeType = null;
        int    position = Integer.MAX_VALUE;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mimeRegistration.getElementValues().entrySet()) {
            Name simpleName = e.getKey().getSimpleName();
            if (simpleName.contentEquals("service")) {
                service = (TypeMirror) e.getValue().getValue();
                continue;
            }
            if (simpleName.contentEquals("mimeType")) {
                mimeType = (String) e.getValue().getValue();
                continue;
            }
            if (simpleName.contentEquals("position")) {
                position = (Integer) e.getValue().getValue();
                continue;
            }
        }

        if (mimeType != null) {
            if (mimeType.length() != 0) mimeType = "/" + mimeType;

            String folder = "";
            TypeElement apiTE = (TypeElement) processingEnv.getTypeUtils().asElement(service);
            TypeElement location = processingEnv.getElementUtils().getTypeElement("org.netbeans.spi.editor.mimelookup.MimeLocation");

            OUTER: for (AnnotationMirror am : apiTE.getAnnotationMirrors()) {
                if (!location.equals(am.getAnnotationType().asElement())) continue;

                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().contentEquals("subfolderName")) {
                        folder = "/" + (String) e.getValue().getValue();
                        break OUTER;
                    }
                }
            }

            instantiableClassOrMethod(toRegister, apiTE);
            layer(toRegister).instanceFile("Editors" + mimeType + folder, null, null).position(position).write();
        }
    }
    
    private void instantiableClassOrMethod(Element anntated, TypeElement apiClass) throws IllegalArgumentException, LayerGenerationException {
        TypeMirror typeMirror = processingEnv.getTypeUtils().getDeclaredType(apiClass);
        
        switch (anntated.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) anntated).toString();
                if (anntated.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", anntated);
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(anntated.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor", anntated);
                    }
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(anntated.asType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + " is not assignable to " + typeMirror, anntated);
                }
                if (!anntated.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(clazz + " is not public", anntated);
                }
                return;
            }
            case METHOD: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) anntated.getEnclosingElement()).toString();
                String method = anntated.getSimpleName().toString();
                if (!anntated.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(clazz + "." + method + " must be static", anntated);
                }
                if (!((ExecutableElement) anntated).getParameters().isEmpty()) {
                    throw new LayerGenerationException(clazz + "." + method + " must not take arguments", anntated);
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement) anntated).getReturnType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + "." + method + " is not assignable to " + typeMirror, anntated);
                }
                return;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + anntated);
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element annotated, AnnotationMirror annotation, ExecutableElement attr, String userText) {
        if (processingEnv == null || annotated == null || !annotated.getKind().isClass()) {
            return Collections.emptyList();
        }

        if (   annotation == null
            || !"org.netbeans.api.editor.mimelookup.MimeRegistration".contentEquals(((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName())) {
            return Collections.emptyList();
        }

        if (!"service".contentEquals(attr.getSimpleName())) {
            return Collections.emptyList();
        }

        TypeElement jlObject = processingEnv.getElementUtils().getTypeElement("java.lang.Object");

        if (jlObject == null) {
            return Collections.emptyList();
        }

        Collection<Completion> result = new LinkedList<Completion>();
        List<TypeElement> toProcess = new LinkedList<TypeElement>();

        toProcess.add((TypeElement) annotated);

        while (!toProcess.isEmpty()) {
            TypeElement c = toProcess.remove(0);

            result.add(new TypeCompletion(c.getQualifiedName().toString() + ".class"));

            List<TypeMirror> parents = new LinkedList<TypeMirror>();

            parents.add(c.getSuperclass());
            parents.addAll(c.getInterfaces());

            for (TypeMirror tm : parents) {
                if (tm == null || tm.getKind() != TypeKind.DECLARED) {
                    continue;
                }

                TypeElement type = (TypeElement) processingEnv.getTypeUtils().asElement(tm);

                if (!jlObject.equals(type)) {
                    toProcess.add(type);
                }
            }
        }

        return result;
    }

    private static final class TypeCompletion implements Completion {

        private final String type;

        public TypeCompletion(String type) {
            this.type = type;
        }

        public String getValue() {
            return type;
        }

        public String getMessage() {
            return null;
        }

    }
}
