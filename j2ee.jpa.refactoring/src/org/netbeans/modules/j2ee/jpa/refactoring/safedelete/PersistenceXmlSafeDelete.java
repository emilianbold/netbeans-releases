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

package org.netbeans.modules.j2ee.jpa.refactoring.safedelete;

import java.text.MessageFormat;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;

/**
 * Handles renaming of the classes that are listed in <code>persistence.xml</code>.
 *
 * @author Erno Mononen
 */
public final class PersistenceXmlSafeDelete extends PersistenceXmlRefactoring {
    
    private final SafeDeleteRefactoring safeDeleteRefactoring;
    
    public PersistenceXmlSafeDelete(SafeDeleteRefactoring safeDeleteRefactoring) {
        super(safeDeleteRefactoring);
        this.safeDeleteRefactoring = safeDeleteRefactoring;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return safeDeleteRefactoring;
    }

    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
                                                                     String clazz,
                                                                     PUDataObject pUDataObject,
                                                                     FileObject persistenceXml) {

        return new PersistenceXmlSafeDeleteRefactoringElement(persistenceUnit, clazz, pUDataObject, persistenceXml);
    }

    
    /**
     * A rename element for persistence.xml
     */
    private static class PersistenceXmlSafeDeleteRefactoringElement extends PersistenceXmlRefactoringElement {
        
        public PersistenceXmlSafeDeleteRefactoringElement(PersistenceUnit persistenceUnit,
                String clazz,  PUDataObject puDataObject, FileObject parentFile) {
            super(persistenceUnit, clazz, puDataObject, parentFile);
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {clazz};
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlSafeDelete.class, "TXT_PersistenceXmlSafeDeleteClass"), args);
        }
        
        public void undoChange() {
            ProviderUtil.addManagedClass(persistenceUnit, clazz, puDataObject);
        }
        
        public void performChange() {
            ProviderUtil.removeManagedClass(persistenceUnit, clazz, puDataObject);
        }
        
    }
    
}
