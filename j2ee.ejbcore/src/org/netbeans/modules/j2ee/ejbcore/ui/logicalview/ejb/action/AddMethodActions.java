/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.SendEmailCodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Action that can always be invoked and work procedurally.
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddMethodActions implements CodeGenerator {

    public static abstract class AbstractFactory implements CodeGenerator.Factory {

        private AbstractAddMethodStrategy strategy;

        private AbstractFactory(AbstractAddMethodStrategy strategy) {
            this.strategy = strategy;
        }

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? SendEmailCodeGenerator.getPathElementOfKind(Tree.Kind.CLASS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element elem = controller.getTrees().getElement(path);
                if (elem != null) {
                    if (elem.getKind() != ElementKind.CLASS)
                        return ret;
                    TypeElement typeElement = (TypeElement)elem;
                    if (!isEnable(strategy, controller.getFileObject(), typeElement)) {
                        return ret;
                    }
                    ret.add(new AddMethodActions(strategy, controller.getFileObject(), typeElement));
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return ret;
        }
    }

    public static class AddBusinessMethodCodeGenerator extends AbstractFactory {
        public AddBusinessMethodCodeGenerator() {
            super(new AddBusinessMethodStrategy());
        }
    }

    public static class AddCreateMethodCodeGenerator extends AbstractFactory {
        public AddCreateMethodCodeGenerator() {
            super(new AddCreateMethodStrategy());
        }
    }

    public static class AddFinderMethodCodeGenerator extends AbstractFactory {
        public AddFinderMethodCodeGenerator() {
            super(new AddFinderMethodStrategy());
        }
    }

    public static class AddHomeMethodCodeGenerator extends AbstractFactory {
        public AddHomeMethodCodeGenerator() {
            super(new AddHomeMethodStrategy());
        }
    }

    public static class AddSelectMethodCodeGenerator extends AbstractFactory {
        public AddSelectMethodCodeGenerator() {

            super(new AddSelectMethodStrategy());
        }
    }


    /** Action context. */
    private FileObject fileObject;
    private TypeElement beanClass;
    private final AbstractAddMethodStrategy strategy;

    public AddMethodActions(AbstractAddMethodStrategy strategy, FileObject fileObject, TypeElement beanClass) {
        this.fileObject = fileObject;
        this.beanClass = beanClass;
        this.strategy = strategy;
    }

    public String getDisplayName(){
        return strategy.getTitle();
    }
    
    public static boolean isEnable(AbstractAddMethodStrategy strategy, FileObject fileObject, TypeElement elementHandle) {
        return strategy.supportsEjb(fileObject, elementHandle.getQualifiedName().toString());
    }
    
    public void invoke() {
        if (strategy.supportsEjb(fileObject, beanClass.getQualifiedName().toString())) {
            try {
                strategy.addMethod(fileObject, beanClass.getQualifiedName().toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
