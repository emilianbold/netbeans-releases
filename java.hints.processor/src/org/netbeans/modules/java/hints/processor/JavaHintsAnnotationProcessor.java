/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.processor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractAnnotationValueVisitor6;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**Inspired by https://sezpoz.dev.java.net/.
 *
 * @author lahvac
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.netbeans.modules.java.hints.jackpot.code.spi.*")
@ServiceProvider(service=Processor.class, position=100)
public class JavaHintsAnnotationProcessor extends LayerGeneratingProcessor {
    
    private static final Logger LOG = Logger.getLogger(JavaHintsAnnotationProcessor.class.getName());
    
    private final Set<String> hintTypes = new HashSet<String>();
    

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (!roundEnv.processingOver()) {
            generateTypeList("org.netbeans.modules.java.hints.jackpot.code.spi.Hint", roundEnv, hintTypes);
        } else {
            generateTypeFile(hintTypes);
        }

        return false;
    }

    private static final String[] TRIGGERS = new String[] {
        "org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind",
        "org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern"
    };

    private void generateTypeList(String annotationName, RoundEnvironment roundEnv, Set<String> hintTypes) {
        TypeElement hint = processingEnv.getElementUtils().getTypeElement(annotationName);

        if (hint == null) return ;
        
        for (Element annotated : roundEnv.getElementsAnnotatedWith(hint)) {
            if (!verifyHintAnnotationAcceptable(annotated)) continue;
            if (!annotated.getKind().isClass() && !annotated.getKind().isInterface()) {
                if (annotated.getKind() != ElementKind.METHOD) {
                    //the compiler should have already warned about this
                    continue;
                }

                annotated = annotated.getEnclosingElement();
            } else {
                if (!annotated.getKind().isClass()) {
                    //the compiler should have already warned about this
                    continue;
                }
            }

            if (!annotated.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Internal error - cannot find class containing the hint", annotated);
                continue;
            }

            TypeElement current = (TypeElement) annotated;
            hintTypes.add(current.getQualifiedName().toString());
        }

        for (String ann : TRIGGERS) {
            TypeElement annRes = processingEnv.getElementUtils().getTypeElement(ann);

            if (annRes == null) continue;

            for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(hint))) {
                verifyHintMethod(method);
            }
        }

    }

    private void generateTypeFile(Set<String> types) {
        for (String fqn : types) {
            TypeElement clazz = processingEnv.getElementUtils().getTypeElement(fqn);
            LayerBuilder builder = layer(clazz);

            File clazzFolder = builder.folder("org-netbeans-modules-java-hints/code-hints/" + getFQN(clazz).replace('.', '-') + ".class");

            for (AnnotationMirror am : clazz.getAnnotationMirrors()) {
                dumpAnnotation(builder, clazzFolder, am);
            }

            for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                if (!ee.getAnnotationMirrors().isEmpty()) {
                    File methodFolder = builder.folder(clazzFolder.getPath() + "/" + ee.getSimpleName() + ".method");

                    for (AnnotationMirror am : ee.getAnnotationMirrors()) {
                        dumpAnnotation(builder, methodFolder, am);
                    }

                    methodFolder.write();
                }
            }

            clazzFolder.write();
        }
    }

    private void dumpAnnotation(LayerBuilder builder, File folder, AnnotationMirror annotation) {
        String fqn = getFQN(((TypeElement) annotation.getAnnotationType().asElement())).replace('.', '-');
        final File   annotationFolder = builder.folder(folder.getPath() + "/" + fqn + ".annotation");

        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annotation.getElementValues().entrySet()) {
            final String attrName = e.getKey().getSimpleName().toString();
            e.getValue().accept(new DumpAnnotationValue(builder, annotationFolder, attrName), null);
        }

        annotationFolder.write();
    }

    private String getFQN(TypeElement clazz) {
        return processingEnv.getElementUtils().getBinaryName(clazz).toString();
    }

    static final String ERR_RETURN_TYPE = "The return type must be either org.netbeans.spi.editor.hints.ErrorDescription or java.util.List<org.netbeans.spi.editor.hints.ErrorDescription>";
    static final String ERR_PARAMETERS = "The method must have exactly one parameter of type org.netbeans.modules.jackpot30.spi.HintContext";
    static final String ERR_MUST_BE_STATIC = "The method must be static";
    static final String WARN_BUNDLE_KEY_NOT_FOUND = "Bundle key %s not found";

    private boolean verifyHintAnnotationAcceptable(Element hint) {
        String id = "";
        for (AnnotationMirror am : hint.getAnnotationMirrors()) {
            if (((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().contentEquals("org.netbeans.modules.java.hints.jackpot.code.spi.Hint")) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().contentEquals("id")) {
                        id = (String) e.getValue().getValue();
                    }
                }
            }
        }

        if (id == null || id.length() == 0) {
            switch (hint.getKind()) {
                case CLASS:
                    id = ((TypeElement) hint).getQualifiedName().toString();
                    break;
                case METHOD:
                    TypeElement hintClass = (TypeElement) hint.getEnclosingElement();
                    id = hintClass.getQualifiedName() + "." + hint.getSimpleName();
                    break;
                default:
                    //compiler should have already wraned about this
                    return false;
            }
        }

        Element hintPackage = hint;

        while (hintPackage.getKind() != ElementKind.PACKAGE) {
            hintPackage = hintPackage.getEnclosingElement();
        }
        try {
            FileObject bundle = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, ((PackageElement) hintPackage).getQualifiedName(), "Bundle.properties");
            ResourceBundle rb = new PropertyResourceBundle(bundle.openInputStream());

            checkBundle(rb, "DN_" + id, hint);
            checkBundle(rb, "DESC_" + id, hint);
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
        } catch (IllegalArgumentException ex) {
            //#179942: the SOURCE_PATH location may be unsupported, skip the check for bundle.
            LOG.log(Level.FINE, null, ex);
        }

        return true;
    }

    private void checkBundle(ResourceBundle bundle, String key, Element ref) {
        try {
            bundle.getString(key);
        } catch (MissingResourceException ex) {
            processingEnv.getMessager().printMessage(Kind.ERROR, String.format(WARN_BUNDLE_KEY_NOT_FOUND, key), ref);
        }
    }
    
    private boolean verifyHintMethod(ExecutableElement method) {
        StringBuilder error = new StringBuilder();
        Elements elements = processingEnv.getElementUtils();
        TypeElement errDesc = elements.getTypeElement("org.netbeans.spi.editor.hints.ErrorDescription");
        TypeElement juList = elements.getTypeElement("java.util.List");
        TypeElement hintCtx = elements.getTypeElement("org.netbeans.modules.java.hints.jackpot.spi.HintContext");

        if (errDesc == null || juList == null || hintCtx == null) {
            return true;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror errDescType = errDesc.asType(); //no type params, no need to erasure
        TypeMirror juListErrDesc = types.getDeclaredType(juList, errDescType);
        TypeMirror ret = method.getReturnType();

        if (!types.isSameType(ret, errDescType) && !types.isSameType(ret, juListErrDesc)) {
            error.append(ERR_RETURN_TYPE);
            error.append("\n");
        }

        if (method.getParameters().size() != 1 || !types.isSameType(method.getParameters().get(0).asType(), hintCtx.asType())) {
            error.append(ERR_PARAMETERS);
            error.append("\n");
        }

        if (!method.getModifiers().contains(Modifier.STATIC)) {
            error.append(ERR_MUST_BE_STATIC);
            error.append("\n");
        }

        if (error.length() == 0) {
            return true;
        }

        if (error.charAt(error.length() - 1) == '\n') {
            error.delete(error.length() - 1, error.length());
        }

        processingEnv.getMessager().printMessage(Kind.ERROR, error.toString(), method);

        return false;
    }

    private class DumpAnnotationValue extends AbstractAnnotationValueVisitor6<Void, Void> {

        private final LayerBuilder builder;
        private final File annotationFolder;
        private final String attrName;

        public DumpAnnotationValue(LayerBuilder builder, File annotationFolder, String attrName) {
            this.builder = builder;
            this.annotationFolder = annotationFolder;
            this.attrName = attrName;
        }

        public Void visitBoolean(boolean b, Void p) {
            annotationFolder.boolvalue(attrName, b);
            return null;
        }

        public Void visitByte(byte b, Void p) {
            annotationFolder.bytevalue(attrName, b);
            return null;
        }

        public Void visitChar(char c, Void p) {
            annotationFolder.charvalue(attrName, c);
            return null;
        }

        public Void visitDouble(double d, Void p) {
            annotationFolder.doublevalue(attrName, d);
            return null;
        }

        public Void visitFloat(float f, Void p) {
            annotationFolder.floatvalue(attrName, f);
            return null;
        }

        public Void visitInt(int i, Void p) {
            annotationFolder.intvalue(attrName, i);
            return null;
        }

        public Void visitLong(long i, Void p) {
            annotationFolder.longvalue(attrName, i);
            return null;
        }

        public Void visitShort(short s, Void p) {
            annotationFolder.shortvalue(attrName, s);
            return null;
        }

        public Void visitString(String s, Void p) {
            annotationFolder.stringvalue(attrName, s);
            return null;
        }

        public Void visitType(TypeMirror t, Void p) {
            annotationFolder.stringvalue(attrName, getFQN(((TypeElement) ((DeclaredType) t).asElement())));
            return null;
        }

        public Void visitEnumConstant(VariableElement c, Void p) {
            TypeElement owner = (TypeElement) c.getEnclosingElement();
            annotationFolder.stringvalue(attrName, getFQN(owner) + "." + c.getSimpleName());
            return null;
        }

        public Void visitAnnotation(AnnotationMirror a, Void p) {
            File f = builder.folder(annotationFolder.getPath() + "/" + attrName);
            
            dumpAnnotation(builder, f, a);

            f.write();
            return null;
        }

        public Void visitArray(List<? extends AnnotationValue> vals, Void p) {
            File arr = builder.folder(annotationFolder.getPath() + "/" + attrName);
            int c = 0;

            for (AnnotationValue av : vals) {
                av.accept(new DumpAnnotationValue(builder, arr, "item" + c), null);
                c++;
            }

            arr.write();

            return null;
        }
    }

}
