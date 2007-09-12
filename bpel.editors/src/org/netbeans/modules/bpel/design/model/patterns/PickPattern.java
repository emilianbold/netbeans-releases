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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;

import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.ConnectionManager;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;


public class PickPattern extends CompositePattern {
    
    private VisualElement forkGateway;
    private VisualElement mergeGateway;
    private List<Connection> onAlarmConnectionsStart = new ArrayList<Connection>();
    private List<Connection> onAlarmConnectionsEnd = new ArrayList<Connection>();
    
    private List<Connection> onMessageConnectionsStart = new ArrayList<Connection>();
    private List<Connection> onMessageConnectionsEnd = new ArrayList<Connection>();
    
    
    public PickPattern(DiagramModel model) {
        super(model);
    }
    
    public VisualElement getFirstElement() {
        return forkGateway;
    }
    
    
    public VisualElement getLastElement() {
        return mergeGateway;
    }
    
    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        registerTextElement(getBorder());
        
        forkGateway = ContentElement.createPickGateway();
        forkGateway.setLabelText(""); // NOI18N
        mergeGateway = ContentElement.createPickGateway();
        mergeGateway.setLabelText(""); // NOI18N
        
        appendElement(forkGateway);
        appendElement(mergeGateway);
        
        OnMessage[] onMessages = ((Pick) getOMReference()).getOnMessages();
        for (OnMessage onMessage : onMessages) {
            Pattern p = getModel().createPattern(onMessage);
            p.setParent(this);
        }
        
        OnAlarmPick[] onAlarms = ((Pick) getOMReference()).getOnAlarms();
        for (OnAlarmPick onAlarm : onAlarms) {
            Pattern p = getModel().createPattern(onAlarm);
            p.setParent(this);
        }
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        OnMessage[] onMessages = ((Pick) getOMReference()).getOnMessages();
        for (OnMessage onMessage : onMessages) {
            patterns.add(getNestedPattern(onMessage));
        }
        
        OnAlarmPick[] onAlarms = ((Pick) getOMReference()).getOnAlarms();
        for (OnAlarmPick onAlarm : onAlarms) {
            patterns.add(getNestedPattern(onAlarm));
        }
        
        float width = 0;
        float height = 24f;
        
        for (Pattern p : patterns) {
            FDimension pSize = p.getBounds().getSize();
            height = Math.max(height, pSize.height);
            width += pSize.width;
        }
        
        width += LayoutManager.VSPACING * (patterns.size() - 1);
        height += LayoutManager.HSPACING * 2 + forkGateway.getHeight() + mergeGateway.getWidth();
        
        width = Math.max(width, 40f);
        
        forkGateway.setCenter(0, -height / 2 + forkGateway.getHeight() / 2);
        
        mergeGateway.setCenter(0, height / 2 - mergeGateway.getHeight() / 2);
        
        float x = - width / 2;
        
        for (Pattern p : patterns) {
            FDimension pSize = p.getBounds().getSize();
            manager.setPatternPosition(p, x, -pSize.height / 2);
            x += pSize.width + LayoutManager.VSPACING;
        }
        
        getBorder().setClientRectangle( -width / 2, -height / 2,
                width, height);
        return getBorder().getBounds();
        
        
    }
    
    
    public String getDefaultName() {
        return "Pick";
    }
    
    public NodeType getNodeType() {
        return NodeType.PICK;
    }
    
    protected void onAppendPattern(Pattern nestedPattern) {
    }
    
    protected void onRemovePattern(Pattern p){
        
    }
    
    public void reconnectElements() {
        Pick pick = (Pick) getOMReference();
        
        OnAlarmPick[] onAlarms = pick.getOnAlarms();
        OnMessage[] onMessages = pick.getOnMessages();
        
        ensureConnectionsCount(onAlarmConnectionsStart, onAlarms.length);
        ensureConnectionsCount(onAlarmConnectionsEnd, onAlarms.length);
        
        ensureConnectionsCount(onMessageConnectionsStart, onMessages.length);
        ensureConnectionsCount(onMessageConnectionsEnd, onMessages.length);
        
        double x11 = forkGateway.getX();
        double x12 = x11 + forkGateway.getWidth();
        
        double x21 = mergeGateway.getX();
        double x22 = x21 + mergeGateway.getY();
        
        for (int i = 0; i < onAlarms.length; i++) {
            Pattern p = getNestedPattern(onAlarms[i]);
            
            Connection cs = onAlarmConnectionsStart.get(i);
            Connection ce = onAlarmConnectionsEnd.get(i);
            
            ConnectionManager.connectVerticaly(forkGateway, cs, p,
                    ce, mergeGateway);
        }
        
        for (int i = 0; i < onMessages.length; i++) {
            Pattern p = getNestedPattern(onMessages[i]);
            
            Connection cs = onMessageConnectionsStart.get(i);
            Connection ce = onMessageConnectionsEnd.get(i);
            
            ConnectionManager.connectVerticaly(forkGateway, cs, p,
                    ce, mergeGateway);
        }
    }
    
    
    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(forkGateway.getShape()));
        res.subtract(new Area(mergeGateway.getShape()));
        return res;
    }
    
    
}
