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
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.openide.util.NbBundle;

/**
 * @author Ajit Bhate
 */
public class FaultsWidget extends AbstractTitledWidget implements TabWidget {

    private MethodModel method;

    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    
    private transient TableModel model;
    private transient TableWidget faultTable;
    
    private transient Widget tabComponent;

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene
     * @param method  
     */
    public FaultsWidget(ObjectScene scene, MethodModel method) {
        super(scene,0,RADIUS,0,BORDER_COLOR);
        this.method = method;
        createContent();
    }
    
    protected Paint getTitlePaint(Rectangle bounds) {
        return TITLE_COLOR_FAULT;
    }
    
    private void createContent() {
        model = new FaultsTableModel(method);
        populateContentWidget(getContentWidget());
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(0,1,1,1));
        headerLabelWidget = new ImageLabelWidget(getScene(), getIcon(), getTitle(), 
                "("+method.getFaults().size()+")");
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(new Widget(getScene()),5);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),4);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);
    }

    private void populateContentWidget(Widget parentWidget) {
        if(model.getRowCount()>0) {
            faultTable = new TableWidget(getScene(),model);
            parentWidget.addChild(faultTable);
        } else {
            LabelWidget noFaults = new LabelWidget(getScene(),
                    NbBundle.getMessage(OperationWidget.class, "LBL_FaultsNone"));
            noFaults.setAlignment(LabelWidget.Alignment.CENTER);
            parentWidget.addChild(noFaults);
        }
    }

    public Object hashKey() {
        return model;
    }

    public String getTitle() {
        return NbBundle.getMessage(OperationWidget.class, "LBL_Faults");
    }

    public Image getIcon() {
        return null;
//        return Utilities.loadImage
//            ("org/netbeans/modules/websvc/design/view/resources/fault.png"); // NOI18N
    }

    public Widget getComponentWidget() {
        if(tabComponent==null) {
            tabComponent = createContentWidget();
            populateContentWidget(tabComponent);
        }
        return tabComponent;
    }
}
