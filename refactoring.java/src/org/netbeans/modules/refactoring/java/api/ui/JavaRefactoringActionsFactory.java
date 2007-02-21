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

import org.netbeans.modules.refactoring.java.ui.ExtractInterfaceAction;
import org.openide.util.ContextAwareAction;

/**
 * Factory class providing instances of refactoring actions.
 * <p><b>Usage:</b></p>
 * <pre>
 * InstanceContent ic = new InstanceContent();
 * ic.add(node);
 * Lookup l = new AbstractLookup(ic);
 * Action a = RefactoringActionsFactory.encapsulateFieldsAction().createContextAwareInstance(l);
 * a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
 * </pre>
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 * 
 * @author Jan Becicka
 */
public final class JavaRefactoringActionsFactory {
    
    private JavaRefactoringActionsFactory(){}
    
   /**
     * Factory method for EncapsulateFieldsAction
     * @return an instance of EncapsulateFieldsAction
     */
    public static ContextAwareAction encapsulateFieldsAction() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return (EncapsulateFieldAction) EncapsulateFieldAction.findObject(EncapsulateFieldAction.class, true);
    }
    
    /**
     * Factory method for ChangeParametersAction
     * @return an instance of ChangeParametersAction
     */
    public static ContextAwareAction changeParametersAction() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return (ChangeParametersAction) ChangeParametersAction.findObject(ChangeParametersAction.class, true);
    }
    
    
    /**
     * Factory method for PullUpAction
     * @return an instance of PullUpAction
     */
    public static ContextAwareAction pullUpAction() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return (PullUpAction) PullUpAction.findObject(PullUpAction.class, true);
    }
    
    /**
     * Factory method for PushDownAction
     * @return an instance of PushDownAction
     */
    public static ContextAwareAction pushDownAction() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return (PushDownAction) PushDownAction.findObject(PushDownAction.class, true);
    }
    
    /**
     * Factory method for InnerToOuterAction
     * @return an instance of InnerToOuterAction
     */
    public static ContextAwareAction innerToOuterAction() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return (InnerToOuterAction) InnerToOuterAction.findObject(InnerToOuterAction.class, true);
    }

    /**
     * Factory method for UseSuperTypeAction
     * @return an instance of UseSuperTypeAction
     */
    public static ContextAwareAction useSuperTypeAction() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return (UseSuperTypeAction) UseSuperTypeAction.findObject(UseSuperTypeAction.class, true);
    }
    
    public static ContextAwareAction extractSuperclassAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static ContextAwareAction extractInterfaceAction() {
        return (ExtractInterfaceAction) ExtractInterfaceAction.findObject(ExtractInterfaceAction.class, true);
    }
}
