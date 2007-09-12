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
package org.netbeans.modules.bpel.properties.editors.controls;

import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/**
 * Provides a preferred action for the any nodes and their children.
 *
 * Be aware that all nodes of BpelNode type use another approach to gain the aim.
 * See the {@link PreferredActionProvider} interface.
 *
 * @author nk160297
 */

public class ActionProxyNode extends FilterNode {

    private static NodeAction myAction;

    public ActionProxyNode(Node original, NodeAction preferredAction) {
        super(original, new ProxyChildren(original));
        myAction = preferredAction;
    }

    public Action getPreferredAction() {
        return myAction;
    }
    
    /* 
     * This class is necessary for the the ClassRulesFilter
     */ 
    public Class getOriginalNodeClass() {
        Node original = getOriginal();
        return original == null ? null : original.getClass();
    }
    
    private static class ProxyChildren extends FilterNode.Children {
        
        public ProxyChildren(Node owner) {
            super(owner);
        }
        
        protected Node copyNode(Node original) {
            return new ActionProxyNode(original, myAction);
        }
    }
}
