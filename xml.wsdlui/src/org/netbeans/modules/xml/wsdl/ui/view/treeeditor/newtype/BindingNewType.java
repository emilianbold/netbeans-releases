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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.io.IOException;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class BindingNewType extends NewType {
    private WSDLComponent mDef = null;

    public BindingNewType(WSDLComponent def) {
        mDef = def;
    }
    

    @Override
    public String getName() {
        return NbBundle.getMessage(BindingNewType.class, "LBL_NewType_Binding");
    }


    @Override
    public void create() throws IOException {
        WSDLModel model = mDef.getModel();
        model.startTransaction();
        Binding binding = model.getFactory().createBinding();
        String bindingName = NameGenerator.getInstance().generateUniqueBindingName(model);
        binding.setName(bindingName);
        model.getDefinitions().addBinding(binding);
        model.endTransaction();
        ActionHelper.selectNode(binding);
    }
}
