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
 * GraphView.java
 *
 * Created on August 15, 2006, 11:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.border.EmptyBorder;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.CollaborationsWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.ExScene;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.MessagesWidget;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Contains the scrollable view that displays the widgets which represent
 * the partner link types view (e.g. collaborations and messages).
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class GraphView extends JPanel  {
    /** Manages the state of the widgets and corresponding objects. */
    private ExScene scene;
    
    private Widget mMainLayer;

    private WSDLModel mModel;

    private DragOverSceneLayer mDragLayer;
    /** Manages the zoom level. */
    private ZoomManager zoomer;
    /** The widget showing the collaborations. */
    private CollaborationsWidget collaborationsWidget;
    /** The widget showing the messages. */
    private MessagesWidget messagesWidget;
    /** That which contains the collaborations and messages widgets. */
    private Widget contentWidget;

    /** Creates a new instance of GraphView */
    public GraphView(WSDLModel model) {
        super(new BorderLayout());
        this.mModel = model;

        scene = new ExScene(mModel);
        scene.createView();
        scene.setBackground(Color.WHITE);
        zoomer = new ZoomManager(scene);
        
        //scene.setLayout(LayoutFactory.createVerticalLayout(SerialAlignment.CENTER, 0));
        /*scene.getActions().addAction (ActionFactory.createZoomAction ());
        scene.getActions().addAction (ActionFactory.createPanAction ());*/

        
        //mMainLayer.setOpaque(true);
        

//        mMainLayer.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        collaborationsWidget = scene.getCollaborationsWidget(); // new CollaborationsWidget(scene, mModel);
        messagesWidget = scene.getMessagesWidget();
        // Note that the arrangement of collaborationsWidget and
        // messagesWidget is also controlled by the View actions below.
        contentWidget = new Widget(scene);
        contentWidget.setBorder(new EmptyBorder(24, 24, 24, 24, false));
        contentWidget.setLayout(LayoutFactory.createVerticalLayout(SerialAlignment.JUSTIFY, 16));
        contentWidget.addChild(collaborationsWidget);
        contentWidget.addChild(messagesWidget);

        mMainLayer = new LayerWidget(scene);
        mMainLayer.addChild(contentWidget);
        mMainLayer.setPreferredLocation(new Point(0, 0));
        mMainLayer.setBackground(Color.WHITE);
//        mMainLayer.getActions().addAction(new ButtonAction());
        
        mDragLayer = scene.getDragOverLayer();
        
        scene.addChild(mMainLayer);
        scene.addChild(mDragLayer);

        JComponent sceneView = scene.getView ();

        JScrollPane panel = new JScrollPane ();
        panel.setBounds(this.getBounds());
// XXX: This is not sufficient to make scrolling work, please see the javadoc
//      for JComponent.setAutoscrolls() on what else is needed.
        panel.setWheelScrollingEnabled(true);
        sceneView.setAutoscrolls(true);
        panel.setViewportView(sceneView);
        this.add(panel, BorderLayout.CENTER);
    }

    /**
     * Return the view content, suitable for printing (i.e. without a
     * scroll pane, which would result in the scroll bars being printed).
     *
     * @return  the view content, sans scroll pane.
     */
    public JComponent getContent() {
        return scene.getView();
    }

    /**
     * Return the ZoomManager for this GraphView instance.
     *
     * @return  the zoom manager.
     */
    public ZoomManager getZoomManager() {
        return zoomer;
    }

    /**
     * Adds the graph actions to the given toolbar (no separators are
     * added to either the beginning or end).
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        zoomer.addToolbarActions(toolbar);
        toolbar.addSeparator();
        Border border = UIManager.getBorder("nb.tabbutton.border"); //NOI18N
        Action[] actions = new Action[] {
            new ViewCollaborationsAction(),
            new ViewMessagesAction(),
        };
        for (Action action : actions) {
            JToggleButton button = new JToggleButton(action);
            // Action has a name for accessibility purposes, but we do
            // not want that to appear in the button label.
            button.setText(null);
            button.setRolloverEnabled(true);
            if (border != null) {
                button.setBorder(border);
            }
            // Everything is initially visible.
            button.setSelected(true);
            toolbar.add(button);
        }
    }

    public void requestFocus() {
        super.requestFocus();
        // Ensure the graph widgets have the focus.
        scene.getView().requestFocus();
    }

    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        // Ensure the graph widgets have the focus.
        return scene.getView().requestFocusInWindow();
    }

    /**
     * Toggles the visibility of the collaborations widget.
     */
    private class ViewCollaborationsAction extends AbstractAction
            implements Runnable {

        /**
         * Creates a new instance of ViewCollaborationsAction.
         */
        public ViewCollaborationsAction() {
            String path = NbBundle.getMessage(ViewCollaborationsAction.class,
                    "IMG_ViewCollaborationsAction");
            Image img = Utilities.loadImage(path);
            if (img != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(img));
            }
            String name = NbBundle.getMessage(ViewCollaborationsAction.class,
                    "ACSN_ViewCollaborationsAction");
            putValue(Action.NAME, name); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(
                    ViewCollaborationsAction.class, "LBL_ViewCollaborationsAction"));
        }

        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(this);
        }

        public void run() {
            if (collaborationsWidget.getParentWidget() == null) {
                // Ensure that collaborations appears before messages.
                List<Widget> children = contentWidget.getChildren();
                int index = children.indexOf(messagesWidget);
                if (index < 0) {
                    index = 0;
                }
                contentWidget.addChild(index, collaborationsWidget);
            } else {
                contentWidget.removeChild(collaborationsWidget);
            }
            scene.validate();
        }
    }

    /**
     * Toggles the visibility of the messages widget.
     */
    private class ViewMessagesAction extends AbstractAction
            implements Runnable {

        /**
         * Creates a new instance of ViewMessagesAction.
         */
        public ViewMessagesAction() {
            String path = NbBundle.getMessage(ViewMessagesAction.class,
                    "IMG_ViewMessagesAction");
            Image img = Utilities.loadImage(path);
            if (img != null) {
                putValue(Action.SMALL_ICON, new ImageIcon(img));
            }
            String name = NbBundle.getMessage(ViewCollaborationsAction.class,
                    "ACSN_ViewMessagesAction");
            putValue(Action.NAME, name); // for accessibility
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(
                    ViewMessagesAction.class, "LBL_ViewMessagesAction"));
        }

        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(this);
        }

        public void run() {
            if (messagesWidget.getParentWidget() == null) {
                // Messages should come after collaborations.
                contentWidget.addChild(messagesWidget);
            } else {
                contentWidget.removeChild(messagesWidget);
            }
            scene.validate();
        }
    }
}
