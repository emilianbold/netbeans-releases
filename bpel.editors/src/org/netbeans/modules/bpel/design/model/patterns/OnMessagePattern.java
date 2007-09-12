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
import java.awt.geom.Area;
import java.util.Collection;
import org.netbeans.modules.bpel.design.geometry.FBounds;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;

public class OnMessagePattern
        extends CompositePattern {
    
    private VisualElement messageEvent;
    private Connection connection;
    private PlaceHolderElement placeHolder;
    
    
    public OnMessagePattern(DiagramModel model) {
        super(model);
    }
    
    
    public VisualElement getFirstElement() {
        return messageEvent;
    }
    
    
    public VisualElement getLastElement() {
        if (getNestedPatterns().isEmpty()) {
            return placeHolder;
        }
        return getNestedPattern().getLastElement();
    }
    
    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setText(getDefaultName()); 
        
        messageEvent = ContentElement.createMessageEvent();
        messageEvent.setLabelText("Message"); // NOI18N
        appendElement(messageEvent);
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        BpelEntity activity = ((OnMessage) getOMReference()).getActivity();
        
        if (activity != null) {
            Pattern p = getModel().createPattern(activity);
            p.setParent(this);
        }
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        double height = messageEvent.getHeight() + LayoutManager.HSPACING;
        double width = messageEvent.getWidth();
        
        double eventX;
        
        if (getNestedPatterns().isEmpty()) {
            height += placeHolder.getHeight();
            width = Math.max(width, placeHolder.getWidth());
            
            placeHolder.setCenter(0, height / 2 - placeHolder.getHeight() / 2);
            
            eventX = 0;
        } else {
            Pattern p = getNestedPattern();
            FBounds pSize = p.getBounds();
            eventX = -pSize.width / 2 + manager.getOriginOffset(p).x;
            
            height += pSize.height;
            width = Math.max(width, pSize.width);
            manager.setPatternPosition(p, 
                    -pSize.width / 2, height / 2 - pSize.height);
            
//          System.out.println("Pattern class=" + p.getClass());
//          System.out.println("OriginOffsetX=" + manager.getOriginOffset(p).x);
//          System.out.println("Width=" + pSize.width);
        }
        
        messageEvent.setCenter(eventX, -height / 2 + messageEvent.getHeight() / 2);
        
        
        getBorder().setClientRectangle(-width / 2, -height / 2,
                width, height);
        return getBorder().getBounds();     
    }
    
    
    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders)
    {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    public void onAppendPattern(Pattern p) {
        removeElement(placeHolder);
    }

    
    protected void onRemovePattern(Pattern p) {
        appendElement(placeHolder);
    }

    
    public String getDefaultName() {
        return "OnMessage"; // NOI18N
    }
    
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_HANDLER;
    }

    
    public void reconnectElements() {
        if (connection == null) {
            connection = new Connection(this);
        }

        if (getNestedPatterns().isEmpty()){
            connection.connect(messageEvent, Direction.BOTTOM, 
                    placeHolder, Direction.TOP);
        } else {
            connection.connect(messageEvent, Direction.BOTTOM, 
                    getNestedPattern().getFirstElement(), Direction.TOP);
        }
        clearConnectionsExcept(connection);
        new PartnerLinkHelper(getModel()).updateMessageFlowLinks(this);
    }


    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(messageEvent.getShape()));
        return res;
    }

    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(OnMessagePattern.this, draggedPattern, 
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            Pattern p = getDraggedPattern();
            ((OnMessage) getOMReference()).setActivity((Activity) p
                    .getOMReference());
        }
    }
    
}
