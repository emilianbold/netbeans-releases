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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jpa.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.jpa.refactoring.JPARefactoring;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.jpa.refactoring.util.PositionBoundsResolver;
import org.netbeans.modules.j2ee.jpa.refactoring.util.ProblemUtil;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A base class for persistence.xml refactorings.
 *
 * @author Erno Mononen
 */
public abstract class PersistenceXmlRefactoring implements JPARefactoring{
    
    private final FileObject refactoringSource;
    private final Project project;
    
    protected PersistenceXmlRefactoring(AbstractRefactoring refactoring) {
        this.refactoringSource = refactoring.getRefactoringSource().lookup(FileObject.class);
        this.project = FileOwnerQuery.getOwner(refactoringSource);
    }
    
    protected FileObject getRefactoringSource() {
        return refactoringSource;
    }
    
    protected Project getProject() {
        return project;
    }
    
    private TreePathHandle resolveTreePathHandle() throws IOException {
        TreePathHandle tph = getRefactoring().getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }
        
        final TreePathHandle[] result = new TreePathHandle[1];
        JavaSource source = JavaSource.forFileObject(refactoringSource);
        
        source.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
                // can't cancel
            }
            public void run(CompilationController co) throws Exception {
                co.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = co.getCompilationUnit();
                result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
            }
            
        }, true);
        
        return result[0];
    }
    
    /**
     * @return true if the refactoring source represents a class that
     * should be handled by persistence.xml refactorings.
     */
    private boolean shouldHandle(){
        
        if (getProject() == null){
            return false; // can handle only classes belonging to projects
        }
        
        final boolean[] result = new boolean[1];
        
        JavaSource source = JavaSource.forFileObject(refactoringSource);
        try{
            source.runUserActionTask(new CancellableTask<CompilationController>(){
                
                public void cancel() {
                }
                
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    TreePathHandle treePathHandle = resolveTreePathHandle();
                    Element element = treePathHandle.resolveElement(info);
                    result[0] = element.getKind() == ElementKind.CLASS;
                }
            }, true);
        } catch(IOException ex){
            Exceptions.printStackTrace(ex);
        }
        
        return result[0];
        
    }
    
    public final Problem preCheck() {
        
        if (!shouldHandle()){
            return null;
        }
        
        Problem result = null;
        
        for (FileObject each : getPersistenceXmls()){
            try {
                PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
            } catch (InvalidPersistenceXmlException ex) {
                Problem newProblem =
                        new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                
                result = ProblemUtil.addToEnd(newProblem, result);
            }
        }
        
        return result;
        
    }
    
    public final Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        
        if (!shouldHandle()){
            return null;
        }
        
        Problem result = null;
        
        ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
        String classNameFQN =
                classPathProvider.findClassPath(refactoringSource, ClassPath.SOURCE).getResourceName(refactoringSource, '.', false);
        
        for (FileObject each : getPersistenceXmls()){
            try {
                PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
                List<PersistenceUnit> punits = getAffectedPersistenceUnits(pUDataObject, classNameFQN);
                for (PersistenceUnit persistenceUnit : punits) {
                    refactoringElementsBag.add(getRefactoring(), getRefactoringElement(persistenceUnit, classNameFQN, pUDataObject, each));
                }
            } catch (InvalidPersistenceXmlException ex) {
                Problem newProblem =
                        new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                
                result = ProblemUtil.addToEnd(newProblem, result);
            }
        }
        
        return result;
    }
    
    /**
     * @return the actual refactoring being performed.
     */
    protected abstract AbstractRefactoring getRefactoring();
    
    /**
     *@return the refactoring element for the given parameters.
     */
    protected abstract RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
            String clazz, PUDataObject pUDataObject, FileObject persistenceXml);
    
    
    /**
     * @return the persistence units that contain the class that is being refactored.
     */
    private List<PersistenceUnit> getAffectedPersistenceUnits(PUDataObject pUDataObject, String clazz){
        List<PersistenceUnit> result = new ArrayList<PersistenceUnit>();
        PersistenceUnit[] persistenceUnits = ProviderUtil.getPersistenceUnits(pUDataObject);
        for(PersistenceUnit each : persistenceUnits){
            if (hasClass(each, clazz)){
                result.add(each);
            }
        }
        return result;
    }
    
    
    private static boolean hasClass(PersistenceUnit persistenceUnit, String clazz){
        for (String each : persistenceUnit.getClass2()){
            if (each.equals(clazz)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return the persistence.xml files in the project to which the refactored
     * class belongs or an empty list if the class does not belong to any project.
     */
    private List<FileObject> getPersistenceXmls(){
        
        if (project == null){
            return Collections.<FileObject>emptyList();
        }
        
        List<FileObject> result = new ArrayList<FileObject>();
        
        PersistenceScope[] persistenceScopes = PersistenceUtils.getPersistenceScopes(project);
        for (int i = 0; i < persistenceScopes.length; i++) {
            result.add(persistenceScopes[i].getPersistenceXml());
        }
        
        return result;
    }
    
    protected abstract static class PersistenceXmlRefactoringElement extends SimpleRefactoringElementImplementation {
        
        protected final PersistenceUnit persistenceUnit;
        protected final PUDataObject puDataObject;
        protected final String clazz;
        protected final FileObject parentFile;
        
        protected PersistenceXmlRefactoringElement(PersistenceUnit persistenceUnit,
                String clazz,  PUDataObject puDataObject, FileObject parentFile) {
            this.clazz = clazz;
            this.persistenceUnit = persistenceUnit;
            this.puDataObject = puDataObject;
            this.parentFile = parentFile;
        }
        
        public final String getText() {
            return getDisplayText();
        }
        
        public final Lookup getLookup() {
            return Lookups.singleton(parentFile);
        }
        
        public final FileObject getParentFile() {
            return parentFile;
        }
        
        public final PositionBounds getPosition() {
            try {
                return new PositionBoundsResolver(DataObject.find(parentFile), clazz).getPositionBounds();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    
}
