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
import org.netbeans.modules.xml.wsdl.model.Output;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Ajit Bhate
 */
public class OutputWidget extends AbstractTitledWidget {
    
    private static final Color TITLE_COLOR = new Color(126,204,126);
    private static final Color TITLE_COLOR2 = new Color(153,204,153);
    private static final Color BORDER_COLOR = new Color(102,204,102);
    private static final int GAP = 16;
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/output.png"); // NOI18N   

    private Output output;
    private transient ImageLabelWidget headerLabelWidget;

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param output  
     */
    public OutputWidget(Scene scene, Output output) {
        super(scene,GAP,BORDER_COLOR);
        this.output = output;
        createContent();
    }
    
    private void createContent() {
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));
        getHeaderWidget().setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        headerLabelWidget = new ImageLabelWidget(getScene(), IMAGE, output==null?
                NbBundle.getMessage(OperationWidget.class, "LBL_OutputNone"): 
                NbBundle.getMessage(OperationWidget.class, "LBL_Output", output.getName()), 
                null);
       getHeaderWidget().addChild(headerLabelWidget);
    }

    protected boolean isExpandable() {
        return false;
    }
}
