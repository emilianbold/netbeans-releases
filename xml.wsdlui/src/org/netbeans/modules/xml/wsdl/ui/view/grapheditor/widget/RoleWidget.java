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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.BorderFactory;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.LabelWidget.VerticalAlignment;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortTypeNode;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Represents a role in the WSDL model.
 */
public class RoleWidget extends AbstractWidget<Role> implements DnDHandler{
    private PortTypeWidget mPortTypeWidget;
    private LabelWidget mLabelWidget;
    private PartnerLinkType mPartnerLinkType;
    private boolean leftSided;
    private int GAP = 25;
    private int MINIMUM_WIDTH = 225;
    private WidgetAction editorAction;

    /**
     * Creates a new instance of RoleWidget.
     *
     * @param  scene   the Scene to contain this widget.
     * @param  role    the WSDL component.
     * @param  lookup  the Lookup for this widget.
     */
    public RoleWidget(Scene scene, Role role, Lookup lookup) {
        super(scene, role, lookup);
        mPartnerLinkType = getLookup().lookup(PartnerLinkType.class);
        init();
    }

    private void init() {
        setMinimumSize(new Dimension(MINIMUM_WIDTH, 0));
        setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, GAP));
        setOpaque(true);
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

            public void setText(Widget widget, String text) {
                String errorMessage = null;
                if (text == null || text.trim().length() == 0) {
                    errorMessage = NbBundle.getMessage(RoleWidget.class, "MSG_BlankRoleName", text);
                } else if (!Utils.isValidNCName(text)) { 
                    errorMessage = NbBundle.getMessage(RoleWidget.class, "MSG_InvalidRoleName", text);
                }
                
                if (errorMessage != null) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(errorMessage, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                    return;
                }
                
                WSDLModel model = mPartnerLinkType.getModel();
                boolean newRoleCreated = false;
                Role role = getWSDLComponent();
                try {
                    if (model.startTransaction()) {
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
                    updateContent();
                }
                if (role != null) {
                    ActionHelper.selectNode(role);
                }
                
            }

            public boolean isEnabled(Widget widget) {
                if (mPartnerLinkType != null) {
                    return XAMUtils.isWritable(mPartnerLinkType.getModel());
                }
                return false;
            }

            public String getText(Widget widget) {
                Role role = getWSDLComponent();
                if (role == null) {
                    String name = mPartnerLinkType.getName() + "Role"; //generate a new name;
                    if (mPartnerLinkType.getRole1() != null && mPartnerLinkType.getRole1().getName().equals(name)
                            || mPartnerLinkType.getRole2() != null && mPartnerLinkType.getRole2().getName().equals(name)) {
                        name = name + "1";
                    }
                    
                    return name; 
                }
                return role.getName();
            }

        }, null);
        updateContent();
        if (getWSDLComponent() != null)
            getActions().addAction(((PartnerScene)getScene()).getDnDAction());
        getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F2) {
                    if (editorAction == null || mLabelWidget == null) return State.REJECTED;
                    InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                    if (inplaceEditorController.openEditor (mLabelWidget)) {
                        return State.createLocked (widget, this);
                    }
                    return State.CONSUMED;
                }
                return State.REJECTED;
            }

        });
    }
    
    private String getName() {
        Role role = getWSDLComponent();
        if (role == null) {
            return NbBundle.getMessage(RoleWidget.class, "ROLEWIDGET_PLACE_HOLDER_ROLENAME");
        }
        
        return role.getName();
    }

    protected void setLeftSided(boolean isLeftSided) {
        leftSided = isLeftSided;
    }
    
    protected boolean isLeftSided() {
        return leftSided;
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
                    updateContent();
                    getScene().validate();
                } else {
                    EventQueue.invokeLater(new Runnable(){
                        public void run() {
                            updateContent();
                            getScene().validate();
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
        if (getWSDLComponent() == null) {
            mLabelWidget = new LabelWidget(getScene(), getName());
            mLabelWidget.setAlignment(Alignment.CENTER);
            mLabelWidget.setVerticalAlignment(VerticalAlignment.CENTER);
            mLabelWidget.setForeground(Color.LIGHT_GRAY);
            mLabelWidget.setToolTipText(NbBundle.getMessage(RoleWidget.class, "RoleWidget_DBL_CLICK_CREATE_NEW_ROLE_TT"));
        } else {
            mLabelWidget = new LabelWidget(getScene(), getName());
            mLabelWidget.setAlignment(Alignment.CENTER);
            mLabelWidget.setVerticalAlignment(VerticalAlignment.CENTER);
            mLabelWidget.setToolTipText(null);
        }
       // mLabelWidget.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mLabelWidget.setMinimumSize(new Dimension(MINIMUM_WIDTH, WidgetConstants.TEXT_LABEL_HEIGHT));
        addChild(mLabelWidget);
        mLabelWidget.getActions().addAction(editorAction);
        WidgetFactory factory = WidgetFactory.getInstance();
        PortType portType = getPortType();
        if (portType != null) {
            mPortTypeWidget = (PortTypeWidget) factory.createWidget(
                    getScene(), portType, getLookup(), true);
            //if already being used, create new one.
            if (mPortTypeWidget.getParentWidget() != null) {
                mPortTypeWidget = (PortTypeWidget) factory.createWidget(
                        getScene(), portType, getLookup());
            }
        } else {
            mPortTypeWidget = (PortTypeWidget) factory.createWidget(
                    getScene(), PortType.class, getLookup());
        }
        
        addChild(mPortTypeWidget);
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
            vertLayout = LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, gap);
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

    public void dragExit() {
        mPortTypeWidget.setBorder(BorderFactory.createEmptyBorder());
        
    }

    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        Transferable transferable = event.getTransferable();

        try {
            if (transferable != null) {
                for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = Node.class.cast(transferable.getTransferData(flavor));
                        if (node instanceof PortTypeNode) {
                            mPortTypeWidget.setBorder(BorderFactory.createLineBorder(WidgetConstants.HIT_POINT_BORDER, 2));
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //do nothing
        }
        return false;
    }

    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
        Transferable transferable = event.getTransferable();
        try {
            if (transferable != null) {
                for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    Object data = transferable.getTransferData(flavor);
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = (Node) data;
                        if (node instanceof PortTypeNode) {
                            mPortTypeWidget.setBorder(BorderFactory.createEmptyBorder());
                            setPortType((PortTypeNode)node);
                        }
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            //do nothing
        }
        return false;
    }

    private void setPortType(PortTypeNode node) {
        PortType pt = (PortType) node.getWSDLComponent();
        if (getWSDLComponent().getModel().startTransaction()) {
            try {
                getWSDLComponent().setPortType(getWSDLComponent().createReferenceTo(pt, PortType.class));
            } finally {
                getWSDLComponent().getModel().endTransaction();
            }
        }
        ActionHelper.selectNode(pt);
    }

    public void expandForDragAndDrop() {}

    public boolean isCollapsed() {
        return false;
    }

    @Override
    protected void updateActions(List<Action> actions) {
        super.updateActions(actions);
        ListIterator<Action> liter = actions.listIterator();
        while (liter.hasNext()) {
            Action action = liter.next();
            if (action instanceof NewAction) {
                liter.remove();
            }
        }
    }
    
    @Override
    protected Shape createSelectionShape() {
        return new Rectangle2D.Double(getBounds().x + 1, getBounds().y + 1, getBounds().width, WidgetConstants.TEXT_LABEL_HEIGHT * 2 + GAP);
    }
    
    @Override
    protected void paintWidget() {
        Graphics2D g = getGraphics();
        Color old = g.getColor();
        Stroke stk = g.getStroke();
        
        if (getWSDLComponent() != null) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(Color.GRAY);
            BasicStroke dotted = new BasicStroke(1, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_MITER, 5.0f, new float[]{10,5,10,5}, 0);
            g.setStroke(dotted);
        }
        if (getState().isSelected()) {
            g.setColor(WidgetConstants.SELECTION_COLOR);
        }
        g.draw(createSelectionShape());
        g.setColor(old);
        g.setStroke(stk);
    }
}
