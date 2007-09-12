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
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.SubprocessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.RepeatUntil;

public class RepeatUntilPattern extends CompositePattern {
    
    private VisualElement gateway;
    private PlaceHolderElement placeHolder;
    private Connection connection1;
    private Connection connection2;
    
    
    public RepeatUntilPattern(DiagramModel model) {
        super(model);
    }
    
    
    public VisualElement getFirstElement() {
        return getBorder();
    }
     
    
    public VisualElement getLastElement() {
        return getBorder();
    }
    
    
    protected void createElementsImpl() {
        connection1 = new Connection(this);
        connection2 = new Connection(this);
        
        setBorder(new SubprocessBorder());
        registerTextElement(getBorder());
        
        gateway = ContentElement.createRepeatUntilGateway();
        appendElement(gateway);
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        RepeatUntil repeatUntilOM = (RepeatUntil) getOMReference();
        
        Activity activity = (Activity) repeatUntilOM.getActivity();
        
        if (activity != null) {
            Pattern p = getModel().createPattern(activity);
            p.setParent(this);
        }
    }
    
    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders) 
    {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        if (placeHolder.getPattern() != null) {
             placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        double width;
        double height;
        
        if (getNestedPatterns().isEmpty()) {
            placeHolder.setLocation(0, 0);
            
            width = placeHolder.getWidth();
            height = placeHolder.getHeight();
            
        } else {
            Pattern p = getNestedPattern();
            FBounds pSize = p.getBounds();
            manager.setPatternPosition(p, 0, 0);
            
            width = pSize.width;
            height = pSize.height;
        }
        

        double x0 = -gateway.getWidth() - LayoutManager.HSPACING;
        double y0 = 0;
        
        gateway.setLocation(x0, height + LayoutManager.VSPACING);
        
        width += gateway.getWidth() + LayoutManager.HSPACING;
        height += gateway.getHeight() + LayoutManager.VSPACING;
        
        setOrigin( x0 + width / 2, y0 + height / 2);
        
        getBorder().setClientRectangle(x0, y0, width, height);
        
        return getBorder().getBounds();
    }
    
    
    public String getDefaultName() {
        return "RepeatUntil"; // NOI18N
    }
    
    
    protected void onAppendPattern(Pattern nestedPattern) {
        removeElement(placeHolder);
    }
    

    protected void onRemovePattern(Pattern nestedPattern) {
        appendElement(placeHolder);
    }
    
    public void reconnectElements() {
        Pattern p = getNestedPattern();

        if (p != null) {
            connection1.connect(gateway, Direction.TOP, 
                    p.getFirstElement(), Direction.LEFT);
            
            connection2.connect(p.getLastElement(), Direction.BOTTOM, 
                    gateway, Direction.RIGHT);
        } else {
            connection1.connect(gateway, Direction.TOP, 
                    placeHolder, Direction.LEFT);
            
            connection2.connect(placeHolder, Direction.BOTTOM, 
                    gateway, Direction.RIGHT);
        }
    }

    public NodeType getNodeType() {
        return NodeType.REPEAT_UNTIL;
    }

    
    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(gateway.getShape()));
        return res;
    }
    

    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(RepeatUntilPattern.this, draggedPattern, placeHolder.getCenterX(),
                    placeHolder.getCenterY());
        }

        public void drop() {
            Pattern p = getDraggedPattern();
            ((RepeatUntil) getOMReference()).setActivity((Activity) p.getOMReference());
        }
    }
    
}
