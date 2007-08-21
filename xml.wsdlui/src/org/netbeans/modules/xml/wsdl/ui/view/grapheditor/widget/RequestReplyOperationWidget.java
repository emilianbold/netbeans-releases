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

/*
 * PortTypeColumnWidget.java
 *
 * Created on November 5, 2006, 10:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;


import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.OneSideJustifiedLayout;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class RequestReplyOperationWidget
        extends OperationWithFaultWidget<RequestResponseOperation> {
    private Widget verticalWidget;

    
    
    /** Creates a new instance of PortTypeColumnWidget */
    public RequestReplyOperationWidget(Scene scene, RequestResponseOperation operation,
            Lookup lookup) {
        super(scene, operation, lookup);
        //setBorder(BorderFactory.createLineBorder(Color.RED));
    }
    
    @Override
    protected void init() {
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
        //already initialized?
        if (getChildren().size() > 0) return;
        
        Scene scene = getScene();
        Lookup lookup = getLookup();
        WidgetFactory factory = WidgetFactory.getInstance();
        Widget inputWidget = factory.createWidget(scene,
                getWSDLComponent().getInput(), lookup, true);
        if (inputWidget.getParentWidget() != null && inputWidget.getParentWidget() != this) {
            inputWidget = factory.createWidget(scene,
                    getWSDLComponent().getInput(), lookup);
        }
        Widget outputWidget = factory.createWidget(scene,
                getWSDLComponent().getOutput(), lookup, true);
        if (outputWidget.getParentWidget() != null && outputWidget.getParentWidget() != this) {
            outputWidget = factory.createWidget(scene,
                    getWSDLComponent().getOutput(), lookup);
        }
        
        verticalWidget = new Widget(scene);
        verticalWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.JUSTIFY, 3));
        verticalWidget.addChild(inputWidget);
        verticalWidget.addChild(outputWidget);
        
        Widget horizontalWidget = new Widget(scene);
        horizontalWidget.setLayout(new OneSideJustifiedLayout(isRightSided()));
        horizontalWidget.addChild(verticalWidget);
        horizontalWidget.addChild(mOperationRectangleWidget);
        
        addChild(getLabel());
        addChild(horizontalWidget);
        
    }
    
    @Override
    protected Widget getVerticalWidget() {
        return verticalWidget;
    }
    
    
}
