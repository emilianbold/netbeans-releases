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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.MessageFolderNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Container for the message widgets.
 */
public class MessagesWidget extends Widget implements
        ActionListener, DnDHandler, PopupMenuProvider {

    private WSDLModel model;
    private Widget headerLabel;
    private Widget headerWidget;
    private ButtonWidget addMessageButton;
    private Widget buttons;

    private Widget contentWidget;
    
    private MessageHitPointWidget messageHitPoint;
    private int messageHitPointIndex = -1;
    
    private StubWidget stubWidget;
    /** The Node for the WSDLComponent, if it has been created. */
    private Node componentNode;
    
    public MessagesWidget(Scene scene, WSDLModel model) {
        super(scene);
        this.model = model;

        setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, WidgetConstants.GAP_BETWEEN_HEADER_AND_CONTENT));
        setOpaque(true);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        stubWidget = new StubWidget(scene, NbBundle.getMessage(
                MessagesWidget.class, 
                "LBL_MessagesWidget_ThereAreNoMessages"));
        stubWidget.setMinimumSize(new Dimension(WidgetConstants.MESSAGE_MINIMUM_WIDTH, 0));
        
        addMessageButton = new ButtonWidget(scene, NbBundle.getMessage(
                MessagesWidget.class,
                "LBL_MessagesWidget_AddMessage")); // NOI18N
        addMessageButton.setActionListener(this);
        
        buttons = new Widget(scene);
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        buttons.addChild(addMessageButton);
        PartnerScene pScene = (PartnerScene) getScene();
        String weight = "FA_AddMessagesButton";
        if (!pScene.getObjects().contains(weight)) {
        	pScene.addObject(weight, addMessageButton);
        }
        
        headerWidget = new Widget(scene);
        headerWidget.setMinimumSize(new Dimension(
                WidgetConstants.HEADER_MINIMUM_WIDTH, 0));
        headerWidget.setLayout(new LeftRightLayout(32));
        headerWidget.setBorder(WidgetConstants.HEADER_BORDER);
        addChild(headerWidget);
        headerWidget.addChild(buttons);
        
        messageHitPoint = new MessageHitPointWidget(scene);
        messageHitPoint.setMinimumSize(new Dimension(WidgetConstants.MESSAGE_MINIMUM_WIDTH, 25));
        
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                SerialAlignment.JUSTIFY, WidgetConstants.GAP_BETWEEN_CHILD_WIDGETS));
        addChild(contentWidget);

        getActions().addAction(((PartnerScene) scene).getDnDAction());
        getActions().addAction(ActionFactory.createPopupMenuAction(this));
        createContent();
    }

    private void createContent() {
        Collection<Message> messages = model.getDefinitions().getMessages();
        
        if (stubWidget.getParentWidget() != null) {
            stubWidget.getParentWidget().removeChild(stubWidget);
        }
        
        if (messages == null) {
            messages = new LinkedList<Message>();
        }
        
        Scene scene = getScene();
        headerLabel = new ImageLabelWidget(scene, IMAGE, NbBundle.getMessage(
                MessagesWidget.class, "LBL_MessagesWidget_Messages"),
                "(" + messages.size() + ")");
        headerWidget.addChild(0, headerLabel);
        
        WidgetFactory factory = WidgetFactory.getInstance();
        
        if (messages.isEmpty()) {
            contentWidget.addChild(stubWidget);
        } else {
            for (Message message : messages) {
                Widget widget = factory.createWidget(scene, message, true);
                contentWidget.addChild(widget);
            }
        }
        
    }
    
    
    public void updateContent() {
    	if (headerWidget.getChildren().contains(headerLabel)) {
    		headerWidget.removeChild(headerLabel);
    	}
        contentWidget.removeChildren();
        createContent();
    }
    
    
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == addMessageButton) {
            Message message = null;
            try {
                if (model.startTransaction()) {
                    message = model.getFactory().createMessage();
                    message.setName(MessagesUtils.createNewMessageName(model));

                    Part newPart = model.getFactory().createPart();
                    newPart.setName(MessagesUtils.createNewPartName(message));
                    //to be consistent with add message from pop-up menu. do not set the type.
                    //newPart.setType(MessagesUtils.getDefaultTypeReference(model));

                    message.addPart(newPart);

                    copyView(null, message);

                    model.getDefinitions().addMessage(message);
                }
            } finally {
                model.endTransaction();
            }
            if (message != null) {
                ActionHelper.selectNode(message);
            }
        }
    }
    
    public void dragExit() {
        hideHitPoint();
    }
    
    
    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        try {
            Transferable t = event.getTransferable();
            if (t != null) {
                for (DataFlavor flavor : t.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = (Node) t.getTransferData(flavor);
                        if ("Message".equals(node.getName())) {  // NOI18N
                            showHitPoint(scenePoint);
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
    

    private boolean hasMessages() {
        Collection<Message> messages = model.getDefinitions().getMessages();
        if (messages == null) return false;
        return !messages.isEmpty();
    }
    
    
    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
        int index = messageHitPointIndex;
        
        hideHitPoint();
        
        if (index >= 0) {
            try {
                if (model.startTransaction()) {
                    Message[] messages = model.getDefinitions().getMessages()
                            .toArray(new Message[0]);

                    Message newMessage = model.getFactory().createMessage();
                    newMessage.setName(MessagesUtils.createNewMessageName(model));
                    Part newPart = model.getFactory().createPart();
                    newPart.setName(MessagesUtils.createNewPartName(newMessage));
                    //dont set default type IZ 95970010
                    //newPart.setType(MessagesUtils.getDefaultTypeReference(model)); 
                    newMessage.addPart(newPart);

                    copyView(null, newMessage);
                    if (index == messages.length) {
                        model.getDefinitions().addMessage(newMessage);
                    } else {
                        Utility.insertIntoDefinitionsAtIndex(index, model, newMessage, Definitions.MESSAGE_PROPERTY);
                    }
                }
            } finally {
                model.endTransaction();
            }
            
            return true; 
        }
        
        return false;
    }
    
    
    private void copyView(Message oldMessage, Message newMessage) {
        MessageWidget newWidget = (MessageWidget) WidgetFactory.getInstance()
                .createWidget(getScene(), newMessage);
        
        MessageWidget oldWidget = null;
        
        if (oldMessage != null) {
            oldWidget = (MessageWidget) ((PartnerScene) getScene())
                    .findWidget(oldMessage);
        }
        
        if (oldWidget == null || !oldWidget.isCollapsed()) {
            newWidget.expandForDragAndDrop();
        }
        
//        if (oldWidget != null) {
//            newWidget.setState(oldWidget.getState());
//        }
    }
    
    
    public boolean isCollapsed() {
        return !contentWidget.isVisible();
    }
    
    public void expandForDragAndDrop() {
    }

    
    private void hideHitPoint() {
        if (messageHitPoint.getParentWidget() != null) {
            messageHitPoint.getParentWidget()
                    .removeChild(messageHitPoint);
        }
        
        if (!hasMessages() && stubWidget.getParentWidget() == null) {
            contentWidget.addChild(stubWidget);
        }
        
        messageHitPointIndex = -1;
    }
    
    
    private void showHitPoint(Point scenePoint) {
        if (contentWidget.getParentWidget() == null) return;
        
        int index = placeHolderIndex(scenePoint);
        
        if (index < 0) return;
        
        messageHitPointIndex = index;
        
        if (messageHitPoint.getParentWidget() != null) {
            messageHitPoint.getParentWidget().removeChild(messageHitPoint);
        }
        
        if (stubWidget.getParentWidget() != null) {
            stubWidget.getParentWidget().removeChild(stubWidget);
        }
        
        contentWidget.addChild(messageHitPointIndex, messageHitPoint);
    }
    
    
    private MessageWidget[] getMessageWidgets() {
        List<MessageWidget> result = new ArrayList<MessageWidget>();
        
        for (Widget widget : contentWidget.getChildren()) {
            if (widget instanceof MessageWidget) {
                result.add((MessageWidget) widget);
            }
        }
        
        return result.toArray(new MessageWidget[result.size()]);
    }
    
    
    private int placeHolderIndex(Point scenePoint) {
        MessageWidget[] messageWidgets = getMessageWidgets();
        
        if (messageWidgets == null) return -1;
        
        if (messageHitPoint.getParentWidget() != null) {
            if (messageHitPoint.isHitAt(messageHitPoint.convertSceneToLocal(scenePoint))) {
                return -1;
            }
        }
        
        for (int i = 0; i < messageWidgets.length; i++) {
            MessageWidget messageWidget = messageWidgets[i];
            Point partPoint = messageWidget.convertSceneToLocal(scenePoint);
            Rectangle partBounds = messageWidget.getBounds();
            
            if (partPoint.y < partBounds.getCenterY()) {
                return i;
            }
        }
        
        return messageWidgets.length;        
    }

    public Object hashKey() {
        return model.getDefinitions().getName();
    }

    /**
     * Locates the TopComponent parent of the view containing the Scene
     * that owns this widget, if possible.
     *
     * @return  the parent TopComponent, or null if not found.
     */
    protected TopComponent findTopComponent() {
        return (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, getScene().getView());
    }

    /**
     * Returns a Node for the WSDL component that this widget represents.
     * If this widget does not have an assigned WSDL component, then this
     * returns an AbstractNode with no interesting properties.
     */
    private synchronized Node getNode() {
        if (componentNode == null) {
            componentNode = new MessageFolderNode(model.getDefinitions()); 
            componentNode = new WidgetFilterNode(componentNode);
        }
        return componentNode;
    }

    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        Node node = getNode();
        if (node != null) {
            // Using Node.getContextMenu() appears to bypass our FilterNode,
            // so we must build out the context menu as follows.
            TopComponent tc = findTopComponent();
            Lookup lookup;
            if (tc != null) {
                // Activate the node just as any explorer view would do.
                tc.setActivatedNodes(new Node[] { node });
                // To get the explorer actions enabled, must have the
                // lookup from the parent TopComponent.
                lookup = tc.getLookup();
            } else {
                lookup = Lookup.EMPTY;
            }
            // Remove the actions that we do not want to support in this view.
            Action[] actions = node.getActions(true);
            return Utilities.actionsToPopup(actions, lookup);
        }
        return null;
    }

    public static final Border MAIN_BORDER = new FilledBorder(1, 1, 8, 8, 
            new Color(0x888888), Color.WHITE);
    
    
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/message.png"); // NOI18N   
    
    
    private static class MessageHitPointWidget extends LabelWidget {
        public MessageHitPointWidget(Scene scene) {
            super(scene, " ");
            setFont(scene.getDefaultFont());
            setBorder(new MessageHitPointBorder());
        }
    }
    
    
    private static class MessageHitPointBorder implements Border {
        public Insets getInsets() {
            return new Insets(8, 8, 8, 8);
        }

        
        public void paint(Graphics2D g2, Rectangle rectangle) {
            Paint oldPaint = g2.getPaint();
            Stroke oldStroke = g2.getStroke();
            
            Object oldStrokeControl = g2.getRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL);
            
            g2.setPaint(Color.WHITE);
            g2.fill(rectangle);
            
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.setPaint(WidgetConstants.HIT_POINT_BORDER); 
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(rectangle.x + 1, rectangle.y + 1, 
                    rectangle.width - 2, rectangle.height - 2, 6, 6);
            
            g2.setPaint(oldPaint);
            g2.setStroke(oldStroke);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    oldStrokeControl);
        }
        

        public boolean isOpaque() {
            return true;
        }
    }
}
