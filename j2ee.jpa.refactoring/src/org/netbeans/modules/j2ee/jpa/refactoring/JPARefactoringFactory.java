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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.jpa.refactoring.rename.PersistenceXmlRename;
import org.netbeans.modules.j2ee.jpa.refactoring.safedelete.PersistenceXmlSafeDelete;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;

/**
 * A refactoring factory for creating JPA refactoring plugins.
 *
 * @author Erno Mononen
 */
public class JPARefactoringFactory implements RefactoringPluginFactory{
    
    public JPARefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {

        List<JPARefactoring> refactorings = new ArrayList<JPARefactoring>();
        
        if (refactoring instanceof RenameRefactoring) {
            RenameRefactoring rename = (RenameRefactoring) refactoring;
            refactorings.add(new PersistenceXmlRename(rename));
            return new JPARefactoringPlugin(refactorings);
        }
        if (refactoring instanceof SafeDeleteRefactoring) {
            SafeDeleteRefactoring safeDeleteRefactoring = (SafeDeleteRefactoring) refactoring;
            refactorings.add(new PersistenceXmlSafeDelete(safeDeleteRefactoring));
            return new JPARefactoringPlugin(refactorings);
        }
        if (refactoring instanceof WhereUsedQuery) {
            WhereUsedQuery whereUsedQuery = (WhereUsedQuery) refactoring;
            //TODO: WhereUsedQuery#getRefactoringSource returns null, so this doesn't work.
            // either invalid usage or a bug in the refactoring api
            //            refactorings.add(new PersistenceXmlWhereUsed(whereUsedQuery, tph));
            //            return new JPARefactoringPlugin(refactorings);
            return null;
        }
        
        return null;
    }
    
}
