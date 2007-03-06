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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Contains the scrollable view that displays the widgets which represent
 * the partner link types view (e.g. collaborations and messages).
 *
 * @DesignViewdval
 * @author Nathan Fiedler
 */
public class DesignView extends JPanel  {
    /** Manages the state of the widgets and corresponding objects. */
    private Scene scene;
    /** Layer containing the visible widgets. */
    private Widget mMainLayer;
    /** That which contains the collaborations and messages widgets. */
    private Widget contentWidget;
    /**
     * Creates a new instance of GraphView.
     */
    public DesignView() {
        super(new BorderLayout());

        scene = new Scene();
        JComponent sceneView = scene.createView();
        scene.setBackground(new Color(255, 255, 191));
        contentWidget = new Widget(scene);
        contentWidget.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        contentWidget.setLayout(LayoutFactory.createVerticalLayout(
                SerialAlignment.JUSTIFY, 16));

        mMainLayer = new LayerWidget(scene);
        mMainLayer.addChild(contentWidget);
        mMainLayer.setPreferredLocation(new Point(0, 0));
        mMainLayer.setBackground(Color.WHITE);

        scene.addChild(mMainLayer);

        JScrollPane panel = new JScrollPane(sceneView);
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
