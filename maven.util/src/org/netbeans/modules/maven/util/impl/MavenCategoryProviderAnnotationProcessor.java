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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.util.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.maven.util.MavenCategoryProvider;
import org.netbeans.modules.maven.util.MavenCategoryProvider.Registration;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MavenCategoryProviderAnnotationProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
                Registration.class.getCanonicalName()
        ));
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Registration.class)) {
            Registration r = e.getAnnotation(Registration.class);
            if (r == null) {
                continue;
            }
            handle(e, r);
        }
        return true;
    }

    private void handle(Element e, Registration r) throws LayerGenerationException {
        String path = "Projects/org-netbeans-modules-maven/Customizer"; //NOI18N
        if (r.category().length() > 0) {
            path += "/" + r.category();
        }
        boolean addsFolder = r.categoryLabel().length() > 0;
        if (addsFolder) {
            handleFolder(path, e, r);
        }
        File f = layer(e).instanceFile(path, null, MavenCategoryProvider.class, r, null);
        f.methodvalue("instanceCreate", "org.netbeans.modules.maven.util.ProxyCategoryProvider", "create"); //NOI18N
        f.position(addsFolder ? 0 : r.position());
        if( r.groupId().isEmpty() )
            throw new LayerGenerationException("Must specify groupId", e, processingEnv, r); //NOI18N
        f.stringvalue( "groupId", r.groupId());
        if( r.artifactId().isEmpty() )
            throw new LayerGenerationException("Must specify artifactId", e, processingEnv, r); //NOI18N
        f.stringvalue( "artifactId", r.artifactId()); //NOI18N

        String[] clazzOrMethod = instantiableClassOrMethod(e, r, null);
        String clazz = clazzOrMethod[0];
        String method = clazzOrMethod[1];
        if( null == method ) {
            f.newvalue("categoryProvider", clazz); //NOI18N
        } else {
            f.methodvalue( "categoryProvider", clazz, method); //NOI18N
        }
        f.write();
    }

    private void handleFolder(String path, Element e, Registration r) throws LayerGenerationException {
        if (r.category().length() == 0) {
            throw new LayerGenerationException("Must specify category", e, processingEnv, r); //NOI18N
        }
        layer(e).folder(path).bundlevalue("displayName", r.categoryLabel(), r, "categoryLabel").position(r.position()).write(); //NOI18N
    }



    private String[] instantiableClassOrMethod(Element originatingElement, Annotation annotation, String annotationMethod) throws IllegalArgumentException, LayerGenerationException {
        Class<?> type = MavenCategoryProvider.class;
        if (originatingElement == null) {
            throw new IllegalArgumentException("Only applicable to builders with exactly one associated element");
        }
        TypeMirror typeMirror = type != null ?
            processingEnv.getTypeUtils().getDeclaredType(
                processingEnv.getElementUtils().getTypeElement(type.getName().replace('$', '.'))) :
            null;
        switch (originatingElement.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) originatingElement).toString();
                if (originatingElement.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", originatingElement, processingEnv, annotation, annotationMethod);
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(originatingElement.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor", originatingElement, processingEnv, annotation, annotationMethod);
                    }
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(originatingElement.asType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + " is not assignable to " + typeMirror, originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (!originatingElement.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(clazz + " is not public", originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (((TypeElement) originatingElement).getNestingKind().isNested() && !originatingElement.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(clazz + " is nested but not static", originatingElement, processingEnv, annotation, annotationMethod);
                }
                return new String[] {clazz, null};
            }
            case METHOD: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) originatingElement.getEnclosingElement()).toString();
                String method = originatingElement.getSimpleName().toString();
                if (!originatingElement.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(clazz + "." + method + " must be static", originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (!((ExecutableElement) originatingElement).getParameters().isEmpty()) {
                    throw new LayerGenerationException(clazz + "." + method + " must not take arguments", originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement) originatingElement).getReturnType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + "." + method + " is not assignable to " + typeMirror, originatingElement, processingEnv, annotation, annotationMethod);
                }
                return new String[] {clazz, method};
            }
            default:
                throw new LayerGenerationException("Annotated element is not loadable as an instance", originatingElement, processingEnv, annotation, annotationMethod);
        }
    }
}
