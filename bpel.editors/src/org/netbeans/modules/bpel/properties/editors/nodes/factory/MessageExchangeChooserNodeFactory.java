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
package org.netbeans.modules.bpel.properties.editors.nodes.factory;

import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.nodes.children.MessageExchangeChildren;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This factory create the nodes tree for the Message Exchange chooser.
 * It contains the following structure:
 *
 * Process
 *  |-Process
 *     |-Scope
 *        |- Message Exchange 1
 *        |- Message Exchange 2
 *     |- Message Exchange 1
 *     |- Message Exchange 2
 *
 * @author nk160297
 */
public class MessageExchangeChooserNodeFactory implements NodeFactory<NodeType> {
    
    private NodeFactory myDelegate;
    
    /** Creates a new instance of PropertyChooserNodeFactory */
    public MessageExchangeChooserNodeFactory(NodeFactory delegate) {
        myDelegate = delegate;
    }
    
    public Node createNode(NodeType nodeType, Object ref, 
            Children children, Lookup lookup) {
        return myDelegate.createNode(nodeType, ref, children, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        //
        Children children;
        Node newNode;
        //
        NodeFactory nodeFactory =
                (NodeFactory)lookup.lookup(NodeFactory.class);
        if (!(nodeFactory instanceof MessageExchangeChooserNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case PROCESS:
            case SCOPE:
                assert ref instanceof BaseScope;
                children = new MessageExchangeChildren((BaseScope)ref, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case MESSAGE_EXCHANGE:
                assert ref instanceof MessageExchange;
                newNode = myDelegate.createNode(nodeType, ref, Children.LEAF, lookup);
                return newNode;
            default:
                newNode = myDelegate.createNode(nodeType, ref, lookup);
                return newNode;
        }
    }
}
