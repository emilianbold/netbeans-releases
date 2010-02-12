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
package org.netbeans.modules.profiler.selector.java.nodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
import org.netbeans.modules.profiler.selector.spi.nodes.MethodNode;
import org.netbeans.modules.profiler.selector.spi.nodes.MethodsNode;
import org.netbeans.modules.profiler.selector.spi.nodes.Modifier;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavaMethodNode extends MethodNode {

    private ClasspathInfo cpInfo;
    private SourceCodeSelection signature;
    private Set<Modifier> modifiers;

    public JavaMethodNode(ClasspathInfo cpInfo, final ExecutableElement method, MethodsNode parent) {
        super(method.getSimpleName().toString(), parent);
        this.cpInfo = cpInfo;

        final String[] signatureString = new String[1];
        JavaSource js = JavaSource.create(cpInfo, new org.openide.filesystems.FileObject[0]);

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController controller)
                        throws Exception {
                    signatureString[0] = SourceUtils.getVMMethodSignature(method, controller);
                }
            }, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (signatureString[0] != null) {
            signature = new SourceCodeSelection(ElementUtilities.getBinaryName(getEnclosingClass(method)),
                    method.getSimpleName().toString(), signatureString[0]);
            modifiers = new HashSet<Modifier>();

            for (javax.lang.model.element.Modifier modifier : method.getModifiers()) {
                switch (modifier) {
                    case STATIC:
                        modifiers.add(Modifier.STATIC);
                        break;
                    case PUBLIC:
                        modifiers.add(Modifier.PUBLIC);
                        break;
                    case PROTECTED:
                        modifiers.add(Modifier.PROTECTED);
                        break;
                    case PRIVATE:
                        modifiers.add(Modifier.PRIVATE);
                        break;
                }
            }
        }
    }

    @Override
    protected Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public SourceCodeSelection getSignature() {
        return signature;
    }

    private TypeElement getEnclosingClass(Element element) {
        Element parent = element.getEnclosingElement();

        if (parent != null) {
            if ((parent.getKind() == ElementKind.CLASS) || (parent.getKind() == ElementKind.ENUM)) {
                return (TypeElement) parent;
            } else {
                return getEnclosingClass(parent);
            }
        }

        return null;
    }
}
