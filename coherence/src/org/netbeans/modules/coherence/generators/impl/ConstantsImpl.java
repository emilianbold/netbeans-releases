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
import org.netbeans.modules.coherence.generators.Constants;
import org.netbeans.modules.coherence.generators.NullParameterException;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
public final class ConstantsImpl implements Constants {

    private static final Logger logger = Logger.getLogger(ConstantsImpl.class.getCanonicalName());
    public final static String CONSTANTS_JAVADOC = "Index Constants used to access the variables in the Read / Write methods";
    private GeneratorFactory factory = null;

    public ConstantsImpl(GeneratorFactory factory) {
        this.factory = factory;
    }

    protected ConstantsImpl() {
        this.factory = GeneratorFactory.getInstance();
    }

    @Override
    public boolean isConstantsPresent(WorkingCopy workingCopy, ClassTree classTree) throws NullParameterException {
        boolean present = false;
        StringBuilder sbConstants = new StringBuilder("");

        if (workingCopy != null && classTree != null) {
            Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree));
            if (classElement != null) {
                TypeElement te = (TypeElement) classElement;
                List enclosedElements = te.getEnclosedElements();
                for (int i = 0; i < enclosedElements.size(); i++) {
                    Element enclosedElement = (Element) enclosedElements.get(i);
                    if (enclosedElement.getKind() == ElementKind.FIELD && !factory.isSerializable((VariableElement) enclosedElement)) {
                        sbConstants.append("[").append(enclosedElement.getSimpleName().toString()).append("]");
                    }
                }
                for (int i = 0; i < enclosedElements.size(); i++) {
                    Element enclosedElement = (Element) enclosedElements.get(i);
                    if (enclosedElement.getKind() == ElementKind.FIELD && factory.isSerializable((VariableElement) enclosedElement)) {
                        if (sbConstants.indexOf(factory.generateConstantName(enclosedElement.getSimpleName().toString())) >= 0) {
                            present = true;
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

        return present;
    }

    @Override
    public ClassTree removeConstants(WorkingCopy workingCopy, ClassTree modifiedClassTree, ClassTree origClassTree) throws NullParameterException {
        if (workingCopy != null && modifiedClassTree != null && origClassTree != null) {
            TreeMaker make = workingCopy.getTreeMaker();
            List<Tree> membersList = (List<Tree>) modifiedClassTree.getMembers();
            VariableTree tree = null;

            // Add the Constants used for index into the Read & Write Methods
            List<VariableTree> constants = generateIdxConstants(workingCopy, origClassTree);
            for (VariableTree vt : constants) {
                for (Tree t : membersList) {
                    if (t.getKind() == Kind.VARIABLE) {
                        tree = (VariableTree) t;
                        if (tree.getName().toString().equals(vt.getName().toString())) {
                            modifiedClassTree = make.removeClassMember(modifiedClassTree, tree);
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
    public ClassTree addConstants(WorkingCopy workingCopy, ClassTree modifiedClassTree, ClassTree origClassTree) throws NullParameterException {
        if (workingCopy != null && modifiedClassTree != null && origClassTree != null) {
            TreeMaker make = workingCopy.getTreeMaker();
            // Add the Constants used for index into the Read & Write Methods
            List<VariableTree> constants = generateIdxConstants(workingCopy, origClassTree);
            for (VariableTree vt : constants) {
                modifiedClassTree = make.addClassMember(modifiedClassTree, vt);
            }
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }
        return modifiedClassTree;
    }

    protected List<VariableTree> generateIdxConstants(WorkingCopy workingCopy, ClassTree clazz) {
        List<VariableTree> idxConstants = new ArrayList<VariableTree>();
        TreeMaker make = workingCopy.getTreeMaker();
        Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));

        if (classElement == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "*** APH-I1 : generateIdxConstants() Cannot resolve class!");
            }
        } else {
            Set<Modifier> modifiers = new TreeSet<Modifier>();
            modifiers.add(Modifier.PUBLIC);
            modifiers.add(Modifier.STATIC);
            modifiers.add(Modifier.FINAL);
            ModifiersTree paramModifiers = make.Modifiers(modifiers);
            VariableTree parameter = null;
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            int idx = 0;
            String constantName = null;
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "*** APH-I2 : generateIdxConstants() Element {0} Type {1} ",
                            new Object[]{enclosedElement.getSimpleName(), enclosedElement.getKind()});
                }

                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    if (factory.isSerializable((VariableElement) enclosedElement)) {
                        constantName = factory.generateConstantName(enclosedElement.getSimpleName().toString());
                        parameter =
                                make.Variable(
                                paramModifiers,
                                constantName,
                                make.PrimitiveType(TypeKind.INT),
                                make.Literal(idx));
                        if (idx == 0) {
                            make.addComment(parameter, Comment.create(Comment.Style.LINE, CONSTANTS_JAVADOC), true);
                        }
                        idxConstants.add(parameter);

                        idx++;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "*** APH-I3 : generateIdxConstants() Adding Parameter ".concat(parameter.toString()));
                        }
                    }
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "*** APH-I1 : generateIdxConstants() Returning {0} Constants {1}",
                    new Object[]{new Integer(idxConstants.size()), idxConstants.toString()});
        }
        return idxConstants;
    }
}
