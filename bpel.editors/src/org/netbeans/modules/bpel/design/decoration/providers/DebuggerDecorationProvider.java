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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.core.annotations.AnnotationListener;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.DecorationProviderFactory;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.DimmDescriptor;
import org.netbeans.modules.bpel.design.decoration.GlowDescriptor;
import org.netbeans.modules.bpel.design.decoration.Positioner;
import org.netbeans.modules.bpel.design.decoration.StripeDescriptor;
import org.netbeans.modules.bpel.design.decoration.components.BreakpointIcon2D;
import org.netbeans.modules.bpel.design.decoration.components.DecorationLabel;
import org.netbeans.modules.bpel.design.decoration.components.DiagramButton;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.connections.Connection;
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

/**
 *
 * @author Alexey
 */
public class DebuggerDecorationProvider extends DecorationProvider
        implements AnnotationListener, ChangeEventListener {
    
    private AnnotationManagerCookie cookie;
    
    public static final String NEVER_EXECUTED_ELEMENT = "NeverExecutedElement";
    
    public static final String STARTED_ELEMENT = "StartedElement";
    
    public static final String COMPLETED_ELEMENT = "CompletedElement";
    
    public static final String FAULTED_ELEMENT = "FaultedElement";
    
    public static final String CURRENT_POSITION = "CurrentPC"; //NOI18N
    
    public static final String ENABLED_BREAKPOINT = "Breakpoint"; //NOI18N
    
    public static final String DISABLED_BREAKPOINT = "DisabledBreakpoint"; //NOI18N
    
    private static final Decoration DECORATE_CURRENT_POSITION = new Decoration(new Descriptor[]{
        new GlowDescriptor(new Color(0x00FF00))
    });
    
//    public static class FactoryImpl implements DecorationProviderFactory{
//        public DecorationProvider createInstance(DesignView view){
//            return new DebuggerDecorationProvider(view);
//        }
//    }
    
    private static class BreakpointDecoration extends Decoration{
        private static BreakpointIcon2D ENABLED_ICON = new BreakpointIcon2D(true);
        private static BreakpointIcon2D DISABLED_ICON = new BreakpointIcon2D(false);
        
        public BreakpointDecoration(boolean enabled){
            super(new Descriptor[]{
                new ComponentsDescriptor()
            });
            DiagramButton btn =
                    new DiagramButton( enabled ? ENABLED_ICON : DISABLED_ICON );
            
            getComponents().add(btn);
            
        }
        
    }
    
    public  DebuggerDecorationProvider(DesignView view) {
        
        super(view);
        
        
        
        
        DataObject dobj = (DataObject) view.getLookup().lookup(DataObject.class);
        if (dobj != null){
            this.cookie = (AnnotationManagerCookie) dobj.getCookie(AnnotationManagerCookie.class);
        }
        cookie.addAnnotationListener(this);
        
        view.getBPELModel().addEntityChangeListener(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getDesignView().getBPELModel().getState() == Model.State.VALID){
                    fireDecorationChanged(null);
                }
            }
        });
        
    }
    public void release(){
        cookie.removeAnnotationListener(this);
        getDesignView().getBPELModel().removeEntityChangeListener(this);
        
    }
    
    
    Positioner breakpointPositioner = new Positioner(){
        public void position(Pattern pattern, Collection<Component> components,
                double zoom) {
            VisualElement ve = pattern.getFirstElement();
            
            if (ve != null && components.size() == 1){
                
                Component component = components.iterator().next();
                
                Collection<Connection> connections = ve.getIncomingConnections();
                
                FBounds bounds = pattern.getBounds();
                
                for (Connection c: connections){
                    if (c instanceof MessageConnection) {
                        continue;
                    }
                    
                    Point center = DesignView.convertDiagramToScreen(
                            new FPoint(c.getEndPoint().x, bounds.y),
                            zoom);
                    
                    
                    Dimension dim = component.getPreferredSize();
                    
                    component.setBounds(center.x - dim.width / 2, center.y - dim.height / 2,
                            dim.width, dim.height);
                    return;
                }
            }
            //by default position breakpoint to the center of element
            ComponentsDescriptor.TOP_CENTER.position(pattern, components,zoom);
        }
        
    };
    
    public Decoration getDecoration(BpelEntity entity) {
        if (!isDiagramEntity(entity)) {
            return null;
        }
        
        DiagramAnnotation annotations[] = cookie.getAnnotations(entity.getUID());
        Decoration result = new Decoration();
        for (DiagramAnnotation annotation: annotations){
            String type = annotation.getAnnotationType();
            Decoration decoration = null;
            if (type.equals(CURRENT_POSITION)){
                decoration = DECORATE_CURRENT_POSITION;
            } else if (type.equals(ENABLED_BREAKPOINT)) {
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(ENABLED_BREAKPOINT_ICON, 0),
                        breakpointPositioner);
                decoration = new Decoration(new Descriptor[] { cd,
                StripeDescriptor.createBreakpoint() });
                
                
            } else if (type.equals(DISABLED_BREAKPOINT)){
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(DISABLED_BREAKPOINT_ICON, 0),
                        breakpointPositioner);
                decoration = new Decoration(new Descriptor[] { cd });
            } else if (type.equals(STARTED_ELEMENT)) {
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(STARTED_ELEMENT_ICON, 0),
                        ComponentsDescriptor.LEFT_TB);
                decoration = new Decoration(new Descriptor[] {cd});
            } else if (type.equals(COMPLETED_ELEMENT)) {
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(COMPLETED_ELEMENT_ICON, 0),
                        ComponentsDescriptor.LEFT_TB);
                decoration = new Decoration(new Descriptor[] {cd});
            } else if (type.equals(FAULTED_ELEMENT)) {
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(FAULTED_ELEMENT_ICON, 0),
                        ComponentsDescriptor.LEFT_TB);
                decoration = new Decoration(new Descriptor[] {cd});
                
            } else if (type.equals(NEVER_EXECUTED_ELEMENT)) {
                decoration = new Decoration(new Descriptor[] {new DimmDescriptor()});
            }
            
            
            
            if (decoration != null){
                result.combineWith(decoration);
            }
            
        }
        return result;
    }
    

    private boolean isDiagramEntity(BpelEntity entity){
        if (entity == null || entity.getModel() == null){
            return false;
        }
        
        Pattern p = getDesignView().getModel().getPattern(entity);

        return ((p != null) && p.isInModel());
    }    
    
    
    public void notifyPropertyUpdated(final PropertyUpdateEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                
                if (event.getName().equals(BpelModel.STATE) &&
                        event.getNewValue().equals(Model.State.VALID)) {
                    fireDecorationChanged(null);
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
    
    
    private void updateDecorations(DiagramAnnotation diagramAnnotation) {
        final BpelModel model = diagramAnnotation.getBpelEntityId().getModel();
        
        Runnable runnable = new Runnable() {
            public void run() {
                if (model.getState().equals(BpelModel.State.VALID)) {
                    fireDecorationChanged(null);
                }
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
    
    public void notifyPropertyRemoved(PropertyRemoveEvent event) {}
    
    public void notifyEntityInserted(EntityInsertEvent event) {}
    
    
    public void notifyEntityRemoved(EntityRemoveEvent event) {}
    
    public void notifyEntityUpdated(EntityUpdateEvent event) {}
    
    public void notifyArrayUpdated(ArrayUpdateEvent event) {}
    
    
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
