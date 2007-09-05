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

import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.util.AbstractTask;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInJ2SE;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Generates appropriate code for retrieving and invoking <code>javax.persistence.EntityManager</code>.
 * The generated code depends on the target class' enviroment.
 *
 * TODO: move this class to different package if anybody else wants to use it
 * @author Martin Adamek, Erno Mononen
 */

public final class EntityManagerGenerator {
    
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
     * The project to which the target file belongs.
     */
    private final Project project;
    
    /**
     * Creates a new EntityManagerGenerator.
     * @param targetFo the file object of the target java source file.
     * @param fqn the fully qualified name of the target java class.
     */
    public EntityManagerGenerator(FileObject targetFo, String fqn) {
        this.fqn = fqn;
        this.targetFo = targetFo;
        this.targetSource = JavaSource.forFileObject(targetFo);
        this.project = FileOwnerQuery.getOwner(targetFo);
    }

    /**
     * Generates the code needed for retrieving and invoking
     * an instance of <code>javax.persistence.EntityManager</code>. The generated 
     * code depends on the environment of the target class (e.g. whether
     * it supports injection or not).
     * 
     * @param options the options for the generation. Must not be null.
     * @return the modified file object of the target java class.
     */
    public FileObject generate(final GenerationOptions options) throws IOException{
        
        final Class<? extends EntityManagerGenerationStrategy> strategyClass = getStrategy();
    
        if (strategyClass == null){
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupported"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            
            return targetFo;
        }
        
        return generate(options, strategyClass);
        
    }
        
    /**
     * Generates the code needed for retrieving and invoking
     * an instance of <code>javax.persistence.EntityManager</code>. The generated 
     * code depends on the given <code>strategyClass</code>. 
     * 
     * @param options the options for the generation. Must not be null.
     * @param strategyClass the generation strategy that should be used. Must not be null.
     * @return the modified file object of the target java class.
     */
    public FileObject generate(final GenerationOptions options, 
            final Class<? extends EntityManagerGenerationStrategy> strategyClass) throws IOException{
    
        Parameters.notNull("options", options); //NO18N
        Parameters.notNull("strategyClass", strategyClass); //NO18N
        
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        EntityManagerGenerationStrategy strategy = instantiateStrategy(strategyClass, workingCopy, make, clazz, options);
                        workingCopy.rewrite(clazz, strategy.generate());
                    }
                }
            }
        };
        
        targetSource.runModificationTask(task).commit();
        
        return targetFo;
    }
    
    private Class<? extends EntityManagerGenerationStrategy> getStrategy(){

        EntityManagerGenerationStrategyResolver resolver = project.getLookup().lookup(EntityManagerGenerationStrategyResolver.class);
        if (resolver != null){
            return resolver.resolveStrategy(targetFo);
        }
        
        // must be a java se project (we don't want it to implement the EntityManagerGenerationStrategyResolver SPI)
        return ApplicationManagedResourceTransactionInJ2SE.class;
    }
    
    private EntityManagerGenerationStrategy instantiateStrategy(Class<? extends EntityManagerGenerationStrategy> strategy, WorkingCopy workingCopy,
            TreeMaker make, ClassTree clazz, GenerationOptions options){
        
        EntityManagerGenerationStrategy result = null;
        
        try{
            result = strategy.newInstance();
            result.setClassTree(clazz);
            result.setWorkingCopy(workingCopy);
            result.setGenerationOptions(options);
            result.setTreeMaker(make);
            result.setPersistenceUnit(getPersistenceUnit());
        } catch (IllegalAccessException iae){
            throw new RuntimeException(iae); //TODO
        } catch (InstantiationException ie){
            throw new RuntimeException(ie); //TODO
        }
        
        return result;
    }
    
    private  PersistenceUnit getPersistenceUnit() {
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
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
}