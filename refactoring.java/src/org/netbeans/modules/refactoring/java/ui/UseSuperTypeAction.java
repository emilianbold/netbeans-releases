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

package org.netbeans.modules.refactoring.java.ui;

import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/*
 * UseSuperTypeAction.java
 *
 * Created on June 20, 2005
 * @author Bharath Ravikumar
 */
/**
 * The Action class for the use super type refactoring
 */
public class UseSuperTypeAction extends JavaRefactoringGlobalAction{
    
    private TreePathHandle classTreeHandle = null;
    
    /** Creates a new instance of UseSuperTypeAction */
    public UseSuperTypeAction() {
        super(NbBundle.getMessage(UseSuperTypeAction.class, "LBL_UseSuperType_Action"), null); // NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void performAction(Lookup context) {
        JavaActionsImplementationFactory.doUseSuperType(context);
    }

    protected boolean enable(Lookup context) {
        return JavaActionsImplementationFactory.canUseSuperType(context);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
}