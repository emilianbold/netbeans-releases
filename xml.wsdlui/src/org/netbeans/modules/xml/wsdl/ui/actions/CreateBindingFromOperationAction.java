/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.wsdl.ui.actions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.cookies.CreateBindingFromOperationCookie;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CreateBindingFromOperationAction extends CommonNodeAction {

    private static final long serialVersionUID = 8950680955260279104L;

    @Override
    protected int mode() {
        return MODE_ALL;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {CreateBindingFromOperationCookie.class};
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null) {
            for (int i = 0; i < activatedNodes.length; i++) {
                Node node = activatedNodes[i];
                CreateBindingFromOperationCookie cookie = node.getCookie(CreateBindingFromOperationCookie.class);
                if (cookie != null) {
                    Operation operation = cookie.getOperation();
                    WSDLModel model = operation.getModel();
                    
                    model.startTransaction();
                    PortType portType = (PortType)operation.getParent();
                    Binding binding = model.getFactory().createBinding();
                    String bindingName = NameGenerator.getInstance().generateUniqueBindingName(model, portType.getName());
                    binding.setName(bindingName);
                    model.getDefinitions().addBinding(binding);
                    binding.setType(binding.createReferenceTo(portType, PortType.class));
                    BindingOperation bo = model.getFactory().createBindingOperation();
                    bo.setName(operation.getName());
                    binding.addBindingOperation(bo);
                    bo.setOperation(bo.createReferenceTo(operation, Operation.class));
                    Input input = operation.getInput();
                    if (input != null) {
                        BindingInput ip = model.getFactory().createBindingInput();
                        ip.setName(input.getName());
                        bo.setBindingInput(ip);
                    }
                    Output output = operation.getOutput();
                    if (output != null) {
                        BindingOutput op = model.getFactory().createBindingOutput();
                        op.setName(output.getName());
                        bo.setBindingOutput(op);
                    }
                    Collection faults = operation.getFaults();
                    if (faults != null && faults.size() > 0) {
                        Iterator faultIter = faults.iterator();
                        while (faultIter.hasNext()) {
                            Fault fault = (Fault) faultIter.next();
                            BindingFault bf = model.getFactory().createBindingFault();
                            bf.setName(fault.getName());
                            bo.addBindingFault(bf);
                        }
                    }
                    model.endTransaction();
                }
                
            }
        }

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CreateBindingFromOperationAction.class, "CreateBindingFromOperation_DISPLAY_NAME");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
