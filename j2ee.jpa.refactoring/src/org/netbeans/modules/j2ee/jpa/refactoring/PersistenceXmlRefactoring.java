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
import org.netbeans.api.fileinfo.NonRecursiveFolder;
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
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.jpa.refactoring.util.PositionBoundsResolver;
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
    
    /**
     *@return the file object for the object being refactored.
     */
    protected FileObject getRefactoringSource() {
        
        FileObject result = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (result != null){
            return result;
        }
        NonRecursiveFolder folder = getRefactoring().getRefactoringSource().lookup(NonRecursiveFolder.class);
        if (folder != null){
            return folder.getFolder();
        }
        try{
            TreePathHandle treePathHandle = resolveTreePathHandle();
            if (treePathHandle != null) {
                result = treePathHandle.getFileObject();
            }
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return result;
        
    }
    
    /**
     *@return the project owning the object being refactored or null if
     * does not belong to any project.
     */
    protected Project getProject() {
        FileObject refactoringSource = getRefactoringSource();
        return refactoringSource == null ? null : FileOwnerQuery.getOwner(refactoringSource);
    }
    
    /**
     * Resolves the TreePathHandle for the object being refactored.
     *
     * @return the TreePathHandle or null if no handle could be resolved
     * the refactored object.
     */
    private TreePathHandle resolveTreePathHandle() throws IOException {
        TreePathHandle tph = getRefactoring().getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }
        FileObject sourceFO = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (sourceFO == null){
            return null;
        }
        
        final TreePathHandle[] result = new TreePathHandle[1];
        JavaSource source = JavaSource.forFileObject(sourceFO);
        
        source.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
                // can't cancel
            }
            public void run(CompilationController co) throws Exception {
                co.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = co.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()){
                    return;
                }
                result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
            }
            
        }, true);
        
        return result[0];
    }
    
    /**
     * Checks whether the object being refactored should be handled by
     * this refactoring. Override in subclasses as appropriated, the
     * default implementation returns true if the refactored object
     * is a class.
     *
     * @return true if the refactoring source represents a class that
     * should be handled by persistence.xml refactorings.
     */
    protected boolean shouldHandle(){
        
        final boolean[] result = new boolean[] { false };
        
        FileObject refactoringSource = getRefactoringSource();
        if (refactoringSource != null && RefactoringUtil.isJavaFile(refactoringSource)) {
            JavaSource source = JavaSource.forFileObject(refactoringSource);
            try{
                source.runUserActionTask(new CancellableTask<CompilationController>(){

                    public void cancel() {
                    }

                    public void run(CompilationController info) throws Exception {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                        TreePathHandle treePathHandle = resolveTreePathHandle();
                        if (treePathHandle == null){
                            result[0] = false;
                        } else {
                            Element element = treePathHandle.resolveElement(info);
                            if (element != null){
                                result[0] = element.getKind() == ElementKind.CLASS;
                            }
                        }
                    }
                }, true);
            } catch(IOException ex){
                Exceptions.printStackTrace(ex);
            }
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
                
                result = RefactoringUtil.addToEnd(newProblem, result);
            }
        }
        
        return result;
        
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        
        if (!shouldHandle()){
            return null;
        }
        
        Problem result = null;
        Project project = getProject();
        if (project == null){
            return null;
        }
        
        FileObject refactoringSource = getRefactoringSource();
        if (refactoringSource != null) {
            String classNameFQN = RefactoringUtil.getQualifiedName(refactoringSource);

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

                    result = RefactoringUtil.addToEnd(newProblem, result);
                }
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
     * Gets the persistence unit from the given <code>PUDataObject</code> that contain
     * a class matching with the given <code>clazz</code>.
     * @param puDataObject
     * @param clazz the fully qualified name of the class
     *
     * @return the persistence units that contain the given class.
     */
    protected final List<PersistenceUnit> getAffectedPersistenceUnits(PUDataObject pUDataObject, String clazz){
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
    protected final List<FileObject> getPersistenceXmls(){
        Project project = getProject();
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
