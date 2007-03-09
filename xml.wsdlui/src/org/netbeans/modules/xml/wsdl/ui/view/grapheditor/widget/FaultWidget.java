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
 * FaultWidget.java
 *
 * Created on November 6, 2006, 6:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.ArrowWidget.ParameterType;
import org.openide.util.Lookup;

/**
 * Represents a fault WSDL component.
 *
 * @author radval
 */
public class FaultWidget extends OperationParameterWidget {
    /**
     * Creates a new instance of FaultWidget.
     *
     * @param  scene   Scene in which this Widget will exist.
     * @param  fault   the WSDL component.
     * @param  lookup  the Lookup for this widget.
     * @param  reversed  Should the direction of the arrow be reversed?
     */
    public FaultWidget(Scene scene, Fault fault, Lookup lookup, boolean reversed) {
        super(scene, fault, lookup);
        DirectionCookie dc = (DirectionCookie) lookup.lookup(DirectionCookie.class);
        boolean rightSided = dc == null ? false : dc.isRightSided();
        boolean direction = reversed ? rightSided : !rightSided;
        ArrowWidget widget = new ArrowWidget(scene, direction, ParameterType.FAULT);
        widget.setColor(WidgetConstants.FAULT_ARROW_COLOR);
        if (isImported()) widget.setColor(Color.GRAY.darker());
        addChild(widget);
     }
    
}
