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
import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends RoundedRectangleWidget implements ExpandableWidget{
    
    private static final Color TITLE_COLOR = new Color(180,180,255);
    private static final Color TITLE_COLOR2 = new Color(204,204,255);
    private static final Color BORDER_COLOR = new Color(153,153,255);
    private static final int GAP = 16;

    private static final Image IMAGE_ONE_WAY  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/oneway_operation.png"); // NOI18N   
    private static final Image IMAGE_REQUEST_RESPONSE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/requestresponse_operation.png"); // NOI18N   
    private static final Image IMAGE_NOTIFICATION  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/notification_operation.png"); // NOI18N   

    private WsdlOperation operation;
    
    private transient Widget contentWidget;
    private transient HeaderWidget headerWidget;
    private transient Widget buttons;
    private transient ExpanderWidget expander;
    private transient ImageLabelWidget headerLabelWidget;

    private Widget inputWidget;
    private Widget outputWidget;
    private Widget faultWidget;
    private Widget descriptionWidget;

    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param operation
     */
    public OperationWidget(Scene scene, WsdlOperation operation) {
        super(scene);
        setBorderColor(BORDER_COLOR);
        setTitleColor(TITLE_COLOR,TITLE_COLOR2);
        setRadius(GAP);
        this.operation=operation;
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
        })));
        createContent();
    }
    
    private void createContent() {
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        boolean expanded = ExpanderWidget.isExpanded(this, true);
        expander = new ExpanderWidget(getScene(), this, expanded);

        headerWidget = new HeaderWidget(getScene(), expander);
        headerWidget.setLayout(new LeftRightLayout(32));

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));

        buttons.addChild(expander);

        headerWidget.addChild(buttons);

        addChild(headerWidget);
        setTitleWidget(headerWidget);
        
        contentWidget = new Widget(getScene());
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        String typeOfOperation ="";
        Image image = null;
        if(operation.getReturnTypeName()==null) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_OneWay");
            image = IMAGE_ONE_WAY;
        } else if (!operation.getParameters().isEmpty()) {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_RequestResponse");
            image = IMAGE_REQUEST_RESPONSE;
        } else {
            typeOfOperation = NbBundle.getMessage(OperationWidget.class, "LBL_Notification");
            image = IMAGE_NOTIFICATION;
        }
        headerLabelWidget = new ImageLabelWidget(getScene(), image, operation.getName(), 
                "("+typeOfOperation+")");
        headerWidget.addChild(0,headerLabelWidget);

        inputWidget = new LabelWidget(getScene(),"parameters");
        outputWidget = new LabelWidget(getScene(),"return type : "+operation.getReturnTypeName());
        faultWidget = new LabelWidget(getScene(),"faults");
        descriptionWidget = new LabelWidget(getScene(),"description");
        contentWidget.addChild(inputWidget);
        contentWidget.addChild(outputWidget);
        contentWidget.addChild(faultWidget);
        contentWidget.addChild(descriptionWidget);
        if(expanded) {
            expandWidget();
        } else {
            collapseWidget();
        }
    }

    public void collapseWidget() {
        if(contentWidget.getParentWidget()!=null) {
            removeChild(contentWidget);
        }
    }

    public void expandWidget() {
        if(contentWidget.getParentWidget()==null) {
            addChild(contentWidget);
        }
    }

    public Object hashKey() {
        return operation==null?null:operation.getName();
    }

}
