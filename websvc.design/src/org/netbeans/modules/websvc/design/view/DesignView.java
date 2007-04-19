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
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProviderRegistry;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.view.widget.ButtonAction;
import org.netbeans.modules.websvc.design.view.widget.OperationsWidget;
import org.netbeans.modules.websvc.design.view.widget.ToggleButtonWidget;
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
    private Widget mMainLayer;
    private Widget messageLayer;
    private Widget contentWidget;
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
        zoomer = new ZoomManager(scene);
        // add actions
        scene.getActions().addAction(ActionFactory.createZoomAction());
        scene.getActions().addAction(ActionFactory.createPanAction());
        scene.getActions().addAction(ButtonAction.DEFAULT);
        
        mMainLayer = new LayerWidget(scene);
        mMainLayer.setPreferredLocation(new Point(0, 0));
        mMainLayer.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        String serviceName = service.getName();
        if (service.getWsdlUrl()!=null)
            serviceName = service.getServiceName()+"["+service.getPortName()+"]";
        LabelWidget serviceWidget = new LabelWidget(scene,serviceName);
        Widget headerWidget = new Widget(scene);
        headerWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 12));
        headerWidget.setBorder(BorderFactory.createOpaqueBorder(12,24,12,0));
        headerWidget.setBackground(new Color(180,180,255));
        headerWidget.setOpaque(true);
        headerWidget.addChild(serviceWidget);
        
        //if there are Configuration Providers, add their buttons
        Set<WSConfigurationProvider> providers = getConfigProviders();
        if(providers.size() > 0){
            headerWidget.addChild(createConfigWidget(providers));
        }
        
        mMainLayer.addChild(headerWidget);
        scene.addChild(mMainLayer);
        messageLayer = new LayerWidget(scene);
        messageLayer.setPreferredLocation(new Point(0, 0));
        scene.addChild(messageLayer);
        scene.addObject(messageLayerKey, messageLayer);
        
        contentWidget = new Widget(scene);
        contentWidget.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 16));
        mMainLayer.addChild(contentWidget);
        //add operations widget
        operationsWidget = new OperationsWidget(scene,service, serviceModel);
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
    
    class ConfigWidgetAction extends AbstractAction{
        WSConfiguration config;
        public ConfigWidgetAction(WSConfiguration config){
            this.config = config;
        }
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals(ToggleButtonWidget.ACTION_COMMAND_SELECTED)){
                config.set();
            }else{
                config.unset();
            }
        }
    }
    
    private Widget createConfigWidget(Set<WSConfigurationProvider> providers){
        Widget configWidget = new Widget(scene);
        configWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        for(WSConfigurationProvider provider : providers){
            WSConfiguration config = provider.getWSConfiguration(service, implementationClass);
            if(config != null){
                ToggleButtonWidget button = new ToggleButtonWidget(scene,config.getIcon(),null);
                button.setAction(new ConfigWidgetAction(config));
                button.setToolTipText(config.getDescription());
                button.setSelected(config.isSet()); //TODO: need to refresh the widget to reflect state
                configWidget.addChild(button);
            }
        }
        return configWidget;
    }
    
    private Set<WSConfigurationProvider> getConfigProviders(){
        return WSConfigurationProviderRegistry.getDefault().getWSConfigurationProviders();
    }
    
}
