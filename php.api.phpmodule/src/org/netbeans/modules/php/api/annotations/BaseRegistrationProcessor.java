/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.api.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;

public abstract class BaseRegistrationProcessor<P, R extends Annotation> extends LayerGeneratingProcessor {

    protected abstract String getPath();
    protected abstract int getPosition(R annotation);

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(registrationClass().getCanonicalName());
    }

    @Override
    protected final boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }

        final Class<P> providerClass = providerClass();
        final Class<R> registrationClass = registrationClass();

        final TypeMirror provider = processingEnv.getElementUtils().getTypeElement(providerClass.getName()).asType();
        for (Element element : roundEnv.getElementsAnnotatedWith(registrationClass)) {
            String classname = null;
            String methodname = null;
            switch (element.getKind()) {
                case CLASS:
                    classname = processingEnv.getElementUtils().getBinaryName((TypeElement) element).toString();
                    if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                        throw new LayerGenerationException("Class needs to be public");
                    }
                    if (element.getModifiers().contains(Modifier.ABSTRACT)) {
                        throw new LayerGenerationException("Class cannot be abstract");
                    }
                    if (!processingEnv.getTypeUtils().isAssignable(element.asType(), provider)) {
                        throw new LayerGenerationException("Class needs to extend " + providerClass.getSimpleName());
                    }
                    boolean found = false;
                    for (Element member : processingEnv.getElementUtils().getAllMembers((TypeElement) element)) {
                        if (member.getKind() != ElementKind.CONSTRUCTOR) {
                            continue;
                        }
                        ExecutableElement exec = (ExecutableElement) member;
                        if (!exec.getModifiers().contains(Modifier.PUBLIC)) {
                            continue;
                        }
                        if (!exec.getParameters().isEmpty()) {
                            continue;
                        }
                        found = true;
                        break;
                    }
                    if (!found) {
                        throw new LayerGenerationException("There needs to be public default constructor");
                    }
                    break;

                case METHOD:
                    classname = processingEnv.getElementUtils().getBinaryName((TypeElement) element.getEnclosingElement()).toString();
                    methodname = ((ExecutableElement) element).getSimpleName().toString();

                    if (!element.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
                        throw new LayerGenerationException("Class needs to be public");
                    }

                    ExecutableElement exec = (ExecutableElement) element;
                    if (!exec.getModifiers().contains(Modifier.PUBLIC)
                            || !exec.getModifiers().contains(Modifier.STATIC)
                            || !exec.getParameters().isEmpty()) {
                        throw new LayerGenerationException("The method needs to be public, static and without arguments");
                    }
                    if (!processingEnv.getTypeUtils().isAssignable(exec.getReturnType(), provider)) {
                        throw new LayerGenerationException("Method needs to return " + providerClass.getSimpleName());
                    }

                    break;
                default:
                    throw new IllegalArgumentException(element.toString());
            }

            File f = layer(element)
                    .file(getPath() + "/" + classname.replace('.', '-') + ".instance") // NOI18N
                    .intvalue("position", getPosition(element.getAnnotation(registrationClass))); // NOI18N
            if (methodname != null) {
                f = f.methodvalue("instanceCreate", classname, methodname); // NOI18N
            }
            f.write();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Class<P> providerClass() {
        return (Class<P>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    private Class<R> registrationClass() {
        return (Class<R>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }
}
