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
 * InputWidget.java
 *
 * Created on 2006/08/15, 22:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.ArrowWidget.ParameterType;
import org.openide.util.Lookup;

/**
 * Widget to represent an Input WSDL component.
 *
 * @author radval
 */
public class InputWidget extends OperationParameterWidget {

    /**
     * Creates a new instance of InputWidget.
     *
     * @param  scene   the widget Scene.
     * @param  input   the corresponding WSDL component.
     * @param  lookup  the Lookup for this widget.
     */
    public InputWidget(Scene scene, Input input, Lookup lookup) {
        super(scene, input, lookup);
        DirectionCookie dc = (DirectionCookie) lookup.lookup(DirectionCookie.class);
        boolean rightSided = dc == null ? false : dc.isRightSided();
        ArrowWidget widget = new ArrowWidget(scene, rightSided, ParameterType.INPUT);
        if (isImported()) widget.setColor(Color.GRAY);
        addChild(widget);
    }
    
}
