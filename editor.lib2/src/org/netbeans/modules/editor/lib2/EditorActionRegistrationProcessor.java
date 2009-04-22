/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import javax.tools.StandardLocation;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Annotation processor for
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({ "org.netbeans.api.editor.EditorActionRegistration", // NOI18N
                            "org.netbeans.api.editor.EditorActionRegistrations" // NOI18N
                          })
public final class EditorActionRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) throws LayerGenerationException
    {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(EditorActionRegistration.class)) {
            EditorActionRegistration annotation = e.getAnnotation(EditorActionRegistration.class);
            register(e, annotation);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(EditorActionRegistrations.class)) {
            EditorActionRegistrations annotationArray = e.getAnnotation(EditorActionRegistrations.class);
            for (EditorActionRegistration annotation : annotationArray.value()) {
                register(e, annotation);
            }
        }
        return true;
    }

    private void register(Element e, EditorActionRegistration annotation) throws LayerGenerationException {
        String className;
        String methodName;
        TypeMirror swingActionType = processingEnv.getTypeUtils().getDeclaredType(
                processingEnv.getElementUtils().getTypeElement("javax.swing.Action"));

        switch (e.getKind()) {
            case CLASS:
                className = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(className + " must not be abstract", e);
                }
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(className + " is not public", e);
                }
                boolean hasDefaultCtor = false;
                for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                    if (constructor.getParameters().isEmpty()) {
                        if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                            throw new LayerGenerationException("Default constructor of " + className + " is not public", e);
                        }
                        hasDefaultCtor = true;
                        break;
                    }
                }
                if (!hasDefaultCtor) {
                    throw new LayerGenerationException(className + " must have a no-argument constructor", e);
                }

                if (!processingEnv.getTypeUtils().isAssignable(e.asType(), swingActionType)) {
                    throw new LayerGenerationException(className + " is not assignable to javax.swing.Action", e);
                }

                methodName = null;
                break;

            case METHOD:
                className = processingEnv.getElementUtils().getBinaryName((TypeElement) e.getEnclosingElement()).toString();
                methodName = e.getSimpleName().toString();
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(className + "." + methodName + " must be static", e);
                }
                // It appears that actually even non-public method registration works - so commented following
//                    if (!e.getModifiers().contains(Modifier.PUBLIC)) {
//                        throw new LayerGenerationException(className + "." + methodName + " must be public", e);
//                    }
                if (!((ExecutableElement) e).getParameters().isEmpty()) {
                    throw new LayerGenerationException(className + "." + methodName + " must not take arguments", e);
                }
                if (swingActionType != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement)e).getReturnType(), swingActionType)) {
                    throw new LayerGenerationException(className + "." + methodName + " is not assignable to javax.swing.Action", e);
                }
                break;

            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);

        }

        String actionName = annotation.name();
        StringBuilder filePath = new StringBuilder(50);
        filePath.append("Editors");
        if (annotation.mimeType().length() > 0) {
            filePath.append("/").append(annotation.mimeType());
        }
        filePath.append("/Actions/").append(actionName).append(".instance");
        LayerBuilder.File file = layer(e).file(filePath.toString());

        file.stringvalue("displayName", actionName);

        // Resolve icon resource
        String iconResource = annotation.iconResource();
        if (iconResource.length() > 0) {
            file.stringvalue("iconBase", iconResource);
        }

        // Resolve short description bundle key
        String shortDescription = annotation.shortDescription();
        if (shortDescription.length() > 0) {
            if ("BY_ACTION_NAME".equals(shortDescription)) {
                shortDescription = "#" + actionName;
            }
            file.bundlevalue(Action.SHORT_DESCRIPTION, shortDescription);
        }

        // Resolve menu text bundle key
        String menuText = annotation.menuText();
        if (menuText.length() > 0) {
            file.bundlevalue("menuText", menuText);
        } else if (shortDescription.length() > 0) { // Use shortDesc instead
            menuText = shortDescription;
            file.bundlevalue("menuText", menuText);
        }

        // Resolve popup menu text bundle key
        String popupText = annotation.popupText();
        if (popupText.length() > 0) {
            file.bundlevalue("popupText", popupText);
        } else if (menuText.length() > 0) { // Use menuText instead
            popupText = menuText;
            file.bundlevalue("popupText", popupText);
        }

        file.methodvalue("instanceCreate", "org.openide.awt.Actions", "alwaysEnabled");
        if (methodName != null) {
            file.methodvalue("delegate", className, methodName);
        } else {
            file.newvalue("delegate", className);
        }
        file.write();
    }

}
