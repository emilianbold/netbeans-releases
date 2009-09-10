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

import java.util.List;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.netbeans.modules.editor.lib2.actions.PresenterEditorAction;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Annotation processor for {@link EditorActionRegistration}
 * and {@link EditorActionRegistrations}.
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
        TypeMirror utilMapType = processingEnv.getTypeUtils().getDeclaredType(
                processingEnv.getElementUtils().getTypeElement("java.util.Map"));
        boolean directActionCreation = false; // Whether construct AlwaysEnabledAction or annotated action directly
        switch (e.getKind()) {
            case CLASS:
                className = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(className + " must not be abstract", e);
                }
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(className + " is not public", e);
                }
                ExecutableElement defaultCtor = null;
                ExecutableElement mapCtor = null;
                for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                    List<? extends VariableElement> params = constructor.getParameters();
                    if (params.isEmpty()) {
                        defaultCtor = constructor;

                    } else if (params.size() == 1 &&
                        processingEnv.getTypeUtils().isAssignable(params.get(0).asType(), utilMapType))
                    {
                        mapCtor = constructor;
                    }
                }
                String msgBase = "No-argument (or single-argument \"Map<String,?> attrs\") constructor";
                if (defaultCtor == null && mapCtor == null) {
                    throw new LayerGenerationException(msgBase + " not present in " + className, e);
                }
                boolean defaultCtorPublic = (defaultCtor != null && defaultCtor.getModifiers().contains(Modifier.PUBLIC));
                boolean mapCtorPublic = (mapCtor != null && mapCtor.getModifiers().contains(Modifier.PUBLIC));
                if (!defaultCtorPublic && !mapCtorPublic) {
                    throw new LayerGenerationException(msgBase + " not public in " + className, e);
                }

                if (!processingEnv.getTypeUtils().isAssignable(e.asType(), swingActionType)) {
                    throw new LayerGenerationException(className + " is not assignable to javax.swing.Action", e);
                }
                if (mapCtorPublic) {
                    directActionCreation = true;
                }
                methodName = null;
                break;

            case METHOD:
                className = processingEnv.getElementUtils().getBinaryName((TypeElement) e.getEnclosingElement()).toString();
                methodName = e.getSimpleName().toString();
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(className + "." + methodName + " must be static", e); // NOI18N
                }
                // It appears that actually even non-public method registration works - so commented following
//                    if (!e.getModifiers().contains(Modifier.PUBLIC)) {
//                        throw new LayerGenerationException(className + "." + methodName + " must be public", e);
//                    }
                List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                boolean emptyParams = params.isEmpty();
                boolean mapParam = (params.size() == 1 && processingEnv.getTypeUtils().isAssignable(
                        params.get(0).asType(), utilMapType));
                if (!emptyParams && !mapParam)
                {
                    throw new LayerGenerationException(className + "." + methodName +
                            " must not take arguments (or have a single-argument \"Map<String,?> attrs\")", e); // NOI18N
                }
                TypeMirror returnType = ((ExecutableElement)e).getReturnType();
                if (swingActionType != null && !processingEnv.getTypeUtils().isAssignable(returnType, swingActionType)) {
                    throw new LayerGenerationException(className + "." + methodName + " is not assignable to javax.swing.Action", e);
                }
                if (mapParam) {
                    directActionCreation = true;
                }
                break;

            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);

        }

        String actionName = annotation.name();
        StringBuilder filePath = new StringBuilder(50);
        String mimeType = annotation.mimeType();
        filePath.append("Editors");
        if (mimeType.length() > 0) {
            filePath.append("/").append(mimeType);
        }
        filePath.append("/Actions/").append(actionName).append(".instance");
        LayerBuilder layer = layer(e);
        LayerBuilder.File file = layer.file(filePath.toString());
        String preferencesKey = annotation.preferencesKey();
        boolean checkBoxPresenter = (preferencesKey.length() > 0);

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

        // Check presenters
        String presenterActionName = null;

        // Check menu path
        String menuPath = annotation.menuPath();
        int menuPosition = annotation.menuPosition();
        if (menuPosition != Integer.MAX_VALUE) {
            StringBuilder presenterFilePath = new StringBuilder(50);
            presenterFilePath.append("Menu/");
            if (menuPath.length() > 0) {
                presenterFilePath.append(menuPath).append('/');
            }
            presenterFilePath.append(actionName).append(".shadow");
            LayerBuilder.File presenterShadowFile = layer.file(presenterFilePath.toString());
            if (presenterActionName == null) {
                if (checkBoxPresenter) { // Point directly to AlwaysEnabledAction
                    presenterActionName = "Editors/Actions/" + actionName + ".instance";
                } else {
                    presenterActionName = generatePresenterAction(layer, actionName);
                }
            }
            presenterShadowFile.stringvalue("originalFile", presenterActionName);
            presenterShadowFile.intvalue("position", menuPosition);
            presenterShadowFile.write();
        }

        // Check popup path
        String popupPath = annotation.popupPath();
        int popupPosition = annotation.popupPosition();
        if (popupPosition != Integer.MAX_VALUE) {
            StringBuilder presenterFilePath = new StringBuilder(50);
            presenterFilePath.append("Editors/Popup/");
            if (mimeType.length() > 0) {
                presenterFilePath.append(mimeType).append("/");
            }
            if (popupPath.length() > 0) {
                presenterFilePath.append(popupPath).append('/');
            }
            presenterFilePath.append(actionName).append(".shadow");
            LayerBuilder.File presenterShadowFile = layer.file(presenterFilePath.toString());
            if (presenterActionName == null) {
                if (checkBoxPresenter) { // Point directly to AlwaysEnabledAction
                    presenterActionName = "Editors/Actions/" + actionName + ".instance";
                } else {
                    presenterActionName = generatePresenterAction(layer, actionName);
                }
            }
            presenterShadowFile.stringvalue("originalFile", presenterActionName);
            presenterShadowFile.intvalue("position", popupPosition);
            presenterShadowFile.write();
        }
        
        int toolBarPosition = annotation.toolBarPosition();
        if (toolBarPosition != Integer.MAX_VALUE) {
            StringBuilder presenterFilePath = new StringBuilder(50);
            presenterFilePath.append("Editors/Toolbar/");
            if (mimeType.length() > 0) {
                presenterFilePath.append(mimeType).append("/");
            }
            presenterFilePath.append(actionName).append(".shadow");
            LayerBuilder.File presenterShadowFile = layer.file(presenterFilePath.toString());
            if (presenterActionName == null) {
                presenterActionName = generatePresenterAction(layer, actionName);
            }
            presenterShadowFile.stringvalue("originalFile", presenterActionName);
            presenterShadowFile.intvalue("position", toolBarPosition);
            presenterShadowFile.write();
        }

        if (preferencesKey.length() > 0) {
            file.stringvalue("PreferencesKey", preferencesKey);
            file.methodvalue("PreferencesNode", EditorActionUtilities.class.getName(), "getGlobalPreferences");
        }

        // Deafult helpID is action's name
        file.stringvalue("helpID", actionName);

        // Resolve accelerator through method
        file.methodvalue(Action.ACCELERATOR_KEY, EditorActionUtilities.class.getName(), "getAccelerator");

        // Always generate Action.NAME since although AlwaysEnabledAction tweaks its retrieval to "displayName"
        // some tools may query FO's properties and expect it there.
        file.stringvalue(Action.NAME, actionName);

        if (directActionCreation) {
            if (methodName != null) {
                file.methodvalue("instanceCreate", className, methodName);
            } else {
                file.newvalue("instanceCreate", className);
            }

        } else { // Create always enabled action
            file.methodvalue("instanceCreate", "org.openide.awt.Actions", "alwaysEnabled");
            file.stringvalue("displayName", actionName);

            if (methodName != null) {
                file.methodvalue("delegate", className, methodName);
            } else {
                file.newvalue("delegate", className);
            }
        }
        file.write();
    }

    private String generatePresenterAction(LayerBuilder layer, String actionName) {
        String presenterActionName = "Editors/ActionPresenters/" + actionName + ".instance";
        LayerBuilder.File presenterActionFile = layer.file(presenterActionName);
        presenterActionFile.methodvalue("instanceCreate", PresenterEditorAction.class.getName(), "create");
        presenterActionFile.stringvalue(Action.NAME, actionName);
        presenterActionFile.write();
        return presenterActionName;
    }

}
