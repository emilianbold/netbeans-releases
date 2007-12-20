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
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class NotificationOperationWidget
extends OperationWidget<NotificationOperation> {
    Widget verticalWidget;

    public NotificationOperationWidget(Scene scene, NotificationOperation operation,
            Lookup lookup) {
        super(scene, operation, lookup);
    }


    @Override
    protected Widget getVerticalWidget() {
        if (verticalWidget == null) {
            Scene scene = getScene();
            verticalWidget = new Widget(scene);
            verticalWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.JUSTIFY, 3));
            Widget outputWidget = WidgetFactory.getInstance().getOrCreateWidget(
                    scene, getWSDLComponent().getOutput(), getLookup(), verticalWidget);
            verticalWidget.addChild(outputWidget);
        }
        return verticalWidget;
    }

}
