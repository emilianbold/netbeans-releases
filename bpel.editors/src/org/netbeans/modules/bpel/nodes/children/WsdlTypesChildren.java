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
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Loads the list of Message Type nodes
 * related to the specific WSDL file or WSDL Import.
 * Also the "Embedded Schema Types" folder can be loaded. 
 *
 * Also the embedded Schema types are shown.
 *
 * @author nk160297
 */
public class WsdlTypesChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public WsdlTypesChildren(WSDLModel wsdlModel, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {wsdlModel});
    }
    
    public WsdlTypesChildren(Import importObj, Lookup lookup) {
        myLookup = lookup;
        //
        WSDLModel wsdlModel = ImportHelper.getWsdlModel(importObj, true);
        if (wsdlModel != null) {
            setKeys(new Object[] {wsdlModel});
        }
    }
    
    protected Node[] createNodes(Object key) {
        assert key instanceof WSDLModel;
        WSDLModel wsdlModel = (WSDLModel)key;
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        Children children = new WsdlEmbeddedSchemasChildren(wsdlModel, myLookup);
        Node newNode = new CategoryFolderNode(
                NodeType.EMBEDDED_SCHEMAS_FOLDER, children, myLookup);
        if (newNode != null) {
            nodesList.add(newNode);
        }
        //
        StereotypeFilter stFilter = (StereotypeFilter)myLookup
                .lookup(StereotypeFilter.class);
        boolean showMessage = true;
        if (stFilter != null) {
            showMessage = false;
            VariableStereotype[] vstTypes = stFilter.getAllowedStereotypes();
            for (VariableStereotype elem : vstTypes) {
                if (VariableStereotype.MESSAGE.equals(elem)) {
                    showMessage = true;
                }
            }
        }

        if (showMessage) {
            Collection<Message> messages = wsdlModel.getDefinitions().getMessages();
            for (Message message : messages) {
                newNode = nodeFactory.createNode(
                        NodeType.MESSAGE_TYPE, message, myLookup);
                nodesList.add(newNode);
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
}
