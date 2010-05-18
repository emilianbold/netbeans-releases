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
 */
public class GoToSourceAction extends TMapAbstractNodeAction {

    @Override
    protected String getBundleName() {
        return NbBundle.getMessage(GoToSourceAction.class,
                "CTL_GoToSourceAction"); // NOI18N
    }

    @Override
    public ActionType getType() {
        return ActionType.GO_TO_SOURCE;
    }

    @Override
    protected void performAction(TMapComponent[] tmapComponents) {
        if (tmapComponents != null && tmapComponents.length > 0) {
            TMapUtil.goToSourceView(tmapComponents[0]);
        }
    }

}
