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
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.CollaborationsWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.MessagesWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.PartnerScene;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetConstants;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
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
    
    private JToggleButton showHidePLTToggle;
    private JToggleButton showHideMessageToggle;

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

        collaborationsWidget = scene.getCollaborationsWidget();
        messagesWidget = scene.getMessagesWidget();
        // Note that the arrangement of collaborationsWidget and
        // messagesWidget is also controlled by the View actions below.
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        contentWidget.addChild(collaborationsWidget);
        
        middleWidget = new Widget(scene);
        middleWidget.setMinimumSize(new Dimension(WidgetConstants.HEADER_MINIMUM_WIDTH, 5));
        middleWidget.setBackground(Color.LIGHT_GRAY);
        middleWidget.setOpaque(true);
        contentWidget.addChild(middleWidget);
        contentWidget.addChild(messagesWidget);
        
        contentWidget.addDependency(new Widget.Dependency() {
        
            public void revalidateDependency() {
                if (middleWidget == null) return;
                Dimension d = middleWidget.getMinimumSize();
                if (!isCollaborationsShowing() || !isMessagesShowing()) {
                    d.height = 0;
                } else {
                    d.height = 5;
                }
                middleWidget.setMinimumSize(d);
                
                if (showHidePLTToggle != null) showHidePLTToggle.setSelected(isCollaborationsShowing());
                if (showHideMessageToggle != null) showHideMessageToggle.setSelected(isMessagesShowing());
            }
        
        });
        
        scene.addChild(contentWidget);
        scene.addSceneListener (new Scene.SceneListener() {
            public void sceneRepaint () {
                //do nothing
            }
            public void sceneValidating () {
                //do nothing
            }
            public void sceneValidated () {
                int width = panel.getViewport().getWidth();
                Rectangle bounds = contentWidget.getBounds();
                if (width <= scene.getBounds().width && bounds != null && bounds.width != width) {
                    contentWidget.setMinimumSize(new Dimension(width, 0));
                    scene.validate();
                }
            }
        });
        mDragLayer = scene.getDragOverLayer();

        scene.addChild(mDragLayer);
        scene.validate();

        panel = new JScrollPane(sceneView);
        panel.getVerticalScrollBar().setUnitIncrement(16);
        panel.getHorizontalScrollBar().setUnitIncrement(16);
        panel.setBorder(null);
        add(panel, BorderLayout.CENTER);
        
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
        
            @Override
            public void ancestorResized(HierarchyEvent e) {
                super.ancestorResized(e);
                if (e.getID() == HierarchyEvent.ANCESTOR_RESIZED) {
                    int width = panel.getViewport().getWidth();
                    if (width <= scene.getBounds().width) {
                        contentWidget.setMinimumSize(new Dimension(width, 0));
                        scene.validate();
                    }
                }
            }
        });

        // vlv: print
        getContent().putClientProperty("print.printable", Boolean.TRUE); // NOI18N
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
        
        showHidePLTToggle = new JToggleButton(new ViewCollaborationsAction());
        showHidePLTToggle.setSelected(isCollaborationsShowing());
        // Action has a name for accessibility purposes, but we do
        // not want that to appear in the button label.
        showHidePLTToggle.setText(null);
        showHidePLTToggle.setRolloverEnabled(true);
        if (border != null) {
            showHidePLTToggle.setBorder(border);
        }
        toolbar.add(showHidePLTToggle);
 
        showHideMessageToggle = new JToggleButton(new ViewMessagesAction());
        showHideMessageToggle.setSelected(isMessagesShowing());
        // Action has a name for accessibility purposes, but we do
        // not want that to appear in the button label.
        showHideMessageToggle.setText(null);
        showHideMessageToggle.setRolloverEnabled(true);

        if (border != null) {
            showHideMessageToggle.setBorder(border);
        }
        toolbar.add(showHideMessageToggle);
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
        //middleWidget.setVisible(visible && messagesWidget.isVisible());
        scene.validate();
    }
    


    /**
     * Change the visibility of the messages container widget.
     *
     * @param  visible  true to make visible, false to hide.
     */
    public void setMessagesVisible(boolean visible) {
        //middleWidget.setVisible(visible && collaborationsWidget.isVisible());
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
            Image img = ImageUtilities.loadImage(path);
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
            Image img = ImageUtilities.loadImage(path);
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
