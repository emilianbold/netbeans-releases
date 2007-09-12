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
package org.netbeans.modules.bpel.nodes.refactoring;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.utils.RefactorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class UsageContextNode extends UsageFilterNode {
    private Node originalNode;
    public UsageContextNode(Node originalNode) {
        super(originalNode, Children.LEAF);
        this.originalNode = originalNode;
        disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                | DELEGATE_GET_NAME | DELEGATE_SET_NAME);
    }
    
    public String getHtmlDisplayName() {
        return getGraedContextName(getName());
    }
    
//    public String getName() {
//        Object ref = originalNode.getReference();
//        if (ref == null || !(ref instanceof BpelEntity)) {
//            return originalNode.getHtmlDisplayName();
//        }
//        return Util.getUsageContextPath((BpelEntity)ref, Sequence.class);
//    }
    
    public String getDisplayName() {
        return originalNode.getDisplayName();
    }
    
    // TODO r|m
    public String getName() {
//    public String getHtmlDisplayName() {
//        String  contextName = getName();
        if (!(originalNode  instanceof BpelNode)) {
            return originalNode.getHtmlDisplayName();
        }
        
        Object ref = ((BpelNode)originalNode).getReference();
        if (ref == null || !(ref instanceof BpelEntity)) {
            return originalNode.getHtmlDisplayName();
        }
        
        String contextName = null;
        NodeType nodeType = ((BpelNode)originalNode).getNodeType();
        switch (nodeType) {
            case VARIABLE_CONTAINER :
            case CORRELATION_SET_CONTAINER :
            case MESSAGE_EXCHANGE_CONTAINER :
                contextName = RefactorUtil.getUsageContextPath(
                        ((BpelNode)originalNode).getHtmlDisplayName()
                        , (BpelEntity)ref
                        , Sequence.class);
                break;
            default :
                contextName =
                        RefactorUtil.getUsageContextPath((BpelEntity)ref, Sequence.class);
        }
        
        if (contextName == null) {
            return originalNode.getHtmlDisplayName();
        }
// TODO r | a
//        contextName = getGraedContextName(contextName);
        return contextName;
    }
    
    public boolean canRename() {
        return false;
    }
    
    private String getGraedContextName(String contextPathName) {
        if (contextPathName == null) {
            return contextPathName;
        }
        
        int lastSepPosition = contextPathName.lastIndexOf(RefactorUtil.ENTITY_SEPARATOR);
        if (lastSepPosition > 0) {
            lastSepPosition++;
            contextPathName = SoaUiUtil.getGrayString(
                    "",contextPathName.substring(0,lastSepPosition)// NOI18N
                    ,contextPathName.substring(lastSepPosition), false);
        }
        return contextPathName;
    }
}
