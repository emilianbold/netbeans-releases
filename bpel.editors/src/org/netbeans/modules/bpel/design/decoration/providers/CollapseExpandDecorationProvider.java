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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.Positioner;
import org.netbeans.modules.bpel.design.decoration.components.ContextToolBarButton;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.elements.icons.Icon2D;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class CollapseExpandDecorationProvider extends DecorationProvider 
//        implements DiagramSelectionListener 
{ 

    

    public CollapseExpandDecorationProvider(DesignView designView) {
        super(designView);


    }
    

    public Decoration getDecoration(BpelEntity entity) {
        Pattern pattern = getDesignView().getModel().getPattern(entity);
        
        if (pattern == null) return null;
        if (pattern.getOMReference() == null) return null;
        
        if (isInsideCollapsed(entity)) {
            return null;
        }
        
        if (isCollapsed(entity)) {
            ComponentsDescriptor components = new ComponentsDescriptor();
            components.add(new ContextToolBarButton(new ExpandAction(
                    pattern, SMALL_EXPAND_ICON), 
                    new Color(0x99FFFFFF, true)), collapseExpandPositioner);
            return new Decoration(components);
        }
        
//        else if (isCollapsable(entity) && isSelected(entity)) {
//            ComponentsDescriptor components = new ComponentsDescriptor();
//            components.add(new JButton(new CollapseAction(pattern)), 
//                    collapseExpandPositioner);
//            return new Decoration(components);
//        }
        
        return null;
    }

    
//    public void selectionChanged(BpelEntity oldSelection, 
//            BpelEntity newSelection) 
//    {
//        if (oldSelection != null) {
//            if (isCollapsable(oldSelection) && !isCollapsed(oldSelection)) {
//                fireDecorationChanged(oldSelection);
//            }
//        }
//        
//        if (newSelection != null) {
//            if (isCollapsable(newSelection) && !isCollapsed(newSelection)) {
//                fireDecorationChanged(newSelection);
//            }
//        }
//    }

    

    private boolean isCollapsed(BpelEntity bpelEntity) {
        return getDesignView().getModel().isCollapsed(bpelEntity);
    }
    

    public boolean isInsideCollapsed(BpelEntity bpelEntity) {
        for (BpelEntity entity = bpelEntity.getParent(); entity != null; 
                entity = entity.getParent()) 
        {
            if (isCollapsed(entity)) return true;
        }
        
        return false;
    }    

    
    public List<BpelEntity> findCollapsedBpelEntitesInside(
            BpelEntity bpelEntity, List<BpelEntity> result) 
    {
        if (result == null) {
            result = new ArrayList<BpelEntity>();
        }
        
        for (BpelEntity child : bpelEntity.getChildren()) {
            if (isCollapsed(child)) {
                result.add(child);
            } else {
                result = findCollapsedBpelEntitesInside(child, result);
            }
        }
        
        return result;
    }
    
    
    
    private boolean isSelected(BpelEntity bpelEntity) {
        UniqueId entityID = bpelEntity.getUID();
        UniqueId selectedEntityID = getDesignView().getSelectionModel()
                .getSelectedID();
        return (entityID != null) && entityID.equals(selectedEntityID);
    }
    
    
    private final Positioner collapseExpandPositioner = new Positioner() {
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            VisualElement ve = null;
            
            if (pattern instanceof CompositePattern) {
                ve = ((CompositePattern) pattern).getBorder();
            }
            
            if (ve == null) {
                ve = pattern.getFirstElement();
            }
            
            Component component = components.iterator().next();
            
            Dimension size = component.getPreferredSize();
            DiagramView view = pattern.getView();
            Point center = view.convertDiagramToScreen( 
                    new FPoint(ve.getCenterX(), ve.getY() + ve.getHeight()));
            
            component.setBounds(center.x - size.width / 2, 
                    center.y - size.height - 2, 
                    size.width, size.height);
        }
    };
    
    
    public Action createCollapseExpandAction(BpelEntity bpelEntity) {
        Pattern pattern = getDesignView().getModel().getPattern(bpelEntity);
        
        if (pattern == null) return null;
        
        return createCollapseExpandAction(pattern);
    }
    
    
    public Action createCollapseExpandAction(Pattern pattern) {
        if (getDesignView().getModel().isCollapsed(pattern.getOMReference())) {
            return new ExpandAction(pattern);
        }
        
        if (pattern.isCollapsable()) {
            return new CollapseAction(pattern); 
        }
        
        return null;
    }
    
    
    private class CollapseAction extends AbstractAction {
        
        private Pattern pattern;
        
        public CollapseAction(Pattern pattern) {
            super(NbBundle.getMessage(CollapseExpandDecorationProvider.class, 
                    "LBL_COLLAPSE_ACTION"), NORMAL_COLLAPSE_ICON);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(
                    CollapseExpandDecorationProvider.class, 
                    "TTT_COLLAPSE_ACTION"));
                    
            this.pattern = pattern;
        }
        
        public void actionPerformed(ActionEvent event) {
            pattern.getModel().setCollapsed(pattern.getOMReference(), true);
            fireDecorationChanged();
           
        }
    }
    
    
    private class ExpandAction extends AbstractAction {
        
        private Pattern pattern;

        
        public ExpandAction(Pattern pattern, Icon icon) {
            super(NbBundle.getMessage(CollapseExpandDecorationProvider.class,
                    "LBL_EXPAND_ACTION"), icon);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(
                    CollapseExpandDecorationProvider.class, 
                    "TTT_EXPAND_ACTION"));
            
            this.pattern = pattern;
        }
        
        
        public ExpandAction(Pattern pattern) {
            this(pattern, NORMAL_EXPAND_ICON);
        }
        
        
        public void actionPerformed(ActionEvent event) {
            pattern.getModel().setCollapsed(pattern.getOMReference(), false);


            fireDecorationChanged();
        }
    }
    
    
    private static final Icon SMALL_EXPAND_ICON;
    private static final Icon NORMAL_EXPAND_ICON;
    private static final Icon NORMAL_COLLAPSE_ICON;
    
    static {
        BufferedImage image = new BufferedImage(7, 7, 
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new Color(0x88FFFFFF, true));
        g2.fillRect(0, 2, 7, 3);
        g2.fillRect(2, 0, 3, 7);
        g2.setPaint(Icon2D.COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, 3, 6, 3);
        g2.drawLine(3, 0, 3, 6);
        g2.dispose();
        SMALL_EXPAND_ICON = new ImageIcon(image);
        
        image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Icon2D.COLOR);
        g2.fillRect(6, 2, 4, 12);
        g2.fillRect(2, 6, 12, 4);
        g2.setPaint(Color.WHITE);
        g2.fillRect(7, 3, 2, 10);
        g2.fillRect(3, 7, 10, 2);
        g2.dispose();
        NORMAL_EXPAND_ICON = new ImageIcon(image);
        
        image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Icon2D.COLOR);
        g2.fillRect(2, 6, 12, 4);
        g2.setPaint(Color.WHITE);
        g2.fillRect(3, 7, 10, 2);
        g2.dispose();
        NORMAL_COLLAPSE_ICON = new ImageIcon(image);
    }
}
