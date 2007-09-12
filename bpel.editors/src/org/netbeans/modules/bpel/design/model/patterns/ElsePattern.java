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


import java.util.Collection;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;


public class ElsePattern extends CompositePattern {
    
    private PlaceHolderElement placeHolder;
    
    
    public ElsePattern(DiagramModel model) {
        super(model);
    }
    
    public boolean isSelectable() {
        return false;
    }
    
    protected void onAppendPattern(Pattern nestedPattern) {
        removeElement(placeHolder);
    }
    
    
    protected void onRemovePattern(Pattern nestedPattern) {
        appendElement(placeHolder);
    }
    
    
    public VisualElement getFirstElement() {
        Pattern p = getNestedPattern();
        return (p != null) ? p.getFirstElement() : placeHolder;
    }
    
    
    public VisualElement getLastElement() {
        Pattern p = getNestedPattern();
        return (p != null) ? p.getLastElement() : placeHolder;
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        Pattern p = getNestedPattern();
        
        double width;
        double height;
        
        if (p != null) {
            FBounds bounds = p.getBounds();
            width = bounds.width;
            height = bounds.height;
            manager.setPatternPosition(p, 0, 0);
        } else {
            width = placeHolder.getWidth();
            height = placeHolder.getHeight();
            placeHolder.setLocation(0, 0);
        }
        
        return null; //manager.setBorderBounds(getBorder(), 0, 0, width, height);
    }
    
    
    protected void createElementsImpl() {
        //setBorder(new GroupBorder());
        //setBorder(new ElseIfBorder());
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        BpelEntity activity = ((Else) getOMReference()).getActivity();
        if (activity != null) {
            getModel().createPattern(activity).setParent(this);
        }
    }
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        if (((Else) getOMReference()).getActivity() != null) return;
        
        placeHolders.add(new InnerPlaceHolder(draggedPattern));
    }
    
    
    public NodeType getNodeType() {
        return NodeType.ELSE;
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ElsePattern.this, draggedPattern,
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            ((Else) getOMReference()).setActivity((Activity)
            getDraggedPattern().getOMReference());
        }
    }
}
