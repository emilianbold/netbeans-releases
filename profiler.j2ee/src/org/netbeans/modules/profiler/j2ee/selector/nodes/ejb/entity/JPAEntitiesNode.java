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

package org.netbeans.modules.profiler.j2ee.selector.nodes.ejb.entity;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
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
public class JPAEntitiesNode extends ContainerNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class Children extends GreedySelectorChildren<JPAEntitiesNode> {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected List<SelectorNode> prepareChildren(final JPAEntitiesNode parent) {
            final List<SelectorNode> entityBeans = new ArrayList<SelectorNode>();

            Project project = parent.getLookup().lookup(Project.class);

            final ClasspathInfo cpInfo = ProjectUtilities.getClasspathInfo(project);
            final JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);

            for (MetadataModel<EntityMappingsMetadata> mdModel : listAllMetadata(project)) {
                try {
                    entityBeans.addAll(mdModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, List<SelectorNode>>() {
                            public List<SelectorNode> run(EntityMappingsMetadata metadata)
                                                   throws Exception {
                                final List<SelectorNode> beanList = new ArrayList<SelectorNode>();

                                Entity[] entities = metadata.getRoot().getEntity();

                                for (Entity entity : entities) {
                                    final Entity entityBean = entity;
                                    js.runUserActionTask(new CancellableTask<CompilationController>() {
                                            public void cancel() {
                                            }

                                            public void run(CompilationController controller)
                                                     throws Exception {
                                                TypeElement type = controller.getElements().getTypeElement(entityBean.getClass2());
                                                beanList.add(new EntityBeanNode(cpInfo, entityBean.getName(), Utils.CLASS_ICON, type, parent));
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

            return entityBeans;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String JPA_ENTITIES_STRING = NbBundle.getMessage(JPAEntitiesNode.class, "JPAEntitiesNode_JPAEntityString"); // NOI18N
                                                                                                                                     // -----

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of SessionBeansNode */
    public JPAEntitiesNode(final ContainerNode parent) {
        super(JPA_ENTITIES_STRING, Utils.PACKAGE_ICON, parent);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected SelectorChildren getChildren() {
        return new Children();
    }

    private static Set<MetadataModel<EntityMappingsMetadata>> listAllMetadata(final Project project) {
        final Set<MetadataModel<EntityMappingsMetadata>> metadata = new HashSet<MetadataModel<EntityMappingsMetadata>>();

        Set<Project> projects = new HashSet<Project>();

        projects.add(project);
        ProjectUtilities.fetchSubprojects(project, projects);

        for (Project subProj : projects) {
            PersistenceScopes scopes = PersistenceScopes.getPersistenceScopes(subProj);

            if (scopes != null) {
                for (PersistenceScope scope : scopes.getPersistenceScopes()) {
                    try {
                        Persistence persistence = PersistenceMetadata.getDefault().getRoot(scope.getPersistenceXml());

                        for (PersistenceUnit pu : persistence.getPersistenceUnit()) {
                            metadata.add(scope.getEntityMappingsModel(pu.getName()));
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }

        return metadata;
    }
}
