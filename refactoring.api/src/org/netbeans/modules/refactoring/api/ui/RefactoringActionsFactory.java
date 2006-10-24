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
import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.RefactoringActionsFactoryImplementation;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

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
    
    private static final Lookup.Result<RefactoringActionsFactoryImplementation> implementations =
        Lookup.getDefault().lookup(new Lookup.Template(RefactoringActionsFactoryImplementation.class));

    public static boolean canRename(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canRename(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static Runnable renameImpl(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canRename(lookup)) {
                return rafi.renameImpl(lookup);
            }
        }
        return null;
    }

    public static boolean canFindUsages(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canFindUsages(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static Runnable findUsagesImpl(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canFindUsages(lookup)) {
                return rafi.findUsagesImpl(lookup);
            }
        }
        return null;
    }
    public static boolean canDelete(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canDelete(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static Runnable deleteImpl(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canDelete(lookup)) {
                return rafi.deleteImpl(lookup);
            }
        }
        return null;
    }
    
    public static Runnable moveImpl(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canMove(lookup)) {
                return rafi.moveImpl(lookup);
            }
        }
        return null;
    }
    
    public static boolean canMove(Lookup lookup) {
        for (RefactoringActionsFactoryImplementation rafi: implementations.allInstances()) {
            if (rafi.canMove(lookup)) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * Factory method for rename action
//     * @return instance of RenameAction
//     */
//    public static ContextAwareAction renameImpl() {
//        return (RenameAction) RenameAction.findObject(RenameAction.class, true);
//    }
//
//    /**
//     * Factory method for MoveClassAction
//     * @return an instance of MoveClassAction
//     */
//    public static ContextAwareAction moveClassAction() {
//        return (MoveClassAction) MoveClassAction.findObject(MoveClassAction.class, true);
//    }
//
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
//     * Factory method for SafeDeleteAction2
//     * @return an instance of SafeDeleteAction2
//     */
//    public static ContextAwareAction safeDeleteAction() {
//        return (SafeDeleteAction2) SafeDeleteAction2.findObject(SafeDeleteAction2.class, true);
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
