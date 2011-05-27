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
import org.netbeans.modules.coherence.generators.Implements;
import org.netbeans.modules.coherence.generators.NullParameterException;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public final class ImplementsImpl implements Implements {
    private static final Logger logger = Logger.getLogger(ImplementsImpl.class.getCanonicalName());
    public final static String PORTABLEOBJECT_CLASS = "com.tangosol.io.pof.PortableObject";
    private GeneratorFactory factory = null;

    public ImplementsImpl(GeneratorFactory factory) {
        this.factory = factory;
    }

    protected ImplementsImpl() {
        this.factory = GeneratorFactory.getInstance();
    }
    
    @Override
    public boolean isImplementsPresent(WorkingCopy workingCopy, ClassTree classTree) throws NullParameterException {
        boolean implementsInterface = false;

        if (workingCopy != null && classTree != null) {
            Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree));
            if (classElement != null) {
                TypeElement te = (TypeElement) classElement;
                List<TypeMirror> implementsList = (List<TypeMirror>) te.getInterfaces();
                for (TypeMirror tm : implementsList) {
                    if (PORTABLEOBJECT_CLASS.equals(tm.toString())) {
                        implementsInterface = true;
                        break;
                    }
                }
            } else {
                StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
            }
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }

        return implementsInterface;
    }

    @Override
    public ClassTree removeImplements(WorkingCopy workingCopy, ClassTree modifiedClassTree, ClassTree origClassTree) throws NullParameterException {
        if (workingCopy != null && modifiedClassTree != null && origClassTree != null) {
            TreeMaker make = workingCopy.getTreeMaker();
            List<Tree> implementsList = (List<Tree>) modifiedClassTree.getImplementsClause();
            Element el = null;
            for (Tree t : implementsList) {
                el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), t));
                if (PORTABLEOBJECT_CLASS.equals(el.asType().toString())) {
                    modifiedClassTree = make.removeClassImplementsClause(modifiedClassTree, t);
                    break;
                }
            }
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }
        return modifiedClassTree;
    }

    @Override
    public ClassTree addImplements(WorkingCopy workingCopy, ClassTree modifiedClassTree, ClassTree origClassTree) throws NullParameterException {
        if (workingCopy != null && modifiedClassTree != null && origClassTree != null) {
            TreeMaker make = workingCopy.getTreeMaker();
            TypeElement element = null;
            ExpressionTree implementsClause = null;

            // Create the implements for PortableObject
            element = workingCopy.getElements().getTypeElement(PORTABLEOBJECT_CLASS);
            implementsClause = make.QualIdent(element);
            modifiedClassTree = make.addClassImplementsClause(modifiedClassTree, implementsClause);
        } else {
            throw new NullParameterException("Null Parameters are not allowed");
        }
        return modifiedClassTree;
    }
}
