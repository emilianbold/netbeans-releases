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
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends AbstractTitledWidget {
    
    private static final Color BORDER_COLOR = new Color(153,153,255);
    private static final int GAP = 16;

    private static final Image IMAGE_ONE_WAY  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/oneway_operation.png"); // NOI18N   
    private static final Image IMAGE_REQUEST_RESPONSE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/requestresponse_operation.png"); // NOI18N   
    private static final Image IMAGE_NOTIFICATION  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/notification_operation.png"); // NOI18N   

    private Operation operation;
    
    private transient Widget contentWidget;
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TabbedPaneWidget tabbedWidget;
    private transient Widget listWidget;
    private transient ButtonWidget viewButton;

    private ParametersWidget inputWidget;
    private OutputWidget outputWidget;
    private FaultsWidget faultWidget;
    private Widget descriptionWidget;
    
    private boolean tabbedView;
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param operation
     */
    public OperationWidget(Scene scene, Operation operation) {
        super(scene,GAP,BORDER_COLOR);
        this.operation=operation;
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
        })));
        createContent();
    }
    
    private void createContent() {
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        String typeOfOperation ="";
        Image image = null;
        if(operation.getOutput()==null) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_OneWay");
            image = IMAGE_ONE_WAY;
        } else if (operation.getInput()!=null) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_RequestResponse");
            image = IMAGE_REQUEST_RESPONSE;
        } else {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_Notification");
            image = IMAGE_NOTIFICATION;
        }
        headerLabelWidget = new ImageLabelWidget(getScene(), image, operation.getName(), 
                "("+typeOfOperation+")");
        getHeaderWidget().addChild(headerLabelWidget);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);

        contentWidget = new Widget(getScene());
        contentWidget.setLayout(LayoutFactory.createCardLayout(contentWidget));

        listWidget = new Widget(getScene());
        listWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));
        inputWidget = new ParametersWidget(getScene(),operation.getInput());
        outputWidget = new OutputWidget(getScene(),operation.getOutput());
        faultWidget = new FaultsWidget(getScene(),operation);
        descriptionWidget = new LabelWidget(getScene(),"description");
        listWidget.addChild(inputWidget);
        listWidget.addChild(outputWidget);
        listWidget.addChild(faultWidget);
        listWidget.addChild(descriptionWidget);

        tabbedWidget = new TabbedPaneWidget(getScene());
        tabbedWidget.addTab(inputWidget);
        tabbedWidget.addTab(outputWidget);
        tabbedWidget.addTab(faultWidget);
        tabbedWidget.addTab("Desription", null, new LabelWidget(getScene(),"description"));
        
        contentWidget.addChild(listWidget);
        contentWidget.addChild(tabbedWidget);
        viewButton = new ButtonWidget(getScene(),"");
        viewButton.setMargin(new Insets(2, 2, 2, 2));
        viewButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                setTabbedView(!isTabbedView());
            }
        });
        if(isExpanded()) {
            expandWidget();
        } else {
            collapseWidget();
        }
        tabbedView = false;
        setTabbedView(!tabbedView);
    }

    protected void collapseWidget() {
        if(contentWidget.getParentWidget()!=null) {
            removeChild(contentWidget);
        }
        if(viewButton.getParentWidget()!=null) {
            viewButton.removeFromParent();
        }
    }

    protected void expandWidget() {
        if(contentWidget.getParentWidget()==null) {
            addChild(contentWidget);
        }
        if(viewButton.getParentWidget()==null) {
            buttons.addChild(0,viewButton);
        }
    }

    public Object hashKey() {
        return operation==null?null:operation.getName();
    }

    private boolean isTabbedView() {
        return tabbedView;
    }
    
    private void setTabbedView(boolean tabbedView) {
        if(isTabbedView()!=tabbedView) {
            this.tabbedView = tabbedView;
            if(tabbedView) {
                LayoutFactory.setActiveCard(contentWidget, tabbedWidget);
                viewButton.setImage(IMAGE_LIST);
            } else {
                LayoutFactory.setActiveCard(contentWidget, listWidget);
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
