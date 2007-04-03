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
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Ajit Bhate
 */
public class ParametersWidget extends AbstractTitledWidget implements TabWidget {
    
    private static final Color BORDER_COLOR = new Color(128,128,255);
    private static final int GAP = 16;
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/input.png"); // NOI18N   

    private transient MethodModel method;

    private transient Widget contentWidget;
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TableModel model;
    private transient TableWidget parameterTable;
    
    private transient Widget tabComponent;
    
    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param method 
     */
    public ParametersWidget(Scene scene, MethodModel method) {
        super(scene,GAP,BORDER_COLOR);
        this.method = method;
        createContent();
    }
    
    private void createContent() {
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        contentWidget = new Widget(getScene());
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        int noOfParams = 0;
        if(!method.getParams().isEmpty()) {
            noOfParams = method.getParams().size();
            model = new ParametersTableModel(method);
            parameterTable = new TableWidget(getScene(),model);
            contentWidget.addChild(parameterTable);
        } else {
            contentWidget.addChild(new LabelWidget(getScene(),
                    NbBundle.getMessage(OperationWidget.class, "LBL_InputNone")));
        }
        headerLabelWidget = new ImageLabelWidget(getScene(), IMAGE, 
                NbBundle.getMessage(OperationWidget.class, "LBL_Input"), 
                "("+noOfParams+")");
        getHeaderWidget().addChild(headerLabelWidget);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));

        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);

        if(isExpanded()) {
            expandWidget();
        } else {
            collapseWidget();
        }
    }

    protected void collapseWidget() {
        if(contentWidget.getParentWidget()!=null) {
            removeChild(contentWidget);
            repaint();
        }
    }

    protected void expandWidget() {
        if(contentWidget.getParentWidget()==null) {
            addChild(contentWidget);
        }
    }

    public Object hashKey() {
        return method==null?null:method.getOperationName()+"_Parameters";
    }
    
    public String getTitle() {
        return NbBundle.getMessage(OperationWidget.class, "LBL_Input");
    }

    public Image getIcon() {
        return IMAGE;
    }

    public Widget getComponentWidget() {
        if(tabComponent==null) {
            if(model!=null) {
                tabComponent = new TableWidget(getScene(),model); 
            } else {
                tabComponent = new LabelWidget(getScene(),
                    NbBundle.getMessage(OperationWidget.class, "LBL_InputNone"));
            }
        }
        return tabComponent;
    }
}
