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
package org.netbeans.modules.xslt.tmap.nodes.actions;

import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.util.TMapUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class GoToTreeAction extends TMapAbstractNodeAction {

    private static final long serialVersionUID = 1L;

    public GoToTreeAction() {
        super();
    }

    @Override
    protected boolean enable(TMapComponent[] tmapComponents) {
        return super.enable(tmapComponents) && tmapComponents != null 
                && tmapComponents.length > 0 && isTreeVisibleComponent(tmapComponents[0]);
    }
    
    private boolean isTreeVisibleComponent(TMapComponent component) {
        return true;
    }

    protected String getBundleName() {
        return NbBundle.getMessage(GoToTreeAction.class,
                "CTL_GoToTreeAction"); // NOI18N
    }

    public ActionType getType() {
        return ActionType.GO_TO_TREE;
    }

    @Override
    protected void performAction(TMapComponent[] tmapComponents) {
        if (tmapComponents != null && tmapComponents.length > 0) {
            TMapUtil.goToTreeView(tmapComponents[0]);
        }
    }
}
