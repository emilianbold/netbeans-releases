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


package org.netbeans.modules.bpel.design.selection;


import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Sequence;


public class ImpliciteSequencePlaceHolder extends PlaceHolder {
    
    private boolean last;
    
    
    public ImpliciteSequencePlaceHolder(Pattern ownerPattern,
            Pattern draggedPattern, float cx, float cy) {
        this(ownerPattern, draggedPattern, cx, cy, false);
    }
    
    
    public ImpliciteSequencePlaceHolder(Pattern ownerPattern,
            Pattern draggedPattern, float cx, float cy, boolean last) {
        super(ownerPattern, draggedPattern, cx, cy);
        this.last = last;
    }
    
    
    
    public ImpliciteSequencePlaceHolder(Pattern ownerPattern,
            Pattern draggedPattern, FPath path) {
        this(ownerPattern, draggedPattern, path, false);
    }
    
    
    public ImpliciteSequencePlaceHolder(Pattern ownerPattern,
            Pattern draggedPattern, FPath path, boolean last) {
        super(ownerPattern, draggedPattern, path);
        this.last = last;
    }
    
    
    public void drop() {
        Sequence s = getOwnerPattern().getBpelModel().getBuilder()
        .createSequence();
        
        ActivityHolder holder = (ActivityHolder) getOwnerPattern()
        .getOMReference();
        
        Activity oldActivity = (Activity) holder.getActivity().cut();
        Activity newActivity = (Activity) getDraggedPattern()
        .getOMReference();
        
        holder.setActivity(s);
        
        if (last) {
            s.addActivity(oldActivity);
            s.addActivity(newActivity);
        } else {
            s.addActivity(newActivity);
            s.addActivity(oldActivity);
        }
    }
    
    
    public static void create(Pattern draggedPattern, Pattern ownerPattern,
            Collection<PlaceHolder> placeHolders) {
        if (!(ownerPattern.getOMReference() instanceof ActivityHolder)) {
            return;
        }
        
        if (draggedPattern.getParent() == ownerPattern) {
            return;
        }
        
        ActivityHolder holder = (ActivityHolder) ownerPattern.getOMReference();
        
        BpelEntity child = holder.getActivity();
        
        if ((child == null) 
                || (child instanceof Sequence) 
                || (ownerPattern instanceof CollapsedPattern)) 
        {
            return;
        }
        
        
        
        Pattern p = ((CompositePattern) ownerPattern).getNestedPattern(child);
        
        Connection c1 = getInboundConnection(p);
        Connection c2 = getOutboundConnection(p);
        
        FPoint center1 = null;
        FPoint center2 = null;
        
        if (c1 != null) {
            FPath path = c1.getPath();
            if (p instanceof CompositePattern) {
                CompositePattern composite = (CompositePattern) p;
                BorderElement border = composite.getBorder();
                
                if (border != null) {
                    path = path.subtract(border.getShape());
                }
            }
            double pathLength = path.length();
            center1 = path.point((pathLength >= 20)
                    ? ((pathLength - 10) / pathLength) : 0.5f);
        } else {
            VisualElement e = null;
            if (p instanceof CompositePattern) {
                CompositePattern composite = (CompositePattern) p;
                e = composite.getBorder();
            }
            
            if (e == null){
                e = p.getFirstElement();
            }
            
            center1 = new FPoint(e.getCenterX(), e.getY() - 10);
        }
        
        if (c2 != null) {
            FPath path = c2.getPath();
            if (p instanceof CompositePattern) {
                CompositePattern composite = (CompositePattern) p;
                BorderElement border = composite.getBorder();
                
                if (border != null) {
                    path = path.subtract(border.getShape());
                }
            }
            double pathLength = path.length();
            center2 = path.point((pathLength >= 20) ? (10 / pathLength) : 0.5f);
        } else {
            if (p instanceof CompositePattern) {
                CompositePattern composite = (CompositePattern) p;
                
                VisualElement e = composite.getBorder();
                
                if (e == null){
                    e = p.getLastElement();
                }
                
                center2 = new FPoint(e.getCenterX(), e.getY()
                + e.getHeight() + 10);
            } else {
                VisualElement e = p.getLastElement();
                center2 = new FPoint(e.getCenterX(), e.getY() + e.getHeight()
                + 10);
            }
        }
        
        
        placeHolders.add(new ImpliciteSequencePlaceHolder(ownerPattern,
                draggedPattern, center1.x, center1.y));
        placeHolders.add(new ImpliciteSequencePlaceHolder(ownerPattern,
                draggedPattern, center2.x, center2.y, true));
    }
    
    
    private static Connection getInboundConnection(Pattern p) {
        for (Connection c : p.getFirstElement().getIncomingConnections()) {
            if (c instanceof MessageConnection) continue;
            return c;
        }
        return null;
    }
    
    
    private static Connection getOutboundConnection(Pattern p) {
        for (Connection c : p.getLastElement().getOutcomingConnections()) {
            if (c instanceof MessageConnection) continue;
            return c;
        }
        return null;
    }
    
}
