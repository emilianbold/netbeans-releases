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


package org.netbeans.modules.j2ee.jpa.refactoring.rename;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.j2ee.jpa.refactoring.RefactoringUtil;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public class PersistenceXmlPackageRename extends PersistenceXmlRefactoring{
    
    private final RenameRefactoring renameRefactoring;
    
    public PersistenceXmlPackageRename(RenameRefactoring renameRefactoring) {
        this.renameRefactoring = renameRefactoring;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return renameRefactoring;
    }
    
    @Override
    protected boolean shouldHandle() {
        return true;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        if (isPackage()){
            FileObject pkg = renameRefactoring.getRefactoringSource().lookup(NonRecursiveFolder.class).getFolder();
            String oldPackageName = RefactoringUtil.getQualifiedName(pkg);
            
            return doPrepare(refactoringElementsBag, pkg, oldPackageName, renameRefactoring.getNewName());
        } else if (isFolder()){
            FileObject folder = renameRefactoring.getRefactoringSource().lookup(FileObject.class);
            ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
            FileObject root = classPath.findOwnerRoot(folder);
            
            String prefix = FileUtil.getRelativePath(root, folder.getParent()).replace('/','.');
            String oldName = buildName(prefix, folder.getName());
            // the new package name
            String newName = buildName(prefix, renameRefactoring.getNewName());
            
            return doPrepare(refactoringElementsBag, folder, oldName, newName);
        }
        return null;
    }
    
    private boolean isPackage(){
        return renameRefactoring.getRefactoringSource().lookup(NonRecursiveFolder.class) != null;
    }
    
    private boolean isFolder(){
        FileObject folder = renameRefactoring.getRefactoringSource().lookup(FileObject.class);
        return folder != null && folder.isFolder();
    }
    
    
    /**
     * Prepares the rename.
     *
     * @param refactoringElementsBag
     * @param folder the folder or package to be renamed.
     * @param oldName the old FQN of the folder / package.
     * @param newName the new FQN of the folder / package.
     */
    private Problem doPrepare(RefactoringElementsBag refactoringElementsBag, FileObject folder, String oldName, String newName){
        Problem result = null;
        
        for (FileObject each : getPersistenceXmls()){
            try {
                PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
                for (String clazz : getClasses(folder, new ArrayList<String>())){
                    List<PersistenceUnit> punits = getAffectedPersistenceUnits(pUDataObject, clazz);
                    String newClassName = clazz.replace(oldName, newName);
                    for (PersistenceUnit persistenceUnit : punits) {
                        refactoringElementsBag.add(getRefactoring(),
                                new PersistenceXmlPackageRenameRefactoringElement(persistenceUnit, clazz, newClassName, pUDataObject, each));
                    }
                }
            } catch (InvalidPersistenceXmlException ex) {
                Problem newProblem =
                        new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                
                result = RefactoringUtil.addToEnd(newProblem, result);
            }
        }
        return result;
        
    }
    
    private String buildName(String prefix, String name){
        if (prefix.length() == 0){
            return name;
        }
        return prefix + "." + name;
    }
    
    /**
     * Collects the names of the classes from the given folder, recursively if possible (i.e. the given
     * folder is not a NonRecursiveFolder).
     *
     * @return a list of fully qualified names of the classes in the given folder and its subfolders.
     */
    private List<String> getClasses(FileObject folder, List<String> result){
        for (FileObject each : folder.getChildren()){
            if (each.isFolder()){
                getClasses(each, result);
            } else {
                result.add(RefactoringUtil.getQualifiedName(each));
            }
        }
        return result;
    }
    
    
    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
            String clazz,
            PUDataObject pUDataObject,
            FileObject persistenceXml) {
        
        return null;
    }
    
    
    /**
     * A rename element for persistence.xml
     */
    private static class PersistenceXmlPackageRenameRefactoringElement extends PersistenceXmlRefactoringElement {
        
        private final String newName;
        
        public PersistenceXmlPackageRenameRefactoringElement(PersistenceUnit persistenceUnit,
                String oldName,  String newName, PUDataObject puDataObject, FileObject parentFile) {
            super(persistenceUnit, oldName, puDataObject, parentFile);
            this.newName = newName;
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {parentFile.getNameExt(), clazz, newName};
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlRename.class, "TXT_PersistenceXmlRename"), args);
        }
        
        public void undoChange() {
            ProviderUtil.renameManagedClass(persistenceUnit, clazz, newName, puDataObject);
        }
        
        /** Performs the change represented by this refactoring element.
         */
        public void performChange() {
            ProviderUtil.renameManagedClass(persistenceUnit, newName, clazz, puDataObject);
        }
        
    }
    
}
