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

import java.util.logging.Level;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.model.spi.NameGenerator;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 */
public class AddParamAction extends TMapAbstractNodeAction {

    @Override
    protected String getBundleName() {
        return NbBundle.getMessage(AddParamAction.class,
                "CTL_AddParamAction"); // NOI18N
    }

    @Override
    protected boolean enable(TMapComponent[] tmapComponents) {
        return tmapComponents != null && tmapComponents.length > 0 &&
                super.enable(tmapComponents) && tmapComponents[0] instanceof Transform;
    }

    @Override
    public ActionType getType() {
        return ActionType.ADD_PARAM;
    }

    @Override
    protected void performAction(TMapComponent[] tmapComponents) {
        if (!enable(tmapComponents)) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(
                    TMapAbstractNodeAction.class, "MSG_NonEnabledActionInvoked", this.getClass()));
            return;
        }
        Transform transform = (Transform)tmapComponents[0];
        
        TMapModel model = transform.getModel();
        if (model != null) {
            try {
                Param param = model.getFactory().createParam();
                param.setType(ParamType.PART);
                param.setName(NameGenerator.getUniqueName(transform, Param.class));


                assert param != null;
                transform.addParam(param);
            } catch (VetoException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }

}
