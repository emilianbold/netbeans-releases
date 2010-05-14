/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.bridge.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.dndbridge.api.DnDHandler;
import org.netbeans.modules.soa.dndbridge.api.DropResult;
import org.netbeans.modules.soa.dndbridge.api.VariableType;
import org.netbeans.modules.wlm.bridge.wizard.WLMWizardAction;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mbhasin
 */
public class DnDHandlerImpl implements DnDHandler {

    public boolean isSupported(Transferable t) {
        boolean isWLMPalletDrop = false;
        try {
            for (DataFlavor flavor : t.getTransferDataFlavors()) {
                Object data = t.getTransferData(flavor);
                Class repClass = flavor.getRepresentationClass();
                if (Node.class.isAssignableFrom(repClass)) {
                    String item = ((Node) data).getName();
                    if (item.equals("humanworkflow")) {
                        isWLMPalletDrop = true;
                        break;
                    }
                }
            }
        } catch (UnsupportedFlavorException ex) {
        } catch (IOException ex) {
        }

        return isWLMPalletDrop;
    }

    public DropResult handleDrop(ModelSource ms, List<VariableType> messages, 
            String namespaceBase) 
    {
        FileObject fo1 = ms.getLookup().lookup(FileObject.class);
        Project proj = FileOwnerQuery.getOwner(fo1);
        FileObject sourceLocation = proj.getProjectDirectory();
        WLMWizardAction action = new WLMWizardAction(sourceLocation, messages, 
                namespaceBase);
        WSDLModel taskServiceWsdlModel = action.performAction();
        return getDropResult(taskServiceWsdlModel);
    }

    private DropResult getDropResult(WSDLModel wsdlModel) {
        // This check was added by Anjeleevich
        // to prevent NPE when wizard was canceled
        if (wsdlModel == null) {
            return null;
        }
        
        Collection<PortType> portTypes = wsdlModel.getDefinitions()
                .getPortTypes();
        Collection<Operation> operations = portTypes.iterator().next()
                .getOperations();
        Operation op = operations.iterator().next();

        VariableType inputType = new VariableType(op.getInput().getMessage().get(), "");
        VariableType outputType = new VariableType(op.getOutput().getMessage().get(), "");
        Collection<Fault> faults = op.getFaults();
        Iterator iter = faults.iterator();
        Fault fault = null;

        VariableType faultType = null;
        if (iter.hasNext()) {
            fault = (Fault) iter.next();
            faultType = new VariableType(fault.getMessage().get(), "");
        }

        DropResult dropResult = new DropResult(inputType, outputType, faultType, op);
        return dropResult;
    }
}
