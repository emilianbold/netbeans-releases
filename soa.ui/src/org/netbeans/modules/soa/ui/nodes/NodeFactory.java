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


package org.netbeans.modules.soa.ui.nodes;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public interface NodeFactory<KeyType> {

    /**
     * Create a new instance of Node by a key.
     * Current implementation implies different Pattern classes as keys.
     * <p>
     * This method implies that the Node's children are defined by it own.
     * If the Node doesn't define any Children then it considered as a LEAF by default.
     * <p>
     * @param key specifies the node type. 
     * @param ref specifies the object the new node should represent. 
     * @param lookup parameter contains all other context.
     * @return the new Node
     */
    Node createNode(KeyType nodeType, Object ref, Lookup lookup);
    
    /**
     * Creates a new instance of Node by a key. 
     * Current implementation implies different Pattern classes as keys.
     * <p>
     * @param key specifies the node type. 
     * @param ref specifies the object the new node should represent. 
     * @param children provides way to load node's children. 
     * @param lookup parameter contains all other context.
     * @return the new Node
     */
    Node createNode(KeyType nodeType, Object ref, 
            Children children, Lookup lookup);
    
    /**
     * This node can be used as a simple node with text. 
     * It's especially helpfull to create a hidden root node. 
     */
    static class TextNode extends AbstractNode {

        public TextNode(Children children, String name) {
            super(children);
            setName(name);
        }

        public String getHtmlDisplayName() {
            return getName();
        }
        
        public Image getIcon(int type) {
            return null;
        }
        
        public Image getOpenedIcon(int type) {
            return null;
        }
    }
 
}
