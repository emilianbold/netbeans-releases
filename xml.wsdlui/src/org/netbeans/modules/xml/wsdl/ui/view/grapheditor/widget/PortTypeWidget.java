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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.LabelWidget.VerticalAlignment;


import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.actions.schema.ExtensibilityElementCreatorVisitor;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditor;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditorProvider;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.OneSideJustifiedLayout;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortTypeNode;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.DisplayObject;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class PortTypeWidget extends AbstractWidget<PortType> implements DnDHandler {
    private LayerWidget mHotSpotLayer;
    private LabelWidget mNameWidget;
    private Role mRole;
    private RectangleWidget mHotspot;
    private PartnerLinkTypeContentWidget mPLTContentWidget;
    private ButtonWidget showComboBoxBtnWidget;
    private Widget nameHolderWidget;
    private Border enabledBorder = BorderFactory.createCompositeBorder(new Border[] {BorderFactory.createLineBorder(1, Color.BLACK), WidgetConstants.GRADIENT_BLUE_WHITE_BORDER});
    private Border disabledBorder = BorderFactory.createCompositeBorder(new Border[] {BorderFactory.createDashedBorder(Color.GRAY, 10, 5), WidgetConstants.GRADIENT_GRAY_WHITE_BORDER});
    private Border importedBorder = BorderFactory.createCompositeBorder(new Border[] {BorderFactory.createLineBorder(1, Color.BLACK), WidgetConstants.GRADIENT_GREEN_WHITE_BORDER});
    private Border border;
    private WidgetAction editorAction;
    
    
    public static enum State {

        ENABLED, DISABLED, NOT_REFERENCEABLE, IMPORTED
    }
    
    /**
     * Creates a new instance of PortTypeWidget.
     *
     * @param  scene     the Scene to contain this widget.
     * @param  portType  the WSDL component.
     * @param  lookup    the Lookup for this widget.
     */
    public PortTypeWidget(Scene scene, PortType portType, Lookup lookup) {
        super(scene, portType, lookup);
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

            public void setText(Widget widget, String text) {
                if (text == null || text.trim().length() == 0) return;
                if (getWSDLComponent() == null) {
                    WSDLModel model = getModel();
                    if (model.startTransaction()) {
                        PortType portType = null;
                        try {
                            //create new one first time.
                            portType = model.getFactory().createPortType();
                            portType.setName(text);
                            model.getDefinitions().addPortType(portType);
                            mRole.setPortType(mRole.createReferenceTo(portType, PortType.class));
                        } finally {
                            model.endTransaction();
                        }
                        if (portType != null) {
                            ActionHelper.selectNode(portType);
                        }
                    }
                } else {
                    if (!getWSDLComponent().getName().equals(text)) {
                        // try rename silent and locally
                        SharedUtils.locallyRenameRefactor(getWSDLComponent(), text);
                    }
                }
                if (getWSDLComponent() != null) {
                    ActionHelper.selectNode(getWSDLComponent());
                }
            }

            public boolean isEnabled(Widget widget) {
                return !isImported() && isWritable();
            }

            public String getText(Widget widget) {
                if (getWSDLComponent() != null)
                    return getWSDLComponent().getName();
                return NameGenerator.getInstance().generateUniquePortTypeName(getModel());
            }

        }, null);
        mPLTContentWidget = getLookup().lookup(PartnerLinkTypeContentWidget.class);
        mRole = getLookup().lookup(Role.class);
        if (mRole != null) {
            PartnerLinkType plt = (PartnerLinkType) mRole.getParent();
            boolean rightSided = mRole == plt.getRole1();
            DirectionCookie dc = new DirectionCookie(rightSided);
            getLookupContent().add(dc);
        }
        
        init();
        
        getActions().addAction(new WidgetAction.Adapter() {
            
            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F2) {
                    if (editorAction == null || mNameWidget == null) return State.REJECTED;
                    InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                    if (inplaceEditorController.openEditor (mNameWidget)) {
                        return State.createLocked (widget, this);
                    }
                    return State.CONSUMED;
                }
                return State.REJECTED;
            }
        
        });
    }

    private void init() {
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
        mHotspot = new RectangleWidget(getScene(), 12, 70);
        mHotspot.setThickness(4);
        mHotspot.setColor(WidgetConstants.HIT_POINT_BORDER);
        
        nameHolderWidget = new Widget(getScene());
        nameHolderWidget.setLayout(new PortTypeLayout());
        //nameHolderWidget.setLayout(mFillLayout);
        nameHolderWidget.setMinimumSize(new Dimension(190, 25));
        
        State state = State.ENABLED;
        if (mRole == null || getWSDLComponent() == null) {
            state = State.DISABLED;
        } else if (isImported()) {
            state = State.IMPORTED;
        }

        NamedComponentReference<PortType> ptRef = mRole == null ? null : mRole.getPortType();
        PortType pt;
        try {
            pt = ptRef == null ? null : ptRef.get();
        } catch (IllegalStateException ise) {
            // Indicates PortType reference is no longer in model.
            pt = null;
        }
        
        
        String name = "";
        //if port type cannot be found, then it may be the default value added for required attributes.
        if (ptRef != null && pt == null && 
                !ptRef.getRefString().equals(NbBundle.getMessage(ExtensibilityElementCreatorVisitor.class, "REQUIRED_PROPERTY_DEFAULT_VALUE"))) {
            name = ptRef.getRefString();
            state = State.NOT_REFERENCEABLE;
            
        } else {
            name = getName();
        }

        mNameWidget = new LabelWidget(getScene(), name);
        mNameWidget.setAlignment(Alignment.CENTER);
        mNameWidget.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = getScene().getFont().deriveFont(Font.BOLD);
        String tooltipText = null;
        border = enabledBorder;
        Color foreground = Color.BLACK;

        
        switch (state) {
        case NOT_REFERENCEABLE:
            font = font.deriveFont(Font.ITALIC);
            tooltipText = NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget.NonReferenceablePortType.TT");
        case ENABLED:
            break;
        case DISABLED:
            border = disabledBorder;
            foreground = Color.LIGHT_GRAY;
            if (mRole == null) {
                tooltipText = NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget.UnConfiguredRole.TT");
            } else if (pt == null) {
                tooltipText = NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget.UnConfiguredPortType.TT");
            }
            break;
        case IMPORTED:
            tooltipText = NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget.ImportedPortType.TT");
            border = importedBorder;
            foreground = Color.BLUE;
            break;
        }
        
        mNameWidget.setForeground(foreground);
        mNameWidget.setToolTipText(tooltipText);
        mNameWidget.setFont(font);
        
        nameHolderWidget.setBorder(border);
        showComboBoxBtnWidget = new ButtonWidget(getScene(), IMAGE_EXPAND, true);
        //showComboBoxBtnWidget.setMaximumSize(new Dimension(20, 20));
        showComboBoxBtnWidget.getActions().addAction(createInplaceEditorAction(
                new ComboBoxInplaceEditorProvider(new ComboBoxInplaceEditor() {
                    
                    DisplayObject blankDispObj = new DisplayObject("", (Object) "Reset");
                    
                    public void setSelectedItem(Object selectedItem) {
                        WSDLModel model = mRole.getModel();
                        try {
                            if (model.startTransaction()) {
                                PortType portType = mRole.getPortType() == null ?
                                    null : mRole.getPortType().get();
                                if (selectedItem instanceof DisplayObject) {
                                    DisplayObject dispObj = (DisplayObject) selectedItem;
                                    if (dispObj == blankDispObj) {
                                        portType = null;
                                    } else {
                                        portType = (PortType) dispObj.getValue();
                                    }
                                    
                                }
                                if (portType != null) {
                                    mRole.setPortType(mRole.createReferenceTo(portType, PortType.class));
                                } else {
                                    mRole.setPortType(null);
                                }
                                
                            }
                        } finally {
                            model.endTransaction();
                        }
                    }

                    public boolean isEnabled(Widget widget) {
                        return isWritable();
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
                        return false;
                    }

                }, EnumSet.<InplaceEditorProvider.ExpansionDirection>of (InplaceEditorProvider.ExpansionDirection.LEFT, 
                        InplaceEditorProvider.ExpansionDirection.RIGHT)), nameHolderWidget));
        showComboBoxBtnWidget.setVisible(false);
        
        if (mRole != null) {
            mNameWidget.getActions().addAction(new WidgetAction.Adapter() {
                
                @Override
                public State mouseClicked(Widget widget, WidgetMouseEvent event) {
                    if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
                        if (isWritable())
                            showComboBoxBtnWidget();
                    }
                    return super.mouseClicked(widget, event);
                }
                
                @Override
                public State focusLost(Widget widget, WidgetFocusEvent event) {
                    removeComboBoxBtnWidget();
                    return super.focusLost(widget, event);
                }
                
            });
            mNameWidget.getActions().addAction(editorAction);
        }
        

        nameHolderWidget.addChild(mNameWidget);
        nameHolderWidget.addChild(showComboBoxBtnWidget);
        
        addChild(nameHolderWidget);
        getScene().validate();
        mHotSpotLayer = new LayerWidget(getScene());
        addChild(mHotSpotLayer);
        setMinimumSize(new Dimension(0, 250));
        if (getWSDLComponent() != null) {
            getActions().addAction(((PartnerScene) getScene()).getDnDAction());
        }
        
    }

    public boolean isWritable() {
        if (mRole != null) {
            return XAMUtils.isWritable(mRole.getModel());
        }
        return false;
    }
    
    private boolean isImported() {
        if (getWSDLComponent() != null) {
            return getModel() != getWSDLComponent().getModel();
        }
        return false;
    }
    private void showComboBoxBtnWidget() {
        if (!showComboBoxBtnWidget.isVisible()) {
            showComboBoxBtnWidget.setVisible(true);
            getScene().revalidate();
        }
    }
    
    private void removeComboBoxBtnWidget() {
        if (showComboBoxBtnWidget.isVisible()) {
            showComboBoxBtnWidget.setVisible(false);
            getScene().revalidate();
        }
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (!state.isSelected()) {
            removeComboBoxBtnWidget();
        }
        super.notifyStateChanged(previousState, state);
    }
    
    private <C extends JComponent> WidgetAction createInplaceEditorAction (InplaceEditorProvider<JComboBox> provider, Widget widget) {
        return new SingleSelectInplaceEditorAction(provider, widget);
    }
    
    
    
    @Override
    protected Node getNodeFilter(Node original) {
        if (isImported()) {
            return new ReadOnlyWidgetFilterNode(original);
        }
        return super.getNodeFilter(original);
    }

    @Override
    protected void postDeleteComponent(Model model) {
        // In addition to removing the component, clear the reference in
        // the role component (using null will remove the attribute).
        mRole.setPortType(null);
    }

    private String getName() {
        PortType pt = getWSDLComponent();
        if (mRole == null) {
            return NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget_PLACEHOLDER_PORTTYPE_NAME");
        }
        if (pt == null) {
            return NbBundle.getMessage(PortTypeWidget.class, "PortTypeWidget_BLANK_PORTTYPE_MSG");
        }
        String ptTNS = pt.getModel().getDefinitions().getTargetNamespace();
        if (ptTNS != null) {
            return Utility.getNameAndDropPrefixIfInCurrentModel(ptTNS, pt.getName(), mRole.getModel());
        }
        return pt.getName();
    }
    
    /* returning -1 doesnt show the hotspot*/
    public int getEffectiveOperationCount() {
        int count = 0;
        DirectionCookie dc = getLookup().lookup(DirectionCookie.class);
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
        gr.setFont (getFont ());
        
        gr.drawLine((x+clientArea.width)/2, y + mNameWidget.getClientArea().height, (x+clientArea.width)/2, newY);
        
        gr.setStroke(oldStroke);
        gr.setColor(oldColor);
        gr.setFont(font);
    }
    
    @Override
    protected Shape createSelectionShape() {
        int startX = 0;
        int startY = 0;
        int width = getBounds().width;
        int height = getBounds().height - 4;
        int labelHeight = WidgetConstants.TEXT_LABEL_HEIGHT;
        int[] x = {startX, width, width, width / 2, width / 2, width / 2, startX, startX};
        int[] y = {startY, startY, labelHeight, labelHeight, height, labelHeight, labelHeight, startY};
        
        GeneralPath polyline = 
            new GeneralPath(GeneralPath.WIND_EVEN_ODD, x.length);

        polyline.moveTo (x[0], y[0]);

        for (int index = 1; index < x.length; index++) {
             polyline.lineTo(x[index], y[index]);
        }
        
        return polyline;
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
        setBorder(BorderFactory.createEmptyBorder());
    }

    
    public boolean dragOver(Point scenePoint, WidgetAction.WidgetDropTargetDragEvent event) {
        Transferable transferable = event.getTransferable();
        
        try {
            if (transferable != null) {
                for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = Node.class.cast(transferable.getTransferData(flavor));
                        if (isAssignable(node)) {
                            if (!isImported()) {
                                showHotSpot();
                                return true;
                            }
                        } else if (node instanceof PortTypeNode) {
                            setBorder(BorderFactory.createLineBorder(2, WidgetConstants.HIT_POINT_BORDER));
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //do nothing
        }
        
        clearHotSpot();
        setBorder(BorderFactory.createEmptyBorder());
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
                        if (node instanceof PortTypeNode) {
                            setBorder(BorderFactory.createEmptyBorder());
                            setPortTypeToRole((PortTypeNode)node);
                            return true;
                        }
                        //else its for operation.
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

    private void setPortTypeToRole(PortTypeNode node) {
        PortType pt = (PortType) node.getWSDLComponent();
        if (mRole.getModel().startTransaction()) {
            try {
                mRole.setPortType(mRole.createReferenceTo(pt, PortType.class));
            } finally {
                mRole.getModel().endTransaction();
            }
        }
        
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
    
    
    class SingleSelectInplaceEditorAction  extends WidgetAction.LockedAdapter implements InplaceEditorProvider.EditorController {

        private InplaceEditorProvider<JComboBox> provider;

        private JComboBox editor = null;
        private Widget replaceableWidget = null;
        private Rectangle rectBounds = null;

        /**
         * The JComponent is drawn with bounds and location of the given widget, though this action may be on a different widget. 
         * 
         * @param provider the provider
         * @param widget the widget upon which the JComponent will be shown
         */
        
        public SingleSelectInplaceEditorAction (InplaceEditorProvider<JComboBox> provider, Widget widget) {
            this.provider = provider;
            this.replaceableWidget = widget;
        }

        @Override
        protected boolean isLocked () {
            return editor != null;
        }

        @Override
        public State mouseClicked (Widget widget, WidgetMouseEvent event) {
            if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
                if (openEditor (replaceableWidget))
                    return State.createLocked (widget, this);
            }
            return State.REJECTED;
        }

        @Override
        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (editor != null)
                closeEditor (true);
            return State.REJECTED;
        }

        @Override
        public State mouseReleased (Widget widget, WidgetAction.WidgetMouseEvent event) {
            if (editor != null)
                closeEditor (true);
            return State.REJECTED;
        }

        @Override
        public State keyTyped (Widget widget, WidgetKeyEvent event) {
            if (event.getKeyChar () == KeyEvent.VK_ENTER)
                if (openEditor (replaceableWidget))
                    return State.createLocked (widget, this);
            return State.REJECTED;
        }

        public final boolean isEditorVisible () {
            return editor != null;
        }

        public final boolean openEditor (Widget widget) {
            if (editor != null)
                return false;

            Scene scene = widget.getScene ();
            JComponent component = scene.getView ();
            if (component == null)
                return false;

            editor = provider.createEditorComponent (this, widget);
            if (editor == null)
                return false;

            component.add (editor);
            provider.notifyOpened (this, widget, editor);

            Rectangle rectangle = widget.getScene ().convertSceneToView (widget.convertLocalToScene (widget.getPreferredBounds ()));

            Point center = new Point (rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
            Dimension size = editor.getMinimumSize ();
            if (rectangle.width > size.width)
                size.width = rectangle.width;
            if (rectangle.height > size.height)
                size.height = rectangle.height;
            int x = center.x - size.width / 2;
            int y = center.y - size.height / 2;

            rectangle = new Rectangle (x, y, size.width, size.height);
            updateRectangleToFitToView (rectangle);

            Rectangle r = provider.getInitialEditorComponentBounds (this, widget, editor, rectangle);
            this.rectBounds = r != null ? r : rectangle;

            editor.setPreferredSize(new Dimension(size.width, size.height));
            editor.setLocation(x, y);
            notifyEditorComponentBoundsChanged ();
            editor.requestFocus ();
            editor.showPopup();
            return true;
        }

        private void updateRectangleToFitToView (Rectangle rectangle) {
            JComponent component = replaceableWidget.getScene ().getView ();
            if (rectangle.x + rectangle.width > component.getWidth ())
                rectangle.x = component.getWidth () - rectangle.width;
            if (rectangle.y + rectangle.height > component.getHeight ())
                rectangle.y = component.getHeight () - rectangle.height;
            if (rectangle.x < 0)
                rectangle.x = 0;
            if (rectangle.y < 0)
                rectangle.y = 0;
        }

        public final void closeEditor (boolean commit) {
            if (editor == null)
                return;
            Container parent = editor.getParent ();
            Rectangle bounds = parent != null ? editor.getBounds () : null;
            provider.notifyClosing (this, replaceableWidget, editor, commit);
            if (bounds != null) {
                parent.remove (editor);
                parent.repaint (bounds.x, bounds.y, bounds.width, bounds.height);
                parent.requestFocus ();
            }
            editor = null;
            rectBounds = null;
        }

        public void notifyEditorComponentBoundsChanged () {
            EnumSet<InplaceEditorProvider.ExpansionDirection> directions = provider.getExpansionDirections (this, replaceableWidget, editor);
            if (directions == null)
                directions = EnumSet.noneOf (InplaceEditorProvider.ExpansionDirection.class);
            Rectangle rectangle = this.rectBounds;
            Dimension size = editor.getPreferredSize ();
            Dimension minimumSize = editor.getMinimumSize ();
            if (minimumSize != null) {
                if (size.width < minimumSize.width)
                    size.width = minimumSize.width;
                if (size.height < minimumSize.height)
                    size.height = minimumSize.height;
            }

            int heightDiff = rectangle.height - size.height;
            int widthDiff = rectangle.width - size.width;

            boolean top = directions.contains (InplaceEditorProvider.ExpansionDirection.TOP);
            boolean bottom = directions.contains (InplaceEditorProvider.ExpansionDirection.BOTTOM);

            if (top) {
                if (bottom) {
                    rectangle.y += heightDiff / 2;
                    rectangle.height = size.height;
                } else {
                    rectangle.y += heightDiff;
                    rectangle.height = size.height;
                }
            } else {
                if (bottom) {
                    rectangle.height = size.height;
                } else {
                }
            }

            boolean left = directions.contains (InplaceEditorProvider.ExpansionDirection.LEFT);
            boolean right = directions.contains (InplaceEditorProvider.ExpansionDirection.RIGHT);

            if (left) {
                if (right) {
                    rectangle.x += widthDiff / 2;
                    rectangle.width = size.width;
                } else {
                    rectangle.x += widthDiff;
                    rectangle.width = size.width;
                }
            } else {
                if (right) {
                    rectangle.width = size.width;
                } else {
                }
            }

            updateRectangleToFitToView (rectangle);

            editor.setBounds (rectangle);
            editor.repaint ();
        }

    }
    
    /** The expand button image. */
    private static final Image IMAGE_EXPAND = new BufferedImage(12, 12,
            BufferedImage.TYPE_INT_ARGB);
    
    static {

        // Create the expand image.
        Graphics2D g2 = ((BufferedImage) IMAGE_EXPAND).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        float w = IMAGE_EXPAND.getWidth(null);
        float h = IMAGE_EXPAND.getHeight(null);
        float r = Math.min(w, h) * 0.5f * 0.75f;
        GeneralPath gp = new GeneralPath();
        float dx = (float) (r * Math.cos(Math.toRadians(-30)));
        float dy = (float) (r * Math.sin(Math.toRadians(-30)));
        gp.moveTo(dx, dy);
        gp.lineTo(0, r);
        gp.lineTo(-dx, dy);
        gp.lineTo(dx, dy);
        gp.closePath();
        g2.translate(w / 2, h / 2);
        g2.setPaint(Color.BLACK);
        g2.fill(gp);
       
    }
    
    
    
    public class PortTypeLayout implements Layout {

        private Layout osjLayout = null;
        private Layout fLayout = LayoutFactory.createOverlayLayout();
        
        public PortTypeLayout() {
            osjLayout = new OneSideJustifiedLayout(true);
        }
        
        public void justify(Widget widget) {
            List<Widget> children = widget.getChildren();
            
            int size = children.size();
            for (Widget child : children) {
                if (!child.isVisible()) {
                    size --;
                }
            }
            
            if (size == 1) {
                fLayout.justify(widget);
            } else if (size == 2) {
                osjLayout.justify(widget);
            }

        }

        public void layout(Widget widget) {
            List<Widget> children = widget.getChildren();
            int size = children.size();
            for (Widget child : children) {
                if (!child.isVisible()) {
                    size --;
                }
            }
            
            if (size == 1) {
                fLayout.layout(widget);
            } else if (size == 2) {
                osjLayout.layout(widget);
            }
        }

        public boolean requiresJustification(Widget widget) {
            // TODO Auto-generated method stub
            return true;
        }

    }
}
