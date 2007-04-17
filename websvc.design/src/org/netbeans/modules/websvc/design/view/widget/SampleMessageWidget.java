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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.xml.soap.SOAPMessage;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.Utils;

/**
 *
 * @author Ajit
 */
public class SampleMessageWidget extends AbstractTitledWidget {
    
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient EditorPaneWidget paneWidget;

    private SOAPMessage message;

    /** 
     * Creates a new instance of SampleMessageWidget 
     * @param scene 
     * @param message 
     * @param title 
     */
    public SampleMessageWidget(Scene scene, SOAPMessage message, String title) {
        super(scene,10,Color.GRAY);
        this.message = message;
        setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 10));
        headerLabelWidget = new ImageLabelWidget(scene, null, title);
        getHeaderWidget().addChild(headerLabelWidget);
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        getHeaderWidget().addChild(buttons);
        final ButtonWidget inputMessage = new ButtonWidget(getScene(), "X");
        inputMessage.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                SampleMessageWidget.this.removeFromParent();
            }
        });
        buttons.addChild(inputMessage);
        paneWidget = new EditorPaneWidget(scene, Utils.getFormatedDocument(message));
        paneWidget.setEditable(false);
        getContentWidget().addChild(paneWidget);
    }
    
}
