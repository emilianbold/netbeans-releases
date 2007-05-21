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


package org.netbeans.modules.j2ee.jpa.refactoring.whereused;


import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.text.MessageFormat;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.j2ee.jpa.refactoring.RefactoringUtil;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.openide.util.Exceptions;

/**
 * Handles renaming of the classes that are listed in <code>persistence.xml</code>.
 *
 * @author Erno Mononen
 */
public final class PersistenceXmlWhereUsed extends PersistenceXmlRefactoring {
    
    private final WhereUsedQuery whereUsedQuery;
    
    public PersistenceXmlWhereUsed(WhereUsedQuery refactoring) {
        this.whereUsedQuery = refactoring;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        Problem result = null;
        TreePathHandle handle = whereUsedQuery.getRefactoringSource().lookup(TreePathHandle.class);
        if (handle == null || Kind.CLASS != handle.getKind()){
            return null;
        }
        Element resElement = handle.resolveElement(RefactoringUtil.getCompilationInfo(handle, whereUsedQuery));
        TypeElement type = (TypeElement) resElement;
        String clazz = type.getQualifiedName().toString();
        for (FileObject each : getPersistenceXmls()){
            try{
                PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
                for (PersistenceUnit persistenceUnit : getAffectedPersistenceUnits(pUDataObject, clazz)){
                    refactoringElementsBag.add(getRefactoring(), getRefactoringElement(persistenceUnit, clazz, pUDataObject, each));
                    
                }
            } catch (InvalidPersistenceXmlException ex) {
                Problem newProblem =
                        new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                
                result = RefactoringUtil.addToEnd(newProblem, result);
            }
            
        }
        return result;
    }
    
    
    protected AbstractRefactoring getRefactoring() {
        return whereUsedQuery;
    }
    
    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
            String clazz,
            PUDataObject pUDataObject,
            FileObject persistenceXml) {
        
        return new PersistenceXmlWhereUsedRefactoringElement(persistenceUnit, clazz, pUDataObject, persistenceXml);
    } 

    protected class PersistenceXmlWhereUsedRefactoringElement extends PersistenceXmlRefactoringElement {
        
        public PersistenceXmlWhereUsedRefactoringElement(PersistenceUnit persistenceUnit,
                String clazz,  PUDataObject puDataObject, FileObject parentFile) {
            super(persistenceUnit, clazz, puDataObject, parentFile);
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {parentFile.getNameExt(), clazz};
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlWhereUsedRefactoringElement.class, "TXT_PersistenceXmlClassWhereUsed"), args);
        }
        
        
        public void performChange() {
            // nothing to do here
        }
        
    }
    
    
}
