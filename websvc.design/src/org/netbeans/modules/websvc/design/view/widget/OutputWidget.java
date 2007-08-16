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

import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.view.layout.BorderLayout;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Ajit Bhate
 */
public class OutputWidget extends AbstractTitledWidget implements TabWidget{
    
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/output.png"); // NOI18N
    
    private MethodModel method;
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient Widget tabComponent;
    
    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param method
     */
    public OutputWidget(ObjectScene scene, MethodModel method) {
        super(scene,0,RADIUS,0,BORDER_COLOR);
        this.method = method;
        createContent();
    }
    
    protected Paint getTitlePaint(Rectangle bounds) {
        return TITLE_COLOR_OUTPUT;
    }
    
    private void createContent() {
        headerLabelWidget = new ImageLabelWidget(getScene(), getIcon(), getTitle());
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        BorderLayout.addLayoutComponent(getHeaderWidget(), headerLabelWidget, BorderLayout.Constraint.CENTER);
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        buttons.addChild(getExpanderWidget());
        BorderLayout.addLayoutComponent(getHeaderWidget(), buttons, BorderLayout.Constraint.EAST);
        
        LabelWidget returnWidget = new LabelWidget(getScene(), method.isOneWay()?
            NbBundle.getMessage(OperationWidget.class, "LBL_ReturnTypeNone"):
            NbBundle.getMessage(OperationWidget.class, "LBL_ReturnType", method.getResult().getResultType()));
        returnWidget.setAlignment(LabelWidget.Alignment.CENTER);
        getContentWidget().addChild(returnWidget);
        
    }
    
    public String getTitle() {
        return NbBundle.getMessage(OperationWidget.class, "LBL_Output");
    }
    
    public Image getIcon() {
        return null;
        //return IMAGE;
    }
    
    public Object hashKey() {
        return method==null?null:method.getResult();
    }
    
    public Widget getComponentWidget() {
        if(tabComponent==null) {
            tabComponent = createContentWidget();
            LabelWidget returnWidget = new LabelWidget(getScene(), method.isOneWay()?
                NbBundle.getMessage(OperationWidget.class, "LBL_ReturnTypeNone"):
                NbBundle.getMessage(OperationWidget.class, "LBL_ReturnType", method.getResult().getResultType()));
            returnWidget.setAlignment(LabelWidget.Alignment.CENTER);
            tabComponent.addChild(returnWidget);
        }
        return tabComponent;
    }
}
