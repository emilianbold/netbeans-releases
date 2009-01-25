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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.apiregistry;

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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.jpda.VariablesFilter;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/** Processor to hide all the complexities of settings layer registration.
 *
 * @author Martin Entlicher
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"org.netbeans.api.debugger.jpda.JPDADebugger.Registration",  // NOI18N
                           "org.netbeans.spi.debugger.ui.BreakpointType.Registration"}) //NOI18N
public class DebuggerProcessor extends LayerGeneratingProcessor {

    public static final String SERVICE_NAME = "serviceName"; // NOI18N


    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations,
        RoundEnvironment env
    ) throws LayerGenerationException {
        if (env.processingOver()) {
            return false;
        }

        int cnt = 0;
        for (Element e : env.getElementsAnnotatedWith(JPDADebugger.Registration.class)) {
            JPDADebugger.Registration reg = e.getAnnotation(JPDADebugger.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, JPDADebugger.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(SmartSteppingCallback.Registration.class)) {
            SmartSteppingCallback.Registration reg = e.getAnnotation(SmartSteppingCallback.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, SmartSteppingCallback.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(SourcePathProvider.Registration.class)) {
            SourcePathProvider.Registration reg = e.getAnnotation(SourcePathProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, SourcePathProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(EditorContext.Registration.class)) {
            EditorContext.Registration reg = e.getAnnotation(EditorContext.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, EditorContext.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(VariablesFilter.Registration.class)) {
            VariablesFilter.Registration reg = e.getAnnotation(VariablesFilter.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, VariablesFilter.class, path);
            cnt++;
        }
        return cnt == annotations.size();
    }

    private void handleProviderRegistration(Element e, Class providerClass, String path) throws IllegalArgumentException, LayerGenerationException {
        String className = instantiableClassOrMethod(e);
        if (!isClassOf(e, providerClass)) {
            throw new IllegalArgumentException("Annotated element "+e+" is not an instance of " + providerClass);
        }
        layer(e).instanceFile("Debugger/"+path, null, providerClass).
                stringvalue(SERVICE_NAME, className).
                stringvalue("serviceClass", providerClass.getName()).
                methodvalue("instanceCreate", providerClass.getName()+"$ContextAware", "createService").
                //methodvalue("instanceCreate", "org.netbeans.modules.debugger.ui.registry."+providerClass.getSimpleName()+"ContextAware", "createService").
                write();
    }

    private void handleProviderRegistrationDisplayName(Element e, Class providerClass, String displayName) throws IllegalArgumentException, LayerGenerationException {
        String className = instantiableClassOrMethod(e);
        if (!isClassOf(e, providerClass)) {
            throw new IllegalArgumentException("Annotated element "+e+" is not an instance of " + providerClass);
        }
        layer(e).instanceFile("Debugger", null, providerClass).
                stringvalue(SERVICE_NAME, className).
                stringvalue("serviceClass", providerClass.getName()).
                stringvalue("displayName", displayName). // TODO bundleValue
                methodvalue("instanceCreate", providerClass.getName()+"$ContextAware", "createService").
                write();
    }

    private boolean isClassOf(Element e, Class providerClass) {
        switch (e.getKind()) {
            case CLASS: {
                TypeElement te = (TypeElement) e;
                TypeMirror superType = te.getSuperclass();
                if (superType.getKind().equals(TypeKind.NONE)) {
                    return false;
                } else {
                    e = ((DeclaredType) superType).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    if (clazz.equals(providerClass.getName())) {
                        return true;
                    } else {
                        return isClassOf(e, providerClass);
                    }
                }
            }
            case METHOD: {
                return true;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
    }

    private static File commaSeparated(File f, String[] arr) {
        if (arr.length == 0) {
            return f;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String s : arr) {
            sb.append(sep);
            sb.append(s);
            sep = ",";
        }
        return f.stringvalue("xmlproperties.ignoreChanges", sb.toString());
    }

    private String instantiableClassOrMethod(Element e) throws IllegalArgumentException, LayerGenerationException {
        switch (e.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", e);
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor", e);
                    }
                }
                /*propType = processingEnv.getElementUtils().getTypeElement("java.util.Properties").asType();
                        if (
                            m.getParameters().size() == 1 &&
                            m.getSimpleName().contentEquals("writeProperties") &&
                            m.getParameters().get(0).asType().equals(propType) &&
                            m.getReturnType().getKind() == TypeKind.VOID
                        ) {
                            hasWrite = true;
                        }
                }
                 * */
                return clazz;
            }
            case METHOD: {
                ExecutableElement ee = (ExecutableElement) e;
                String methodName = ee.getSimpleName().toString();
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) ee.getEnclosingElement()).toString();
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(ee + " must be static", e);
                }
                if (ee.getParameters().size() > 0) {
                    throw new LayerGenerationException(ee + " must not have any parameters", e);
                }
                return clazz+"."+methodName+"()";
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
    }
}
