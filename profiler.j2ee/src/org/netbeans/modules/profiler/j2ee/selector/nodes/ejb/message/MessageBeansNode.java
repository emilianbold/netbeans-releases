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

package org.netbeans.modules.profiler.j2ee.selector.nodes.ejb.message;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.modules.profiler.j2ee.ui.Utils;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.profiler.selector.spi.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.spi.nodes.GreedySelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class MessageBeansNode extends ContainerNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class Children extends GreedySelectorChildren<MessageBeansNode> {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected List<SelectorNode> prepareChildren(final MessageBeansNode parent) {
            final List<SelectorNode> sessionBeans = new ArrayList<SelectorNode>();

            Project project = parent.getProject();

            final ClasspathInfo cpInfo = ProjectUtilities.getClasspathInfo(project);
            final JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);

            for (MetadataModel<EjbJarMetadata> mdModel : listAllMetadata(project)) {
                try {
                    sessionBeans.addAll(mdModel.runReadAction(new MetadataModelAction<EjbJarMetadata, List<SelectorNode>>() {
                            public List<SelectorNode> run(EjbJarMetadata metadata)
                                                   throws Exception {
                                final List<SelectorNode> beanList = new ArrayList<SelectorNode>();
                                MessageDriven[] mdbs = metadata.getRoot().getEnterpriseBeans().getMessageDriven();

                                for (MessageDriven mdb : mdbs) {
                                    final MessageDriven mdbBean = mdb;
                                    js.runUserActionTask(new CancellableTask<CompilationController>() {
                                            public void cancel() {
                                            }

                                            public void run(CompilationController controller)
                                                     throws Exception {
                                                TypeElement type = controller.getElements().getTypeElement(mdbBean.getEjbClass());
                                                beanList.add(new MessageBeanNode(cpInfo, Utils.CLASS_ICON, type, parent));
                                            }
                                        }, true);
                                }

                                return beanList;
                            }
                        }));
                } catch (MetadataModelException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return sessionBeans;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String MESSAGE_BEANS_STRING = NbBundle.getMessage(MessageBeansNode.class,
                                                                           "MessageDrivenBeansNode_MessageDrivenBeansString"); // NOI18N
                                                                                                                               // -----

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of MessageBeansNode
     */
    public MessageBeansNode(final ContainerNode parent) {
        super(MESSAGE_BEANS_STRING, Utils.PACKAGE_ICON, parent);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected SelectorChildren getChildren() {
        return new Children();
    }

    private static Set<MetadataModel<EjbJarMetadata>> listAllMetadata(final Project project) {
        final Set<MetadataModel<EjbJarMetadata>> metadata = new HashSet<MetadataModel<EjbJarMetadata>>();
        Set<Project> projects = new HashSet<Project>();

        projects.add(project);
        ProjectUtilities.fetchSubprojects(project, projects);

        for (Project testProject : projects) {
            EjbJarImplementation jar = testProject.getLookup().lookup(EjbJarImplementation.class);

            if (jar == null) {
                continue;
            }

            metadata.add(jar.getMetadataModel());
        }

        return metadata;
    }
}
