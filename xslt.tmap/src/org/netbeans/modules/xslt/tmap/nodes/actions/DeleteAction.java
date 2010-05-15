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

import javax.swing.KeyStroke;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentContainer;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DeleteAction extends TMapAbstractNodeAction {

    private static final String DELETE_KEYSTROKE = "DELETE"; // NOI18N
    public DeleteAction() {
        super();
        putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(DELETE_KEYSTROKE));
    }

    @Override
    protected String getBundleName() {
        return NbBundle.getMessage(TMapAbstractNodeAction.class, "CTL_DeleteAction");
    }

    @Override
    public ActionType getType() {
        return ActionType.REMOVE;
    }

    @Override
    protected void performAction(TMapComponent[] tmapComponents) {
        if (!enable(tmapComponents)) {
            return;
        }
        
        TMapComponent component2remove = tmapComponents[0];
        TMapComponentContainer parent = (TMapComponentContainer)component2remove.getParent();
        assert parent != null;
        
        parent.remove(component2remove);
    }

    @Override
    protected boolean enable(TMapComponent[] tmapComponents) {
        return super.enable(tmapComponents) 
                && !(tmapComponents[0] instanceof TransformMap);
    }
    
}
