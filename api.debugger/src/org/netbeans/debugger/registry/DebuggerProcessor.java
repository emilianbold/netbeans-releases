/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
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
public class DebuggerProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            ActionsProvider.Registration.class.getCanonicalName(),
            DebuggerEngineProvider.Registration.class.getCanonicalName(),
            SessionProvider.Registration.class.getCanonicalName(),
            LazyActionsManagerListener.Registration.class.getCanonicalName(),
            DebuggerServiceRegistration.class.getCanonicalName()
        ));
    }

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
            final String[] actions = reg.actions();
            final String[] mimeTypes = reg.activateForMIMETypes();
            handleProviderRegistrationInner(e, ActionsProvider.class, path, actions, mimeTypes);
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
            handleProviderRegistrationInner(e, LazyActionsManagerListener.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerEngineProvider.Registration.class)) {
            DebuggerEngineProvider.Registration reg = e.getAnnotation(DebuggerEngineProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistrationInner(e, DebuggerEngineProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(SessionProvider.Registration.class)) {
            SessionProvider.Registration reg = e.getAnnotation(SessionProvider.Registration.class);

            final String path = reg.path();
            handleProviderRegistrationInner(e, SessionProvider.class, path);
            cnt++;
        }
        for (Element e : env.getElementsAnnotatedWith(DebuggerServiceRegistration.class)) {
            DebuggerServiceRegistration reg = e.getAnnotation(DebuggerServiceRegistration.class);

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
                            classNames = elementValues.get(ee).getValue().toString();
                        }
                    }
                }
            }
            //System.err.println("classNames before translation = "+classNames);
            classNames = translateClassNames(classNames);
            if (!implementsInterfaces(e, classNames)) {
                throw new IllegalArgumentException("Annotated element "+e+" does not implement all interfaces " + classNames);
            }
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
            String path = reg.path();
            if (path != null && path.length() > 0) {
                path = "Debugger/"+path;
            } else {
                path = "Debugger";
            }
            layer(e).instanceFile(path, null, null).
                    stringvalue(ContextAwareServiceHandler.SERVICE_NAME, instantiableClassOrMethod(e)).
                    //stringvalue(ContextAwareServiceHandler.SERVICE_CLASSES, classNames). - taken from instanceOf so that we do not have to provide it twice
                    stringvalue("instanceOf", classNames).
                    methodvalue("instanceCreate", "org.netbeans.spi.debugger.ContextAwareSupport", "createService").
                    write();
            cnt++;
        }
        return cnt == annotations.size();
    }

    private void handleProviderRegistrationInner(Element e, Class providerClass, String path) throws IllegalArgumentException, LayerGenerationException {
        handleProviderRegistrationInner(e, providerClass, path, null, null);
    }

    private void handleProviderRegistrationInner(Element e, Class providerClass, String path,
                                                 String[] actions, String[] enabledOnMIMETypes
                                                 ) throws IllegalArgumentException, LayerGenerationException {
        String className = instantiableClassOrMethod(e);
        if (!isClassOf(e, providerClass)) {
            throw new IllegalArgumentException("Annotated element "+e+" is not an instance of " + providerClass);
        }
        if (path != null && path.length() > 0) {
            path = "Debugger/"+path;
        } else {
            path = "Debugger";
        }
        File f = layer(e).instanceFile(path, null, providerClass).
                stringvalue(ContextAwareServiceHandler.SERVICE_NAME, className).
                stringvalue("serviceClass", providerClass.getName());
        if (actions != null && actions.length > 0) {
            f.stringvalue(ContextAwareServiceHandler.SERVICE_ACTIONS, Arrays.toString(actions));
        }
        if (enabledOnMIMETypes != null && enabledOnMIMETypes.length > 0) {
            f.stringvalue(ContextAwareServiceHandler.SERVICE_ENABLED_MIMETYPES, Arrays.toString(enabledOnMIMETypes));
        }
        f.stringvalue("instanceOf", providerClass.getName());
        f.methodvalue("instanceCreate", providerClass.getName()+"$ContextAware", "createService");
        f.write();
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
                TypeMirror retType = ((ExecutableElement) e).getReturnType();
                if (retType.getKind().equals(TypeKind.NONE)) {
                    return false;
                } else {
                    e = ((DeclaredType) retType).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    if (clazz.equals(providerClass.getName())) {
                        return true;
                    } else {
                        return isClassOf(e, providerClass);
                    }
                }
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
    }

    private boolean implementsInterfaces(Element e, String classNames) {
        Set<String> interfaces = new HashSet(Arrays.asList(classNames.split("[, ]+")));
        return implementsInterfaces(e, interfaces);
    }

    private boolean implementsInterfaces(Element e, Set<String> interfaces) {
        switch (e.getKind()) {
            case CLASS:
            case INTERFACE: {
                TypeElement te = (TypeElement) e;
                List<? extends TypeMirror> interfs = te.getInterfaces();
                for (TypeMirror tm : interfs) {
                    e = ((DeclaredType) tm).asElement();
                    String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                    boolean contains = interfaces.remove(clazz);
                    if (!contains) {
                        implementsInterfaces(e, interfaces);
                    }
                }
                break;
            }
            case METHOD: {
                TypeMirror retType = ((ExecutableElement) e).getReturnType();
                if (retType.getKind().equals(TypeKind.NONE)) {
                    return false;
                } else {
                    TypeElement te = (TypeElement) ((DeclaredType) retType).asElement();
                    List<? extends TypeMirror> interfs = te.getInterfaces();
                    for (TypeMirror tm : interfs) {
                        e = ((DeclaredType) tm).asElement();
                        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                        interfaces.remove(clazz);
                    }
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);
        }
        return interfaces.isEmpty();
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

    /**
     * Translates "org.MyClass1.class, org.MyClass2.class, ... " to
     * "org.MyClass1, org.MyClass2, ..."
     * @param classNames
     * @return comma-separated class names
     */
    private String translateClassNames(String classNames) {
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
