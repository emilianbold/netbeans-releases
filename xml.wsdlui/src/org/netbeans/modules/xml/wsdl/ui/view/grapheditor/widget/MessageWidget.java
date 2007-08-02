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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JTextField;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.TextFieldInplaceEditorProvider;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.Node;
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
    private Widget table;
    private Widget buttons;
    
    private PartHitPointWidget partHitPointWidget; 
    private PartHitPointPosition partHitPointPosition;

    private Object draggedObject;
    
    private ExpanderWidget expanderWidget;
    
    
    public MessageWidget(Scene scene, Message message, Lookup lookup) {
        super(scene, message, lookup);
        setMinimumSize(new Dimension(WidgetConstants.MESSAGE_MINIMUM_WIDTH, 0));
        
        boolean expanded = ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT);
        expanderWidget = new ExpanderWidget(scene, this, expanded);
        
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout());

        addPartButton = new ButtonWidget(scene, NbBundle.getMessage(
                MessageWidget.class, 
                "LBL_MessageWidget_AddPart")); // NOI18N
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
        
        createContent();
        addChild(contentWidget);
        contentWidget.setVisible(expanded);
        
        getActions().addAction(((PartnerScene) scene).getDnDAction());
    }
    
    private void removeContent() {
        if (table != null) {
            table.removeChildren();
        }
        
        contentWidget.removeChildren();
        
        removeChild(header);
    }
    
    
    private void createContent() {
        header = createHeader(getScene(), getWSDLComponent());
        addChild(0, header);

        table = createPartsTable(getScene(), getWSDLComponent());
        if (table != null) {
            contentWidget.addChild(table);
        }
        
        contentWidget.addChild(buttons);

        updateButtonState();
    }

    void updateButtonState() {
        boolean enabled = false;
        if (table != null && table.getChildren() != null) {
            for (Widget w : table.getChildren()) {
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
            
            try {
                if (model.startTransaction()) {
                    Part newPart = model.getFactory().createPart();
                    newPart.setName(MessagesUtils.createNewPartName(message));
                    newPart.setType(MessagesUtils.getDefaultTypeReference(model));

                    message.addPart(newPart);
                }
            } finally {
                model.endTransaction();
            }
        } else if (event.getSource() == removePartButton) {
            for (Widget w : table.getChildren()) {
                if (w instanceof PartWidget) {
                    PartWidget partWidget = (PartWidget) w;
                    if (partWidget.getState().isSelected()) {
                        Message message = getWSDLComponent();
                        Part part = partWidget.getWSDLComponent();
                        
                        WSDLModel model = message.getModel();
                        
                        try {
                            if (model.startTransaction()) {
                                message.removePart(part);
                            }
                        } finally {
                            model.endTransaction();
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
        
        Widget[] children = table.getChildren().toArray(new Widget[0]);
        
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
    
    
    private String getPartCount(Message message) {
        Collection<Part> parts = message.getParts(); 
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
                
        if (table == null) {
            table = createEmptyTable(getScene());
            contentWidget.addChild(0, table);
        }
        
        if (!newPosition.equals(oldPosition)) {
            if (newPosition.column == 0) {
                if (partHitPointWidget.getParentWidget() != null) {
                    table.removeChild(partHitPointWidget);
                }

                table.addChild(newPosition.row + 1, partHitPointWidget);
            } else {
                if (partHitPointWidget.getParentWidget() != null) {
                    table.removeChild(partHitPointWidget);
                }

                repaint();
            }
        }
        
        partHitPointPosition = newPosition;
    }
    
    
    private void hideHitPoint() {
        if (partHitPointWidget.getParentWidget() != null) {
            partHitPointWidget.getParentWidget().removeChild(partHitPointWidget);
        }
        
        if (!hasParts() && (table != null)) {
            if (table.getParentWidget() != null) {
                table.getParentWidget().removeChild(table);
            }
        }
        
        partHitPointPosition = null;
        draggedObject = null;
    }

    private Widget createHeaderLabel(Scene scene, Message message) {
        String name = message.getName();
        
        if (name == null) {
            name = NbBundle.getMessage(MessageWidget.class, "LBL_Undefined"); // NOI18N
        } else if (name.trim().equals("")) { // NOI18N
            name = NbBundle.getMessage(MessageWidget.class, "LBL_Empty"); // NOI18N
        }
        
        ImageLabelWidget result = new ImageLabelWidget(scene, IMAGE, name, 
                getPartCount(message));
        result.getActions().addAction(ActionFactory
                .createInplaceEditorAction((InplaceEditorProvider<JTextField>) 
                new MessageNameInplaceEditorProvider()));
        
        return result;
    }

    private Widget createHeader(Scene scene, Message message) {
        Widget result = new HeaderWidget(scene, expanderWidget);

        result.addChild(createHeaderLabel(scene, message));
        
        if (expanderWidget.getParentWidget() != null) {
            expanderWidget.getParentWidget().removeChild(expanderWidget);
        }
        
        result.addChild(expanderWidget);
        result.setLayout(new LeftRightLayout(32));
        result.setBorder(WidgetConstants.GRADIENT_BLUE_WHITE_BORDER);
        
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

    private Widget createPartsTable(Scene scene, Message message) {
        List<Part> parts = message.getChildren(Part.class);
        
        if (parts == null) return null;
        if (parts.isEmpty()) return null;
        
        Widget result = createEmptyTable(scene);
        
        for (Part part : parts) {
            result.addChild(WidgetFactory.getInstance().createWidget(scene, 
                    part, getLookup(), true));
        }
        
        return result;
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
    
    
    @Override
    public void updateContent() {
        removeContent();
        createContent();
    }

    
    public void dragExit() {
        hideHitPoint();
    }


    public boolean dragOver(Point scenePoint, WidgetAction.WidgetDropTargetDragEvent event) {
        if (isCollapsed()) return false;
        
        try {
            Transferable t = event.getTransferable();
            if (t != null) {
                for (DataFlavor flavor : t.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    if (Node.class.isAssignableFrom(repClass)) {
                        SchemaComponent sc = MessagesUtils
                                .extractSchemaComponent((Node) 
                                t.getTransferData(flavor));

                        if (sc != null) {
                            showHitPoint(scenePoint, sc);
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

    public boolean drop(Point scenePoint, 
            WidgetAction.WidgetDropTargetDropEvent event) 
    {
        if (isCollapsed()) return false;
        
        SchemaComponent sc = (SchemaComponent) draggedObject;

        PartHitPointPosition position = partHitPointPosition;
        
        hideHitPoint();
        
        if ((sc == null) || (position == null)) {
            return false;
        }
        
        Message message = getWSDLComponent();
        WSDLModel model = message.getModel();

        Part part = null;
        if (model.startTransaction()) {
        	try {
        		if (position.column == 0) {
        			part = model.getFactory().createPart();
        			part.setName(MessagesUtils.createNewPartName(message));
        			((AbstractComponent<WSDLComponent>) message).insertAtIndex(Message.PART_PROPERTY, part, position.row);
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

        return true;
    }
    
    
    public void expandForDragAndDrop() {
        expanderWidget.setExpanded(true);
    }

    public Object hashKey() {
        Message comp = getWSDLComponent();
        return comp != null ? comp.getName() : this;
    }

    private static final Border BUTTONS_BORDER = new FilledBorder(
            new Insets(2, 0, 0, 0), new Insets(4, 8, 4, 8),
            Color.LIGHT_GRAY, Color.WHITE);
    
    
    private static final Border TABLE_BORDER = new FilledBorder(
            new Insets(2, 0, 0, 0), new Insets(0, 0, 0, 0),
            Color.LIGHT_GRAY, new Color(0x999999));

    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/message.png"); // NOI18N
    
    public static final Border HEADER_CELL_BORDER = new FilledBorder(0, 0, 1, 8, null, 
            new Color(0xEEEEEE));

    @Override
    protected void paintChildren() {
        super.paintChildren();
        
        if (partHitPointPosition == null) return;
        if (partHitPointPosition.column != 1) return;
        
        int row = partHitPointPosition.row;
        
        PartWidget partWidget = (PartWidget) table.getChildren().get(row + 1);
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
    
    
    private static class MessageNameInplaceEditorProvider implements 
            InplaceEditorProvider<JTextField>, 
            TextFieldInplaceEditor 
    {
        private TextFieldInplaceEditorProvider editorProvider;
        
        public MessageNameInplaceEditorProvider() {
            this.editorProvider = new TextFieldInplaceEditorProvider(this, 
                    EnumSet.of(InplaceEditorProvider.ExpansionDirection.RIGHT));
        }
        
        
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
        
        
        public void notifyOpened(
                InplaceEditorProvider.EditorController controller, 
                Widget widget, JTextField component) 
        {
            editorProvider.notifyOpened(controller, widget, component);
            
            double k = widget.getScene().getZoomFactor();
            
            Dimension minimumSize = component.getMinimumSize();
            
            if (minimumSize == null) {
                minimumSize = new Dimension(64, 19);
            }
            
            minimumSize.width = (int) Math.ceil(widget.getBounds().getWidth() 
                    * k);
            
            component.setMinimumSize(minimumSize);
        }

        
        public void notifyClosing(
                InplaceEditorProvider.EditorController controller, 
                Widget widget, 
                JTextField component, 
                boolean commit) 
        {
            editorProvider.notifyClosing(controller, widget, component, commit);
        }

        
        public JTextField createEditorComponent(
                InplaceEditorProvider.EditorController controller, 
                Widget widget) 
        {
            return editorProvider.createEditorComponent(controller, widget);
        }
        

        public Rectangle getInitialEditorComponentBounds(
                InplaceEditorProvider.EditorController controller, 
                Widget widget, 
                JTextField component, 
                Rectangle bounds) 
        {
            double k = widget.getScene().getZoomFactor();
            
            Widget parent = widget.getParentWidget();
            
            Rectangle widgetBounds = widget.convertLocalToScene(widget.getBounds());
            Rectangle parentBounds = parent.convertLocalToScene(parent.getBounds());
            Insets parentInsets = parent.getBorder().getInsets();
            
            int x1 = widgetBounds.x + 24;
            int y1 = parentBounds.y + 24 + parentInsets.top;
            
            int x2 = x1 + widgetBounds.width;
            int y2 = y1 + parentBounds.height 
                    - parentInsets.top - parentInsets.bottom;
            
            int x = (int) Math.floor(k * x1);
            int y = (int) Math.floor(k * y1);
            
            int w = (int) Math.ceil(k * x2) - x;
            int h = (int) Math.ceil(k * y2) - y;
            
            Dimension preferredSize = component.getPreferredSize();
            
            if (preferredSize.height > h) {
                y -= (preferredSize.height - h) / 2;
                h = preferredSize.height;
            }
            
            return new Rectangle(x, y, w, h);
        }

        
        public EnumSet<InplaceEditorProvider.ExpansionDirection> 
                getExpansionDirections(
                        InplaceEditorProvider.EditorController controller, 
                        Widget widget, 
                        JTextField component) 
        {
            return editorProvider.getExpansionDirections(controller, widget,
                    component);
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
