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
package org.netbeans.modules.profiler.selector.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.profiler.selector.java.nodes.JavaClassNode;
import org.netbeans.modules.profiler.selector.spi.SelectionTreeBuilder;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Bachorik
 */
public class SingleFileSelectionTreeBuilder extends SelectionTreeBuilder {

    private FileObject file;

    public SingleFileSelectionTreeBuilder(FileObject file) {
        super(new Type("single-file", "Single File"), false);
        this.file = file;
    }

    @Override
    public List<SelectorNode> buildSelectionTree() {
        final SelectorNode[] classNode = new SelectorNode[1];
        JavaSource js = JavaSource.forFileObject(file);
        if (js != null) {
            try {
                js.runWhenScanFinished(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        TypeElement classElement = getToplevelClass(controller);
                        classNode[0] = new JavaClassNode(controller.getClasspathInfo(), classElement, null);
                    }
                }, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classNode[0] != null ? Collections.singletonList(classNode[0]) : Collections.EMPTY_LIST;
    }

    @Override
    public int estimatedNodeCount() {
        return 1;
    }

    private static TypeElement getToplevelClass(final CompilationController controller) throws IOException {
        // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
        if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
            return null;
        }

        TreePathScanner<TypeElement, Void> scanner = new TreePathScanner<TypeElement, Void>() {
            public TypeElement visitClass(ClassTree node, Void p) {
                try {
                    return (TypeElement)controller.getTrees().getElement(getCurrentPath());
                } catch (NullPointerException e) {
                    Exceptions.printStackTrace(e);
                    return null;
                }
            }
        };

        return scanner.scan(controller.getCompilationUnit(), null);
    }
}
