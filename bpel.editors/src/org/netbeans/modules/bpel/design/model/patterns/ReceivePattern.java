/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
