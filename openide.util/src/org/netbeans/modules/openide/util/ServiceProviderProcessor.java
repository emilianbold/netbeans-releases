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

package org.netbeans.modules.openide.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"org.openide.util.lookup.ServiceProvider", "org.openide.util.lookup.ServiceProviders"})
public class ServiceProviderProcessor extends AbstractProcessor {

    /** public for ServiceLoader */
    public ServiceProviderProcessor() {}

    private final Map<String, List<String>> outputFiles = new HashMap<String,List<String>>();
    private final Map<String, List<Element>> originatingElements = new HashMap<String,List<Element>>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.errorRaised()) {
            return false;
        }
        if (roundEnv.processingOver()) {
            writeServices();
            return false;
        } else {
            for (Element el : roundEnv.getElementsAnnotatedWith(ServiceProvider.class)) {
                TypeElement clazz = (TypeElement) el;
                if (!verifyServiceProviderSignature(clazz)) {
                    continue;
                }
                ServiceProvider sp = clazz.getAnnotation(ServiceProvider.class);
                register(clazz, sp);
            }
            for (Element el : roundEnv.getElementsAnnotatedWith(ServiceProviders.class)) {
                TypeElement clazz = (TypeElement) el;
                if (!verifyServiceProviderSignature(clazz)) {
                    continue;
                }
                ServiceProviders spp = clazz.getAnnotation(ServiceProviders.class);
                for (ServiceProvider sp : spp.value()) {
                    register(clazz, sp);
                }
            }
            return true;
        }
    }

    private void register(TypeElement clazz, ServiceProvider svc) {
        TypeMirror type;
        try {
            svc.service();
            assert false;
            return;
        } catch (MirroredTypeException e) {
            type = e.getTypeMirror();
        }
        String impl = processingEnv.getElementUtils().getBinaryName(clazz).toString();
        String xface = processingEnv.getElementUtils().getBinaryName((TypeElement) processingEnv.getTypeUtils().asElement(type)).toString();
        if (!processingEnv.getTypeUtils().isAssignable(clazz.asType(), type)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, impl + " is not assignable to " + xface, clazz/*, XXX annotation mirror & value*/);
            return;
        }
        processingEnv.getMessager().printMessage(Kind.NOTE, impl + " to be registered as a " + xface);
        String rsrc = (svc.path().length() > 0 ? "META-INF/namedservices/" + svc.path() + "/" : "META-INF/services/") + xface;
        {
            List<Element> origEls = originatingElements.get(rsrc);
            if (origEls == null) {
                origEls = new ArrayList<Element>();
                originatingElements.put(rsrc, origEls);
            }
            origEls.add(clazz);
        }
        List<String> lines = outputFiles.get(rsrc);
        if (lines == null) {
            lines = new ArrayList<String>();
            try {
                try {
                    FileObject in = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", rsrc);
                    in.openInputStream().close();
                    processingEnv.getMessager().printMessage(Kind.ERROR,
                            "Cannot generate " + rsrc + " because it already exists in sources: " + in.toUri());
                    return;
                } catch (FileNotFoundException x) {
                    // Good.
                }
                try {
                    FileObject in = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", rsrc);
                    InputStream is = in.openInputStream();
                    try {
                        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null) {
                            lines.add(line);
                        }
                    } finally {
                        is.close();
                    }
                } catch (FileNotFoundException x) {
                    // OK, created for the first time
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, x.toString());
                return;
            }
            outputFiles.put(rsrc, lines);
        }
        int idx = lines.indexOf(impl);
        if (idx != -1) {
            lines.remove(idx);
            while (lines.size() > idx && lines.get(idx).matches("#position=.+|#-.+")) {
                lines.remove(idx);
            }
        }
        lines.add(impl);
        if (svc.position() != Integer.MAX_VALUE) {
            lines.add("#position=" + svc.position());
        }
        for (String exclude : svc.supersedes()) {
            lines.add("#-" + exclude);
        }
    }

    private boolean verifyServiceProviderSignature(TypeElement clazz) {
        if (!clazz.getModifiers().contains(Modifier.PUBLIC)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must be public", clazz);
            return false;
        }
        if (clazz.getModifiers().contains(Modifier.ABSTRACT)) {
            processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must not be abstract", clazz);
            return false;
        }
        {
            boolean hasDefaultCtor = false;
            for (ExecutableElement constructor : ElementFilter.constructorsIn(clazz.getEnclosedElements())) {
                if (constructor.getModifiers().contains(Modifier.PUBLIC) && constructor.getParameters().isEmpty()) {
                    hasDefaultCtor = true;
                    break;
                }
            }
            if (!hasDefaultCtor) {
                processingEnv.getMessager().printMessage(Kind.ERROR, clazz + " must have a public no-argument constructor", clazz);
                return false;
            }
        }
        return true;
    }

    private void writeServices() {
        for (Map.Entry<String, List<String>> entry : outputFiles.entrySet()) {
            try {
                FileObject out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", entry.getKey(),
                        originatingElements.get(entry.getKey()).toArray(new Element[0]));
                OutputStream os = out.openOutputStream();
                try {
                    PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                    for (String line : entry.getValue()) {
                        w.println(line);
                    }
                    w.flush();
                    w.close();
                } finally {
                    os.close();
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write to " + entry.getKey() + ": " + x.toString());
            }
        }
    }

}
