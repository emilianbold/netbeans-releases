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

package org.netbeans.debugger.registry;

import java.util.Map;
import java.util.Set;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.SessionProvider;
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
@SupportedAnnotationTypes({"org.netbeans.spi.debugger.ActionsProvider.Registration",        //NOI18N
                           "org.netbeans.spi.debugger.DebuggerEngineProvider.Registration", //NOI18N
                           "org.netbeans.spi.debugger.SessionProvider.Registration",        //NOI18N
                           "org.netbeans.spi.debugger.DebuggerServiceRegistration"          //NOI18N
                          })
public class DebuggerProcessor extends LayerGeneratingProcessor {


    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations,
        RoundEnvironment env
    ) throws LayerGenerationException {
        if (env.processingOver()) {
            return false;
        }

        int cnt = 0;
        for (Element e : env.getElementsAnnotatedWith(ActionsProvider.Registration.class)) {
            ActionsProvider.Registration reg = e.getAnnotation(ActionsProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, ActionsProvider.class, path);
            //layer(e).instanceFile("Debugger/"+path, null, ActionsProvider.class).
            //        stringvalue("serviceName", instantiableClassOrMethod(e)).
            //        stringvalue("serviceClass", ActionsProvider.class.getName()).
            //        methodvalue("instanceCreate", "org.netbeans.debugger.registry.ActionsProviderContextAware", "createService").
            //        write();
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(LazyActionsManagerListener.Registration.class)) {
            LazyActionsManagerListener.Registration reg = e.getAnnotation(LazyActionsManagerListener.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, LazyActionsManagerListener.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerEngineProvider.Registration.class)) {
            DebuggerEngineProvider.Registration reg = e.getAnnotation(DebuggerEngineProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, DebuggerEngineProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(SessionProvider.Registration.class)) {
            SessionProvider.Registration reg = e.getAnnotation(SessionProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistration(e, SessionProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerServiceRegistration.class)) {
            DebuggerServiceRegistration reg = e.getAnnotation(DebuggerServiceRegistration.class);

            final String path = reg.path();
            // Class[] classes = reg.types(); - Cant NOT do that, classes are not created at compile time.
            // e.getAnnotationMirrors() - use this not to generate MirroredTypeException
            String classNames = null;
            for (AnnotationMirror am : e.getAnnotationMirrors()) {
                if (am.getAnnotationType().toString().equals(DebuggerServiceRegistration.class.getName())) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                            am.getElementValues();
                    //System.err.println("am:\n elementValues = "+elementValues);
                    for (ExecutableElement ee : elementValues.keySet()) {
                        if (ee.getSimpleName().contentEquals("types")) { // NOI18N
                            classNames = elementValues.get(ee).toString();
                        }
                    }
                }
            }
            //System.err.println("classNames before translation = "+classNames);
            classNames = translateClassNames(classNames);
            //System.err.println("classNames after  translation = "+classNames);
            /*
            Class[] classes;
            String classNames;
            try {
                classes = reg.types();
                //className = clazz.getName();
                classNames = null;
            } catch (MirroredTypeException mtex) {
                TypeMirror tm = mtex.getTypeMirror();
                classes = null;
                classNames = tm.toString();
            }
            */
            layer(e).instanceFile("Debugger/"+path, null, null).
                    stringvalue(ContextAwareServiceHandler.SERVICE_NAME, instantiableClassOrMethod(e)).
                    stringvalue(ContextAwareServiceHandler.SERVICE_CLASSES, classNames).
                    methodvalue("instanceCreate", "org.netbeans.debugger.registry.ContextAwareServiceHandler", "createService").
                    write();
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
                stringvalue("serviceName", className).
                stringvalue("serviceClass", providerClass.getName()).
                methodvalue("instanceCreate", "org.netbeans.debugger.registry."+providerClass.getSimpleName()+"ContextAware", "createService").
                write();
    }

    private boolean isClassOf(Element e, Class providerClass) {
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
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
    }

    /**
     * Translates "{org.MyClass1.class, org.MyClass2.class, ... }" to
     * "org.MyClass1, org.MyClass2, ..."
     * @param classNames
     * @return comma-separated class names
     */
    private String translateClassNames(String classNames) {
        classNames = classNames.substring(1, classNames.length() - 1).trim();
        StringBuilder builder = new StringBuilder();
        int i1 = 0;
        int i2;
        while ((i2 = classNames.indexOf(',', i1)) > 0) {
            if (i1 > 0) builder.append(',');
            builder.append(translateClass(classNames.substring(i1, i2).trim()));
            i1 = i2 + 1;
        }
        if (i1 > 0) builder.append(',');
        builder.append(translateClass(classNames.substring(i1).trim()));

        return builder.toString();
    }

    private String translateClass(String className) {
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - ".class".length());
        }
        TypeElement type = processingEnv.getElementUtils().getTypeElement(className);
        //System.err.println("translateClass("+className+") type = "+type);
        return processingEnv.getElementUtils().getBinaryName(type).toString();
    }
}
