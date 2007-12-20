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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Dimension;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.util.Lookup;

/**
 *
 * @author rico
 */
public abstract class OperationWithFaultWidget<T extends Operation> extends OperationWidget<T> {
    private Widget dummyWidget;
    
    /** Creates a new instance of OperationWithFaultWidget */
    public OperationWithFaultWidget(Scene scene, T operation, Lookup lookup) {
        super(scene, operation, lookup);
        dummyWidget = new Widget(scene);
        dummyWidget.setMinimumSize(new Dimension(5, 10));
    }
    
    @Override
    public void setRightSided(boolean rightSided) {
        super.setRightSided(rightSided);
        refreshFaults(getVerticalWidget());
    }

    
    private void refreshFaults(Widget verticalWidget) {
        dummyWidget.removeFromParent();
        for(Fault fault : getWSDLComponent().getFaults()){
            Widget faultWidget = WidgetFactory.getInstance().getOrCreateWidget(getScene(), fault, getLookup(), verticalWidget);
            verticalWidget.addChild(faultWidget); //adjust for dummy widget. add the fault before dummy widget.
        }
        verticalWidget.addChild(dummyWidget);
    }
    
    @Override
    public void childrenAdded() {
        refreshFaults(getVerticalWidget());
    }
    
}
