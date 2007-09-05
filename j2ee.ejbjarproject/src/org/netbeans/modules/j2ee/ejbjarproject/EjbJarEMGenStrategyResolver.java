/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionNonInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTANonInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * An EntityManagerGenerationStrategyResolver implementation that resolves an
 * appropriate generation strategy for classes in an EJB module.
 *
 * @author Erno Mononen
 */
public class EjbJarEMGenStrategyResolver implements EntityManagerGenerationStrategyResolver{
    
    
    /** Creates a new instance of EjbJarEMGenStrategyResolver */
    public EjbJarEMGenStrategyResolver() {
    }
    
    public Class<? extends EntityManagerGenerationStrategy> resolveStrategy(FileObject target) {
        
        Object j2eeModuleType = getJ2eeModuleType(target);
        PersistenceUnit persistenceUnit = getPersistenceUnit(target);
        
        if (j2eeModuleType == null || !J2eeModule.EJB.equals(j2eeModuleType)) {
            // handle only ejb module
            return null;
        }
        
        String jtaDataSource = persistenceUnit.getJtaDataSource();
        String transactionType = persistenceUnit.getTransactionType();
            boolean isInjectionTarget = isInjectionTarget(target);
        boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
        boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.equals("")) && isJTA; //NO18N
        
        if (isContainerManaged && isInjectionTarget) { // Container-managed persistence context, managed class
            return ContainerManagedJTAInjectableInEJB.class;
        } else if (isContainerManaged){
            return ContainerManagedJTANonInjectableInWeb.class;
        } else if (!isJTA) { // Application-managed resource-local persistence context
            if (isInjectionTarget) { // session, MDB
                return ApplicationManagedResourceTransactionInjectableInEJB.class;
            } else { // other classes
                return ApplicationManagedResourceTransactionNonInjectableInEJB.class;
            }
        }
        
        return null;
        
    }
    
    private boolean isInjectionTarget(FileObject target) {
        final boolean[] result = new boolean[1];
        JavaSource source = JavaSource.forFileObject(target);
        try{
            source.runModificationTask(new AbstractTask<WorkingCopy>(){
                public void run(WorkingCopy parameter) throws Exception {
                    GenerationUtils genUtils = GenerationUtils.newInstance(parameter);
                    result[0] = InjectionTargetQuery.isInjectionTarget(parameter, genUtils.getTypeElement());
                }
            });
        } catch (IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
    /**
     * @return the J2eeModule associated with the project of our target file or
     *  null if there was no associated J2eeModule.
     */
    protected Object getJ2eeModuleType(FileObject target){
        J2eeModule result = null;
        Project project = FileOwnerQuery.getOwner(target);
        J2eeModuleProvider j2eeModuleProvider = null;
        if (project != null){
            j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        }
        if (j2eeModuleProvider != null) {
            result = j2eeModuleProvider.getJ2eeModule();
        }
        return result != null ? result.getModuleType() : null;
        
    }
    
    private PersistenceUnit getPersistenceUnit(FileObject target) {
        PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(target);
        if (persistenceScope == null){
            return null;
        }
        
        try {
            // TODO: fix ASAP! 1st PU is taken, needs to find the one which realy owns given file
            Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml());
            if(persistence != null){
                return persistence.getPersistenceUnit(0);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    
}
