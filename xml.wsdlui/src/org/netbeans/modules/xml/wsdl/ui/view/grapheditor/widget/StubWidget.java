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

import java.awt.Color;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;

/**
 *
 * @author anjeleevich
 */
public class StubWidget extends LabelWidget {
    
    public StubWidget(Scene scene, String text) {
        super(scene, text);
        setBorder(BORDER);
        setFont(scene.getDefaultFont());
        setForeground(COLOR);
        setAlignment(Alignment.CENTER);
    }
    
    private static Color COLOR = new Color(0xCCCCCC);
    private static Border BORDER = new FilledBorder(1, 1, 7, 7, COLOR, Color.WHITE);
    
}
