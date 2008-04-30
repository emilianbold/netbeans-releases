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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.QueryProgress;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.Public;
import org.netbeans.modules.spring.java.Static;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class JavaMethodCompletor extends Completor {

    @Override
    protected void computeCompletionItems(final CompletionContext context, QueryProgress progress) {
        try {
            final String classBinaryName = getTypeName(context);
            final Public publicFlag = getPublicFlag(context);
            final Static staticFlag = getStaticFlag(context);
            final int argCount = getArgCount(context);

            if (classBinaryName == null || classBinaryName.equals("")) { // NOI18N
                return;
            }

            final JavaSource javaSource = JavaUtils.getJavaSource(context.getFileObject());
            if (javaSource == null) {
                return;
            }

            javaSource.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement classElem = JavaUtils.findClassElementByBinaryName(classBinaryName, controller);
                    if (classElem == null) {
                        return;
                    }

                    ElementUtilities eu = controller.getElementUtilities();
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {

                        public boolean accept(Element e, TypeMirror type) {
                            // XXX : display methods of java.lang.Object? 
                            // Displaying them adds unnecessary clutter in the completion window
                            if (e.getKind() == ElementKind.METHOD) {
                                TypeElement te = (TypeElement) e.getEnclosingElement();
                                if (te.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                                    return false;
                                }

                                // match name
                                if (!e.getSimpleName().toString().startsWith(context.getTypedPrefix())) {
                                    return false;
                                }

                                ExecutableElement method = (ExecutableElement) e;
                                // match argument count
                                if (argCount != -1 && method.getParameters().size() != argCount) {
                                    return false;
                                }

                                // match static
                                if (staticFlag != Static.DONT_CARE) {
                                    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                                    if ((isStatic && staticFlag == Static.NO) || (!isStatic && staticFlag == Static.YES)) {
                                        return false;
                                    }
                                }

                                // match public
                                if (publicFlag != Public.DONT_CARE) {
                                    boolean isPublic = method.getModifiers().contains(Modifier.PUBLIC);
                                    if ((isPublic && publicFlag == Public.NO) || (!isPublic && publicFlag == Public.YES)) {
                                        return false;
                                    }
                                }

                                return true;
                            }

                            return false;
                        }
                    };

                    int substitutionOffset = context.getCurrentToken().getOffset() + 1;
                    Iterable<? extends Element> methods = eu.getMembers(classElem.asType(), acceptor);

                    methods = filter(methods);

                    for (Element e : methods) {
                        SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createMethodItem(
                                substitutionOffset, (ExecutableElement) e, e.getEnclosingElement() != classElem,
                                controller.getElements().isDeprecated(e));
                        addItem(item);
                    }

                    setAnchorOffset(substitutionOffset);

                }
            }, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Should the method be public
     */
    protected abstract Public getPublicFlag(CompletionContext context);

    /**
     * Should the method be static
     */
    protected abstract Static getStaticFlag(CompletionContext context);

    /**
     * Number of arguments of the method
     */
    protected abstract int getArgCount(CompletionContext context);

    /**
     * Binary name of the class which should be searched for methods
     */
    protected abstract String getTypeName(CompletionContext context);

    /**
     * Post process applicable methods, for eg. return only those
     * methods which return do not return void
     */
    protected Iterable<? extends Element> filter(Iterable<? extends Element> methods) {
        return methods;
    }
}
