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
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends LabelWidget{
    
    private static final Border BORDER_4 = BorderFactory.createLineBorder(4, new Color(128,191,255));

    private WidgetAction moveAction = ActionFactory.createMoveAction ();

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param label 
     */
    public OperationWidget(Scene scene, String label) {
        super(scene,label);
        setBorder(BORDER_4);
        getActions().addAction(moveAction);
        setOpaque(true);
        setBackground(new Color(191,255,255));
    }
    
}
