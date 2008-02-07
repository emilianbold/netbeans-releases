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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.Collection;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import org.netbeans.modules.bpel.core.annotations.AnnotationListener;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.core.debugger.DebuggerHelper;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.DimmDescriptor;
import org.netbeans.modules.bpel.design.decoration.GlowDescriptor;
import org.netbeans.modules.bpel.design.decoration.Positioner;
import org.netbeans.modules.bpel.design.decoration.StripeDescriptor;
import org.netbeans.modules.bpel.design.decoration.components.ButtonRenderer;
import org.netbeans.modules.bpel.design.decoration.components.DecorationComponent;
import org.netbeans.modules.bpel.design.decoration.components.DecorationLabel;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.xml.xam.Model;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 * @author Kirill Sorokin
 */
public class DebuggerDecorationProvider extends DecorationProvider
        implements AnnotationListener, ChangeEventListener {
    
    private AnnotationManagerCookie cookie;
    
    
    private Positioner breakpointPositioner = new Positioner(){
        public void position(
                final Pattern pattern, 
                final Collection<Component> components,
                final double zoom) {
            
            final VisualElement ve = pattern.getFirstElement();
            
            if ((ve != null) && (components.size() == 1)) {
                final Component component = 
                        components.iterator().next();
                final Collection<Connection> connections = 
                        ve.getIncomingConnections();
                final FBounds bounds = pattern.getBounds();
                
                for (Connection c: connections){
                    if (c instanceof MessageConnection) {
                        continue;
                    }
                    
                    if (c.getTargetDirection() != Direction.TOP) {
                        continue;
                    }
                    
                    final DiagramView view = pattern.getView();
                    final Point center = view.convertDiagramToScreen(
                            new FPoint(c.getEndPoint().x, bounds.y));
                    final Dimension dim = component.getPreferredSize();
                    
                    component.setBounds(
                            center.x - dim.width / 2 + 1, 
                            center.y - dim.height / 2,
                            dim.width, 
                            dim.height);
                    
                    return;
                }
            }
            
            // By default position breakpoint to the center of element
            ComponentsDescriptor.TOP_CENTER.position(pattern, components, zoom);
        }
    };
    
    private Positioner currentlyExecutingPositioner = new Positioner() {
        public void position(
                Pattern pattern, 
                Collection<Component> components, 
                double zoom){
            
            final FBounds fBounds = getPatternBounds(pattern);
            final DiagramView view = pattern.getView();
            
            final Point p1 = view.convertDiagramToScreen(fBounds.getTopLeft());
            final Point p2 = view.convertDiagramToScreen(fBounds.getBottomRight());
            
            for (Component component : components){
                component.setBounds(
                        p1.x, 
                        p1.y, 
                        p2.x - p1.x, 
                        p2.y - p1.y);
            }
        }
    };
    
    public DebuggerDecorationProvider(
            final DesignView view) {
        
        super(view);
        
        final DataObject dataObject = view.getLookup().lookup(DataObject.class);
        
        if (dataObject != null){
            this.cookie = dataObject.getCookie(AnnotationManagerCookie.class);
        }
        cookie.addAnnotationListener(this);
        
        view.getBPELModel().addEntityChangeListener(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getDesignView().getBPELModel().getState() == 
                        Model.State.VALID){
                    
                    fireDecorationChanged();
                }
            }
        });
    }
    
    @Override
    public void release(){
        cookie.removeAnnotationListener(this);
        getDesignView().getBPELModel().removeEntityChangeListener(this);
    }
    
    public Decoration getDecoration(
            final BpelEntity entity) {
        
        if (!isDiagramEntity(entity)) {
            return null;
        }
        
        final DiagramAnnotation annotations[] = 
                cookie.getAnnotations(entity.getUID());
        
        final Decoration result = new Decoration();
        for (DiagramAnnotation annotation: annotations){
            final String type = annotation.getAnnotationType();
            
            Decoration decoration = null;
            if (type.equals(CURRENT_POSITION)){
                decoration = DECORATE_CURRENT_POSITION;
            } else if (type.equals(CURRENTLY_EXECUTING)) {
                final ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new InfiniteRoundProgress(), currentlyExecutingPositioner);
                
                decoration = new Decoration(new Descriptor[] { cd });
            } else if (type.equals(ENABLED_BREAKPOINT)) {
                final ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new BreakpointButton(
                        ENABLED_BREAKPOINT_ICON, entity, getDesignView()),
                        breakpointPositioner);
                decoration = new Decoration(new Descriptor[] { cd,
                StripeDescriptor.createBreakpoint() });
            } else if (type.equals(DISABLED_BREAKPOINT)){
                final ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new BreakpointButton(
                        DISABLED_BREAKPOINT_ICON, entity, getDesignView()),
                        breakpointPositioner);
                decoration = new Decoration(new Descriptor[] { cd });
            } else if (type.equals(STARTED_ELEMENT)) {
                final ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(STARTED_ELEMENT_ICON, 0),
                        ComponentsDescriptor.LEFT_TB);
                decoration = new Decoration(new Descriptor[] {cd});
            } else if (type.equals(COMPLETED_ELEMENT)) {
                final ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(COMPLETED_ELEMENT_ICON, 0),
                        ComponentsDescriptor.LEFT_TB);
                decoration = new Decoration(new Descriptor[] {cd});
            } else if (type.equals(FAULTED_ELEMENT)) {
                final ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(FAULTED_ELEMENT_ICON, 0),
                        ComponentsDescriptor.LEFT_TB);
                decoration = new Decoration(new Descriptor[] {cd});
            } else if (type.equals(NEVER_EXECUTED_ELEMENT)) {
                decoration = new Decoration(
                        new Descriptor[] {new DimmDescriptor()});
            }
            
            if (decoration != null){
                result.combineWith(decoration);
            }
        }
        
        return result;
    }
    
    public void notifyPropertyUpdated(final PropertyUpdateEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                
                if (event.getName().equals(BpelModel.STATE) &&
                        event.getNewValue().equals(Model.State.VALID)) {
                    fireDecorationChanged();
                }
            }
        });
    }
    
    public void annotationAdded(DiagramAnnotation diagramAnnotation) {
        updateDecorations(diagramAnnotation);
    }
    
    public void annotationRemoved(DiagramAnnotation diagramAnnotation) {
        updateDecorations(diagramAnnotation);
    }
    
    public void notifyPropertyRemoved(PropertyRemoveEvent event) {}
    
    public void notifyEntityInserted(EntityInsertEvent event) {}
    
    public void notifyEntityRemoved(EntityRemoveEvent event) {}
    
    public void notifyEntityUpdated(EntityUpdateEvent event) {}
    
    public void notifyArrayUpdated(ArrayUpdateEvent event) {}
    
    // Private /////////////////////////////////////////////////////////////////
    private boolean isDiagramEntity(BpelEntity entity){
        if (entity == null || entity.getModel() == null){
            return false;
        }
        
        Pattern p = getDesignView().getModel().getPattern(entity);

        return ((p != null) && p.isInModel());
    }    
    
    private void updateDecorations(DiagramAnnotation diagramAnnotation) {
        final BpelModel model = diagramAnnotation.getBpelEntityId().getModel();
        
        final Runnable runnable = new Runnable() {
            public void run() {
                if (model.getState().equals(BpelModel.State.VALID)) {
                    fireDecorationChanged();
                }
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static final class BreakpointButton extends JButton implements DecorationComponent {
        
        private Icon myIcon;
        private Node myNode;
        private BpelEntity myEntity;
        
        public BreakpointButton(
                final Icon icon,
                final BpelEntity entity,
                final DesignView designView) {
            super(icon);
            
            myIcon = icon;
            myEntity = entity;
            myNode = designView.getNodeForPattern(
                    designView.getModel().getPattern(entity));
            
            setOpaque(false);
            setBorder(null);
            setRolloverEnabled(true);
            setContentAreaFilled(false);
            setFocusable(false);
            
            setPreferredSize(new Dimension(
                    myIcon.getIconWidth() + 6, 
                    myIcon.getIconHeight() + 6));
            
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    final DebuggerHelper helper = 
                            Lookup.getDefault().lookup(DebuggerHelper.class);
                    
                    helper.toggleBreakpointEnabledState(myNode, myEntity);
                }
            });
            
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        
        @Override
        public String getToolTipText() {
            if (myIcon == ENABLED_BREAKPOINT_ICON) {
                return NbBundle.getMessage(
                        DebuggerDecorationProvider.class, 
                        "LBL_Disable_Breakpoint"); // NOI18N
            } else {
                return NbBundle.getMessage(
                        DebuggerDecorationProvider.class, 
                        "LBL_Enable_Breakpoint"); // NOI18N
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            final ButtonModel buttonModel = getModel();
            
            if (buttonModel.isPressed()) {
                ButtonRenderer.paintButton(this, g, 
                        ButtonRenderer.PRESSED_FILL_COLOR, false, 
                        ButtonRenderer.PRESSED_BORDER_COLOR, 
                        ButtonRenderer.PRESSED_STROKE_WIDTH, myIcon);
            } else if (buttonModel.isRollover()) {
                ButtonRenderer.paintButton(this, g, 
                        ButtonRenderer.ROLLOVER_FILL_COLOR, true, 
                        ButtonRenderer.ROLLOVER_BORDER_COLOR, 
                        ButtonRenderer.ROLLOVER_STROKE_WIDTH, myIcon);
            } else if (buttonModel.isSelected()) {
                ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                        ButtonRenderer.PRESSED_BORDER_COLOR, 
                        ButtonRenderer.PRESSED_STROKE_WIDTH, myIcon);
            } else {
                ButtonRenderer.paintButton(this, g, BACKGROUND, false, 
                        null, ButtonRenderer.NORMAL_STROKE_WIDTH, myIcon);
            }
        }
        
        private static final Color BACKGROUND = new Color(0xCCFFFFFF, true);
        
        private static final long serialVersionUID = 1L;
    }
    
    public static final class InfiniteRoundProgress extends JComponent implements HierarchyListener, ActionListener, DecorationComponent {
        private float angleStart = (float) 0.;
        private float angleLimit = (float) (2 * Math.PI);
        private float angleCorrection = (float) (-Math.PI / 50.);
        private float colorStart = 1.0F;
        private float colorLimit = 0.0F;
        
        private int steps = 10;
        
        private Timer timer = new Timer(75, this);
        
        public InfiniteRoundProgress() {
            addHierarchyListener(this);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g;
            
            final RenderingHints oldHints = g2.getRenderingHints();
            final RenderingHints newHints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setRenderingHints(newHints);
            
            final java.awt.Rectangle bounds = getBounds();
            
            final float centerX = bounds.width / 2;
            final float centerY = bounds.height / 2;
            
            final float lower = bounds.width < bounds.height ? 
                bounds.width : bounds.height;
            
            final float distance = lower / 6;
            final float length = lower / 6;
            
            float radius = lower / 30;
            
            final float angleStep = (angleLimit - angleStart) / steps;
            final float colorStep = (colorLimit - colorStart) / steps;
            
            for (int i = 0; i < steps; i++) {
                final float angle = angleStart + angleStep * i;
                final float color = colorStart + colorStep * i;
                
                g2.setColor(new Color(0.F, 0.F, 0.F, color));
                g2.setStroke(new BasicStroke(2.0F));
                
                paintTile(
                        (Graphics2D) g, 
                        centerX, 
                        centerY, 
                        angle, 
                        distance, 
                        length, 
                        radius);
            }
            
            g2.setRenderingHints(oldHints);
        }
        
        private void paintTile(
                Graphics2D g, 
                float centerX, 
                float centerY, 
                float angle, 
                float distance, 
                float length, 
                float radius) {
            
            final float sin = (float) Math.sin(angle);
            final float cos = (float) Math.cos(angle);
            
            final float r2 = (float) (radius * 2.);
            
            final float r_sin = radius * sin;
            final float r_cos = radius * cos;
            
            final float l1_sin = distance * sin;
            final float l1_cos = distance * cos;
            
            final float l1_l2_sin = (distance + length) * sin;
            final float l1_l2_cos = (distance + length) * cos;
            
            if (radius < 1.0F) {
                g.draw(new Line2D.Float(
                        centerX + l1_cos, 
                        centerY - l1_sin, 
                        centerX + l1_l2_cos, 
                        centerY - l1_l2_sin));
                return;
            }
            
            final GeneralPath path = new GeneralPath();
            path.moveTo(
                    centerX + l1_cos + r_sin, 
                    centerY - l1_sin + r_cos);
            path.lineTo(
                    centerX + l1_cos - r_sin, 
                    centerY - l1_sin - r_cos);
            path.lineTo(
                    centerX + l1_l2_cos - r_sin, 
                    centerY - l1_l2_sin - r_cos);
            path.lineTo(
                    centerX + l1_l2_cos + r_sin, 
                    centerY - l1_l2_sin + r_cos);
            path.closePath();
            
            final Ellipse2D ellipse1 = new Ellipse2D.Float(
                    centerX + l1_cos - radius, 
                    centerY - l1_sin - radius, 
                    r2, 
                    r2);
            final Ellipse2D ellipse2 = new Ellipse2D.Float(
                    centerX + l1_l2_cos - radius, 
                    centerY - l1_l2_sin - radius, 
                    r2, 
                    r2);
            
            final Area area1 = new Area(path);
            final Area area2 = new Area(ellipse1);
            final Area area3 = new Area(ellipse2);
            
            area1.add(area2);
            area1.add(area3);
            
            g.fill(area1);
        }
        
        public void actionPerformed(ActionEvent e) {
            angleLimit += angleCorrection;
            angleStart += angleCorrection;
            
            invalidate();
            repaint();
        }
        
        private static final long serialVersionUID = 1L;

        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & (long) HierarchyEvent.PARENT_CHANGED) > 0) {
                if (getParent() == null) {
                    timer.stop();
                } else {
                    timer.start();
                }
            }
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String NEVER_EXECUTED_ELEMENT = 
            "NeverExecutedElement";
    
    public static final String STARTED_ELEMENT = 
            "StartedElement";
    
    public static final String COMPLETED_ELEMENT = 
            "CompletedElement";
    
    public static final String FAULTED_ELEMENT = "FaultedElement";
    
    public static final String CURRENT_POSITION = "CurrentPC"; //NOI18N
    
    public static final String CURRENTLY_EXECUTING = "CurrentlyExecuting"; // NOI18N
    
    public static final String ENABLED_BREAKPOINT = "BpelBreakpoint_normal"; //NOI18N
    
    public static final String DISABLED_BREAKPOINT = "BpelBreakpoint_disabled"; //NOI18N
    
    private static final Decoration DECORATE_CURRENT_POSITION = new Decoration(
            new Descriptor[] {
        new GlowDescriptor(new Color(0x00FF00))
    });
    
    private static final Icon ENABLED_BREAKPOINT_ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/enabled_breakpoint.png")); // NOI18N
    
    private static final Icon DISABLED_BREAKPOINT_ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/disabled_breakpoint.png")); // NOI18N
    
    private static final Icon STARTED_ELEMENT_ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/execution.png")); // NOI18N
    
    private static final Icon COMPLETED_ELEMENT_ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/execution_ok.png")); // NOI18N
    
    private static final Icon FAULTED_ELEMENT_ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/execution_faild.png")); // NOI18N
}
