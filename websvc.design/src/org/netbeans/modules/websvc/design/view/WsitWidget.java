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

import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookie;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProviderRegistry;
import org.netbeans.modules.websvc.design.view.widget.AbstractTitledWidget;
import org.netbeans.modules.websvc.design.view.widget.ButtonWidget;
import org.netbeans.modules.websvc.design.view.widget.ImageLabelWidget;
import org.netbeans.modules.websvc.design.view.widget.CheckBoxWidget;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ajit Bhate
 */
public class WsitWidget extends AbstractTitledWidget {
    
    private transient FileObject implementationClass;
    private transient Widget buttons;
    private transient Widget configButtons;
    private transient ButtonWidget advancedButton;
    private transient ImageLabelWidget headerLabelWidget;
    private Service service;
    
    
    
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param service
     * @param serviceModel
     */
    public WsitWidget(ObjectScene scene, final Service service, FileObject implementationClass) {
        super(scene,RADIUS,BORDER_COLOR);
        this.implementationClass = implementationClass;
        this.service=service;
        setOpaque(true);
        setBackground(TITLE_COLOR_PARAMETER);
        createContent();
    }
    
    private void createContent() {
        headerLabelWidget = new ImageLabelWidget(getScene(), null,
                NbBundle.getMessage(WsitWidget.class, "LBL_Wsit"));
        headerLabelWidget.getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        
        buttons.addChild(getExpanderWidget());
        
        getHeaderWidget().addChild(buttons);

        getContentWidget().setBorder(BorderFactory.createEmptyBorder(RADIUS));

        configButtons = new Widget(getScene());
        configButtons.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, RADIUS));
        getContentWidget().addChild(configButtons);
        populateConfigWidget();

        Widget advancedButtonContainer = new Widget(getScene());
        advancedButtonContainer.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.RIGHT_BOTTOM, RADIUS));
        advancedButton = new ButtonWidget(getScene(),
                new AdvancedAction(service,implementationClass));
        advancedButton.setOpaque(true);
        advancedButton.setRoundedBorder(advancedButton.BORDER_RADIUS, 4, 0, null);
        advancedButtonContainer.addChild(advancedButton);
        getContentWidget().addChild(advancedButtonContainer);
    }
    
    private void populateConfigWidget() {
        configButtons.removeChildren();
        for(WSConfigurationProvider provider : getConfigProviders()){
            WSConfiguration config = provider.getWSConfiguration(service, implementationClass);
            if(config != null){
                CheckBoxWidget button = new CheckBoxWidget(getScene(), config.getDisplayName());
                button.setAction(new ConfigWidgetAction(config));
                button.setToolTipText(config.getDescription());
                button.getLabelWidget().setFont(
                        getScene().getFont().deriveFont(Font.BOLD));
                button.setSelected(config.isSet()); //TODO: need to refresh the widget to reflect state
                configButtons.addChild(button);
            }
        }
        configButtons.setVisible(!configButtons.getChildren().isEmpty());
    }
    
    protected Paint getBodyPaint(Rectangle bounds) {
        return TITLE_COLOR_PARAMETER;
    }
    
    public Object hashKey() {
        return service==null?null:service.getServiceName()+"_wsit";
    }
    
    private Set<WSConfigurationProvider> getConfigProviders(){
        return WSConfigurationProviderRegistry.getDefault().getWSConfigurationProviders();
    }
    
    class ConfigWidgetAction extends AbstractAction{
        WSConfiguration config;
        public ConfigWidgetAction(WSConfiguration config){
            this.config = config;
        }
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals(CheckBoxWidget.ACTION_COMMAND_SELECTED)){
                config.set();
            }else{
                config.unset();
            }
        }
    }
    
    class AdvancedAction extends AbstractAction{
        private transient FileObject implementationClass;
        private Service service;
        private EditWSAttributesCookie cookie;
        public AdvancedAction(Service service, FileObject implementationClass){
            super(NbBundle.getMessage(WsitWidget.class, "LBL_Wsit_Advanced"));
            this.implementationClass = implementationClass;
            this.service = service;
            setEnabled(false);
            initializeCookie();
        }
        public void actionPerformed(ActionEvent event) {
            if(cookie==null) return;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    cookie.openWSAttributesEditor();
                }
            }, 10);
        }
        private void initializeCookie() {
            Project project = FileOwnerQuery.getOwner(implementationClass);
            if (project==null) return;
            JAXWSView view = JAXWSView.getJAXWSView();
            if (view==null) return;
            final Node node = view.createJAXWSView(project);
            if(node==null) return;
            final FileObject currentImplBean = implementationClass;
            final Service currentService = service;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for(Node n :node.getChildren().getNodes()) {
                        Service sv = n.getLookup().lookup(Service.class);
                        if (sv==null ) continue;
                        if(n.getLookup().lookup(FileObject.class) == currentImplBean &&
                                 sv.getName().equals(currentService.getName())) {
                            cookie = n.getLookup().lookup(EditWSAttributesCookie.class);
                            setEnabled(cookie!=null);
                            return;
                        }
                    }
                }
            });
        }
    }
    
}
