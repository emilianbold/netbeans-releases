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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 *
 * @author nam
 */
public class TypeUtil {

    public static final String AN_KEY_NAME = "name";

    public static List<Annotation> getClassAnnotations(Class type) {
        List<Annotation> result = new ArrayList<Annotation>(Arrays.asList(type.getAnnotations()));
        return result;
    }
    
    public static boolean isXmlRoot(Class type) {
        for (Annotation ann : type.getAnnotations()) {
            String annType = ann.annotationType().getName();
            if (annType.equals(Constants.XML_ROOT_ELEMENT)) {
                return true;
            }
        }
        return false;
    }

    public static Annotation getXmlElementAnnotation(Method m) {
        for (Annotation ann : m.getAnnotations()) {
            if (ann.annotationType().getName().equals(Constants.XML_ELEMENT)) {
                return ann;
            }
        }
        return null;
    }

    public static Annotation getXmlAttributeAnnotation(Field f) {
        for (Annotation ann : f.getAnnotations()) {
            if (ann.annotationType().getName().equals(Constants.XML_ATTRIBUTE)) {
                return ann;
            }
        }
        return null;
    }
    
    public static Annotation getXmlAttributeAnnotation(Method m) {
        for (Annotation ann : m.getAnnotations()) {
            if (ann.annotationType().getName().equals(Constants.XML_ATTRIBUTE)) {
                return ann;
            }
        }
        return null;
    }

    public static Annotation getXmlElementAnnotation(Field f) {
        for (Annotation ann : f.getAnnotations()) {
            if (ann.annotationType().getName().equals(Constants.XML_ELEMENT)) {
                return ann;
            }
        }
        return null;
    }

    public static List<Annotation> getFieldAnnotations(Class type) {
        List<Annotation> result = new ArrayList<Annotation>();
        for (Field f : type.getDeclaredFields()) {
            result.addAll(Arrays.asList(f.getAnnotations()));
        }
        return result;
    }

    public static List<Annotation> getMethodAnnotations(Class type) {
        List<Annotation> result = new ArrayList<Annotation>();
        for (Method m : type.getDeclaredMethods()) {
            result.addAll(Arrays.asList(m.getAnnotations()));
        }
        return result;
    }

    public static List<Annotation> getAnnotations(Class type, boolean deep) {
        List<Annotation> result = getFieldAnnotations(type);
        result.addAll(getMethodAnnotations(type));
        result.addAll(getClassAnnotations(type));
        if (deep) {
            for (Class c : type.getDeclaredClasses()) {
                result.addAll(getAnnotations(c, deep));
            }
        }
        return result;
    }

    public static Map<String, String> getSimpleAnnotationValues(Annotation annotation) {
        Map<String, String> result = new HashMap<String, String>();
        String value = annotation.toString();
        int begin = value.indexOf('(') + 1;
        int end = value.lastIndexOf(')');
        value = value.substring(begin, end);
        String[] pairs = value.split(",");
        for (String s : pairs) {
            String pair[] = s.split("=");
            if (pair.length == 2) {
                result.put(pair[0], pair[1]);
            }
        }
        return result;
    }

    public static String getAnnotationValueName(Annotation annotation) {
        return getSimpleAnnotationValues(annotation).get(AN_KEY_NAME);
    }

    public static String getAnnotationValueName(CompilationController controller, TypeElement classElement, TypeElement annotationElement) {
        List<? extends AnnotationMirror> annotations = classElement.getAnnotationMirrors();
        for (AnnotationMirror anMirror : annotations) {
            if (controller.getTypes().isSameType(annotationElement.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: expressions.entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("name")) { //NOI18N
                        return (String) expressions.get(entry.getKey()).getValue();
                    }
                }
            }
        }
        return null;
    }

    public static String getQualifiedClassName(String simpleName, JavaSource context) throws IOException {
        final String suffix = "." + simpleName;
        final String[] found = new String[1];

        context.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.RESOLVED);
                for (String imp : JavaSourceHelper.getImports(controller)) {
                    if (imp.endsWith(suffix)) {
                        found[0] = imp;
                        break;
                    }
                }
            }
        }, true);

        return found[0];
    }

    public static Class getClass(String name, JavaSource context, Project project) throws IOException {
        if (name.indexOf('.') > 0) {
            return Util.getType(project, name);
        } else {
            String qualifiedName = getQualifiedClassName(name, context);
            if (qualifiedName != null) {
                return Util.getType(project, qualifiedName);
            }
        }
        return null;
    }

    public static Annotation getJpaTableAnnotation(Class c) {
        for (Annotation ann : c.getAnnotations()) {
            if (ann.annotationType().getName().equals(Constants.PERSISTENCE_TABLE)) {
                return ann;
            }
        }
        return null;
    }

    public static Annotation getJpaEntityAnnotation(Class c) {
        for (Annotation ann : c.getAnnotations()) {
            if (ann.annotationType().getName().equals(Constants.PERSISTENCE_ENTITY)) {
                return ann;
            }
        }
        return null;
    }
}
