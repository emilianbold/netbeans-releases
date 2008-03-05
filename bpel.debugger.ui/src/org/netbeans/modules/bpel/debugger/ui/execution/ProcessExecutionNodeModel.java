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

import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * Model describing nodes in the tree column of the Process Execution View.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessExecutionNodeModel implements NodeModel {
    /**{@inheritDoc}*/
    public String getDisplayName(
            final Object object) throws UnknownTypeException {
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                    ProcessExecutionNodeModel.class, 
                    "CTL_PEV_Column_Name"); // NOI18N
        }
        
        if (object instanceof ProcessExecutionTreeModel.Dummy) {
            return NbBundle.getMessage(
                    ProcessExecutionNodeModel.class, 
                    "CTL_PEV_Empty_Model"); // NOI18N
        }
        
        return Helper.getDisplayName(object, true);
    }
    
    /**{@inheritDoc}*/
    public String getShortDescription(
            final Object object) throws UnknownTypeException {
        if (object == TreeModel.ROOT) {
            return NbBundle.getMessage(
                ProcessExecutionNodeModel.class, 
                "CTL_PEV_Column_Name_Tooltip"); // NOI18N
        }
        
        if (object instanceof ProcessExecutionTreeModel.Dummy) {
            getDisplayName(object);
        }
        
        return Helper.getDisplayName(object, false);
    }
    
    /**{@inheritDoc}*/
    public String getIconBase(
            final Object object) throws UnknownTypeException {
        if (object == TreeModel.ROOT) {
            return Constants.ROOT_ICON;
        } 
        
        if (object instanceof ProcessExecutionTreeModel.Dummy) {
            return null;
        }
        
        return Helper.getIconBase(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        // does nothing
    }
    
    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        // does nothing
    }
}
