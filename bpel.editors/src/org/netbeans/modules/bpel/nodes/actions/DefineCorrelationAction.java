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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import java.awt.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.editors.DefineCorrelationWizard;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author Alex Petrov (27.12.2007)
 */
public class DefineCorrelationAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;

    public DefineCorrelationAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_DefineCorrelationAction")); // NOI18N
    }    
    
    @Override
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_DefineCorrelationAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.DEFINE_CORRELATION;
    }
    
    @Override
    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    @Override
    public void performAction(Node[] nodes) {
        final BpelNode bpelNode = (BpelNode) nodes[0];
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefineCorrelationWizard wizard = new DefineCorrelationWizard(bpelNode);
                wizard.showWizardDialog();
            }
        });
    }
    
    @Override
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        BpelEntity bpelEntity = bpelEntities[0];
        return (isAllowableBpelEntity(bpelEntity));
    }

    @Override
    public boolean enable(Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }
        if (!(nodes[0] instanceof BpelNode)) return false;
        BpelNode bpelNode = (BpelNode) nodes[0];
        return (isAllowableBpelNode(bpelNode));
    }
    
    protected static boolean isAllowableBpelNode(BpelNode bpelNode) {
        return ((bpelNode.getNodeType() == NodeType.RECEIVE)  || 
                (bpelNode.getNodeType() == NodeType.REPLY)    ||
                (bpelNode.getNodeType() == NodeType.INVOKE)   ||
                (bpelNode.getNodeType() == NodeType.ON_EVENT) ||
                (bpelNode.getNodeType() == NodeType.MESSAGE_HANDLER));
    }
    
    protected static boolean isAllowableBpelEntity(BpelEntity bpelEntity) {
        return ((bpelEntity instanceof Receive) || 
                (bpelEntity instanceof Reply)   ||
                (bpelEntity instanceof Invoke)   ||
                (bpelEntity instanceof OnEvent)   ||
                (bpelEntity instanceof OnMessage));
    }
}
