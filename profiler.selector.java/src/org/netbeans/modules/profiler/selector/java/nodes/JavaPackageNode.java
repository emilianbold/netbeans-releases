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
package org.netbeans.modules.profiler.selector.java.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.modules.profiler.selector.spi.nodes.PackageNode;
import org.netbeans.modules.profiler.selector.spi.nodes.ClassNode;
import org.netbeans.modules.profiler.selector.spi.nodes.ContainerNode;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JavaPackageNode extends PackageNode {
    private ClasspathInfo cpInfo;
    private Set<SearchScope> scope;
    private SourceCodeSelection signature;

    public JavaPackageNode(ClasspathInfo cpInfo, String name, ContainerNode parent, Set<SearchScope> scope) {
        super(name, parent);
        this.cpInfo = cpInfo;
        this.scope = scope;

        this.signature = new SourceCodeSelection(name + ".**", null, null); // NOI18N
    }

    @Override
    protected List<ClassNode> getContainedClasses() {
        final List<ClassNode> nodes = new ArrayList<ClassNode>();
        JavaSource source = JavaSource.create(cpInfo, new FileObject[0]);

        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController controller)
                        throws Exception {
                    controller.toPhase(JavaSource.Phase.PARSED);

                    PackageElement pelem = controller.getElements().getPackageElement(getNodeName());

                    if (pelem != null) {
                        for (TypeElement type : ElementFilter.typesIn(pelem.getEnclosedElements())) {
                            if ((type.getKind() == ElementKind.CLASS) || (type.getKind() == ElementKind.ENUM)) {
                                nodes.add(new JavaClassNode(cpInfo, false, type, JavaPackageNode.this));
                            }
                        }
                    } else {
                        LOGGER.log(Level.FINEST, "Package name {0} resulted into a NULL element", getNodeName()); // NOI18N
                    }
                }
            }, true);
        } catch (IOException ex) {
            LOGGER.severe(ex.getLocalizedMessage());
        }
        return nodes;
    }

    @Override
    protected List<PackageNode> getContainedPackages() {
        ClassIndex index = cpInfo.getClassIndex();
        List<PackageNode> nodes = new ArrayList<PackageNode>();

        for (String pkgName : index.getPackageNames(getNodeName() + ".", true, scope)) { // NOI18N
            nodes.add(new JavaPackageNode(cpInfo, pkgName, this, scope));
        }

        return nodes;
    }

    @Override
    public SourceCodeSelection getSignature() {
        return signature;
    }


}
