/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl.processor;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
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
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

/**Inspired by https://sezpoz.dev.java.net/.
 *
 * @author lahvac
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.netbeans.spi.java.hints.*")
@ServiceProvider(service=Processor.class, position=100)
public class JavaHintsAnnotationProcessor extends LayerGeneratingProcessor {
    
    private static final Logger LOG = Logger.getLogger(JavaHintsAnnotationProcessor.class.getName());
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (!roundEnv.processingOver()) {
            generateTypeList("org.netbeans.spi.java.hints.Hint", roundEnv);
        }

        return false;
    }

    private static final String[] TRIGGERS = new String[] {
        "org.netbeans.spi.java.hints.TriggerTreeKind",
        "org.netbeans.spi.java.hints.TriggerPattern",
        "org.netbeans.spi.java.hints.TriggerPatterns",
    };

    private static final String[] OPTIONS = new String[] {
        "org.netbeans.spi.java.hints.BooleanOption"
    };

    private void generateTypeList(String annotationName, RoundEnvironment roundEnv) {
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

            TypeElement clazz = (TypeElement) annotated;
            LayerBuilder builder = layer(clazz);

            File clazzFolder = builder.folder("org-netbeans-modules-java-hints/code-hints/" + getFQN(clazz).replace('.', '-') + ".class");

            for (AnnotationMirror am : clazz.getAnnotationMirrors()) {
                dumpAnnotation(builder, clazzFolder, clazz, am);
            }

            for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                if (!ee.getAnnotationMirrors().isEmpty()) {
                    File methodFolder = builder.folder(clazzFolder.getPath() + "/" + ee.getSimpleName() + ".method");

                    for (AnnotationMirror am : ee.getAnnotationMirrors()) {
                        dumpAnnotation(builder, methodFolder, ee, am);
                    }

                    methodFolder.write();
                }
            }

            for (VariableElement var : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
                if (!var.getAnnotationMirrors().isEmpty()) {
                    File fieldFolder = builder.folder(clazzFolder.getPath() + "/" + var.getSimpleName() + ".field");

                    for (AnnotationMirror am : var.getAnnotationMirrors()) {
                        dumpAnnotation(builder, fieldFolder, var, am);
                    }

                    if (var.getConstantValue() instanceof String) {
                        fieldFolder.stringvalue("constantValue", (String) var.getConstantValue());
                    }

                    fieldFolder.write();
                }
            }

            clazzFolder.write();
        }

        for (String ann : TRIGGERS) {
            TypeElement annRes = processingEnv.getElementUtils().getTypeElement(ann);

            if (annRes == null) continue;

            for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(annRes))) {
                verifyHintMethod(method);
                verifyTriggerAnnotations(method);
            }
        }

        for (String ann : OPTIONS) {
            TypeElement annRes = processingEnv.getElementUtils().getTypeElement(ann);

            if (annRes == null) continue;

            for (VariableElement var : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(annRes))) {
                verifyOptionField(var);
            }
        }

    }

    private void dumpAnnotation(LayerBuilder builder, File folder, Element errElement, AnnotationMirror annotation) {
        String fqn = getFQN(((TypeElement) annotation.getAnnotationType().asElement())).replace('.', '-');
        final File   annotationFolder = builder.folder(folder.getPath() + "/" + fqn + ".annotation");

        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annotation.getElementValues().entrySet()) {
            final String attrName = e.getKey().getSimpleName().toString();
            e.getValue().accept(new DumpAnnotationValue(builder, annotationFolder, attrName, errElement, annotation, e.getValue()), null);
        }

        annotationFolder.write();
    }

    private String getFQN(TypeElement clazz) {
        return processingEnv.getElementUtils().getBinaryName(clazz).toString();
    }

    static final String ERR_RETURN_TYPE = "The return type must be either org.netbeans.spi.editor.hints.ErrorDescription or java.util.List<org.netbeans.spi.editor.hints.ErrorDescription>";
    static final String ERR_PARAMETERS = "The method must have exactly one parameter of type org.netbeans.spi.java.hints.HintContext";
    static final String ERR_MUST_BE_STATIC = "The method must be static";
    static final String ERR_OPTION_TYPE = "The option field must be of type java.lang.String";
    static final String ERR_OPTION_MUST_BE_STATIC_FINAL = "The option field must be static final";
    static final String WARN_BUNDLE_KEY_NOT_FOUND = "Bundle key %s not found";

    private static AnnotationMirror findAnnotation(Iterable<? extends AnnotationMirror> annotations, String annotationFQN) {
        for (AnnotationMirror am : annotations) {
            if (((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationFQN)) {
                return am;
            }
        }

        return null;
    }

    private <T> T getAttributeValue(AnnotationMirror annotation, String attribute, Class<T> clazz) {
        if (clazz.isArray()) {
            Iterable<?> attributes = getAttributeValueInternal(annotation, attribute, Iterable.class);
            Collection<Object> coll = new ArrayList<Object>();

            for (Object internal : NbCollections.iterable(NbCollections.checkedIteratorByFilter(attributes.iterator(), clazz.getComponentType(), false))) {
                coll.add(internal);
            }

            return clazz.cast(coll.toArray((Object[]) Array.newInstance(clazz.getComponentType(), 0)));
        } else {
            return getAttributeValueInternal(annotation, attribute, clazz);
        }
    }

    private <T> T getAttributeValueInternal(AnnotationMirror annotation, String attribute, Class<T> clazz) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : processingEnv.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(attribute)) {
                Object value = e.getValue().getValue();

                if (clazz.isAssignableFrom(value.getClass())) {
                    return clazz.cast(value);
                }

                return null;
            }
        }
        
        return null;
    }

    private AnnotationValue getAttributeValueDescription(AnnotationMirror annotation, String attribute) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : processingEnv.getElementUtils().getElementValuesWithDefaults(annotation).entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(attribute)) {
                return e.getValue();
            }
        }

        return null;
    }
    
    private String hintId(Element hint) {
        AnnotationMirror hintMirror = findAnnotation(hint.getAnnotationMirrors(), "org.netbeans.spi.java.hints.Hint");

        if (hintMirror != null) {
            String id = getAttributeValue(hintMirror, "id", String.class);

            if (id != null && !id.isEmpty()) return id;
        }

        switch (hint.getKind()) {
            case CLASS:
                return ((TypeElement) hint).getQualifiedName().toString();
            case METHOD:
                TypeElement hintClass = (TypeElement) hint.getEnclosingElement();
                return hintClass.getQualifiedName() + "." + hint.getSimpleName();
            default:
                //compiler should have already warned about this
                return null;
        }
    }

    private boolean verifyHintAnnotationAcceptable(Element hint) {
        String id = hintId(hint);

        if (id == null) {
            return false;
        }

        return true;
    }

    private boolean verifyHintMethod(ExecutableElement method) {
        StringBuilder error = new StringBuilder();
        Elements elements = processingEnv.getElementUtils();
        TypeElement errDesc = elements.getTypeElement("org.netbeans.spi.editor.hints.ErrorDescription");
        TypeElement jlIterable = elements.getTypeElement("java.lang.Iterable");
        TypeElement hintCtx = elements.getTypeElement("org.netbeans.spi.java.hints.HintContext");

        if (errDesc == null || jlIterable == null || hintCtx == null) {
            return true;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror errDescType = errDesc.asType(); //no type params, no need to erasure
        TypeMirror jlIterableErrDesc = types.getDeclaredType(jlIterable, errDescType);
        TypeMirror ret = method.getReturnType();

        if (!types.isSameType(ret, errDescType) && !types.isAssignable(ret, jlIterableErrDesc)) {
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

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$[a-zA-Z0-9_]+");
    private boolean verifyTriggerAnnotations(ExecutableElement method) {
        List<AnnotationMirror> patternAnnotations = new ArrayList<AnnotationMirror>();
        AnnotationMirror am = findAnnotation(method.getAnnotationMirrors(), "org.netbeans.spi.java.hints.TriggerPattern");

        if (am != null) {
            patternAnnotations.add(am);
        }

        am = findAnnotation(method.getAnnotationMirrors(), "org.netbeans.spi.java.hints.TriggerPatterns");

        if (am != null) {
            patternAnnotations.addAll(Arrays.asList(getAttributeValue(am, "value", AnnotationMirror[].class)));
        }

        for (AnnotationMirror patternDescription : patternAnnotations) {
            String pattern = getAttributeValue(patternDescription, "value", String.class);

            if (pattern == null) continue;

            Set<String> variables = new HashSet<String>();
            Matcher m = VARIABLE_PATTERN.matcher(pattern);

            while (m.find()) {
                variables.add(m.group(0));
            }

            for (AnnotationMirror constraint : getAttributeValue(patternDescription, "constraints", AnnotationMirror[].class)) {
                String variable = getAttributeValue(constraint, "variable", String.class);
                String type = getAttributeValue(constraint, "type", String.class);

                if (variable == null || type == null) continue;

                if (!variables.contains(variable)) {
                    processingEnv.getMessager().printMessage(Kind.WARNING, "Variable " + variable + " not used in the pattern", method, constraint, getAttributeValueDescription(constraint, "variable"));
                }
            }
        }

        return false;
    }

    private boolean verifyOptionField(VariableElement field) {
        StringBuilder error = new StringBuilder();
        Elements elements = processingEnv.getElementUtils();
        TypeElement jlString = elements.getTypeElement("java.lang.String");

        if (jlString == null) {
            return true;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror jlStringType = jlString.asType(); //no type params, no need to erasure

        if (!types.isSameType(field.asType(), jlStringType)) {
            error.append(ERR_RETURN_TYPE);
            error.append("\n");
        }

        if (!field.getModifiers().contains(Modifier.STATIC) || !field.getModifiers().contains(Modifier.FINAL)) {
            error.append(ERR_OPTION_MUST_BE_STATIC_FINAL);
            error.append("\n");
        }

        Object key = field.getConstantValue();

        if (key == null) {
            error.append("Option field not a compile-time constant");
            error.append("\n");
        }

        if (error.length() == 0) {
            return true;
        }

        if (error.charAt(error.length() - 1) == '\n') {
            error.delete(error.length() - 1, error.length());
        }

        processingEnv.getMessager().printMessage(Kind.ERROR, error.toString(), field);

        return false;
    }

    private class DumpAnnotationValue extends AbstractAnnotationValueVisitor6<Void, Void> {

        private final LayerBuilder builder;
        private final File annotationFolder;
        private final String attrName;
        private final Element errElement;
        private final AnnotationMirror errAnnotationMirror;
        private final AnnotationValue errAnnotationValue;

        public DumpAnnotationValue(LayerBuilder builder, File annotationFolder, String attrName, Element errElement, AnnotationMirror errAnnotationMirror, AnnotationValue errAnnotationValue) {
            this.builder = builder;
            this.annotationFolder = annotationFolder;
            this.attrName = attrName;
            this.errElement = errElement;
            this.errAnnotationMirror = errAnnotationMirror;
            this.errAnnotationValue = errAnnotationValue;
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
            if ("displayName".equals(attrName) || "description".equals(attrName) || "tooltip".equals(attrName)) {
                try {
                    annotationFolder.bundlevalue(attrName, s);
                } catch (LayerGenerationException ex) {
                   processingEnv.getMessager().printMessage(Kind.ERROR, ex.getLocalizedMessage(), errElement, errAnnotationMirror, errAnnotationValue);
                }
            } else {
                annotationFolder.stringvalue(attrName, s);
            }
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
            
            dumpAnnotation(builder, f, errElement, a);

            f.write();
            return null;
        }

        public Void visitArray(List<? extends AnnotationValue> vals, Void p) {
            File arr = builder.folder(annotationFolder.getPath() + "/" + attrName);
            int c = 0;

            for (AnnotationValue av : vals) {
                av.accept(new DumpAnnotationValue(builder, arr, "item" + c, errElement, errAnnotationMirror, av), null);
                c++;
            }

            arr.write();

            return null;
        }
    }

}
