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


package org.netbeans.modules.bpel.design.model.patterns;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.DnDHandler;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.support.TBoolean;

/**
 *
 * @author Alexey Yarmolenko
 */
public class ReceivePattern extends BasicActivityPattern{
    /**
     * Creates a new instance of BasicActivityPattern.
     * @param view - DesignView used to draw diagram
     */
    
    public ReceivePattern(DiagramModel model) {
        super(model);
    }
    
    
    protected void createElementsImpl() {
        VisualElement element = ContentElement.createReceive();
        appendElement(element);
        registerTextElement(element);
    }
    
    
    public String getDefaultName() {
        return "Receive"; // NOI18N
    }
    
    public NodeType getNodeType() {
        return NodeType.RECEIVE;
    }
    
    public void reconnectElements() {
        clearConnections();
        new PartnerLinkHelper(getModel()).updateMessageFlowLinks(this);
    }
    
    public void setParent(CompositePattern newParent) {
        final Receive r = (Receive)getOMReference();
        if (r != null && r.getCookie(DnDHandler.class) == DnDHandler.class){
            
            r.removeCookie(DnDHandler.class);
            
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    BpelModel model = getBpelModel();
                    CreateInstanceActivity first = findCreateInstance(model.getProcess().getActivity());
                    if (r.equals(first)){
                        r.setCreateInstance(TBoolean.YES);
                    }
                }
            });
        }
        super.setParent(newParent);
    }
    
    
    private static CreateInstanceActivity findCreateInstance(BpelEntity entity) {
        if (entity instanceof CreateInstanceActivity){
            return (CreateInstanceActivity) entity;
        }
        if (entity instanceof BpelContainer){
            for (BpelEntity e: ((BpelContainer) entity).getChildren()){
                CreateInstanceActivity res = findCreateInstance(e);
                if (res != null){
                    return res;
                }
            }
        }
        return null;
    }
}
