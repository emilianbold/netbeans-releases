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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.editor.parser;

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.Opcodes;
import java.io.IOException;
import java.security.CodeSource;
import java.util.concurrent.ExecutionException;
import javax.lang.model.element.TypeElement;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public final class NbCompilationUnit extends CompilationUnit {

    public NbCompilationUnit(CompilerConfiguration configuration, CodeSource security, GroovyClassLoader loader, JavaSource javaSource, boolean waitScanFinished) {
        super(configuration, security, loader);
        this.ast = new NbCompileUnit(this.classLoader, security, this.configuration, javaSource, waitScanFinished);
    }

    private static final class NbCompileUnit extends CompileUnit {

        private final JavaSource javaSource;
        private final boolean waitScanFinished;

        public NbCompileUnit(GroovyClassLoader classLoader, CodeSource codeSource, CompilerConfiguration config, JavaSource javaSource, boolean waitScanFinished) {
            super(classLoader, codeSource, config);
            this.javaSource = javaSource;
            this.waitScanFinished = waitScanFinished;
        }

        @Override
        public ClassNode getClass(final String name) {
            final ClassNode[] classNodes = new ClassNode[] { super.getClass(name) };
            if (classNodes[0] == null) {
                try {
                    Task<CompilationController> task = new Task<CompilationController>() {
                        public void run(CompilationController controller) throws Exception {
                            TypeElement typeElement = controller.getElements().getTypeElement(name);
                            if (typeElement != null) {
                                int modifiers = 0;
                                if (typeElement.getKind().isInterface()) {
                                    modifiers = Opcodes.ACC_INTERFACE;
                                }
                                classNodes[0] = new ClassNode(name, modifiers, null);
                            }
                        }
                    };
                    if (waitScanFinished) {
                        javaSource.runWhenScanFinished(task, true).get();
                    } else {
                        javaSource.runUserActionTask(task, true);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return classNodes[0];
        }

    }

}
