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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;

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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.CollaborationsWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.MessagesWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.PartnerScene;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetConstants;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Contains the scrollable view that displays the widgets which represent
 * the partner link types view (e.g. collaborations and messages).
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class GraphView extends JPanel {
    

    /** Manages the state of the widgets and corresponding objects. */
    private PartnerScene scene;
    /** The component model. */
    private WSDLModel mModel;
    /** Layer for drag and drop actions. */
    private DragOverSceneLayer mDragLayer;
    /** Manages the zoom level. */
    private ZoomManager zoomer;
    /** The widget showing the collaborations. */
    private CollaborationsWidget collaborationsWidget;
    /** The widget showing the messages. */
    private MessagesWidget messagesWidget;
    /** That which contains the collaborations and messages widgets. */
    private Widget contentWidget;
    
    private Widget middleWidget;
    
    private JScrollPane panel;
    

    /**
     * Creates a new instance of GraphView.
     */
    public GraphView(WSDLModel model) {
        super(new BorderLayout());
        this.mModel = model;

        scene = new PartnerScene(mModel);
        scene.setBackground(Color.WHITE);
        zoomer = new ZoomManager(scene);

        JComponent sceneView = scene.createView();
        // dirty hack to fix issue 93508
        if (sceneView instanceof MouseWheelListener) {
            sceneView.removeMouseWheelListener((MouseWheelListener) sceneView);
        }        
        
        collaborationsWidget = scene.getCollaborationsWidget();
        messagesWidget = scene.getMessagesWidget();
        // Note that the arrangement of collaborationsWidget and
        // messagesWidget is also controlled by the View actions below.
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.JUSTIFY, 0));
        contentWidget.addChild(collaborationsWidget);
        
        middleWidget = new Widget(scene);
        middleWidget.setMinimumSize(new Dimension(WidgetConstants.HEADER_MINIMUM_WIDTH, 5));
        middleWidget.setBackground(Color.LIGHT_GRAY);
        middleWidget.setOpaque(true);
        contentWidget.addChild(middleWidget);
        contentWidget.addChild(messagesWidget);
        
        scene.addChild(contentWidget);
        scene.addSceneListener (new Scene.SceneListener() {
            public void sceneRepaint () {
            }
            public void sceneValidating () {

            }
            public void sceneValidated () {
                int width = panel.getViewport().getWidth();
                if (width <= scene.getBounds().width) {
                    contentWidget.setMinimumSize(new Dimension(width, 0));
                }
            }
        });
        mDragLayer = scene.getDragOverLayer();

        scene.addChild(mDragLayer);
        sceneView.setFocusCycleRoot(true);
        panel = new JScrollPane(sceneView);
        panel.getVerticalScrollBar().setUnitIncrement(16);
        panel.getHorizontalScrollBar().setUnitIncrement(16);
        panel.setBorder(null);
        add(panel, BorderLayout.CENTER);

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
        boolean[] visible = new boolean[] {
            isCollaborationsShowing(),
            isMessagesShowing(),
        };
        for (int ii = 0; ii < actions.length; ii++) {
            Action action = actions[ii];
            JToggleButton button = new JToggleButton(action);
            // Action has a name for accessibility purposes, but we do
            // not want that to appear in the button label.
            button.setText(null);
            button.setRolloverEnabled(true);
            if (border != null) {
                button.setBorder(border);
            }
            button.setSelected(visible[ii]);
            toolbar.add(button);
        }
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
     * Indicates if the collaborations container widget is visible or not.
     *
     * @return  true if collaborations is showing, false otherwise.
     */
    public boolean isCollaborationsShowing() {
        return collaborationsWidget.isVisible();
    }

    /**
     * Indicates if the messages container widget is visible or not.
     *
     * @return  true if messages is showing, false otherwise.
     */
    public boolean isMessagesShowing() {
        return messagesWidget.isVisible();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        // Ensure the graph widgets have the focus.
        scene.getView().requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        // Ensure the graph widgets have the focus.
        return scene.getView().requestFocusInWindow();
    }

    /**
     * Change the visibility of the collaborations container widget.
     *
     * @param  visible  true to make visible, false to hide.
     */
    public void setCollaborationsVisible(boolean visible) {
        collaborationsWidget.setVisible(visible);
        middleWidget.setVisible(visible && messagesWidget.isVisible());
        scene.validate();
    }
    


    /**
     * Change the visibility of the messages container widget.
     *
     * @param  visible  true to make visible, false to hide.
     */
    public void setMessagesVisible(boolean visible) {
        middleWidget.setVisible(visible && collaborationsWidget.isVisible());
        messagesWidget.setVisible(visible);
        scene.validate();
    }

    /**
     * Attempt to show the corresponding widget for the given component.
     *
     * @param  comp  model component to show.
     */
    public void showComponent(Component comp) {
    	// Make the widget visible and select it (our select provider
    	// will make the widget visible within the scroll pane).
    	// Reset whatever the current selection is first.
    	scene.userSelectionSuggested(Collections.singleton(comp), false);
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
            setCollaborationsVisible(!isCollaborationsShowing());
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
            setMessagesVisible(!isMessagesShowing());
        }
    }
}
