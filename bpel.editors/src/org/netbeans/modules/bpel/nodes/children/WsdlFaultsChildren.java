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
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Fault Nodes which correspods to faults
 * declared at the specified WSDL files and WSDL Import
 *
 * @author nk160297
 */
public class WsdlFaultsChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public WsdlFaultsChildren(WSDLModel wsdlModel, Lookup lookup) {
        myLookup = lookup;
        setKeys(new Object[] {wsdlModel});
    }
    
    public WsdlFaultsChildren(Import importObj, Lookup lookup) {
        myLookup = lookup;
        //
        WSDLModel wsdlModel = ImportHelper.getWsdlModel(importObj, true);
        if (wsdlModel != null) {
            setKeys(new Object[] {wsdlModel});
        }
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof WSDLModel)) {
            return null;
        }
        WSDLModel wsdlModel = (WSDLModel)key;
        ArrayList<Node> nodesList = new ArrayList<Node>();
        NodeFactory nodeFactory = myLookup.lookup(NodeFactory.class);
        BpelNode.DisplayNameComparator comparator =
                new BpelNode.DisplayNameComparator();
        //
        Set<String> faultNamesSet = new TreeSet<String>();
        //
        Collection<PortType> portTypes = wsdlModel.getDefinitions().getPortTypes();
        for (PortType portType : portTypes) {
            Collection<Operation> operations = portType.getOperations();
            for (Operation operation : operations) {
                Collection<Fault> faults = operation.getFaults();
                for (Fault fault : faults) {
                    faultNamesSet.add(fault.getName());
                }
            }
        }
        //
        String namespace = wsdlModel.getDefinitions().getTargetNamespace();
        BpelModel bpelModel = myLookup.lookup(BpelModel.class);
        NamespaceContext nsContext = bpelModel.getProcess().getNamespaceContext();
        String prefix = nsContext.getPrefix(namespace);
        //
        for (String faultName : faultNamesSet) {
            QName faultQName = null;
            //
            if (prefix == null || prefix.length() == 0) {
                faultQName = new QName(namespace, faultName);
            } else {
                faultQName = new QName(namespace, faultName, prefix);
            }
            //
            Node newNode = nodeFactory.createNode(
                    NodeType.FAULT, faultQName, myLookup);
            nodesList.add(newNode);
        }
        //
        Collections.sort(nodesList, comparator);
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
}
