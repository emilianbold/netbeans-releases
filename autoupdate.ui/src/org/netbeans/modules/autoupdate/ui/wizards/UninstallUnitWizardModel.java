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

package org.netbeans.modules.autoupdate.ui.wizards;

import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.modules.autoupdate.ui.Containers;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallUnitWizardModel extends OperationWizardModel {
    private OperationType operationType;
    
    /** 
     @param doAction if is null it means doUninstall, false means doDisable, true means doEnable
     */
    public UninstallUnitWizardModel (OperationType doOperation) {
        this.operationType = doOperation;
        switch (operationType) {
            case UNINSTALL :
                assert Containers.forUninstall () != null;
                break;
            case ENABLE :
                assert Containers.forEnable () != null;
                break;
            case DISABLE :
                assert Containers.forDisable () != null;
                break;
            default:
                assert false;
        }
    }
    
    public OperationType getOperation () {
        return operationType;
    }
    
    public OperationContainer getContainer () {
        switch (operationType) {
            case UNINSTALL :
                return Containers.forUninstall ();
            case ENABLE :
                return Containers.forEnable ();
            case DISABLE :
                return Containers.forDisable ();
            default:
                assert false;
        }
        return null;
    }
    
    @Override
    public boolean hasBrokenDependencies () {
        // doesn't matter broken dependencies when uninstall
        return false;
    }
}
