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

package org.netbeans.modules.web.project;

import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionNonInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInWeb;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTANonInjectableInWeb;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * An implementation of EntityManagerGenerationStrategyResolver for web projects.
 *
 * @author Erno Mononen
 */
public class WebEMGenStrategyResolver implements EntityManagerGenerationStrategyResolver{
    
    
    /** Creates a new instance of WebEMGenStrategyResolver */
    public WebEMGenStrategyResolver() {
    }
    
    public Class<? extends EntityManagerGenerationStrategy> resolveStrategy(FileObject target) {
        
        PersistenceUnit persistenceUnit = getPersistenceUnit(target);
        String jtaDataSource = persistenceUnit.getJtaDataSource();
        String transactionType = persistenceUnit.getTransactionType();
        boolean isInjectionTarget = isInjectionTarget(target);
        boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
        boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.equals("")) && isJTA; //NO18N
        
        if (isContainerManaged) { // Container-managed persistence context
            if (isInjectionTarget) { // servlet, JSF managed bean ...
                return ContainerManagedJTAInjectableInWeb.class;
            } else { // other classes
                return ContainerManagedJTANonInjectableInWeb.class;
            }
        } else if (!isJTA){ // Application-managed persistence context (Resource-transaction)
            if (isInjectionTarget) { // servlet, JSF managed bean ...
                return ApplicationManagedResourceTransactionInjectableInWeb.class;
            } else { // other classes
                return ApplicationManagedResourceTransactionNonInjectableInWeb.class;
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
