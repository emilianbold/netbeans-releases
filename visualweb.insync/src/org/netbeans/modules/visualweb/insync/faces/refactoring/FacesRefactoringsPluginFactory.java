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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.lang.reflect.Field;
import java.util.Collection;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.util.Exceptions;

/**
 * 
 */
public class FacesRefactoringsPluginFactory implements RefactoringPluginFactory {
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        // Disable for M8
        if (true) {
            return null;
        }
        boolean userInvokedRefactoring = isUserInvokedRefactoring(refactoring);
        if (refactoring instanceof RenameRefactoring) {
            RenameRefactoring renameRefactoring = (RenameRefactoring) refactoring;
            return newRenamePlugin(renameRefactoring, userInvokedRefactoring);
        } else if (refactoring instanceof MoveRefactoring) { 
            MoveRefactoring moveRefactoring = (MoveRefactoring) refactoring;
            return newMovePlugin(moveRefactoring, userInvokedRefactoring);
        }
        return null;
    }
    
    protected FacesRenameRefactoringPlugin newRenamePlugin(RenameRefactoring refactoring, boolean userInvokedRefactoring) {        
        FacesRenameRefactoringPlugin plugin = new FacesRenameRefactoringPlugin(refactoring, userInvokedRefactoring);        
        return plugin;
    }

    protected FacesMoveRefactoringPlugin newMovePlugin(MoveRefactoring refactoring, boolean userInvokedRefactoring) {
        FacesMoveRefactoringPlugin plugin = new FacesMoveRefactoringPlugin(refactoring, userInvokedRefactoring);
        // TODO Check if we should participate
        return plugin;
    }
    
    // Hacky way to get at the private state of RefactoringPlugin to determine if it
    // was invoked by the user
    private static Field currStateField;
    
    static {
        try {
            currStateField = AbstractRefactoring.class.getDeclaredField("currentState"); // NOI18N
            currStateField.setAccessible(true);
        } catch (SecurityException e) {
            Exceptions.printStackTrace(e);
        } catch (NoSuchFieldException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    private static boolean isUserInvokedRefactoring(AbstractRefactoring refactoring) {
        if (currStateField != null) {
            try {
                return currStateField.getInt(refactoring) == AbstractRefactoring.INIT;
            } catch (IllegalArgumentException e) {
                Exceptions.printStackTrace(e);
            } catch (IllegalAccessException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        return false;
    }
}
