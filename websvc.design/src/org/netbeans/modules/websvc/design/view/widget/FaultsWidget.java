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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Ajit Bhate
 */
public class FaultsWidget extends RoundedRectangleWidget {
    
    private static final Color TITLE_COLOR = new Color(255,153,102);
    private static final Color TITLE_COLOR2 = new Color(255,178,128);
    private static final Color BORDER_COLOR = new Color(255,138,76);
    private static final int GAP = 16;
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/fault.png"); // NOI18N   

    private WsdlOperation operation;
    private transient ImageLabelWidget headerLabelWidget;

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene
     * @param operation  
     */
    public FaultsWidget(Scene scene, WsdlOperation operation) {
        super(scene);
        this.operation = operation;
        setRadius(GAP);
        setBorderColor(BORDER_COLOR);
        setTitleColor(TITLE_COLOR,TITLE_COLOR2);
        createContent();
    }
    
    private void createContent() {
        if (operation==null) return;
        
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        headerLabelWidget = new ImageLabelWidget(getScene(), IMAGE, 
                NbBundle.getMessage(OperationWidget.class, "LBL_Faults"), 
                "To be implemented");
        addChild(0,headerLabelWidget);
        setTitleWidget(headerLabelWidget);

    }

}
