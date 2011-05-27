/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.generators.impl;

import org.netbeans.modules.coherence.GeneratorFactory;
import org.netbeans.modules.coherence.generators.NullParameterException;
import org.netbeans.modules.coherence.generators.WriteExternal;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public final class WriteExternalImpl implements WriteExternal {

    private static final Logger logger = Logger.getLogger(WriteExternalImpl.class.getCanonicalName());
    public final static String POFWRITER_CLASS = "com.tangosol.io.pof.PofWriter";
    public final static String WRITE_VAR_NAME = "writer";
    public final static String WRITE_EXTERNAL = "writeExternal";
    public final static String WRITE_JAVADOC = "Handles POF Write Serialization\n@param writer POF Writer\n@throws IOException if an I/O error occurs\n";

    GeneratorFactory factory = null;

    public WriteExternalImpl(GeneratorFactory factory) {
        this.factory = factory;
    }

    protected WriteExternalImpl() {
        this.factory = GeneratorFactory.getInstance();
    }

    @Override
    public boolean isWriteExternalPresent(WorkingCopy workingCopy, ClassTree classTree) throws NullParameterException {
        boolean method = false;

        if (workingCopy != null && classTree != null) {
            Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree));
            if (classElement != null) {
                TypeElement te = (TypeElement) classElement;
                List enclosedElements = te.getEnclosedElements();
                List<VariableElement> parameters = null;
                for (int i = 0; i < enclosedElements.size(); i++) {
                    Element enclosedElement = (Element) enclosedElements.get(i);
                    if (enclosedElement.getKind() == ElementKind.METHOD && WRITE_EXTERNAL.equals(enclosedElement.getSimpleName().toString())) {
                        parameters = (List<VariableElement>) ((ExecutableElement) enclosedElement).getParameters();
                        if (!parameters.isEmpty() && WRITE_VAR_NAME.equals(parameters.get(parameters.size() - 1).getSimpleName().toString())) {
                            method = true;
                            break;
                        }
                    }
                }
            } else {
                StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
            }
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }

        return method;
    }

    @Override
    public ClassTree removeWriteExternal(WorkingCopy workingCopy, ClassTree modifiedClassTree, ClassTree origClassTree) throws NullParameterException {
        if (workingCopy != null && modifiedClassTree != null && origClassTree != null) {
            TreeMaker make = workingCopy.getTreeMaker();
            List<Tree> membersList = (List<Tree>) modifiedClassTree.getMembers();
            MethodTree tree = null;
            List<VariableTree> parameters = null;

            /*
             * Loop through the class members looking for Methods.
             */
            for (Tree t : membersList) {
                if (t.getKind() == Kind.METHOD) {
                    tree = (MethodTree) t;
                    if (WRITE_EXTERNAL.equals(tree.getName().toString())) {
                        parameters = (List<VariableTree>) tree.getParameters();
                        /* Check to see if the first parameter of the method is
                         * WRITE_VAR_NAME. If so then this is the WriteExternal Method
                         * that was generated in a previous invocation.
                         */
                        if (!parameters.isEmpty() && parameters.size() == 1 && parameters.get(0).getName().toString().equals(WRITE_VAR_NAME)) {
                            modifiedClassTree = make.removeClassMember(modifiedClassTree, tree);
                            break;
                        }
                    }
                }
            }
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }
        return modifiedClassTree;
    }

    @Override
    public ClassTree addWriteExternal(WorkingCopy workingCopy, ClassTree modifiedClassTree, ClassTree origClassTree) throws NullParameterException {
        if (workingCopy != null && modifiedClassTree != null && origClassTree != null) {
            TreeMaker make = workingCopy.getTreeMaker();
            ModifiersTree methodModifiers = null;
            MethodTree newMethod = null;
            TypeElement element = null;
            VariableTree parameter = null;
            ExpressionTree throwsClause = null;
            StringBuilder sbComment = new StringBuilder(WRITE_JAVADOC);

            // Add Read External Method
            methodModifiers =
                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                    Collections.<AnnotationTree>emptyList());
            element = workingCopy.getElements().getTypeElement(POFWRITER_CLASS);
            parameter =
                    make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                    WRITE_VAR_NAME,
                    make.QualIdent(element),
                    null);
            element = workingCopy.getElements().getTypeElement("java.io.IOException");
            throwsClause = make.QualIdent(element);
            newMethod =
                    make.Method(methodModifiers,
                    WRITE_EXTERNAL,
                    make.PrimitiveType(TypeKind.VOID),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.singletonList(parameter),
                    Collections.<ExpressionTree>singletonList(throwsClause),
                    generateWriteExternalBodyStatements(workingCopy, origClassTree),
                    null);

            make.addComment(newMethod, Comment.create(Comment.Style.JAVADOC, sbComment.toString().replace("{CLASS}", modifiedClassTree.getSimpleName())), true);

            // Modify the working copy
            modifiedClassTree = make.addClassMember(modifiedClassTree, newMethod);
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }
        return modifiedClassTree;
    }

    protected BlockTree generateWriteExternalBodyStatements(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));
        List<StatementTree> statements = new ArrayList<StatementTree>();
        String methodName = null;
        List<ExpressionTree> arguments = null;
        ExpressionTree expression = null;
        MethodInvocationTree invocation = null;
        boolean isSQLDate = false;

        if (classElement == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        } else {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            int idx = 0;
            VariableElement ve = null;
            Element enclosedElement = null;
            for (Object obj : enclosedElements) {
                enclosedElement = (Element) obj;
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    ve = (VariableElement) enclosedElement;
                    if (factory.isSerializable(ve)) {
                        String typeName = ve.asType().toString();

                        arguments = new ArrayList<ExpressionTree>();
                        arguments.add(make.Identifier(factory.generateConstantName(enclosedElement.getSimpleName().toString())));
                        methodName = "writeObject";
                        isSQLDate = false;

                        if (typeName != null) {
                            if (ve.asType().getKind().isPrimitive()) {
                                String methodType = factory.initCaps(typeName);
                                methodType = factory.initCaps(typeName);
                                methodName = "write" + methodType;
                            } else if (((VariableElement) enclosedElement).asType().getKind().equals(TypeKind.ARRAY)) {
                                String methodType = factory.initCaps(typeName);
                                int pos = typeName.lastIndexOf(".");
                                int openPos = typeName.indexOf("[]");
                                if (pos < 0) {
                                    // Primative Array
                                    methodType = factory.initCaps(typeName.substring(0, openPos));
                                    methodName = "write" + methodType + "Array";
                                } else {
                                    methodName = "writeObjectArray";
                                }
                            } else {
                                try {
                                    if (factory.isString(ve)) {
                                        methodName = "writeString";
                                    } else if (factory.isBigDecimal(ve)) {
                                        methodName = "writeBigDecimal";
                                    } else if (factory.isBigInteger(ve)) {
                                        methodName = "writeBigInteger";
                                    } else if (factory.isDate(ve)) {
                                        methodName = "writeDate";
                                    } else if (factory.isSQLDate(ve)) {
                                        methodName = "writeDate";
                                        isSQLDate = true;
                                    } else if (factory.isBinary(ve)) {
                                        methodName = "writeBinary";
                                    } else if (factory.isMap(ve)) {
                                        methodName = "writeMap";
                                    } else if (factory.isCollection(ve)) {
                                        methodName = "writeCollection";
                                    } else {
                                        methodName = "writeObject";
                                    }
                                } catch (Exception ex) {
                                    // We can't find the Class in the Classpath so we will treat it as an object
                                    methodName = "writeObject";
                                }
                            }
                        }

                        idx++;
                        // Build Assignment and add to Statement Tree
                        if (isSQLDate) {
                            arguments.add(
                                    make.NewClass(null, Collections.<ExpressionTree>emptyList(),
                                    make.Identifier("java.util.Date"),
                                    Collections.<ExpressionTree>singletonList(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(enclosedElement), "getTime"), Collections.<ExpressionTree>emptyList())),
                                    null));
                        } else {
                            arguments.add(make.Identifier(enclosedElement));
                        }

                        expression = make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                make.MemberSelect(make.Identifier("writer"), methodName),
                                arguments);

                        statements.add(make.ExpressionStatement(expression));
                    }
                }
            }
        }

        return make.Block(statements, false);
    }

}
