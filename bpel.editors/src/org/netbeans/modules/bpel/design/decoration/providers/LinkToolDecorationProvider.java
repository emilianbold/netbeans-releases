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

package org.netbeans.modules.bpel.design.decoration.providers;

import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.Positioner;
import org.netbeans.modules.bpel.design.decoration.components.LinkToolButton;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import org.netbeans.modules.bpel.design.selection.FlowlinkTool;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author aa160298
 */
public class LinkToolDecorationProvider extends DecorationProvider implements
        DiagramSelectionListener {
    

    
    private ArrayList<UniqueId> linkedEntities = new ArrayList<UniqueId>();
    private Decoration linkBtnDecoration;
    private DragSource dragSource = new DragSource();
    
    private static final Icon ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/enabled_breakpoint.png"));
    
    public LinkToolDecorationProvider(DesignView designView) {
        super(designView);

        getDesignView().getSelectionModel().addSelectionListener(this);
    }
    
    
    public Decoration getDecoration(BpelEntity entity) {
        
        UniqueId entityID = entity.getUID();
        UniqueId selectedEntityID = getDesignView().getSelectionModel().getSelectedID();
        
        
        if (entityID!= null && entityID.equals(selectedEntityID) &&
            entity instanceof PartnerLinkReference &&
            linkBtnDecoration != null) {
            
            return (getDesignView().getFlowLinkTool().isActive()) ? null :
                linkBtnDecoration;
        }
        
        
        
        return null;
    }
    
    
    public void selectionChanged(BpelEntity oldSelection, BpelEntity newSelection) {
        
        
        
        if (newSelection instanceof PartnerLinkReference){
            Pattern p = getDesignView().getModel().getPattern(newSelection);
            LinkToolButton button = new LinkToolButton(p);
            
            dragSource.createDefaultDragGestureRecognizer(
                    button,
                    DnDConstants.ACTION_MOVE,
                    getDesignView().getDndHandler());
            
            
            ComponentsDescriptor cd = new ComponentsDescriptor();
            cd.add(button, linkToolPositioner);
            linkBtnDecoration = new Decoration( new Descriptor[]{cd});
            
            fireDecorationChanged(newSelection);
        } else {
            linkBtnDecoration = null;
        }
        
        
        
        if (oldSelection != null) {
            fireDecorationChanged(oldSelection);
        }
        
        
        
    }
    private Positioner linkToolPositioner = new Positioner() {
        private static final int HSPACING = 0;
        private static final int VSPACING = 3;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            assert (components.size() == 1) 
                    : "Only one LinkToolButton per element allowed";
            
            LinkToolButton btn = ((LinkToolButton) components.toArray()[0]);
            
            FlowlinkTool flt = getDesignView().getFlowLinkTool();
            
            if (flt.isActive()){
                btn.setPosition(flt.getPosition());
            } else {
                FBounds bounds = pattern.getFirstElement().getBounds();
                
                Point p = DesignView.convertDiagramToScreen(
                        new FPoint(bounds.getX(), bounds.getCenterY()),
                        zoom);
                
                
                btn.setPosition(p);
            }
            
            
            
            
            
        }
        
    };
    
    
  
}
