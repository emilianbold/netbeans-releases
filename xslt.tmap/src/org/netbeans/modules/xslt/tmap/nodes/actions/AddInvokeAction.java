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
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xslt.tmap.model.api.InvokeHandler;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.ui.editors.ServiceParamChooser;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 */
public class AddInvokeAction extends TMapAbstractNodeAction {

    @Override
    protected String getBundleName() {
        return NbBundle.getMessage(AddInvokeAction.class,
                "CTL_AddInvokeAction"); // NOI18N
    }

    @Override
    protected boolean enable(TMapComponent[] tmapComponents) {
        return tmapComponents != null && tmapComponents.length > 0 &&
                super.enable(tmapComponents) && tmapComponents[0] instanceof InvokeHandler;
    }

    @Override
    public ActionType getType() {
        return ActionType.ADD_INVOKE;
    }

    @Override
    protected void performAction(TMapComponent[] tmapComponents) {
        ServiceParamChooser chooser = new ServiceParamChooser(tmapComponents[0].getModel(),
                org.netbeans.modules.xml.wsdl.model.Operation.class);
        if (!ServiceParamChooser.showDlg(chooser)) {
            return; // The cancel is pressed
        }
        
        WSDLComponent selOperation = chooser.getSelectedValue();

        if (selOperation == null) {
            return;
        }
        if (!(selOperation instanceof org.netbeans.modules.xml.wsdl.model.Operation)) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(AddInvokeAction.class, "MSG_WrongElementWereSelected", selOperation.getClass(), Operation.class));
        }
        
        AddTMapComponentFactory.getInstaince().addInvoke((InvokeHandler)tmapComponents[0], (org.netbeans.modules.xml.wsdl.model.Operation)selOperation);
    }

}
