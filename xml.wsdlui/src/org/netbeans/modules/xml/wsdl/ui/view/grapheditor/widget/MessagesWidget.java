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
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
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
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.FolderChildFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.MessageFolderNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Container for the message widgets.
 */
public class MessagesWidget extends Widget implements
        ActionListener, DnDHandler, PopupMenuProvider {

    private final WSDLModel model;
    private ImageLabelWidget headerLabel;
    private Widget headerWidget;
    private ButtonWidget addMessageButton;
    private Widget buttons;

    private final Widget contentWidget;
    
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
                "LBL_MessagesWidget_AddMessage"), true); // NOI18N
        addMessageButton.setActionListener(this);
        
        buttons = new Widget(scene);
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        buttons.addChild(addMessageButton);
        
        headerWidget = new Widget(scene);
        headerWidget.setMinimumSize(new Dimension(
                WidgetConstants.HEADER_MINIMUM_WIDTH, 0));
        headerWidget.setLayout(WidgetConstants.HEADER_LAYOUT);
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
                Widget widget = factory.getOrCreateWidget(scene, message, contentWidget);
                contentWidget.addChild(widget);
            }
        }
        
    }
    
    
    private void update() {
        stubWidget.removeFromParent();
        
        Collection<Message> messages = model.getDefinitions().getMessages();
        headerLabel.setComment("(" + messages.size() + ")");
        if (messages.isEmpty()) {
            contentWidget.addChild(stubWidget);
        }
    }
    
    void updateContent(PropertyChangeEvent evt) {
        //called only if source is definitions.
        if (evt.getPropertyName().equals(Definitions.MESSAGE_PROPERTY)) {
            update();
            Object obj = evt.getNewValue();
            if (obj != null && obj instanceof Message) {
                if (evt.getOldValue() == null) {//New message added
                    Message msg = (Message) obj;
                    Widget widget = WidgetFactory.getInstance().getOrCreateWidget(getScene(), msg, contentWidget);
                    Collection<Message> messages = model.getDefinitions().getMessages();
                    int i = 0;
                    for (Message message : messages) {
                        if (message == msg) {
                            break;
                        }
                        i++;
                    }
                    if (i > contentWidget.getChildren().size()) {
                        contentWidget.addChild(widget);
                    } else {
                        contentWidget.addChild(i, widget);
                    }
                }
            } else {
                obj = evt.getOldValue();
                if (obj != null && obj instanceof Message) {
                    WidgetHelper.removeObjectFromScene(getScene(), obj);
                }
            }
            getScene().validate();
        }
    }
    
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == addMessageButton) {
            Message message = null;
            try {
                if (model.startTransaction()) {
                    message = model.getFactory().createMessage();
                    NameGenerator nGen = NameGenerator.getInstance();
                    message.setName(nGen.generateUniqueMessageName(model));

                    Part newPart = model.getFactory().createPart();
                    newPart.setName(nGen.generateUniqueMessagePartName(message));
                    //to be consistent with add message from pop-up menu. do not set the type.
                    //newPart.setType(MessagesUtils.getDefaultTypeReference(model));

                    message.addPart(newPart);

                    model.getDefinitions().addMessage(message);
                }
            } finally {
                model.endTransaction();
            }
            if (message != null) {
                ActionHelper.selectNode(message);
                WidgetEditCookie ec = WidgetHelper.getWidgetLookup(message, getScene()).lookup(WidgetEditCookie.class);
                if (ec != null) ec.edit();
            }
        }
    }
    
    public void dragExit() {
        hideHitPoint();
        getScene().validate();
    }
    
    
    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        Transferable t = event.getTransferable();
        if (t != null) {
            Node node = Utility.getPaletteNode(t);
            if (node != null && "Message".equals(node.getName())) {  // NOI18N
                showHitPoint(scenePoint);
                getScene().validate();
                return true;
            }
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
        getScene().validate();
        if (index >= 0) {
        	Message newMessage = null;
            try {
                if (model.startTransaction()) {
                    Message[] messages = model.getDefinitions().getMessages()
                            .toArray(new Message[0]);
                    NameGenerator nGen = NameGenerator.getInstance();
                    newMessage = model.getFactory().createMessage();
                    newMessage.setName(nGen.generateUniqueMessageName(model));
                    Part newPart = model.getFactory().createPart();
                    newPart.setName(nGen.generateUniqueMessagePartName(newMessage));
                    //dont set default type IZ 95970010
                    //newPart.setType(MessagesUtils.getDefaultTypeReference(model)); 
                    newMessage.addPart(newPart);

                    if (index == messages.length) {
                        model.getDefinitions().addMessage(newMessage);
                    } else {
                        Utility.insertIntoDefinitionsAtIndex(index, model, newMessage, Definitions.MESSAGE_PROPERTY);
                    }
                }
            } finally {
                model.endTransaction();
            }
            ActionHelper.selectNode(newMessage);
            WidgetEditCookie ec = WidgetHelper.getWidgetLookup(newMessage, getScene()).lookup(WidgetEditCookie.class);
            if (ec != null) ec.edit();
            return true; 
        }
        
        return false;
    }
    
    public boolean isCollapsed() {
        return !isVisible();
    }
    
    public void expandForDragAndDrop() {
        setVisible(true);
    }

    
    private void hideHitPoint() {
        messageHitPoint.removeFromParent();
        
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
        
        messageHitPoint.removeFromParent();
        stubWidget.removeFromParent();
        
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
            componentNode = new MessageFolderNode(model.getDefinitions(), new FolderChildFactory(model.getDefinitions(), Message.class)); 
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
    
    
    private static final Image IMAGE  = ImageUtilities.loadImage
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
