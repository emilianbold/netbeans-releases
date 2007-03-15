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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.actions.AddOperationAction;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class OperationsWidget extends LayerWidget implements ExpandableWidget{
    
    private static final Color FILL_COLOR_DARK = new Color(255,255,102);
    private static final Color FILL_COLOR_LIGHT = new Color(255,255,204);
    private static final Color BORDER_COLOR = new Color(153,204,255);
    private static final int radius = 10;
    public  static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/operation.png"); // NOI18N   

    private transient WsdlService wsdlService;
    private transient Action addAction;

    private transient Widget contentWidget;
    private transient HeaderWidget headerWidget;
    private transient Widget buttons;
    private transient ExpanderWidget expander;
    private transient ImageLabelWidget headerLabelWidget;

    
    
    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param service 
     * @param implementationClass 
     */
    public OperationsWidget(Scene scene, Service service, FileObject implementationClass) {
        super(scene);
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 16));
        addAction = new AddOperationAction(service, implementationClass);
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            addAction,
        })));
        initialize(service);
        createContent();
    }
    
    /**
     * Initialize the model. Try to find if the Service is created from WSDL.
     * If so find the WsdlService object representing JAXWS service
     */
    private void initialize(Service service) {
        try {
            String wsdlUrlStr = service.getWsdlUrl();
            if(wsdlUrlStr==null) return;
            URL wsdlUrl = new URL(wsdlUrlStr);
            if(wsdlUrl==null) return;
            WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlUrl);
            if(modeler==null) return;
            WsdlModel model = modeler.getAndWaitForWsdlModel();
            if(model==null) return;
            wsdlService = model.getServiceByName(service.getServiceName());
        } catch(MalformedURLException e) {
        }
    }

    private void createContent() {
        if (wsdlService==null) return;
        
        boolean expanded = ExpanderWidget.isExpanded(this, true);
        expander = new ExpanderWidget(getScene(), this, expanded);

        headerWidget = new HeaderWidget(getScene(), expander);
        headerWidget.setLayout(new LeftRightLayout(32));

        headerLabelWidget = new ImageLabelWidget(getScene(), IMAGE, "Messages", "(1)");
        headerWidget.addChild(headerLabelWidget);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));

        ButtonWidget addButton = new ButtonWidget(getScene(), 
                addAction.getValue(Action.NAME).toString());
        addButton.getButton().setAction(addAction);

        buttons.addChild(addButton);
        buttons.addChild(expander);

        headerWidget.addChild(buttons);

        addChild(headerWidget);
        
        contentWidget = new Widget(getScene());
        for(WsdlPort port:wsdlService.getPorts()) {
            for(WsdlOperation operation:port.getOperations()) {
                contentWidget.addChild(new OperationContentWidget(getScene(),operation));
            }
        }
        
        if(expanded) {
            expandWidget();
        } else {
            collapseWidget();
        }
    }

    protected void paintWidget() {
        Rectangle bounds = getBounds();
        Graphics2D g = getGraphics();
        Paint oldPaint = g.getPaint();
        g.setPaint(FILL_COLOR_LIGHT);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, radius, radius);
        g.setPaint(BORDER_COLOR);
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, radius, radius);
        g.setPaint(oldPaint);
    }
    
    public void collapseWidget() {
        if(contentWidget.getParentWidget()!=null) {
            removeChild(contentWidget);
            revalidate();
            repaint();
            getScene().repaint();
            getScene().revalidate();
        }
    }

    public void expandWidget() {
        if(contentWidget.getParentWidget()==null) {
            addChild(contentWidget);
            revalidate();
            repaint();
            getScene().repaint();
            getScene().revalidate();
        }
    }

    public Object hashKey() {
        return wsdlService==null?null:wsdlService.getName();
    }

}
