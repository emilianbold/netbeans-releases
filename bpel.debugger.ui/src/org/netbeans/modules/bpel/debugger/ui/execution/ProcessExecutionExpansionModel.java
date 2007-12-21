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

package org.netbeans.modules.bpel.debugger.ui.execution;

import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Model describing the tree expansion/collapse behavior of the tree column of
 * the Process Execution View.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessExecutionExpansionModel implements TreeExpansionModel {
    
    /**{@inheritDoc}*/
    public boolean isExpanded(
            final Object object) throws UnknownTypeException {
        if (object instanceof PemEntity) {
            final PemEntity pemEntity = (PemEntity) object;
            
            if (pemEntity.getState() == PemEntity.State.STARTED) {
                return true;
            }
            
            final PemEntity lastStarted = 
                    pemEntity.getModel().getLastStartedEntity();
            if ((lastStarted != null) && lastStarted.isInTree(pemEntity)) {
                return true;
            }
            
            return false;
        } 
        
        if (object instanceof PsmEntity) {
            return false;
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void nodeExpanded(Object object) {
        // does nothing
    }

    /**{@inheritDoc}*/
    public void nodeCollapsed(Object object) {
        // does nothing
    }
}
