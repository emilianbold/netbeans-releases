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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.design.schema2java.OperationGeneratorHelper;
import org.netbeans.modules.websvc.design.view.DesignView;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.actions.RemoveOperationAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends AbstractTitledWidget {
    
    private static final Image IMAGE_ONE_WAY  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/oneway_operation.png"); // NOI18N   
    private static final Image IMAGE_REQUEST_RESPONSE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/requestresponse_operation.png"); // NOI18N   
    private static final Image IMAGE_NOTIFICATION  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/notification_operation.png"); // NOI18N   

    private Service service;
    private MethodModel operation;
    private ServiceModel serviceModel;
    
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TabbedPaneWidget tabbedWidget;
    private transient Widget listWidget;
    private transient ButtonWidget viewButton;
    
    private transient RemoveOperationAction removeAction;

    private ParametersWidget inputWidget;
    private OutputWidget outputWidget;
    private FaultsWidget faultWidget;
    private DescriptionWidget descriptionWidget;
    
    private boolean tabbedView;
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param operation
     */
    public OperationWidget(ObjectScene scene, ServiceModel serviceModel, Service service, MethodModel operation) {
        super(scene,RADIUS,RADIUS,RADIUS/2,BORDER_COLOR);
        this.service = service;
        this.operation=operation;
        this.serviceModel = serviceModel;
        
        removeAction = new RemoveOperationAction(service);
        removeAction.setWorkingSet(Collections.singleton(operation));
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            removeAction
        })));
        createContent();
    }
    
    /**
     * Obtain the underlying MethodModel
     */
     public MethodModel getMethodModel(){
         return operation;
     }
    
    private void createContent() {
        String typeOfOperation ="";
        Image image = null;
        if(operation.isOneWay()) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_OneWay");
            image = IMAGE_ONE_WAY;
        } else if (!operation.getParams().isEmpty()) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_RequestResponse");
            image = IMAGE_REQUEST_RESPONSE;
        } else {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_Notification");
            image = IMAGE_NOTIFICATION;
        }
        headerLabelWidget = new ImageLabelWidget(getScene(), image, operation.getOperationName());
        headerLabelWidget.getLabelWidget().setFont(getScene().getFont().deriveFont(Font.BOLD));
        final LabelWidget nameWidget = headerLabelWidget.getLabelWidget();
        nameWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(
                new TextFieldInplaceEditor(){
            public boolean isEnabled(Widget widget) {
                return true;
            }
            
            public String getText(Widget widget) {
                return nameWidget.getLabel();
            }
            
            public void setText(Widget widget, String text) {
                if (service.getWsdlUrl()!=null) {
                    OperationGeneratorHelper.changeWSDLOperationName(serviceModel, service, operation, text);
                }
                operation.setOperationName(text);
                nameWidget.setLabel(text);
            }
        }));
        headerLabelWidget.setToolTipText(typeOfOperation);
        getHeaderWidget().addChild(headerLabelWidget);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        for(final SampleMessageWidget.Type type:SampleMessageWidget.Type.values()) {
            final ButtonWidget sampleButton = new ButtonWidget(getScene(), type.getIcon());
            sampleButton.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent arg0) {
                    Widget messageLayer = getObjectScene().findWidget(DesignView.messageLayerKey);
                    messageLayer.removeChildren();
                    SampleMessageWidget messageWidget = new SampleMessageWidget(
                            getObjectScene(), operation, type);
                    messageLayer.addChild(messageWidget);
                }
            });
            sampleButton.setToolTipText(type.getDescription());
            buttons.addChild(sampleButton);
        }
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);

        getContentWidget().setLayout(LayoutFactory.createCardLayout(getContentWidget()));

        listWidget = new Widget(getScene());
        listWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, RADIUS/2));
        inputWidget = new ParametersWidget(getObjectScene(),operation);
        outputWidget = new OutputWidget(getObjectScene(),operation);
        faultWidget = new FaultsWidget(getObjectScene(),operation);
        descriptionWidget = new DescriptionWidget(getObjectScene(),operation);
        listWidget.addChild(inputWidget);
        listWidget.addChild(outputWidget);
        listWidget.addChild(faultWidget);
        listWidget.addChild(descriptionWidget);

        tabbedWidget = new TabbedPaneWidget(getScene());
        tabbedWidget.addTab(inputWidget);
        tabbedWidget.addTab(outputWidget);
        tabbedWidget.addTab(faultWidget);
        tabbedWidget.addTab(descriptionWidget);
        
        getContentWidget().addChild(listWidget);
        getContentWidget().addChild(tabbedWidget);
        viewButton = new ButtonWidget(getScene(),"");
        viewButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                setTabbedView(!isTabbedView());
            }
        });
        buttons.addChild(0,viewButton);
//        getObjectScene().addObject(new Object(), viewButton);
        tabbedView = false;
        setTabbedView(!tabbedView);
    }

    protected void collapseWidget() {
        super.collapseWidget();
        if(buttons!=null && buttons.getParentWidget()!=null) {
            getHeaderWidget().removeChild(buttons);
            buttons.removeChild(getExpanderWidget());
            getHeaderWidget().addChild(getExpanderWidget());
        }
    }

    protected void expandWidget() {
        super.expandWidget();
        if(buttons!=null && buttons.getParentWidget()==null) {
            getHeaderWidget().removeChild(getExpanderWidget());
            buttons.addChild(getExpanderWidget());
            getHeaderWidget().addChild(buttons);
        }
    }

    public Object hashKey() {
        return operation;
    }

    private boolean isTabbedView() {
        return tabbedView;
    }
    
    private void setTabbedView(boolean tabbedView) {
        if(isTabbedView()!=tabbedView) {
            this.tabbedView = tabbedView;
            if(tabbedView) {
                LayoutFactory.setActiveCard(getContentWidget(), tabbedWidget);
                viewButton.setImage(IMAGE_LIST);
            } else {
                LayoutFactory.setActiveCard(getContentWidget(), listWidget);
                viewButton.setImage(IMAGE_TABBED);
            }
        }
    }

    /** The expand button image. */
    private static final Image IMAGE_TABBED = new BufferedImage(12, 12,
            BufferedImage.TYPE_INT_ARGB);
    /** The collapse button image. */
    private static final Image IMAGE_LIST = new BufferedImage(12, 12,
            BufferedImage.TYPE_INT_ARGB);

    static {

        // Create the expand image.
        Graphics2D g2 = ((BufferedImage) IMAGE_TABBED).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        float w = IMAGE_TABBED.getWidth(null);
        float h = IMAGE_TABBED.getHeight(null);
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0, 0);
        gp.lineTo(w/2, 0);
        gp.lineTo(w/2, h/4);
        gp.lineTo(w, h/4);
        gp.lineTo(w, h);
        gp.lineTo(0, h);
        gp.closePath();
        g2.translate(0, 0 );
        g2.setPaint(new Color(0x888888));
        g2.fill(gp);

        // Create the collapse image.
        g2 = ((BufferedImage) IMAGE_LIST).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        w = IMAGE_LIST.getWidth(null);
        h = IMAGE_LIST.getHeight(null);
        g2.translate(0, 0);
        g2.setPaint(new Color(0x888888));
        g2.drawLine(0, (int)h/4, 1, (int)h/4);
        g2.drawLine(3, (int)h/4, (int)w, (int)h/4);
        g2.drawLine(0, (int)h/2, 1,(int)h/2);
        g2.drawLine(3, (int)h/2, (int)w, (int)h/2);
        g2.drawLine(0, (int)(3*h/4), 1, (int)(3*h/4));
        g2.drawLine(3, (int)(3*h/4), (int)w, (int)(3*h/4));
    }
}
