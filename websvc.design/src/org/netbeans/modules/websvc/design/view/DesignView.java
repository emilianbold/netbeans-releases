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

package org.netbeans.modules.websvc.design.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.view.widget.OperationsWidget;
import org.openide.filesystems.FileObject;

/**
 * WebService Designer
 *
 * @author Ajit Bhate
 */
public class DesignView extends JPanel  {
    /** Manages the state of the widgets and corresponding objects. */
    private transient Scene scene;
    /** Manages the zoom level. */
    private ZoomManager zoomer;
    private transient Widget mMainLayer;
    private transient Widget contentWidget;
    private transient OperationsWidget operationsWidget;
    /**
     * Creates a new instance of GraphView.
     */
    public DesignView(Service service, FileObject implementationClass) {
        super(new BorderLayout());

        scene = new Scene();
        zoomer = new ZoomManager(scene);
        // add actions
        scene.getActions().addAction(ActionFactory.createZoomAction ());
        scene.getActions().addAction(ActionFactory.createPanAction ());

        mMainLayer = new LayerWidget(scene);
        mMainLayer.setPreferredLocation(new Point(0, 0));
        mMainLayer.setBackground(Color.WHITE);
        scene.addChild(mMainLayer);
        contentWidget = new Widget(scene);
        contentWidget.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 16));
        mMainLayer.addChild(contentWidget);
        //add operations widget
        operationsWidget = new OperationsWidget(scene,service,implementationClass);
        contentWidget.addChild(operationsWidget);

        JComponent sceneView = scene.createView();
        JScrollPane panel = new JScrollPane(sceneView);
        panel.getVerticalScrollBar().setUnitIncrement(16);
        panel.getHorizontalScrollBar().setUnitIncrement(16);
        panel.setBorder(null);
        add(panel, BorderLayout.CENTER);
    }

    /**
     * Adds the graph actions to the given toolbar.
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        toolbar.addSeparator();
        zoomer.addToolbarActions(toolbar);
        toolbar.addSeparator();
        operationsWidget.addToolbarActions(toolbar);
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

}
