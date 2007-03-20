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
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.websvc.design.view.layout.LeftRightLayout;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
Opeit Bhate
 */
public class ParametersWidget extends RoundedRectangleWidget implements ExpandableWidget{
    
    private static final Color TITLE_COLOR = new Color(153,153,255);
    private static final Color TITLE_COLOR2 = new Color(178,178,255);
    private static final Color BORDER_COLOR = new Color(128,128,255);
    private static final int GAP = 16;
    private static final Image IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/input.png"); // NOI18N   

    private transient Input input;

    private transient Widget contentWidget;
    private transient HeaderWidget headerWidget;
    private transient Widget buttons;
    private transient ExpanderWidget expander;
    private transient ImageLabelWidget headerLabelWidget;

    
    
    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param operation 
     */
    public ParametersWidget(Scene scene, Input input) {
        super(scene);
        this.input = input;
        setRadius(GAP);
        setBorderColor(BORDER_COLOR);
        setTitleColor(TITLE_COLOR,TITLE_COLOR2);
        createContent();
    }
    
    private void createContent() {
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        boolean expanded = ExpanderWidget.isExpanded(this, true);
        expander = new ExpanderWidget(getScene(), this, expanded);

        headerWidget = new HeaderWidget(getScene(), expander);
        headerWidget.setLayout(new LeftRightLayout(32));

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));

        buttons.addChild(expander);

        headerWidget.addChild(buttons);

        addChild(headerWidget);
        setTitleWidget(headerWidget);
        
        contentWidget = new Widget(getScene());
        contentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, GAP));

        int noOfParams = 0;
        Message message = null;
        if(input!=null && input.getMessage()!=null && ((message=input.getMessage().get())!=null)) {
            noOfParams = message.getParts().size();
            for(Part part:message.getParts()) {
                String parameter = (part.getElement()!=null?""+part.getElement().getQName():
                    part.getType()!=null?""+part.getType().getQName():"");
                contentWidget.addChild(new LabelWidget(getScene(),parameter));
            }
        } else {
            contentWidget.addChild(new LabelWidget(getScene(),
                    NbBundle.getMessage(OperationWidget.class, "LBL_InputNone")));
        }
        headerLabelWidget = new ImageLabelWidget(getScene(), IMAGE, 
                NbBundle.getMessage(OperationWidget.class, "LBL_Input"), 
                "("+noOfParams+")");
        headerWidget.addChild(0,headerLabelWidget);

        
        if(expanded) {
            expandWidget();
        } else {
            collapseWidget();
        }
    }

    public void collapseWidget() {
        if(contentWidget.getParentWidget()!=null) {
            removeChild(contentWidget);
            repaint();
        }
    }

    public void expandWidget() {
        if(contentWidget.getParentWidget()==null) {
            addChild(contentWidget);
        }
    }

    public Object hashKey() {
        return input==null?null:input.getName()+"_Parameters";
    }
    

}
