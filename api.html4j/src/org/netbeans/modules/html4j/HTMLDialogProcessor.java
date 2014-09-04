/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package org.netbeans.modules.html4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.html4j.HTMLComponent;
import org.netbeans.api.html4j.HTMLDialog;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@ServiceProvider(service = Processor.class)
public class HTMLDialogProcessor extends AbstractProcessor
implements Comparator<ExecutableElement> {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<>();
        hash.add(HTMLDialog.class.getCanonicalName());
        hash.add(HtmlComponent.class.getCanonicalName());
        return hash;
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re)  {
        Map<String,Set<ExecutableElement>> names = new TreeMap<>();
        for (Element e : re.getElementsAnnotatedWith(HTMLDialog.class)) {
            HTMLDialog reg = e.getAnnotation(HTMLDialog.class);
            if (reg == null || e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement) e;
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("Method annotated by @HTMLDialog needs to be static", e);
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("Method annotated by @HTMLDialog cannot be private", e);
            }
            if (!ee.getThrownTypes().isEmpty()) {
                error("Method annotated by @HTMLDialog cannot throw exceptions", e);
            }
            
            PackageElement pkg = findPkg(ee);
            
            String fqn = pkg.getQualifiedName() + "." + reg.className();
            
            Set<ExecutableElement> elems = names.get(fqn);
            if (elems == null) {
                elems = new TreeSet<>(this);
                names.put(fqn, elems);
            }
            elems.add(ee);
        }
        for (Element e : re.getElementsAnnotatedWith(HTMLComponent.class)) {
            HTMLComponent reg = e.getAnnotation(HTMLComponent.class);
            if (reg == null || e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement) e;
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("Method annotated by @HTMLComponent needs to be static", e);
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("Method annotated by @HTMLComponent cannot be private", e);
            }
            if (!ee.getThrownTypes().isEmpty()) {
                error("Method annotated by @HTMLComponent cannot throw exceptions", e);
            }
            
            PackageElement pkg = findPkg(ee);
            
            String fqn = pkg.getQualifiedName() + "." + reg.className();
            
            Set<ExecutableElement> elems = names.get(fqn);
            if (elems == null) {
                elems = new TreeSet<>(this);
                names.put(fqn, elems);
            }
            elems.add(ee);
        }
        
        for (Map.Entry<String, Set<ExecutableElement>> entry : names.entrySet()) {
            String clazzName = entry.getKey();
            Set<ExecutableElement> elems = entry.getValue();
            Element first = elems.iterator().next();
            try {
                JavaFileObject f = processingEnv.getFiler().createSourceFile(
                    clazzName, elems.toArray(new Element[0])
                );
                Writer w = f.openWriter();

                final String[] arr = splitPkg(clazzName, first);
                w.append("package ").append(arr[0]).append(";\n");
                w.append("\n");
                w.append("import org.netbeans.api.html4j.HTMLDialog.Builder;\n");
                w.append("class ").append(arr[1]).append(" {\n");
                w.append("  private ").append(arr[1]).append("() {\n  }\n");
                w.append("\n");
                
                for (ExecutableElement ee : elems) {
                    HTMLDialog reg = ee.getAnnotation(HTMLDialog.class);
                    HTMLComponent comp = ee.getAnnotation(HTMLComponent.class);
                    if (reg != null) {
                        String url = findURL(reg.url(), ee);
                        if (url == null) {
                            continue;
                        }
                        generateDialog(w, ee, url);
                    }
                    if (comp != null) {
                        String url = findURL(comp.url(), ee);
                        if (url == null) {
                            continue;
                        }
                        String t;
                        try {
                            t = comp.type().getName();
                        } catch (MirroredTypeException ex) {
                            t = ex.getTypeMirror().toString();
                        }
                        if (
                            !t.equals("javafx.scene.web.WebView") &&
                            !t.equals("javafx.embed.swing.JFXPanel")
                        ) {
                            error("type() can be either WebView.class or JFXPanel.class", ee);
                        }
                        generateComponent(w, ee, t, url);
                    }
                }
                
                w.append("}\n");
                w.close();
                
            } catch (IOException ex) {
                error("Cannot create " + clazzName, first);
            }
        }
        
        return true;
    }

    private String findURL(final String relativeURL, ExecutableElement ee) {
        String url;
        try {
            URL u = new URL(relativeURL);
            url = u.toExternalForm();
        } catch (MalformedURLException ex2) {
            try {
                final String res = LayerBuilder.absolutizeResource(ee, relativeURL);
                validateResource(res, ee, null, null, false);
                url = "nbresloc:/" + res;
            } catch (LayerGenerationException ex) {
                error("Cannot find resource " + relativeURL, ee);
                url = null;
            }
        }
        return url;
    }

    private void generateDialog(Writer w, ExecutableElement ee, String url) throws IOException {
        w.append("  public static String ").append(ee.getSimpleName());
        w.append("(");
        String sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append("final ").append(v.asType().toString()).append(" ").append(v.getSimpleName());
            sep = ", ";
        }
        
        w.append(") {\n");
        w.append("    return Builder.newDialog(\"").append(url).append("\").\n");
        w.append("      loadFinished(new Runnable() {\n");
        w.append("        public void run() {\n");
        w.append("          ").append(ee.getEnclosingElement().getSimpleName())
                .append(".").append(ee.getSimpleName()).append("(");
        sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append(v.getSimpleName());
            sep = ", ";
        }
        w.append(");\n");
        w.append("        }\n");
        w.append("      }).\n");
        w.append("      showAndWait();\n");
        w.append("  }\n");
    }
    
    private void generateComponent(
        Writer w, ExecutableElement ee, String type, String url
    ) throws IOException {
        w.append("  public static ").append(type).append(" ").append(ee.getSimpleName());
        w.append("(");
        String sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append("final ").append(v.asType().toString()).append(" ").append(v.getSimpleName());
            sep = ", ";
        }
        
        w.append(") {\n");
        w.append("    return Builder.newDialog(\"").append(url).append("\").\n");
        w.append("      loadFinished(new Runnable() {\n");
        w.append("        public void run() {\n");
        w.append("          ").append(ee.getEnclosingElement().getSimpleName())
                .append(".").append(ee.getSimpleName()).append("(");
        sep = "";
        for (VariableElement v : ee.getParameters()) {
            w.append(sep);
            w.append(v.getSimpleName());
            sep = ", ";
        }
        w.append(");\n");
        w.append("        }\n");
        w.append("      }).\n");
        w.append("      component(").append(type).append(".class);\n");
        w.append("  }\n");
    }

    private void error(final String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
    
    private static PackageElement findPkg(Element e) {
        while (e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return (PackageElement)e;
    }
    
    private String[] splitPkg(String s, Element e) {
        int last = s.lastIndexOf('.');
        if (last == -1) {
            error("Cannot generate " + s + " into default package!", e);
            return new String[] { "x", s };
        } else {
            return new String[] { s.substring(0, last), s.substring(last + 1) };
        }
    }

    @Override
    public int compare(ExecutableElement o1, ExecutableElement o2) {
        if (o1 == o2) {
            return 0;
        }
        int names = o1.getSimpleName().toString().compareTo(
            o2.getSimpleName().toString()
        );
        if (names != 0) {
            return names;
        }
        names = o1.getEnclosingElement().getSimpleName().toString().compareTo(
            o2.getEnclosingElement().getSimpleName().toString()
        );
        if (names != 0) {
            return names;
        }
        int id1 = System.identityHashCode(o1);
        int id2 = System.identityHashCode(o2);
        if (id1 == id2) {
            throw new IllegalStateException("Cannot order " + o1 + " and " + o2);
        }
        return id1 - id2;
    }
    
    FileObject validateResource(String resource, Element originatingElement, Annotation annotation, String annotationMethod, boolean searchClasspath) throws LayerGenerationException {
        if (resource.startsWith("/")) {
            throw new LayerGenerationException("do not use leading slashes on resource paths", originatingElement, processingEnv, annotation, annotationMethod);
        }
        if (searchClasspath) {
            for (JavaFileManager.Location loc : new JavaFileManager.Location[]{StandardLocation.SOURCE_PATH, /* #181355 */ StandardLocation.CLASS_OUTPUT, StandardLocation.CLASS_PATH, StandardLocation.PLATFORM_CLASS_PATH}) {
                try {
                    FileObject f = processingEnv.getFiler().getResource(loc, "", resource);
                    if (loc.isOutputLocation()) {
                        f.openInputStream().close();
                    }
                    return f;
                } catch (IOException ex) {
                    continue;
                }
            }
            throw new LayerGenerationException("Cannot find resource " + resource, originatingElement, processingEnv, annotation, annotationMethod);
        } else {
            try {
                try {
                    FileObject f = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", resource);
                    f.openInputStream().close();
                    return f;
                } catch (FileNotFoundException x) {
                    try {
                        FileObject f = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", resource);
                        f.openInputStream().close();
                        return f;
                    } catch (IOException x2) {
                        throw x;
                    }
                }
            } catch (IOException x) {
                throw new LayerGenerationException("Cannot find resource " + resource, originatingElement, processingEnv, annotation, annotationMethod);
            }
        }
    }
    
}
