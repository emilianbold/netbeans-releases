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

package org.netbeans.modules.apisupport.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveClassRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;

/**
 * netbeans related support for refactoring
 * @author Milos Kleint
 */
public class NbRefactoringFactory implements RefactoringPluginFactory {
    

    /**
     * Creates a new instance of NbRefactoringFactory
     */
    public NbRefactoringFactory() { }

    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin shimport org.openide.ErrorManager;
ould operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        if (refactoring instanceof WhereUsedQuery) {
            return new NbWhereUsedRefactoringPlugin(refactoring);
        }
        
        if (refactoring instanceof RenameRefactoring) {
            return new NbRenameRefactoringPlugin(refactoring); 
        }
        if (refactoring instanceof MoveClassRefactoring) {
            return new NbMoveRefactoringPlugin(refactoring);
        }
        if (refactoring instanceof SafeDeleteRefactoring) {
            return new NbSafeDeleteRefactoringPlugin(refactoring);
        }
        return null;
    }
    
}
