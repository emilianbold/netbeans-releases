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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;

 
public class FaultHandlersPattern extends CompositePattern {
    
    
    public FaultHandlersPattern(DiagramModel model) {
        super(model);
    }

    
    protected void onAppendPattern(Pattern nestedPattern) {}

    
    protected void onRemovePattern(Pattern nestedPattern) {}

    
    public VisualElement getFirstElement() {
        return getBorder();
    }

    
    public VisualElement getLastElement() {
        return getBorder();
    }

    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setLabelText(getDefaultName());
        
        FaultHandlers faultHandlers = (FaultHandlers) getOMReference();

        Catch[] catches = faultHandlers.getCatches();
        CompensatableActivityHolder catchAll = faultHandlers.getCatchAll();
        
        for (Catch c : catches) {
            Pattern p = getModel().createPattern(c);
            p.setParent(this);
        }
        
        if (catchAll != null) {
            Pattern p = getModel().createPattern(catchAll);
            p.setParent(this);
        }
    }

    
    public FBounds layoutPattern(LayoutManager manager) {
        FaultHandlers faultHandlers = (FaultHandlers) getOMReference();

        Catch[] catches = faultHandlers.getCatches();
        CompensatableActivityHolder catchAll = faultHandlers.getCatchAll();

        float xPosition = 0;
        
        float width;
        float height = 0;
        
        if (catchAll != null) {
            Pattern p = getNestedPattern(catchAll);
            manager.setPatternPosition(p, xPosition, 0);
            
            FDimension size =  p.getBounds().getSize();
            
            height = size.height;
            xPosition += size.width + LayoutManager.HSPACING;
        }
        
        for (Catch c : catches) {
            Pattern p = getNestedPattern(c);
            manager.setPatternPosition(p, xPosition, 0);
            
            FDimension size =  p.getBounds().getSize();
            
            height = Math.max(height, size.height);
            xPosition += size.width + LayoutManager.HSPACING;
        }
        
        height = Math.max(height, 20);
        width = Math.max(xPosition - LayoutManager.HSPACING, 20);
        
        getBorder().setClientRectangle(0, 0, width, height);
        return getBorder().getBounds();
                
    }
    

    public String getDefaultName() {
        return "Fault Handlers"; // NOI18N
    }     
    

    public NodeType getNodeType() {
        return NodeType.FAULT_HANDLERS;
    }
}
