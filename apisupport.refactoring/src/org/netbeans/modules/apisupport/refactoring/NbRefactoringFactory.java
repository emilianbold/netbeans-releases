/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.ChangeParametersRefactoring;
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
