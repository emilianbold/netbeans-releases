/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.awt.Dialog;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.BindingConfigurationDialogPanel;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.BindingGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class BindingAndServiceNewType extends NewType {
    private PortType mPortType;
    
    public BindingAndServiceNewType(WSDLComponent portType) {
        mPortType = (PortType) portType;
    }
    
    
    @Override
    public String getName() {
        return NbBundle.getMessage(BindingAndServiceNewType.class, "LBL_NewType_BindingAndService");
    }
    
    
    @Override
    public void create() throws IOException {
        WSDLModel model = mPortType.getModel();
        BindingConfigurationDialogPanel panel = new BindingConfigurationDialogPanel(model, mPortType);
        final DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(BindingAndServiceNewType.class, "LBL_Generate_Binding_and_ServicePort"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(BindingAndServiceNewType.class),
                null);
        panel.setDialogDescriptor(descriptor);
        
        panel.setAutoCreateServicePort(true);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
        
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            String bindName = panel.getBindingName();
            
            panel.commit();
            if (model.isIntransaction()) {
                model.endTransaction();
            }
            Binding binding = model.findComponentByName(bindName, Binding.class);
            if (binding != null) {
                ActionHelper.selectNode(binding);
            }
        } else {
            panel.rollback();
            if (model.isIntransaction()) {
                model.rollbackTransaction();
            }
        }
    }
    
}
