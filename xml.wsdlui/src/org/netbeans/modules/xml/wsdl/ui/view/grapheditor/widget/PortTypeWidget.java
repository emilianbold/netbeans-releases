/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
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
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.actions.schema.ExtensibilityElementCreatorVisitor;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditor;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditorProvider;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortTypeNode;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.DisplayObject;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class PortTypeWidget extends AbstractWidget<PortType> implements DnDHandler {
    private LabelWidget mNameWidget;
    private Role mRole;
    private PartnerLinkTypeContentWidget mPLTContentWidget;
    private ButtonWidget showComboBoxBtnWidget;
    private Widget nameHolderWidget;
    private Border enabledBorder = BorderFactory.createCompositeBorder(BorderFactory.createLineBorder(1, Color.BLACK), WidgetConstants.GRADIENT_BLUE_WHITE_BORDER);
    private Border disabledBorder = BorderFactory.createCompositeBorder(BorderFactory.createDashedBorder(Color.GRAY, 8, 4, true), WidgetConstants.GRADIENT_GRAY_WHITE_BORDER);
    private Border importedBorder = BorderFactory.createCompositeBorder(BorderFactory.createLineBorder(1, Color.BLACK), WidgetConstants.GRADIENT_GREEN_WHITE_BORDER);
    private WidgetAction editorAction;
    private Border defaultBorder = BorderFactory.createEmptyBorder();
    
    
    public enum State {

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
                    PortType pt = model.findComponentByName(text, PortType.class);
                    if (pt != null) {
                        NotifyDescriptor desc = new NotifyDescriptor.Message(NbBundle.getMessage(PortTypeWidget.class, "ERR_PORTTYPE_NAME_EXISTS", text), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        return;
                    }
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
                        ActionHelper.selectNode(portType);
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
        init();
        
        getActions().addAction(new WidgetAction.Adapter() {
            
            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F2 || event.getKeyCode() == KeyEvent.VK_ENTER) {
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
        
        getLookupContent().add(new WidgetEditCookie() {
            
            public void edit() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.openEditor (mNameWidget);
            }

            public void close() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                if (inplaceEditorController.isEditorVisible()) inplaceEditorController.closeEditor(false);
            }
        
        });
    }

    final void setRightSided(boolean rightSided) {
        DirectionCookie dc = getLookup().lookup(DirectionCookie.class);
        if (dc != null) {
            if (dc.isRightSided() ^ rightSided) {
                dc.setRightSided(rightSided);
            }
        } else {
            dc = new DirectionCookie(rightSided);
            getLookupContent().add(dc);
        }
    }
    
    private void init() {
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
        nameHolderWidget = new Widget(getScene());
        nameHolderWidget.setLayout(new PortTypeLayout());
        nameHolderWidget.setMinimumSize(new Dimension(WidgetConstants.PORTTYPE_MINIMUM_WIDTH, WidgetConstants.TEXT_LABEL_HEIGHT));
        
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
        mNameWidget.setBorder(WidgetConstants.EMPTY_2PX_BORDER);
        Font font = getScene().getFont().deriveFont(Font.BOLD);
        String tooltipText = null;
        Border border = enabledBorder;
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
        

        nameHolderWidget.addChild(mNameWidget, 1);
        nameHolderWidget.addChild(showComboBoxBtnWidget);
        
        addChild(nameHolderWidget);
        if (getWSDLComponent() != null) {
            getActions().addAction(((PartnerScene) getScene()).getDnDAction());
        }
        
        Widget w = new Widget(getScene()) {
        
            @Override
            protected void paintWidget() {
                Graphics2D gr = getGraphics ();
                
                Rectangle bounds = getBounds();

                int x = bounds.x;
                
                Stroke oldStroke = gr.getStroke();
                Color oldColor = gr.getColor();
                Font font = gr.getFont();
                
                BasicStroke dotted = new BasicStroke(1, BasicStroke.CAP_SQUARE, 
                             BasicStroke.JOIN_ROUND, 10.0f, new float[]{5,10,5,10}, 0);
                
                gr.setStroke(dotted);
                gr.setFont (getFont ());
                int midPointX = (x+bounds.width)/2;
                gr.drawLine(midPointX, 0, midPointX, bounds.height);
                
                gr.setStroke(oldStroke);
                gr.setColor(oldColor);
                gr.setFont(font);
            }
        
        };
        addChild(w, 1);
        
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
            getParentWidget().getParentWidget().revalidate();
            getScene().validate();
        }
    }
    
    private void removeComboBoxBtnWidget() {
        if (showComboBoxBtnWidget.isVisible()) {
            showComboBoxBtnWidget.setVisible(false);
            getScene().validate();
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
    
    @Override
    protected Shape createSelectionShape() {
        int startX = 0;
        int startY = 0;
        int width = getBounds().width;
        int height = getBounds().height - 4;
        int labelHeight = getLabelHeight();
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
    
    int getLabelHeight() {
        int labelHeight = WidgetConstants.TEXT_LABEL_HEIGHT;
        if (nameHolderWidget != null && nameHolderWidget.getBounds() != null) {
            labelHeight = nameHolderWidget.getBounds().height;
        }
        return labelHeight;
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
        Operation operation = null;
        if (model.startTransaction()) {
        	try {
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
        	} finally {
        		model.endTransaction();
        	}
        	ActionHelper.selectNode(operation);
            WidgetEditCookie ec = WidgetHelper.getWidgetLookup(operation, getScene()).lookup(WidgetEditCookie.class);
            if (ec != null) ec.edit();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	super.propertyChange(evt);
    	if (evt.getSource() == getWSDLComponent()) {
    	    if (evt.getPropertyName().equals(PortType.NAME_PROPERTY)) {
    	        mNameWidget.setLabel(getName());
    	        getScene().validate();
    	    }
    	}
    }
    
    // DnDHandler implementation BEGIN 
    
    public void dragExit() {
        clearHotSpot();
        setDefaultBorder();
        getScene().validate();
    }

    
    public boolean dragOver(Point scenePoint, WidgetAction.WidgetDropTargetDragEvent event) {
        Transferable t = event.getTransferable();
        
        if (t != null) {
            Node node = Utility.getPaletteNode(t);
            if (node == null) {
                Node[] nodes = Utility.getNodes(t);
                if (nodes.length == 1) {
                    node = nodes[0];
                }
            }
            if (node != null) {
                if (isAssignable(node)) {
                    if (!isImported()) {
                        showHotSpot();
                        getScene().validate();
                        return true;
                    }
                } else if (node instanceof PortTypeNode) {
                    setHitPointBorder();
                    return true;
                }
            }
        }
        
        dragExit();
        return false;
    }

    
    public boolean drop(Point scenePoint, WidgetAction.WidgetDropTargetDropEvent event) {
        Transferable t = event.getTransferable();
        if (t != null) {
            Node node = Utility.getPaletteNode(t);
            if (node == null) {
                Node[] nodes = Utility.getNodes(t);
                if (nodes.length == 1) {
                    node = nodes[0];
                }
            }
            if (node != null) {
                if (node instanceof PortTypeNode) {
                    setDefaultBorder();
                    setPortTypeToRole((PortTypeNode)node);
                    return true;
                }
                //else its for operation.
                clearHotSpot();
                addOperation(node.getName());
                return true;
            }
        }
        return false;
    }

    private void setPortTypeToRole(PortTypeNode node) {
        PortType pt = node.getWSDLComponent();
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
                    //nothing
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
                    //nothing
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
            osjLayout = LayoutFactory.createHorizontalFlowLayout();
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
            return true;
        }

    }

    public void showHotSpot() {
        DirectionCookie dc = getLookup().lookup(DirectionCookie.class);
        if (dc != null) {
            mPLTContentWidget.getOperationSceneLayer().showHotSpot(dc.isRightSided());
        }
    }

    public void clearHotSpot() {
        mPLTContentWidget.getOperationSceneLayer().clearHotSpot();
        
    }

    public void setDefaultBorder() {
        setBorder(defaultBorder);
    }

    public void setHitPointBorder() {
        setBorder(BorderFactory.createLineBorder(2, WidgetConstants.HIT_POINT_BORDER));
    }
}
