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

package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.websvc.wsitconf.refactoring.WSITRenameRefactoringPlugin;

/**
 *
 * @author Martin Grebac
 */
public class WSITRefactoringFactory implements RefactoringPluginFactory {
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.websvc.wsitconf.refactoring");

    /**
     * Creates a new instance of WSITRefactoringFactory
     */
    public WSITRefactoringFactory() { }

    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin should operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {

        LOGGER.log(Level.FINE, "Create instance called: " + refactoring);

        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph == null) return null;

        if (refactoring instanceof RenameRefactoring) {
            LOGGER.log(Level.FINE, "Rename refactoring");
            return new WSITRenameRefactoringPlugin((RenameRefactoring) refactoring);
        }
        
        if (refactoring instanceof SafeDeleteRefactoring) {
            LOGGER.log(Level.FINE, "Safe delete refactoring");
            return new WSITSafeDeleteRefactoringPlugin((SafeDeleteRefactoring) refactoring);
        }

        return null;
    }
    
}
