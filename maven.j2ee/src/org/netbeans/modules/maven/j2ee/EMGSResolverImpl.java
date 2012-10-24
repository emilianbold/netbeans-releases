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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionNonInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTANonInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * a hard copy of the EMGSR implementation in web projects..
 * 
 * @author mkleint
 */
@ProjectServiceProvider(service = EntityManagerGenerationStrategyResolver.class, projectType = {
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB
})
public class EMGSResolverImpl implements EntityManagerGenerationStrategyResolver {

    @Override
    public Class<? extends EntityManagerGenerationStrategy> resolveStrategy(FileObject target) {

        PersistenceUnit persistenceUnit = getPersistenceUnit(target);
        String jtaDataSource = persistenceUnit.getJtaDataSource();
        String transactionType = persistenceUnit.getTransactionType();
        boolean isInjectionTarget = isInjectionTarget(target);
        boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
        boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.equals("")) && isJTA; //NO18N

        if (isContainerManaged) { // Container-managed persistence context
            Project prj = FileOwnerQuery.getOwner(target);
            if (prj != null) {
                ContainerClassPathModifier modifier = prj.getLookup().lookup(ContainerClassPathModifier.class);
                if (modifier != null) {
                    modifier.extendClasspath(target,
                        new String[] {
                            ContainerClassPathModifier.API_ANNOTATION,
                            ContainerClassPathModifier.API_PERSISTENCE,
                            ContainerClassPathModifier.API_TRANSACTION
                        });

                }
            }
            if (isInjectionTarget) { // servlet, JSF managed bean ...
                //uses javax.persistence.PersistenceContext
                // andjavax.transaction.UserTransaction, javax.naming.Context
                //add jee5 on classpath

                return ContainerManagedJTAInjectableInWeb.class;
            } else { // other classes
                //javax.transaction.UserTransaction
                //javax.naming.Context
                return ContainerManagedJTANonInjectableInWeb.class;
            }
        } else if (!isJTA){ // Application-managed persistence context (Resource-transaction)
            if (isInjectionTarget) { // servlet, JSF managed bean ...
                //javax.persistence.EntityManager
                return ApplicationManagedResourceTransactionInjectableInWeb.class;
            } else { // other classes
                //javax.persistence.EntityManager
                return ApplicationManagedResourceTransactionNonInjectableInWeb.class;
            }
        }

        return null;
    }

    private boolean isInjectionTarget(FileObject target) {
        final boolean[] result = new boolean[1];
        final JavaSource source = JavaSource.forFileObject(target);
        if (source == null) {
            return false;
        }

        try{
            source.runModificationTask(new Task<WorkingCopy>(){
                @Override
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(parameter);
                    result[0] = InjectionTargetQuery.isInjectionTarget(parameter, typeElement);
                }
            });
        } catch (IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }


    private PersistenceUnit getPersistenceUnit(FileObject target) {

        PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(target);
        if (persistenceScope == null){
            return null;
        }

        try {
            // TODO: fix ASAP! 1st PU is taken, needs to find the one which realy owns given file
            Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml());
            if (persistence != null) {
                return persistence.getPersistenceUnit(0);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}
