/*
 * DebuggerDecorationProvider.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.GlowDescriptor;
import org.netbeans.modules.bpel.design.decoration.Positioner;
import org.netbeans.modules.bpel.design.decoration.components.BreakpointIcon2D;
import org.netbeans.modules.bpel.design.decoration.components.DecorationLabel;
import org.netbeans.modules.bpel.design.decoration.components.DiagramButton;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.geom.FRectangle;
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
    private DesignView designView;
    
    public static final String  CURRENT_POSITION = "CurrentPC"; //NOI18N
    
    public static final String ENABLED_BREAKPOINT = "Breakpoint"; //NOI18N
    
    public static final String DISABLED_BREAKPOINT = "DisabledBreakpoint"; //NOI18N
    
    private static final Decoration DECORATE_CURRENT_POSITION = new Decoration(new Descriptor[]{
        new GlowDescriptor(new Color(0x00FF00))
    });
    
    
    
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
    
    public DebuggerDecorationProvider(DesignView view) {
        
        super(view.getDecorationManager());
        
        designView = view;
        
        
        DataObject dobj = (DataObject) view.getLookup().lookup(DataObject.class);
        if (dobj != null){
            this.cookie = (AnnotationManagerCookie) dobj.getCookie(AnnotationManagerCookie.class);
        }
        cookie.addAnnotationListener(this);
        
        view.getBPELModel().addEntityChangeListener(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (designView.getBPELModel().getState() == Model.State.VALID){
                    fireDecorationChanged(null);
                }
            }
        });
        
    }
    public void unsubscribe(){
        cookie.removeAnnotationListener(this);
        designView.getBPELModel().removeEntityChangeListener(this);
        
    }
    
    Positioner breakpointPositioner = new Positioner(){
        public void position(Pattern pattern, Collection<Component> components, float zoom) {
            VisualElement ve = pattern.getFirstElement();
            
            if (ve != null && components.size() == 1){
                
                Component component = components.iterator().next();
                
                Collection<Connection> connections = ve.getIncomingConnections();
                
                FRectangle bounds = pattern.getBounds();
                
                for (Connection c: connections){
                    if (c instanceof MessageConnection) {
                        continue;
                    }
                    
                    Point center = DesignView.convertDiagramToScreen(
                            new FPoint( c.getEndPoint().x, bounds.y),
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
        DiagramAnnotation annotations[] = cookie.getAnnotations(entity.getUID());
        Decoration result = new Decoration();
        for (DiagramAnnotation annotation: annotations){
            String type = annotation.getAnnotationType();
            Decoration decoration = null;
            if (type == CURRENT_POSITION){
                decoration = DECORATE_CURRENT_POSITION;
            } else if (type.equals(ENABLED_BREAKPOINT)) {
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(ENABLED_BREAKPOINT_ICON, 0),
                        breakpointPositioner);
                decoration = new Decoration(new Descriptor[] { cd });
                
                
            } else if (type.equals(DISABLED_BREAKPOINT)){
                ComponentsDescriptor cd = new ComponentsDescriptor();
                cd.add(new DecorationLabel(DISABLED_BREAKPOINT_ICON, 0),
                        breakpointPositioner);
                decoration = new Decoration(new Descriptor[] { cd });
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
            .getResource("resources/enabled_breakpoint.png"));
    
    private static final Icon DISABLED_BREAKPOINT_ICON
            = new ImageIcon(Decoration.class
            .getResource("resources/disabled_breakpoint.png"));
}
