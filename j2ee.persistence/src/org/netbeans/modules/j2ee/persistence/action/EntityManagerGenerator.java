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

package org.netbeans.modules.j2ee.persistence.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Generates appropriate code for retrieving and invoking <code>javax.persistence.EntityManager</code>.
 * The generated code depends on the target class' enviroment.
 *
 * TODO: move this class to different package if anybody else wants to use it
 * @author Martin Adamek, Erno Mononen
 */

public class EntityManagerGenerator {
    
    /**
     * The fully qualified name of the target class.
     */
    private final String fqn;
    /**
     *  The target java source file.
     */
    private final JavaSource targetSource;
    /**
     * The file object of the target source file.
     */
    private final FileObject targetFo;
    
    /**
     * Creates a new EntityManagerGenerator.
     * @param targetFo the file object of the target java source file.
     * @param fqn the fully qualified name of the target java class.
     */
    public EntityManagerGenerator(FileObject targetFo, String fqn) {
        this.fqn = fqn;
        this.targetFo = targetFo;
        this.targetSource = JavaSource.forFileObject(targetFo);
    }
    
    /**
     * Generates the code needed for retrieving and invoking
     * <code>javax.persistence.EntityManager</code>. The generated code depends
     * on the target class' environment.
     * @param options the options for the generation.
     * @return the modified file object of the target java class.
     */
    public FileObject generate(final GenerationOptions options) throws IOException{
        
        final boolean[] supportedUseCase = new boolean[1];
        
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        EntityManagerGenerationStrategy strategy = getStrategy(cut, workingCopy, make, clazz, options);
                        if (strategy != null){
                            supportedUseCase[0] = true;
                            ClassTree modifiedClazz = strategy.generate();
                            workingCopy.rewrite(clazz, modifiedClazz);
                        }
                    }
                }
            }
        };
        
        targetSource.runModificationTask(task).commit();
        
        if (!supportedUseCase[0]){
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupportedAMJTA"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        
        return targetFo;
    }
    
    /**
     * @return the generation strategy based on the project type of our target file.
     */ 
    protected EntityManagerGenerationStrategy getStrategy(CompilationUnitTree compilationUnit, WorkingCopy workingCopy, TreeMaker make, ClassTree clazz, GenerationOptions options){

        Object j2eeModuleType = getJ2eeModuleType();
        PersistenceUnit persistenceUnit = getPersistenceUnit();
        
        if (j2eeModuleType == null) {
            // Application-managed persistence context in J2SE project (Resource-transaction)
            return new ApplicationManagedResourceTransactionInJ2SE(workingCopy, make, clazz,persistenceUnit, options);
        } else {
            // it is Web or EJB, let's get all needed information
            String jtaDataSource = persistenceUnit.getJtaDataSource();
            String nonJtaDataSource = persistenceUnit.getNonJtaDataSource();
            String transactionType = persistenceUnit.getTransactionType();
            TreePath treePath = workingCopy.getTrees().getPath(compilationUnit, clazz);
            boolean isInjectionTarget = isInjectionTarget(workingCopy, (TypeElement) workingCopy.getTrees().getElement(treePath));
            boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.equals("")) && (transactionType != null && transactionType.equals("JTA"));
            boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
            
            if (j2eeModuleType.equals(J2eeModule.WAR)) { // Web project
                if (isContainerManaged) { // Container-managed persistence context
                    if (isInjectionTarget) { // servlet, JSF managed bean ...
                        return new ContainerManagedJTAInjectableInEJB(workingCopy, make, clazz,persistenceUnit, options);
                    } else { // other classes
                        return new ContainerManagedJTAInjectableInWeb(workingCopy, make, clazz, persistenceUnit, options);
                    }
                } else { // Application-managed persistence context (Resource-transaction)
                    if (isJTA) { // JTA
                        if (isInjectionTarget) { // servlet, JSF managed bean ...
                            // not supported
                            return null;
                        } else { // other classes
                            // not supported
                            return null;
                        }
                    } else { // Resource-transaction
                        if (isInjectionTarget) { // servlet, JSF managed bean ...
                            return new ApplicationManagedResourceTransactionInjectableInWeb(workingCopy, make, clazz, persistenceUnit, options);
                        } else { // other classes
                            return new ApplicationManagedResourceTransactionNonInjectableInWeb(workingCopy, make, clazz,persistenceUnit, options);
                        }
                    }
                }
            } else if (j2eeModuleType.equals(J2eeModule.EJB)) { // EJB project
                if (isContainerManaged) { // Container-managed persistence context
                    if (isInjectionTarget) { // session, MessageDriven
                        return new ContainerManagedJTAInjectableInEJB(workingCopy, make, clazz, persistenceUnit, options);
                    } else { // other classes
                        // ???
                        return null;
                    }
                } else { // Application-managed persistence context
                    if (isJTA) { // JTA
                        if (isInjectionTarget) { // session, MDB
                            // not supported
                            return null;
                        } else { // other classes
                            // not supported
                            return null;
                        }
                    } else { // Resource-transaction
                        if (isInjectionTarget) { // session, MDB
                            return new ApplicationManagedResourceTransactionInjectableInEJB(workingCopy, make, clazz, persistenceUnit, options);
                        } else { // other classes
                            return new ApplicationManagedResourceTransactionNonInjectableInEJB(workingCopy, make,clazz, persistenceUnit, options);
                        }
                    }
                }
            }
        }
        // not supported
        return null;
    }
    
    protected boolean isInjectionTarget(WorkingCopy workingCopy, TypeElement element){
        return InjectionTargetQuery.isInjectionTarget(workingCopy, element);
    }
    
    /**
     * @return the J2eeModule associated with the project of our target file or 
     *  null if there was no associated J2eeModule.
     */ 
    protected Object getJ2eeModuleType(){
        J2eeModule result = null;
        Project project = FileOwnerQuery.getOwner(targetFo);
        J2eeModuleProvider j2eeModuleProvider = null;
        if (project != null){
            j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        }
        if (j2eeModuleProvider != null) {
            result = j2eeModuleProvider.getJ2eeModule();
        }
        return result != null ? result.getModuleType() : null;
        
    }
    
    protected PersistenceUnit getPersistenceUnit() {
        PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(targetFo);
        
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
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    // for tests
    protected final JavaSource getTargetSource(){
        return this.targetSource;
    }
}
