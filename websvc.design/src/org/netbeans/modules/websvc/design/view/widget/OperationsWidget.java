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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.Action;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.actions.AddOperationAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class OperationsWidget extends AbstractTitledWidget {
    
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/operation.png"); // NOI18N
    
    private transient ServiceModel serviceModel;
    private transient Action addAction;
    
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    
    
    
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param service
     * @param serviceModel
     */
    public OperationsWidget(ObjectScene scene, final Service service, ServiceModel serviceModel) {
        super(scene,RADIUS,BORDER_COLOR);
        this.serviceModel = serviceModel;
        serviceModel.addServiceChangeListener(new ServiceChangeListener() {
            
            public void propertyChanged(String propertyName, String oldValue,
                    String newValue) {
                System.out.println("receieved propertychangeevent for propertyName="+propertyName);
            }
            
            public void operationAdded(MethodModel method) {
                OperationWidget operationWidget = new OperationWidget(getScene(),service, method);
                getContentWidget().addChild(operationWidget);
                getObjectScene().addObject(method, operationWidget);
                updateHeaderLabel();
                getScene().validate();
            }
            
            public void operationRemoved(MethodModel method) {
                Widget operationWidget = getObjectScene().findWidget(method);
                if(operationWidget!=null) {
                    getObjectScene().removeObject(method);
                    getContentWidget().removeChild(operationWidget);
                    updateHeaderLabel();
                    getScene().validate();
                }
            }
            
            public void operationChanged(MethodModel oldMethod, MethodModel newMethod) {
                operationRemoved(oldMethod);
                operationAdded(newMethod);
            }
            
        });
        addAction = new AddOperationAction(service, serviceModel.getImplementationClass());
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            addAction,
        })));
        createContent(service);
    }
    
    private void createContent(Service service) {
        if (serviceModel==null) return;
        
        headerLabelWidget = new ImageLabelWidget(getScene(), null,
                NbBundle.getMessage(OperationWidget.class, "LBL_Operations"));
        headerLabelWidget.getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        updateHeaderLabel();
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        
        ButtonWidget addButton = new ButtonWidget(getScene(), addAction);
        
        buttons.addChild(addButton);
        buttons.addChild(getExpanderWidget());
        
        getHeaderWidget().addChild(buttons);
        
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(RADIUS));
        if(serviceModel.getOperations()!=null) {
            for(MethodModel operation:serviceModel.getOperations()) {
                OperationWidget operationWidget = new OperationWidget(getScene(),service, operation);
                getContentWidget().addChild(operationWidget);
                getObjectScene().addObject(operation, operationWidget);
            }
        }
    }
    
    private void updateHeaderLabel() {
        int noOfOperations = serviceModel.getOperations()==null?0:serviceModel.getOperations().size();
        headerLabelWidget.setComment("("+noOfOperations+")");
    }
    
    public Object hashKey() {
        return serviceModel==null?null:serviceModel.getServiceName();
    }
    
    /**
     * Adds the widget actions to the given toolbar (no separators are
     * added to either the beginning or end).
     *
     * @param  toolbar  to which the actions are added.
     */
    public void addToolbarActions(JToolBar toolbar) {
        toolbar.add(addAction);
    }
    
    private ObjectScene getObjectScene() {
        return (ObjectScene)getScene();
    }
}
