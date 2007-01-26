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

/*
 * PortTypeColumnWidget.java
 *
 * Created on November 5, 2006, 10:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditor;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditorProvider;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.HoverActionProvider;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.DisplayObject;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author radval
 */
public class PortTypeWidget extends AbstractWidget<PortType> implements DnDHandler {
    
    private LayerWidget mMainLayer;
    private LayerWidget mHotSpotLayer;
    private CenteredLabelWidget mNameWidget;
    private Role mRole;
    private IconNodeWidget mHotspot;
    private PartnerLinkTypeContentWidget mPLTContentWidget;

    /**
     * Creates a new instance of PortTypeWidget.
     *
     * @param  scene     the Scene to contain this widget.
     * @param  portType  the WSDL component.
     * @param  lookup    the Lookup for this widget.
     */
    public PortTypeWidget(Scene scene, PortType portType, Lookup lookup) {
        super(scene, portType, lookup);
        mPLTContentWidget = (PartnerLinkTypeContentWidget) getLookup().lookup(PartnerLinkTypeContentWidget.class);
        mRole = (Role) getLookup().lookup(Role.class);
        if (mRole != null) {
            PartnerLinkType plt = (PartnerLinkType) mRole.getParent();
            boolean rightSided = mRole == plt.getRole1();
            DirectionCookie dc = new DirectionCookie(rightSided);
            getLookupContent().add(dc);
        }
        
        init();
    }

    private void init() {
        setOpaque(true);
        mHotspot = new IconNodeWidget(getScene(), IconNodeWidget.TextOrientation.BOTTOM_CENTER);
        mHotspot.setImage(IMAGE_HOTSPOT);
        mMainLayer = new LayerWidget(getScene());
        mMainLayer.setLayout(LayoutFactory.createVerticalLayout(SerialAlignment.CENTER, 1));
        addChild(mMainLayer);

        NamedComponentReference<PortType> ptRef = mRole == null ? null : mRole.getPortType();
        PortType pt;
        try {
            pt = ptRef == null ? null : ptRef.get();
        } catch (IllegalStateException ise) {
            // Indicates PortType reference is no longer in model.
            pt = null;
        }
        if (ptRef != null && pt == null) {
            mNameWidget = new CenteredLabelWidget(getScene(), ptRef.getRefString(),
                    new Color(217, 244, 218));
            mNameWidget.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC));
        } else {
            mNameWidget = new CenteredLabelWidget(getScene(), getName(),
                    new Color(217, 244, 218));
        }
        mNameWidget.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        if (mRole != null) {
            mNameWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new ComboBoxInplaceEditorProvider(new ComboBoxInplaceEditor () {
                
                DisplayObject blankDispObj = new DisplayObject("", (Object) "Reset");
                
                public void setSelectedItem(Object selectedItem) {
                    Model model = mRole.getModel();
                    try {
                        if (model.startTransaction()) {
                            PortType pt = mRole.getPortType() == null ?
                                null : mRole.getPortType().get();
                            if (selectedItem instanceof DisplayObject) {
                                DisplayObject dispObj = (DisplayObject) selectedItem;
                                if (dispObj == blankDispObj) {
                                    pt = null;
                                } else {
                                    pt = (PortType) dispObj.getValue();
                                }
                                
                            } else if (selectedItem instanceof String) {
                                
                                String portTypeName = (String)selectedItem;
                                if (portTypeName != null &&
                                        portTypeName.trim().length() > 0) {
                                    if (pt == null) {
                                        //create new one first time.
                                        pt = mRole.getModel().getFactory().createPortType();
                                        pt.setName(portTypeName);
                                        mRole.getModel().getDefinitions().addPortType(pt);
                                    } else {
                                        //rename the existing port type
                                        pt.setName(portTypeName);
                                    }
                                }
                            }
                            if (pt != null) {
                                mRole.setPortType(mRole.createReferenceTo(pt, PortType.class));
                            } else {
                                mRole.setPortType(null);
                            }
                            
                        }
                    } finally {
                        model.endTransaction();
                    }
                }

                public boolean isEnabled(Widget widget) {
                    return true;
                }

                public Object getSelectedItem() {
                    if (mRole.getPortType() != null)
                        return mRole.getPortType().get();
                    return null;
                }

                public ComboBoxModel getModel() {
                    Vector<DisplayObject> list = getAllPortTypes(mRole.getModel());
                    
                    list.insertElementAt(blankDispObj, 0);
                    DefaultComboBoxModel model = new DefaultComboBoxModel(list);

                    if (getSelectedItem() != null) {
                        DisplayObject selectedObject = null;
                        for (DisplayObject dispObj : list) {
                            if (dispObj.getValue().equals(getSelectedItem())) {
                                selectedObject = dispObj;
                                break;
                            }
                        }
                        if (selectedObject != null) {
                            model.setSelectedItem(selectedObject);
                        }
                    }
                    return model;
                }

                public boolean getEditable() {
                    return true;
                }

            }, EnumSet.<InplaceEditorProvider.ExpansionDirection>of (InplaceEditorProvider.ExpansionDirection.LEFT, 
                    InplaceEditorProvider.ExpansionDirection.RIGHT)))); 
        }
        mNameWidget.getActions().addAction(HoverActionProvider.getDefault(getScene()).getHoverAction());

        mMainLayer.addChild(mNameWidget);
        
        mHotSpotLayer = new LayerWidget(getScene());
        addChild(mHotSpotLayer);
        setMinimumSize(new Dimension(0, 250));
        mMainLayer.setOpaque(true);
        //setBorder(BorderFactory.createLineBorder(Color.CYAN));
        if (getWSDLComponent() != null) {
            getActions().addAction(((ExScene) getScene()).getDnDAction());
        }
    }

    @Override
    protected void postDeleteComponent(Model model) {
        // In addition to removing the component, clear the reference in
        // the role component (using null will remove the attribute).
        mRole.setPortType(null);
    }

    private String getName() {
        PortType pt = getWSDLComponent();
        if (pt == null) {
            return NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget_BLANK_PORTTYPE_MSG");
        }
        return pt.getName();
    }
    
    /* returning -1 doesnt show the hotspot*/
    public int getEffectiveOperationCount() {
        int count = 0;
        DirectionCookie dc = (DirectionCookie) getLookup().lookup(
                DirectionCookie.class);
        if (dc != null && dc.isLeftSided()) {
            PartnerLinkType plt = (PartnerLinkType) mRole.getParent();
            if (plt != null) {
                Role role1 = plt.getRole1();
                if (role1 != null && role1.getPortType() != null) {
                    PortType pt = role1.getPortType().get();
                    if (pt != null) {
                        count += pt.getOperations().size();
                    }
                }
            }
            
        }
        if (mRole != null && mRole.getPortType() != null) {
            PortType pt = mRole.getPortType().get();
            if (pt != null) {
                if (pt.getOperations() != null) {
                    count += pt.getOperations().size();
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
        return count;
    }
    
    public Point getLastAnchor() {
        int count = getEffectiveOperationCount();
       // int oslayerCount = mContext.getContentWidget().getOperationSceneLayer().getChildren().size();
/*        if (oslayerCount != 1 && oslayerCount > count) {
            if (!mRightSided) {
                count ++;
            }
        }*/
        if (count == 0 || count == -1) {
            return null;
        }
        int height = 67 + 20; //15 = operation name height
        int y = (height + 25) * count;//25 = gap between operations
        if (mHotspot.getParentWidget() == null) {
            return new Point(0, y);
        }
        return mHotspot.getParentWidget().convertLocalToScene(new Point(0, y));//we dont need to find x.
    }
    
    public Point getMidPoint() {
        Rectangle clientArea = getBounds();
        if (clientArea == null) return new Point();
        
        int x = clientArea.x;
        int y = clientArea.y;

        return new Point(x+clientArea.width/2, y + 70);
    }
    
    private static Image IMAGE_HOTSPOT  = Utilities.loadImage
    ("org/netbeans/modules/xml/wsdl/ui/view/grapheditor/palette/resources/hotspot.png");
    
    
    public void showHotSpot() {
        if (mHotSpotLayer.getChildren() != null && mHotSpotLayer.getChildren().size() == 0) {
            mPLTContentWidget.getOperationSceneLayer().showBlankWidget(getEffectiveOperationCount());
            mHotSpotLayer.addChild(mHotspot);
            mHotspot.setPreferredLocation(getHotSpotLocation());
            getScene().getView().scrollRectToVisible(getScene().getView().getVisibleRect());
        }
    }
    
    
    public void clearHotSpot() {
        if(mHotSpotLayer.getChildren().contains(mHotspot)) {
            mHotSpotLayer.removeChild(mHotspot);
            mHotSpotLayer.setPreferredLocation(null);
            mPLTContentWidget.getOperationSceneLayer().removeBlankWidget();
            //getScene().revalidate();
       }
    }
    
    
    private Point getHotSpotLocation() {
        Point p = getLastAnchor();
        if(p == null) {
            Rectangle clientArea = getBounds();
            int x = clientArea.x;
            int y = clientArea.y;
            return new Point((x + getMidPoint().x) - 4, y + mNameWidget.getBounds().height + 10);
        }
        
        p = convertSceneToLocal(p);
        return new Point(getMidPoint().x - 6, p.y + mNameWidget.getBounds().height + 10);
        
    }
    

    @Override
    protected Rectangle calculateClientArea() {
        Rectangle bounds = getBounds();
        if (bounds == null) return super.calculateClientArea();
        
        int y = bounds.y;
        
        int newY = y + bounds.height;
        if (mPLTContentWidget.getBounds() != null) {
            int tempY = mPLTContentWidget.getBounds().y + mPLTContentWidget.getBounds().height;
            Point scenePoint = mPLTContentWidget.convertLocalToScene(new Point(0, tempY));
            newY = convertSceneToLocal(scenePoint).y;
        }
        int height = newY - y;
        Rectangle clientArea = super.calculateClientArea();
        clientArea.height = height;
        return clientArea;
    }

    /**
     * Paints the label widget.
     */
    @Override
    protected void paintWidget () {
        Graphics2D gr = getGraphics ();
        
        Rectangle clientArea = getBounds();

        int x = clientArea.x;
        int y = clientArea.y;
        
        int newY = y + clientArea.height;
        if (mPLTContentWidget.getBounds() != null) {
            int tempY = mPLTContentWidget.getBounds().y + mPLTContentWidget.getBounds().height;
            Point scenePoint = mPLTContentWidget.convertLocalToScene(new Point(0, tempY));
            newY = convertSceneToLocal(scenePoint).y;
        }
        Stroke oldStroke = gr.getStroke();
        Color oldColor = gr.getColor();
        Font font = gr.getFont();
        
        BasicStroke dotted = new BasicStroke(1, BasicStroke.CAP_SQUARE, 
                     BasicStroke.JOIN_ROUND, 10.0f, new float[]{5,10,5,10}, 0);
        
        gr.setStroke(dotted);
        gr.setColor(Color.GRAY);
        gr.setFont (getFont ());
        
        gr.drawLine((x+clientArea.width)/2, y + mNameWidget.getClientArea().height, (x+clientArea.width)/2, newY);
        
        gr.setStroke(oldStroke);
        gr.setColor(oldColor);
        gr.setFont(font);
    }
    
    private Vector<DisplayObject> getAllPortTypes(WSDLModel model) {
        Vector<DisplayObject> list = new Vector<DisplayObject>();
        
        list.addAll(getAllAvailablePortTypes(model, model));
        
        for (WSDLModel imported : Utility.getImportedDocuments(model)) {
            list.addAll(getAllAvailablePortTypes(model, imported));
        }
        
        return list;
        
    }
    
    private static List<DisplayObject> getAllAvailablePortTypes(WSDLModel source,  WSDLModel document) {
        ArrayList<DisplayObject> portTypesList = new ArrayList<DisplayObject>();

        Definitions definition =  document.getDefinitions();
        
        for (PortType portType : definition.getPortTypes()) {
            String name = portType.getName();
            String targetNamespace = document.getDefinitions().getTargetNamespace();
            String prefix = Utility.getNamespacePrefix(targetNamespace, source);
            if(name != null) {
                if(prefix != null) {
                    String portTypeQNameStr = prefix + ":" + name;
                    portTypesList.add(new DisplayObject(portTypeQNameStr, portType));
                } else {
                    portTypesList.add(new DisplayObject(name, portType));
                }
            }
        }
        
        return  portTypesList;
    }
    
    public void addOperation(String name) {
        PortType pt = getWSDLComponent();
        if (pt == null || name == null) {
            return;
        }
        
        WSDLModel model = getWSDLComponent().getModel();
        WSDLComponentFactory factory = model.getFactory();
        try {
            if (model.startTransaction()) {
                Operation operation = null;
                if (name.startsWith("RequestReply")) {
                    operation = factory.createRequestResponseOperation();
                    operation.setName(NameGenerator.getInstance().
                            generateUniqueOperationName(pt));
                    pt.addOperation(operation);
                    Input in = factory.createInput();
                    in.setName(NameGenerator.getInstance().
                            generateUniqueOperationInputName(operation));
                    operation.setInput(in);
                    Output out = factory.createOutput();
                    out.setName(NameGenerator.getInstance().
                            generateUniqueOperationOutputName(operation));
                    operation.setOutput(out);
                } else if (name.startsWith("OneWay")) {
                    operation = factory.createOneWayOperation();
                    operation.setName(NameGenerator.getInstance().
                            generateUniqueOperationName(pt));
                    pt.addOperation(operation);
                    Input in = factory.createInput();
                    in.setName(NameGenerator.getInstance().
                            generateUniqueOperationInputName(operation));
                    operation.setInput(in);
                } else if (name.startsWith("Notification")) {
                    operation = factory.createNotificationOperation();
                    operation.setName(NameGenerator.getInstance().
                            generateUniqueOperationName(pt));
                    pt.addOperation(operation);
                    Output out = factory.createOutput();
                    out.setName(NameGenerator.getInstance().
                            generateUniqueOperationOutputName(operation));
                    operation.setOutput(out);
                } else if (name.startsWith("SolicitResponse")) {
                    operation = factory.createSolicitResponseOperation();
                    operation.setName(NameGenerator.getInstance().
                            generateUniqueOperationName(pt));
                    pt.addOperation(operation);
                    Input in = factory.createInput();
                    in.setName(NameGenerator.getInstance().
                            generateUniqueOperationInputName(operation));
                    operation.setInput(in);
                    Output out = factory.createOutput();
                    out.setName(NameGenerator.getInstance().
                            generateUniqueOperationOutputName(operation));
                    operation.setOutput(out);
                }
            }
        } finally {
            model.endTransaction();
        }
    }
  


    @Override
    public void updateContent() {
        if (!getName().equals(mNameWidget.getLabel())) {
            mNameWidget.setLabel(getName());
        }
    }

    
    // DnDHandler implementation BEGIN 
    
    public void dragExit() {
        clearHotSpot();
    }

    
    public boolean dragOver(Point scenePoint, WidgetAction.WidgetDropTargetDragEvent event) {
        Transferable transferable = event.getTransferable();
        
        try {
            if (transferable != null) {
                for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    if (Node.class.isAssignableFrom(repClass)) {
                        if (isAssignable(Node.class.cast(transferable.getTransferData(flavor)))) {
                            showHotSpot();
                            event.acceptDrag(event.getDropAction());
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //do nothing
        }
        
        clearHotSpot();
        return false;
    }

    
    public boolean drop(Point scenePoint, WidgetAction.WidgetDropTargetDropEvent event) {
        Transferable transferable = event.getTransferable();
        try {
            if (transferable != null) {
                for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    Object data = transferable.getTransferData(flavor);
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = (Node) data;
                        clearHotSpot();
                        addOperation(node.getName());
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            //do nothing
        }
        return false;
    }

    public boolean isCollapsed() {
        return false;
    }

    public void expandForDragAndDrop() {
    }
    
    
    private boolean isAssignable(Node node) {
        if (node.getName().startsWith("Notification") ||
                node.getName().startsWith("SolicitResponse") ||
                node.getName().startsWith("RequestReply") ||
                node.getName().startsWith("OneWay")) {
            return true;
        }

        return false;
    }    
    
    // DnDHandler implamentation END
}
