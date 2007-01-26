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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.HoverActionProvider;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Represents a role in the WSDL model.
 */
public class RoleWidget extends AbstractWidget<Role> {
    private PortTypeWidget mPortTypeWidget;
    private CenteredLabelWidget mLabelWidget;
    private PartnerLinkType mPartnerLinkType;

    /**
     * Creates a new instance of RoleWidget.
     *
     * @param  scene   the Scene to contain this widget.
     * @param  role    the WSDL component.
     * @param  lookup  the Lookup for this widget.
     */
    public RoleWidget(Scene scene, Role role, Lookup lookup) {
        super(scene, role, lookup);
        mPartnerLinkType = (PartnerLinkType) getLookup().lookup(PartnerLinkType.class);
        init();
    }

    private void init() {
        setLayout(new RoleWidgetLayout(10));
        
        setOpaque(true);
        refreshPortTypeColumn();
        
        //setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }
    
    private String getName() {
        Role role = getWSDLComponent();
        if (role == null) {
            return NbBundle.getMessage(RoleWidget.class, "ROLEWIDGET_PLACE_HOLDER_ROLENAME");
        }
        
        return role.getName();
    }

    public void showHotSpot(boolean show) {
        if (show) {
            mPortTypeWidget.showHotSpot();
        } else {
            mPortTypeWidget.clearHotSpot();
        }
    }

    @Override
    public void updateContent() {
        refreshPortTypeColumn();
    }

    @Override
    public void childrenDeleted(ComponentEvent event) {
        super.childrenDeleted(event);
        // Ignore whether this event is for our component or not, since it
        // may be for a PortType shared between multiple roles.
        if (event.getSource() instanceof Definitions) {
            // Check if the port type is no longer in the model.
            PortType portType = getPortType();
            PortType widgetPT = mPortTypeWidget.getWSDLComponent();
            if (widgetPT != null && portType == null) {
                if (EventQueue.isDispatchThread()) {
                    refreshPortTypeColumn();
                } else {
                    EventQueue.invokeLater(new Runnable(){
                        public void run() {
                            refreshPortTypeColumn();
                        }
                    });
                }
            }
        }
    }

    /**
     * Retrieve the PortType for this RoleWidget.
     *
     * @return  PortType, or null if not available.
     */
    private PortType getPortType() {
        try {
            Role role = getWSDLComponent();
            if (role != null) {
                NamedComponentReference<PortType> ptref = role.getPortType();
                if (ptref != null) {
                    return ptref.get();
                }
            }
        } catch (IllegalStateException ise) {
            // Indicates the referencing component is no longer in the model.
            // Fall through and return null.
        }
        return null;
    }

    private void refreshPortTypeColumn() {
        removeChildren();
        mLabelWidget = new CenteredLabelWidget(getScene(), getName(), Color.WHITE);
        addChild(mLabelWidget);
        //mLabelWidget.setBackground(Color.WHITE);
        mLabelWidget.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mLabelWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

            public void setText(Widget widget, String text) {
                WSDLModel model = mPartnerLinkType.getModel();
                boolean newRoleCreated = false;
                try {
                    if (model.startTransaction()) {
                        Role role = getWSDLComponent();
                        if (role == null) {
                            role = (Role) mPartnerLinkType.getModel().
                                    getFactory().create(mPartnerLinkType,
                                    BPELQName.ROLE.getQName());
                            if (mPartnerLinkType.getRole1() == null) {
                                mPartnerLinkType.setRole1(role);
                            } else if (mPartnerLinkType.getRole2() == null) {
                                mPartnerLinkType.setRole2(role);
                            }
                            newRoleCreated = true;
                        }
                        role.setName(text);
                    }
                } finally {
                    model.endTransaction();
                }
                if (newRoleCreated) {
                    refreshPortTypeColumn();
                }
            }

            public boolean isEnabled(Widget widget) {
                return true;
            }

            public String getText(Widget widget) {
                Role role = getWSDLComponent();
                if (role == null) {
                    return mPartnerLinkType.getName() + "Role"; //generate a new name;
                }
                return role.getName();
            }

        },
        EnumSet.<InplaceEditorProvider.ExpansionDirection>of (InplaceEditorProvider.ExpansionDirection.LEFT, 
                InplaceEditorProvider.ExpansionDirection.RIGHT)));
        mLabelWidget.getActions().addAction(HoverActionProvider.getDefault(
                getScene()).getHoverAction());
        WidgetFactory factory = WidgetFactory.getInstance();
        PortType portType = getPortType();
        if (portType != null) {
            mPortTypeWidget = (PortTypeWidget) factory.createWidget(
                    getScene(), portType, getLookup());
        } else {
            mPortTypeWidget = (PortTypeWidget) factory.createWidget(
                    getScene(), PortType.class, getLookup());
        }
        
        addChild(mPortTypeWidget);
        getScene().revalidate();
    }
    
    /**
     * This layout makes the second widget (which is the port type widget) to fill the 
     * remaining height of the parent widget, after the first widget is placed.
     * 
     * @author skini
     *
     */
    class RoleWidgetLayout implements Layout {
        
        private Layout vertLayout; 
        private int mGap = 0;
        
        public RoleWidgetLayout(int gap) {
            mGap = gap;
            vertLayout = LayoutFactory.createVerticalLayout(SerialAlignment.JUSTIFY, gap);
        }
        
        public void justify(Widget widget) {
            List<Widget> children = widget.getChildren();
            
            if (children.size() < 2) return;
            
            Rectangle parentBounds = widget.getClientArea();
            
            Widget nameWidget = children.get(0);
            Widget portTypeWidget = children.get(1);
            
            Point nameWLocation = nameWidget.getLocation();
            Rectangle nameBounds = nameWidget.getBounds();
            
            int parentX1 = parentBounds.x;
            int parentX2 = parentX1 + parentBounds.width;
            int nameX1 = nameWLocation.x + nameBounds.x;
            int nameX2 = nameX1 + nameBounds.width;
            

            nameBounds.x = Math.min (parentX1, nameX1);
            nameBounds.width = Math.max (parentX2, nameX2) - nameBounds.x;
            nameBounds.x -= nameWLocation.x;
            nameWidget.resolveBounds (nameWLocation, nameBounds);
            
            
            Point portTypeWLocation = portTypeWidget.getLocation();
            Rectangle portTypeBounds = portTypeWidget.getBounds();
            
            int portTypeX1 = portTypeWLocation.x + portTypeBounds.x;
            int portTypeX2 = portTypeX1 + portTypeBounds.width;
            

            portTypeBounds.x = Math.min (parentX1, portTypeX1);
            portTypeBounds.width = Math.max (parentX2, portTypeX2) - portTypeBounds.x;
            portTypeBounds.x -= portTypeWLocation.x;
            portTypeBounds.height = parentBounds.height - (nameBounds.height + mGap);
            
            portTypeWidget.resolveBounds (portTypeWLocation, portTypeBounds);
        }

        public void layout(Widget widget) {
            vertLayout.layout(widget);
        }

        public boolean requiresJustification(Widget widget) {
            return true;
        }
        
    }
}
