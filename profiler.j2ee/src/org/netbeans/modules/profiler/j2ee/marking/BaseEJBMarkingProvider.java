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

package org.netbeans.modules.profiler.j2ee.marking;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.marker.MethodMarker;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.profiler.categories.CustomMarker;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;


/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class BaseEJBMarkingProvider extends CustomMarker {
    private MethodMarker delegate = new MethodMarker();
    private static Logger LOGGER = Logger.getLogger(BaseEJBMarkingProvider.class.getName());

    public BaseEJBMarkingProvider(Project project, Mark assignedMark) {
        super(project, assignedMark);
        addEjbMethods();
    }

    public MarkMapping[] getMappings() {
        return delegate.getMappings();
    }

    public Mark[] getMarks() {
        return delegate.getMarks();
    }

    protected abstract boolean isValid(ExecutableElement method);

    private void addEjbMethods() {
        final ClasspathInfo cpInfo = ProjectUtilities.getClasspathInfo(getProject());
            final JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);

            for (MetadataModel<EjbJarMetadata> mdModel : listAllMetadata()) {
                try {
                    mdModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                            public Void run(EjbJarMetadata metadata)
                                                   throws Exception {
                                System.out.println(metadata.getRoot().getVersion().toString() + " = " + metadata.getRoot().VERSION_3_0);
                                Ejb[] ejbs = metadata.getRoot().getEnterpriseBeans().getEjbs();

                                for (final Ejb ejb : ejbs) {
                                    js.runUserActionTask(new CancellableTask<CompilationController>() {
                                            public void cancel() {
                                            }

                                            public void run(CompilationController controller)
                                                     throws Exception {

                                                TypeElement type = controller.getElements()
                                                                             .getTypeElement(ejb.getEjbClass());
                                                addTypeMethods(delegate, type, controller);
                                            }
                                        }, true);
                                }
                                return null;
                            }
                        });
                } catch (MetadataModelException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
    }

    private Set<MetadataModel<EjbJarMetadata>> listAllMetadata() {
        final Set<MetadataModel<EjbJarMetadata>> metadata = new HashSet<MetadataModel<EjbJarMetadata>>();
        Set<Project> projects = new HashSet<Project>();

        projects.add(getProject());
        ProjectUtilities.fetchSubprojects(getProject(), projects);

        for (Project testProject : projects) {
            EjbJarImplementation jar = testProject.getLookup().lookup(EjbJarImplementation.class);

            if (jar == null) {
                continue;
            }

            metadata.add(jar.getMetadataModel());
        }

        return metadata;
    }

    private void addTypeMethods(final MethodMarker marker, final TypeElement type, final CompilationController controller) {
        if ((marker == null) || (type == null) || (getMark() == null) || (controller == null)) {
            return;
        }
        try {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            // process all methods from the implementor
            for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
                if ((method.getKind() == ElementKind.METHOD) && !method.getModifiers().contains(Modifier.ABSTRACT)) {
                    if (isValid(method)) {
                        try {
                            marker.addMethodMark(ElementUtilities.getBinaryName(type), method.getSimpleName().toString(),
                                    SourceUtils.getVMMethodSignature(method, controller), getMark());
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.throwing(BaseEJBMarkingProvider.class.getName(), "addTypeMethods", e); // NOI18N
        }
    }
}
