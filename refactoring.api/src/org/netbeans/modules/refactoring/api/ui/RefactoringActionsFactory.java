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

package org.netbeans.modules.refactoring.api.ui;

import java.awt.event.ActionEvent;
import org.netbeans.modules.refactoring.spi.impl.*;
import org.openide.util.ContextAwareAction;

/**
 * Factory class providing instances of refactoring actions.
 * <p><b>Usage:</b></p>
 * <pre>
 * ContextAwareAction rename = RefactoringActionsFactory.renameImpl().createContextAwareInstance(actionContext);
 * rename.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
 * </pre>
 * 
 * @author Jan Becicka
 */
public final class RefactoringActionsFactory {
    
    public static final ActionEvent DEFAULT_EVENT = new ActionEvent(new Object(), 0, null) {
        public void setSource(Object newSource) {        
            throw new UnsupportedOperationException();
        }
    };
    
    private RefactoringActionsFactory(){}
    

    /**
     * Factory method for rename action
     * @return instance of RenameAction
     */
    public static ContextAwareAction renameAction() {
        return RenameAction.findObject(RenameAction.class, true);
    }

    /**
     * Factory method for MoveAction
     * @return an instance of MoveAction
     */
    public static ContextAwareAction moveAction() {
        return MoveAction.findObject(MoveAction.class, true);
    }
    
    /**
     * Factory method for SafeDeleteAction
     * @return an instance of SafeDeleteAction
     */
    public static ContextAwareAction safeDeleteAction() {
        return SafeDeleteAction.findObject(SafeDeleteAction.class, true);
    }
    
    /**
     * Factory method for CopyAction
     * @return an instance of CopyAction
     */
    public static ContextAwareAction copyAction() {
        return SafeDeleteAction.findObject(CopyAction.class, true);
    }
    

//    /**
//     * Factory method for EncapsulateFieldsAction
//     * @return an instance of EncapsulateFieldsAction
//     */
//    public static ContextAwareAction encapsulateFieldsAction() {
//        return (EncapsulateFieldAction) EncapsulateFieldAction.findObject(EncapsulateFieldAction.class, true);
//    }
//    
//    /**
//     * Factory method for ChangeParametersAction
//     * @return an instance of ChangeParametersAction
//     */
//    public static ContextAwareAction changeParametersAction() {
//        return (ChangeParametersAction) ChangeParametersAction.findObject(ChangeParametersAction.class, true);
//    }
//    
//    /**
//     * Factory method for ExtractMethodAction
//     * @return an instance of ExtractMethodAction
//     */
//    public static ContextAwareAction extractMethodAction() {
//        return (ExtractMethodAction) ExtractMethodAction.findObject(ExtractMethodAction.class, true);
//    }
//    
//    /**
//     * Factory method for PullUpAction
//     * @return an instance of PullUpAction
//     */
//    public static ContextAwareAction pullUpAction() {
//        return (PullUpAction) PullUpAction.findObject(PullUpAction.class, true);
//    }
//    
//    /**
//     * Factory method for PushDownAction
//     * @return an instance of PushDownAction
//     */
//    public static ContextAwareAction pushDownAction() {
//        return (PushDownAction) PushDownAction.findObject(PushDownAction.class, true);
//    }
//    
//    /**
//     * Factory method for AnonymousToInnerAction
//     * @return an instance of AnonymousToInnerAction
//     */
//    public static ContextAwareAction anonymousToInnerAction() {
//        return (AnonymousToInnerAction) AnonymousToInnerAction.findObject(AnonymousToInnerAction.class, true);
//    }
//
//    /**
//     * Factory method for InnerToOuterAction
//     * @return an instance of InnerToOuterAction
//     */
//    public static ContextAwareAction innerToOuterAction() {
//        return (InnerToOuterAction) InnerToOuterAction.findObject(InnerToOuterAction.class, true);
//    }
//
//    /**
//     * Factory method for UseSuperTypeAction
//     * @return an instance of UseSuperTypeAction
//     */
//    public static ContextAwareAction useSuperTypeAction() {
//        return (UseSuperTypeAction) UseSuperTypeAction.findObject(UseSuperTypeAction.class, true);
//    }
}
