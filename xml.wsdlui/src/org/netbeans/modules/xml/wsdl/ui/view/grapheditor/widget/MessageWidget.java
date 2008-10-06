/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * MessageWidget.java
 *
 * Created on August 16, 2006, 11:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.refactoring.CannotRefactorException;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.DragDropDecorator;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ExtendedDragDropAction;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public class MessageWidget extends AbstractWidget<Message>
        implements ExpandableWidget, ActionListener, DnDHandler {
    private static final boolean EXPANDED_DEFAULT = true;
    private ButtonWidget addPartButton;
    private ButtonWidget removePartButton;
    
    private Widget contentWidget;
    private Widget header;
    private final Widget tableWidget;
    private Widget buttons;
    
    private PartHitPointWidget partHitPointWidget; 
    private PartHitPointPosition partHitPointPosition;

    private Object draggedObject;
    
    private ExpanderWidget expanderWidget;
	private WidgetAction editorAction;
	private ImageLabelWidget labelWidget;
    
    
    public MessageWidget(Scene scene, Message message, Lookup lookup) {
        super(scene, message, lookup);
        setMinimumSize(new Dimension(WidgetConstants.MESSAGE_MINIMUM_WIDTH, 0));
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {
            
            public boolean isEnabled(Widget widget) {
                Message message = getMessage(widget);
                if (message != null) {
                    return XAMUtils.isWritable(message.getModel());
                }
                return false;
            }

            
            public String getText(Widget widget) {
                Message message = getMessage(widget);
                String name = (message != null) ? message.getName() : null;
                return (name != null) ? name : ""; // NOI18N
            }

            
            public void setText(Widget widget, String text) {
                Message message = getMessage(widget);
                if (message != null && !message.getName().equals(text)) {
                    // try rename silent and locally
                    SharedUtils.locallyRenameRefactor(message, text);
                 }
            }
            
            
            private Message getMessage(Widget widget) {
                MessageWidget messageWidget = getMessageWidget(widget);
                return (messageWidget != null) 
                        ? messageWidget.getWSDLComponent() 
                        : null;
            }
            
            
            private MessageWidget getMessageWidget(Widget widget) {
                for (Widget w = widget; w != null; w = w.getParentWidget()) {
                    if (w instanceof MessageWidget) {
                        return (MessageWidget) w;
                    }
                }
                return null;
            }
        
        }, null);
        
        boolean expanded = ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT);
        expanderWidget = new ExpanderWidget(scene, this, expanded);
        
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout());

        addPartButton = new ButtonWidget(scene, NbBundle.getMessage(
                MessageWidget.class, 
                "LBL_MessageWidget_AddPart"), true); // NOI18N
        addPartButton.setActionListener(this);
        removePartButton = new ButtonWidget(scene, NbBundle.getMessage(
                MessageWidget.class, 
                "LBL_MessageWidget_RemovePart")); // NOI18N
        removePartButton.setActionListener(this);
        removePartButton.setButtonEnabled(false);
        
        buttons = new Widget(scene);
        buttons.setBorder(BUTTONS_BORDER);
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                SerialAlignment.LEFT_TOP, 8));
        buttons.addChild(addPartButton);
        buttons.addChild(removePartButton);
        
        setBorder(WidgetConstants.OUTER_BORDER);
        setOpaque(true);
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
        header = createHeader(getScene());
        addChild(0, header);

        tableWidget = createEmptyTable(scene);
        refreshParts();
        contentWidget.addChild(tableWidget);
        
        contentWidget.addChild(buttons);
        updateButtonState();
        
        addChild(contentWidget);
        contentWidget.setVisible(expanded);
        
        getActions().addAction(((PartnerScene) scene).getDnDAction());
        getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER && (event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                    if (header != null) {
                        return header.getActions().keyPressed(widget, event);
                    }
                } else if (event.getKeyCode() == KeyEvent.VK_F2 || event.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (editorAction == null || labelWidget == null) return State.REJECTED;
                    InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                    if (inplaceEditorController.openEditor (labelWidget)) {
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
                inplaceEditorController.openEditor (labelWidget);
            }
            
            public void close() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.closeEditor(false);
            }
        
        });
        
    }

    void updateButtonState() {
        boolean enabled = false;
        if (tableWidget != null && tableWidget.getChildren() != null) {
            for (Widget w : tableWidget.getChildren()) {
                if (w instanceof PartWidget) {
                    enabled |= w.getState().isSelected();
                }
            }
        }
        if (enabled != removePartButton.isButtonEnabled()) {
            removePartButton.setButtonEnabled(enabled);
        }
    }
    
    
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == addPartButton) {
            Message message = getWSDLComponent();
            WSDLModel model = message.getModel();
            Part newPart = null;
            try {
                if (model.startTransaction()) {
                    newPart = model.getFactory().createPart();
                    newPart.setName(NameGenerator.getInstance().generateUniqueMessagePartName(message));
                    newPart.setType(MessagesUtils.getDefaultTypeReference(model));

                    message.addPart(newPart);
                }
            } finally {
                model.endTransaction();
            }
            if (newPart != null) {
            	ActionHelper.selectNode(newPart);
            	WidgetEditCookie ec = WidgetHelper.getWidgetLookup(newPart, getScene()).lookup(WidgetEditCookie.class);
                if (ec != null) ec.edit();
            }
        } else if (event.getSource() == removePartButton) {
            for (Widget w : tableWidget.getChildren()) {
                if (w instanceof PartWidget) {
                    PartWidget partWidget = (PartWidget) w;
                    if (partWidget.getState().isSelected()) {
                        Part part = partWidget.getWSDLComponent();
                        try {
                            SharedUtils.silentDeleteRefactor(part, true);
                        } catch (CannotRefactorException e) {
                            SharedUtils.showDeleteRefactoringUI(part);
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }

                        return;
                    }
                }
            }
        }
    }
    
    
    private PartHitPointPosition getPartHitPointPosition(Point scenePoint) {
        if (!hasParts()) {
            return new PartHitPointPosition(0, 0);
        }
        
        Widget[] children = tableWidget.getChildren().toArray(new Widget[0]);
        
        int partIndex = 0;
        boolean hitPointIsAbove = false;
        
        int partCount = getWSDLComponent().getParts().size();
        
        for (Widget widget : children) {
            if (widget instanceof PartWidget) {
                PartWidget partWidget = (PartWidget) widget;
                Point partPoint = partWidget.convertSceneToLocal(scenePoint);
                Rectangle partBounds = partWidget.getBounds();
                
                if (partWidget.isHitAt(partPoint) 
                        && (partBounds.getCenterX() < partPoint.x)) 
                {
                    int row = (hitPointIsAbove) ? (partIndex + 1) : partIndex;
                    
                    if (row < partCount) {
                        return new PartHitPointPosition(row, 1);
                    }
                }

                partIndex++;
            } else if (widget == partHitPointWidget) {
                Point point = partHitPointWidget.convertSceneToLocal(scenePoint);
                Rectangle bounds = partHitPointWidget.getBounds();

                if (partHitPointWidget.isHitAt(point) 
                        && (bounds.getCenterX() < point.x)
                        && (partIndex < partCount))
                {
                    return new PartHitPointPosition(partIndex, 1);
                }
                
                hitPointIsAbove = true;
            }
        }
        
        if (partHitPointWidget != null && partHitPointWidget.getParentWidget() != null) {
            if (partHitPointWidget.isHitAt(partHitPointWidget.convertSceneToLocal(
                    scenePoint))) 
            {
                return partHitPointPosition;
            }
        }
        
        partIndex = 0;
        
        for (Widget widget : children) {
            if (widget instanceof PartWidget) {
                PartWidget partWidget = (PartWidget) widget;
                Point partPoint = partWidget.convertSceneToLocal(scenePoint);
                
                if (partPoint.y < partWidget.getBounds().getCenterY()) {
                    return new PartHitPointPosition(partIndex, 0);
                }
                        
                partIndex++;
            }
        }
        
        return new PartHitPointPosition(partCount, 0);
    }
    
    
    private boolean hasParts() {
        Collection<Part> parts = getWSDLComponent().getParts();
        if (parts == null) return false;
        return !parts.isEmpty();
    }
    
    
    private String getPartCount() {
        Collection<Part> parts = getWSDLComponent().getParts(); 
        int count = (parts == null) ? 0 : parts.size();
        return (count == 1) ? "(1 part)" : ("(" + count + " parts)"); // NOI18N
    }

    private void showHitPoint(Point scenePoint, Object draggedObj) {
        this.draggedObject = draggedObj;
        if (partHitPointWidget == null) {
            partHitPointWidget = new PartHitPointWidget(getScene());
        }
        PartHitPointPosition newPosition = getPartHitPointPosition(scenePoint);
        PartHitPointPosition oldPosition = partHitPointPosition;
                

        tableWidget.setVisible(true);
        
        if (!newPosition.equals(oldPosition)) {
            partHitPointWidget.removeFromParent();
            if (newPosition.column == 0) {
                tableWidget.addChild(newPosition.row + 1, partHitPointWidget);
            }
        }
        
        partHitPointPosition = newPosition;
    }
    
    
    private void hideHitPoint() {
    	if (partHitPointWidget == null) return;
    	partHitPointWidget.removeFromParent();
        
        if (!hasParts()) {
            tableWidget.setVisible(false);
        }
        
        partHitPointPosition = null;
        draggedObject = null;
    }
    
    private String getName() {
        String name = getWSDLComponent().getName();

        if (name == null) {
            name = NbBundle.getMessage(MessageWidget.class, "LBL_Undefined"); // NOI18N
        } else if (name.trim().equals("")) { // NOI18N
            name = NbBundle.getMessage(MessageWidget.class, "LBL_Empty"); // NOI18N
        }
        return name;
    }

    private ImageLabelWidget createHeaderLabel(Scene scene) {
        String name = getName();
        ImageLabelWidget result = new ImageLabelWidget(scene, IMAGE, name, 
                getPartCount());
        result.getActions().addAction(editorAction);
        return result;
    }

    private Widget createHeader(Scene scene) {
        Widget result = new HeaderWidget(scene, expanderWidget);
        labelWidget = createHeaderLabel(scene);
        result.addChild(labelWidget);
        
        if (expanderWidget.getParentWidget() != null) {
            expanderWidget.getParentWidget().removeChild(expanderWidget);
        }
        
        result.addChild(expanderWidget);
        result.setLayout(WidgetConstants.HEADER_LAYOUT);
        result.setBorder(WidgetConstants.GRADIENT_BLUE_WHITE_BORDER);
        
        result.getActions().addAction(new ExtendedDragDropAction(
                new DragDropDecorator() {

                    public Widget createDragWidget(Scene scene) {
                        return new ImageLabelWidget(scene, IMAGE, getWSDLComponent().getName());
                    }

                }, 
                ((PartnerScene)getScene()).getDragOverLayer(), 
                new ConnectProvider() {

                    public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    public ConnectorState isTargetWidget(Widget sourceWidget,
                            Widget targetWidget) {
                        if (targetWidget instanceof OperationParameterWidget) {
                            return ConnectorState.ACCEPT;
                        }
                        return ConnectorState.REJECT;
                    }

                    public boolean isSourceWidget(Widget sourceWidget) {
                        if (sourceWidget == header) {
                            return true;
                        }
                        return false;
                    }

                    public boolean hasCustomTargetWidgetResolver(Scene scene) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    public void createConnection(Widget sourceWidget, Widget targetWidget) {
                        if (targetWidget instanceof OperationParameterWidget) {
                            Node node = ((OperationParameterWidget)targetWidget).getNode();
                            if (node != null) {
                                OperationParameter param = node.getLookup().lookup(OperationParameter.class);
                                if (param != null) {
                                    Message message = getWSDLComponent();
                                    WSDLModel model = message.getModel();
                                    try {
                                        model.startTransaction();
                                        param.setMessage(param.createReferenceTo(message, Message.class));
                                    } finally {
                                        if (model.isIntransaction()) {
                                            model.endTransaction();
                                        }
                                    }
                                    ActionHelper.selectNode(param);
                                }
                            }
                        }

                    }

                }, MouseEvent.CTRL_MASK)
        );
        return result;
    }

    private Widget createPartsTableHeaderCell(Scene scene, String text) {
        LabelWidget result = new LabelWidget(scene, text);
        result.setBorder(HEADER_CELL_BORDER);
        result.setFont(scene.getDefaultFont());
        result.setAlignment(LabelWidget.Alignment.CENTER);
        result.setVerticalAlignment(LabelWidget.VerticalAlignment.CENTER);
        return result;
    }    

    private Widget createPartsTableHeader(Scene scene) {
        Widget result = new Widget(scene);
        result.addChild(createPartsTableHeaderCell(scene, NbBundle.getMessage(
                MessageWidget.class,
                "LBL_MessageWidget_PartName"))); // NOI18N
        result.addChild(createPartsTableHeaderCell(scene, NbBundle.getMessage(
                MessageWidget.class,
                "LBL_MessageWidget_PartElementOrType"))); // NOI18N
        result.setLayout(PartWidget.ROW_LAYOUT);
        return result;
    }

    private void refreshParts() {
        List<Part> parts = getWSDLComponent().getChildren(Part.class);
        if (parts == null || parts.isEmpty()) {
            tableWidget.setVisible(false);
            updateButtonState();
            return;
        }
        if (!tableWidget.isVisible()) {
            tableWidget.setVisible(true);
        }
        WidgetFactory factory = WidgetFactory.getInstance();
        for (Part part : parts) {
            tableWidget.addChild(factory.getOrCreateWidget(getScene(), 
                    part, getLookup(), tableWidget));
        }
        
        updateButtonState();
    }

    private Widget createEmptyTable(Scene scene) {
        Widget result = new Widget(scene);
        result.addChild(createPartsTableHeader(scene));
        result.setBorder(TABLE_BORDER);
        result.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment
                .JUSTIFY, 0));
        return result;
    }
    
    
    public void collapseWidget(ExpanderWidget expander) {
        contentWidget.setVisible(false);
    }

    public void expandWidget(ExpanderWidget expander) {
        contentWidget.setVisible(true);
    }
    
    public void expandWidget() {
        expanderWidget.setExpanded(true);
    }
    
    public void collapseWidget() {
        expanderWidget.setExpanded(false);
    }

    public boolean isCollapsed() {
        return !contentWidget.isVisible();
    }
    
    

    @Override
    protected Shape createSelectionShape() {
        Rectangle rect = getBounds();
        return new Rectangle2D.Double(rect.x + 2, rect.y + 2, rect.width - 4, 
                rect.height - 4);
    }    
    
    void update() {
        labelWidget.setComment(getPartCount());
    }
    
    @Override
    public void childrenAdded() {
        update();
        refreshParts();
    }
    
    @Override
    public void updated() {
        labelWidget.setLabel(getName());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (evt.getSource() == getWSDLComponent() && evt.getPropertyName().equals(Message.PART_PROPERTY)) {
            if (evt.getOldValue() != null) {
                update();
                WidgetHelper.removeObjectFromScene(getScene(), evt.getOldValue());
                refreshParts();
            }
        }
    }
    
    
    public void dragExit() {
        hideHitPoint();
        getScene().validate();
    }


    public boolean dragOver(Point scenePoint, WidgetAction.WidgetDropTargetDragEvent event) {
        if (isCollapsed()) return false;

        Transferable t = event.getTransferable();
        if (t != null) {
            
            for (Node node : Utility.getNodes(t)) {
                SchemaComponent sc = MessagesUtils
                .extractSchemaComponent(node);

                if (sc != null) {
                    showHitPoint(scenePoint, sc);
                    getScene().validate();
                    return true;
                }
            }
        }
        
        return false;
    }

    public boolean drop(Point scenePoint, 
            WidgetAction.WidgetDropTargetDropEvent event) 
    {
        if (isCollapsed()) return false;
        
        SchemaComponent sc = (SchemaComponent) draggedObject;

        PartHitPointPosition position = partHitPointPosition;
        
        hideHitPoint();
        getScene().validate();
        if ((sc == null) || (position == null)) {
            return false;
        }
        
        Message message = getWSDLComponent();
        WSDLModel model = message.getModel();
        boolean newlyCreated = false;
        Part part = null;
        if (model.startTransaction()) {
            try {
                if (position.column == 0) {
                    part = model.getFactory().createPart();
                    part.setName(NameGenerator.getInstance().generateUniqueMessagePartName(message));
                    ((AbstractComponent<WSDLComponent>) message).insertAtIndex(Message.PART_PROPERTY, part, position.row);
                    newlyCreated = true;
                } else {
                    Part[] parts = message.getParts().toArray(new Part[0]);
                    part = parts[position.row];
                }
                if (part != null) {
                    if (sc instanceof GlobalType) {
                        part.setType(model.getDefinitions().createSchemaReference(
                                (GlobalType) sc, GlobalType.class));
                        part.setElement(null);
                    } else {
                        part.setElement(model.getDefinitions().createSchemaReference(
                                (GlobalElement) sc, GlobalElement.class));
                        part.setType(null);
                    }
                } else {
                    model.rollbackTransaction();
                }
            } finally {
                if (model.isIntransaction()) model.endTransaction();
            }
        }
        ActionHelper.selectNode(part);
        if (newlyCreated) {
            WidgetEditCookie ec = WidgetHelper.getWidgetLookup(part, getScene()).lookup(WidgetEditCookie.class);
            if (ec != null) ec.edit();
        }
        return true;
    }
    
    
    public void expandForDragAndDrop() {
        expanderWidget.setExpanded(true);
    }

    public Object hashKey() {
        Message comp = getWSDLComponent();
        if (comp != null) {
            QName qname = Utility.getQNameForWSDLComponent(comp, comp.getModel());
            if (qname != null) {
                return qname;
            }
        }
        return this;
    }

    private static final Border BUTTONS_BORDER = new FilledBorder(
            new Insets(2, 0, 0, 0), new Insets(4, 8, 4, 8),
            Color.LIGHT_GRAY, Color.WHITE);
    
    
    private static final Border TABLE_BORDER = new FilledBorder(
            new Insets(2, 0, 0, 0), new Insets(0, 0, 0, 0),
            Color.LIGHT_GRAY, new Color(0x999999));

    private static final Image IMAGE  = ImageUtilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/message.png"); // NOI18N
    
    public static final Border HEADER_CELL_BORDER = new FilledBorder(0, 0, 1, 8, null, 
            new Color(0xEEEEEE));

    @Override
    protected void paintChildren() {
        super.paintChildren();
        
        if (partHitPointPosition == null) return;
        if (partHitPointPosition.column != 1) return;
        
        int row = partHitPointPosition.row;
        
        PartWidget partWidget = (PartWidget) tableWidget.getChildren().get(row + 1);
        PartTypeChooserWidget typeChooserWidget = partWidget
                .getPartChooserWidget();
        ButtonWidget buttonWidget = typeChooserWidget
                .getPartTypeChooserButton();
        
        Rectangle partBounds = partWidget
                .convertLocalToScene(partWidget.getBounds());
        Rectangle typeChooserBounds = typeChooserWidget
                .convertLocalToScene(typeChooserWidget.getBounds());
        Rectangle buttonBounds = buttonWidget
                .convertLocalToScene(buttonWidget.getBounds());
        
        int x = convertSceneToLocal(typeChooserBounds.getLocation()).x;
        int y = convertSceneToLocal(partBounds.getLocation()).y + 1;
        
        int width = buttonBounds.x - typeChooserBounds.x;
        int height = partBounds.height - 1;
        
        
        Graphics2D g2 = getGraphics();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();
        Object oldStrokeControl = g2.getRenderingHint(RenderingHints
                .KEY_STROKE_CONTROL);
        
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        Shape s = new RoundRectangle2D.Float(x + 2, y + 2, width - 4, 
                height - 4, 6, 6);
        g2.setPaint(Color.WHITE);
        g2.fill(s);
        
        g2.setStroke(new BasicStroke(2));
        g2.setPaint(WidgetConstants.HIT_POINT_BORDER);
        g2.draw(s);
        
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                oldStrokeControl);
    }

    
    
    private static class PartHitPointWidget extends LabelWidget {
        public PartHitPointWidget(Scene scene) {
            super(scene, " ");
            setBorder(hitPointBorder);
            setFont(scene.getDefaultFont());
        }
    }
    
    private static HitPointBorder hitPointBorder = new HitPointBorder();
    
    private static class HitPointBorder implements Border {
        public Insets getInsets() {
            return new Insets(3, 2, 2, 2);
        }

        
        public void paint(Graphics2D g2, Rectangle rectangle) {
            Paint oldPaint = g2.getPaint();
            Stroke oldStroke = g2.getStroke();
            
            Object oldStrokeControl = g2.getRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL);
            
            g2.setPaint(Color.WHITE);
            g2.fill(rectangle);
            
            g2.setPaint(new Color(0x888888));
            g2.fillRect(rectangle.x, rectangle.y, rectangle.width, 1);
            
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.setPaint(WidgetConstants.HIT_POINT_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(rectangle.x + 2, rectangle.y + 3, 
                    rectangle.width - 4, rectangle.height - 5, 6, 6);
            
            g2.setPaint(oldPaint);
            g2.setStroke(oldStroke);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    oldStrokeControl);
        }
        

        public boolean isOpaque() {
            return true;
        }
    }
    
    private static class PartHitPointPosition {
        public final int row;
        public final int column;
        
        public PartHitPointPosition(int row, int column) {
            this.row = row;
            this.column = column;
        }
        
        public boolean equals(PartHitPointPosition position) {
            if (position == null) return false;
            return (position.row == row) && (position.column == column);
        }
    }
}
