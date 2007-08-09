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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.view.widget.OperationsWidget;
import org.openide.filesystems.FileObject;

/**
 * WebService Designer
 *
 * @author Ajit Bhate
 */
public class DesignView extends JPanel  {
    
    public static final Object messageLayerKey = new Object();
    
    private FileObject implementationClass;
    private Service service;
    private ServiceModel serviceModel;
    /** Manages the state of the widgets and corresponding objects. */
    private ObjectScene scene;
    /** Manages the zoom level. */
    private ZoomManager zoomer;
    private Widget mainLayer;
    private Widget messageWidget;
    private Widget headerWidget;
    private Widget contentWidget;
    private Widget mainWidget;
    private Widget separatorWidget;
    private OperationsWidget operationsWidget;
    
    /**
     * Creates a new instance of GraphView.
     * @param service
     * @param implementationClass
     */
    public DesignView(Service service, FileObject implementationClass) {
        super(new BorderLayout());
        
        this.service = service;
        this.implementationClass = implementationClass;
        this.serviceModel = ServiceModel.getServiceModel(implementationClass);
        
        scene = new ObjectScene();
        final JComponent sceneView = scene.createView();
        zoomer = new ZoomManager(scene);

        scene.getActions().addAction(ActionFactory.createCycleObjectSceneFocusAction());
        scene.setKeyEventProcessingType (EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        mainLayer = new LayerWidget(scene);
        mainLayer.setPreferredLocation(new Point(0, 0));
        mainLayer.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        scene.addChild(mainLayer);
        
        mainWidget = new Widget(scene);
        mainWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        
        String serviceName = service.getName();
        if (service.getWsdlUrl()!=null)
            serviceName = service.getServiceName()+" ["+service.getPortName()+"]";
        
        headerWidget = new LabelWidget(scene,serviceName);
        headerWidget.setFont(scene.getFont().deriveFont(Font.BOLD));
        headerWidget.setForeground(Color.GRAY);
        headerWidget.setBorder(BorderFactory.createEmptyBorder(6,28,0,0));
        mainWidget.addChild(headerWidget);
        
        separatorWidget = new SeparatorWidget(scene,
                SeparatorWidget.Orientation.HORIZONTAL);
        separatorWidget.setForeground(Color.ORANGE);
        mainWidget.addChild(separatorWidget);
        
        
        contentWidget = new Widget(scene);
        contentWidget.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 16));
        mainWidget.addChild(contentWidget);
        
        //add operations widget
        operationsWidget = new OperationsWidget(scene,service, serviceModel);
        contentWidget.addChild(operationsWidget);
        //add wsit widget
        WsitWidget wsitWidget = new WsitWidget(scene,service, implementationClass);
        contentWidget.addChild(wsitWidget);
        
        sceneView.removeMouseWheelListener((MouseWheelListener)sceneView);
        final JScrollPane panel = new JScrollPane(sceneView);
        panel.getVerticalScrollBar().setUnitIncrement(16);
        panel.getHorizontalScrollBar().setUnitIncrement(16);
        panel.setBorder(null);
        add(panel);
        mainLayer.addChild(mainWidget);

        messageWidget = new Widget(scene);
        messageWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 4));
        mainLayer.addChild(messageWidget);
        scene.addObject(messageLayerKey, messageWidget);
        
        scene.addSceneListener(new ObjectScene.SceneListener() {
            public void sceneRepaint() {
            }
            public void sceneValidating() {

            }
            public void sceneValidated() {
                int width = panel.getViewport().getWidth();
                if (width <= scene.getBounds().width) {
                    mainWidget.setMinimumSize(new Dimension(width, 0));
                }
            }
        });
        
        
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
