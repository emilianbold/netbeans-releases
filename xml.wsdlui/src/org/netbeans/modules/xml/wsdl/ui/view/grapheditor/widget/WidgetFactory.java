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
/*
 * WidgetFactory.java
 *
 * Created on August 15, 2006, 11:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.openide.util.Lookup;

/**
 * Factory for creating Widget instances to represent the various WSDL
 * model components (e.g. Operation, Role, PartnerLinkType).
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class WidgetFactory {
    /** The single instance of this class. */
    private static WidgetFactory mFactory;
    
    /** Creates a new instance of WidgetFactory */
    private WidgetFactory() {
    }
    
    /**
     * Get the singleton instance of this factory.
     *
     * @return  WSDL widget factory.
     */
    public static WidgetFactory getInstance() {
        if (mFactory == null) {
            mFactory = new WidgetFactory();
        }
        return mFactory;
    }
    
    /**
     * Creates a Widget to represent the given WSDL model component.
     *
     * @param  scene      the widgets will be created in this Scene.
     * @param  component  the WSDL component to represent.
     * @return  the new widget.
     */
    public Widget createWidget(Scene scene, WSDLComponent component) {
        return createWidget(scene, component, false);
    }
    
    /**
     * Creates a Widget to represent the given WSDL model component.
     *
     * @param  scene      the widgets will be created in this Scene.
     * @param  component  the WSDL component to represent.
     * @param  reuse      if true, find and re-use an existing widget.
     * @return  the new widget.
     */
    public Widget createWidget(Scene scene, WSDLComponent component,
            boolean reuse) {
        // Default to using an empty Lookup, if none is given.
        return createWidget(scene, component, Lookup.EMPTY, reuse);
    }
    
    /**
     * Creates a Widget to represent the given WSDL model component.
     *
     * @param  scene      the widgets will be created in this Scene.
     * @param  component  the WSDL component to represent.
     * @param  lookup     the Lookup for the widget.
     * @return  the new widget.
     */
    public Widget createWidget(Scene scene, WSDLComponent component,
            Lookup lookup) {
        return createWidget(scene, component, lookup, false);
    }
    
    /**
     * Creates a Widget to represent the given WSDL model component.
     *
     * @param  scene      the widgets will be created in this Scene.
     * @param  component  the WSDL component to represent.
     * @param  lookup     the Lookup for the widget.
     * @param  reuse      if true, find and re-use an existing widget.
     * @return  the new widget.
     */
    public Widget createWidget(Scene scene, WSDLComponent component,
            Lookup lookup, boolean reuse) {
        Widget widget = null;
        
        if (reuse) {
            widget = ((PartnerScene) scene).findWidget(component);
            if (widget != null) {
                return widget;
            }
        }
        
        if (component instanceof Fault) {
            Fault fault = (Fault)component;
            widget = new FaultWidget(scene, fault, lookup,
                    fault.getParent() instanceof SolicitResponseOperation );
        } else if (component instanceof Input) {
            widget = new InputWidget(scene, (Input) component, lookup);
        } else if (component instanceof Message) {
            widget = new MessageWidget(scene, (Message) component, lookup);
        } else if (component instanceof Part) {
            widget = new PartWidget(scene, (Part) component, lookup);
        } else if (component instanceof NotificationOperation) {
            widget = new NotificationOperationWidget(
                    scene, (NotificationOperation) component, lookup);
        } else if (component instanceof OneWayOperation) {
            widget = new OneWayOperationWidget(
                    scene, (OneWayOperation) component, lookup);
        } else if (component instanceof Output) {
            widget = new OutputWidget(scene, (Output) component, lookup);
        } else if (component instanceof PartnerLinkType) {
            widget = new PartnerLinkTypeWidget(
                    scene, (PartnerLinkType) component, lookup);
        } else if (component instanceof PortType) {
            widget = new PortTypeWidget(scene, (PortType) component, lookup);
        } else if (component instanceof RequestResponseOperation) {
            widget = new RequestReplyOperationWidget(
                    scene, (RequestResponseOperation) component, lookup);
        } else if (component instanceof Role) {
            widget = new RoleWidget(scene, (Role) component, lookup);
        } else if (component instanceof SolicitResponseOperation) {
            widget = new SolicitResponseOperationWidget(
                    scene, (SolicitResponseOperation) component, lookup);
        }
        if (widget != null) {
            prepareWidget(scene, widget, component);
        }
        return widget;
    }
    
    /**
     * Creates a Widget to represent a WSDL component of the given type.
     * The widget will not have an assigned component, which means it
     * can not be selected, and essentially does not exist. It is only
     * useful as a placeholder in the diagram.
     *
     * @param  scene   the widgets will be created in this Scene.
     * @param  type    the WSDL component type to be represented.
     * @param  lookup  the Lookup for the widget.
     * @return  the new widget.
     */
    public Widget createWidget(Scene scene, Class<? extends WSDLComponent> type,
            Lookup lookup) {
        Widget widget = null;
        
        if (Fault.class.isAssignableFrom(type)) {
            widget = new FaultWidget(scene, null, lookup, false);
        } else if (Input.class.isAssignableFrom(type)) {
            widget = new InputWidget(scene, null, lookup);
        } else if (Message.class.isAssignableFrom(type)) {
            widget = new MessageWidget(scene, null, lookup);
        } else if (NotificationOperation.class.isAssignableFrom(type)) {
            widget = new NotificationOperationWidget(scene, null, lookup);
        } else if (OneWayOperation.class.isAssignableFrom(type)) {
            widget = new OneWayOperationWidget(scene, null, lookup);
        } else if (Output.class.isAssignableFrom(type)) {
            widget = new OutputWidget(scene, null, lookup);
        } else if (PartnerLinkType.class.isAssignableFrom(type)) {
            widget = new PartnerLinkTypeWidget(scene, null, lookup);
        } else if (PortType.class.isAssignableFrom(type)) {
            
            widget = new PortTypeWidget(scene, null, lookup);
        } else if (RequestResponseOperation.class.isAssignableFrom(type)) {
            widget = new RequestReplyOperationWidget(scene, null, lookup);
        } else if (Role.class.isAssignableFrom(type)) {
            widget = new RoleWidget(scene, null, lookup);
        } else if (SolicitResponseOperation.class.isAssignableFrom(type)) {
            widget = new SolicitResponseOperationWidget(scene, null, lookup);
        }
        if (widget != null) {
            prepareWidget(scene, widget, null);
        }
        return widget;
    }
    
    /**
     * Creates a Widget to represent an OperationParameter, which could
     * be either an input or an output. This method exists because it is
     * not possible to create both Input/Output and OperationParameter
     * widgets using the instanceof keyword.
     *
     * @param  scene      the widgets will be created in this Scene.
     * @param  component  the operation parameter to be represented.
     * @param  lookup     the Lookup for the widget.
     * @return  the new widget.
     */
    public Widget createOperationParameterWidget(Scene scene,
            OperationParameter component, Lookup lookup) {
        Widget widget = new OperationParameterWidget(scene, component, lookup);
        prepareWidget(scene, widget, component);
        return widget;
    }
    
    /**
     * Perform additional preparation on the widget now that it has been
     * created, including adding actions and mapping it in the scene.
     *
     * @param  scene      the Scene for the widget.
     * @param  widget     the widget to prepare.
     * @param  component  the WSDL component for the widget; may be null.
     */
    private void prepareWidget(Scene scene, Widget widget,
            WSDLComponent component) {
        if (scene instanceof PartnerScene) {
            // Add the object selection action to the widget.
            widget.getActions().addAction(((PartnerScene) scene).getSelectAction());
        }
        if (scene instanceof ObjectScene) {
            ObjectScene os = (ObjectScene) scene;
            if (component != null) {
                // Add the object-widget mapping in the scene.
                List<Widget> widgets = os.findWidgets(component);
                if (widgets == null) {
                    widgets = new ArrayList<Widget>();
                } else {
                    // Remove the original mapping.
                    os.removeObject(component);
                    // The List that comes back is immutable...
                    widgets = new ArrayList<Widget>(widgets);
                }
                widgets.add(widget);
                os.addObject(component, widgets.toArray(
                        new Widget[widgets.size()]));
            }
        }
    }
}
