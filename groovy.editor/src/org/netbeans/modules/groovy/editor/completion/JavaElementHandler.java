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

package org.netbeans.modules.groovy.editor.completion;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.groovy.editor.api.completion.GroovyCompletionHandler;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class JavaElementHandler {

    private static final Logger LOG = Logger.getLogger(GroovyElementHandler.class.getName());

    private final CompilationInfo info;

    private JavaElementHandler(CompilationInfo info) {
        this.info = info;
    }

    public static JavaElementHandler forCompilationInfo(CompilationInfo info) {
        return new JavaElementHandler(info);
    }

    public Map<MethodSignature, ? extends CompletionItem> getMethods(String className,
            String prefix, int anchor, String[] typeParameters, ClassType type) {
        JavaSource javaSource = createJavaSource();

        if (javaSource == null) {
            return Collections.emptyMap();
        }

        CountDownLatch cnt = new CountDownLatch(1);

        Map<MethodSignature, CompletionItem> result = Collections.synchronizedMap(new HashMap<MethodSignature, CompletionItem>());
        try {
            javaSource.runUserActionTask(new MethodCompletionHelper(cnt, javaSource, className, typeParameters,
                    Collections.singleton(AccessLevel.PUBLIC), prefix, anchor, result), true);
        } catch (IOException ex) {
            LOG.log(Level.FINEST, "Problem in runUserActionTask :  {0}", ex.getMessage());
            return Collections.emptyMap();
        }

        try {
            cnt.await();
        } catch (InterruptedException ex) {
            LOG.log(Level.FINEST, "InterruptedException while waiting on latch :  {0}", ex.getMessage());
            return Collections.emptyMap();
        }
        return result;
    }

    private JavaSource createJavaSource() {
        FileObject fileObject = info.getFileObject();
        if (fileObject == null) {
            return null;
        }

        // get the JavaSource for our file.
        JavaSource javaSource = JavaSource.create(ClasspathInfo.create(fileObject));

        if (javaSource == null) {
            LOG.log(Level.FINEST, "Problem retrieving JavaSource from ClassPathInfo, exiting.");
            return null;
        }

        return javaSource;
    }

    public static enum ClassType {

        CLASS,

        SUPERCLASS,

        SUPERINTERFACE
    }

    private static enum AccessLevel {

        PUBLIC {
            @Override
            public ElementAcceptor getAcceptor() {
                return new ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        return e.getModifiers().contains(Modifier.PUBLIC);
                    }
                };
            }
        },

        PACKAGE {
            @Override
            public ElementAcceptor getAcceptor() {
                return new ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        Set<Modifier> modifiers = e.getModifiers();
                        return !modifiers.contains(Modifier.PUBLIC)
                                && !modifiers.contains(Modifier.PROTECTED)
                                && !modifiers.contains(Modifier.PRIVATE);
                    }
                };
            }
        },

        PROTECTED {
            @Override
            public ElementAcceptor getAcceptor() {
                return new ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        return e.getModifiers().contains(Modifier.PROTECTED);
                    }
                };
            }
        },

        PRIVATE {
            @Override
            public ElementAcceptor getAcceptor() {
                return new ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        return e.getModifiers().contains(Modifier.PRIVATE);
                    }
                };
            }
        };

        public abstract ElementAcceptor getAcceptor();
    }

    private static class MethodCompletionHelper implements Task<CompilationController> {

        private final CountDownLatch cnt;

        private final JavaSource javaSource;

        private final String className;

        private final String[] typeParameters;

        private final Set<AccessLevel> levels;

        private final String prefix;

        private final int anchor;

        private final Map<MethodSignature, CompletionItem> proposals;

        public MethodCompletionHelper(CountDownLatch cnt, JavaSource javaSource, String className,
                String[] typeParameters, Set<AccessLevel> levels, String prefix, int anchor,
                Map<MethodSignature, CompletionItem> proposals) {

            this.cnt = cnt;
            this.javaSource = javaSource;
            this.className = className;
            this.typeParameters = typeParameters;
            this.levels = levels;
            this.prefix = prefix;
            this.anchor = anchor;
            this.proposals = proposals;
        }

        public void run(CompilationController info) throws Exception {

            Elements elements = info.getElements();
            if (elements != null) {
                ElementAcceptor acceptor = new ElementAcceptor() {

                    public boolean accept(Element e, TypeMirror type) {
                        if (e.getKind() != ElementKind.METHOD) {
                            return false;
                        }
                        for (AccessLevel level : levels) {
                            if (level.getAcceptor().accept(e, type)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };

                TypeElement te = elements.getTypeElement(className);
                if (te != null) {
                    for (ExecutableElement element : ElementFilter.methodsIn(te.getEnclosedElements())) {
                        if (!acceptor.accept(element, te.asType())) {
                            continue;
                        }

                        String simpleName = element.getSimpleName().toString();
                        String parameterString = GroovyCompletionHandler.getParameterListForMethod((ExecutableElement) element);
                        // FIXME this should be more accurate
                        TypeMirror returnType = ((ExecutableElement) element).getReturnType();

                        if (simpleName.toUpperCase(Locale.ENGLISH).startsWith(prefix.toUpperCase(Locale.ENGLISH))) {
                            if (LOG.isLoggable(Level.FINEST)) {
                                LOG.log(Level.FINEST, simpleName + " " + parameterString + " " + returnType.toString());
                            }

                            proposals.put(getSignature(te, element, typeParameters, info.getTypes()), new CompletionItem.JavaMethodItem(simpleName, parameterString,
                                    returnType, element.getModifiers(), anchor));
                        }
                    }
                }
            }

            cnt.countDown();
        }

        private MethodSignature getSignature(TypeElement classElement, ExecutableElement element, String[] typeParameters, Types types) {
            String name = element.getSimpleName().toString();
            String[] parameters = new String[element.getParameters().size()];

            for (int i = 0; i < parameters.length; i++) {
                VariableElement var = element.getParameters().get(i);
                TypeMirror type = var.asType();
                String typeString = null;

                if (type.getKind() == TypeKind.TYPEVAR) {
                    List<? extends TypeParameterElement> declaredTypeParameters = element.getTypeParameters();
                    if (declaredTypeParameters.isEmpty()) {
                        declaredTypeParameters = classElement.getTypeParameters();
                    }
                    int j = -1;
                    for (TypeParameterElement typeParam : declaredTypeParameters) {
                        j++;
                        if (typeParam.getSimpleName().toString().equals(type.toString())) {
                            break;
                        }
                    }
                    if (j >= 0 && j < typeParameters.length) {
                        typeString = typeParameters[j];
                    } else {
                        typeString = types.erasure(type).toString();
                    }
                } else {
                    typeString = type.toString();
                }

                int index = typeString.indexOf('<');
                if (index >= 0) {
                    typeString = typeString.substring(0, index);
                }
                parameters[i] = typeString;
            }
            return new MethodSignature(name, parameters);
        }
    }
}
