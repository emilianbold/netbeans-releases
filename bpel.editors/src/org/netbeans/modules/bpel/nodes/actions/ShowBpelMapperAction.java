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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ShowBpelMapperAction extends BpelNodeAction {
    
    private static final String MAPPER_TC = "mapperTC"; // NOI18N
    
    protected String getBundleName() {
        return NbBundle.getMessage(ShowBpelMapperAction.class,
                "CTL_ShowBpelMapperAction"); // NOI18N
    }

    public ActionType getType() {
        return ActionType.SHOW_BPEL_MAPPER;
    }

    protected void performAction(BpelEntity[] bpelEntities) {
        TopComponent mapperTC = WindowManager.getDefault().findTopComponent(MAPPER_TC); // NOI18N
        if (mapperTC == null) {
            return;
        }
        
        if (!(mapperTC.isOpened())) {
            mapperTC.open();
        }
        mapperTC.requestVisible();
        mapperTC.requestActive();
    }
    
    public boolean isChangeAction() {
        return false;
    }    
}
