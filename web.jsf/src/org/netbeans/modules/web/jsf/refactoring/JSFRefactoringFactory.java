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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;

/**
 *
 * @author Petr Pisl
 */

public class JSFRefactoringFactory implements RefactoringPluginFactory {
    
    private static final Logger LOGGER = Logger.getLogger(JSFRefactoringFactory.class.getName());
    
    /** Creates a new instance of J2EERefactoringFactory */
    public JSFRefactoringFactory() { }
    
    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin should operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        RefactoringPlugin plugin = null;
        
        LOGGER.fine("Create instance called: " + refactoring);                  //NOI18N
        if (refactoring instanceof RenameRefactoring) {
            LOGGER.fine("Rename refactoring");                                  //NOI18N
            plugin =  new JSFRenamePlugin((RenameRefactoring)refactoring);
        } else if (refactoring instanceof WhereUsedQuery) {
            LOGGER.fine("Where used refactoring");                              //NOI18N
            plugin = new JSFWhereUsedPlugin((WhereUsedQuery)refactoring);
        } else if (refactoring instanceof MoveRefactoring) {
            LOGGER.fine("Move refactoring");                                    //NOI18N
            plugin = new JSFMoveClassPlugin((MoveRefactoring)refactoring);
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            LOGGER.fine("Safe delete refactoring");                             //NOI18N
            return new JSFSafeDeletePlugin((SafeDeleteRefactoring)refactoring);
        }
        return plugin;
    }
}
