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
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.xml.soap.SOAPMessage;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.Utils;
import org.netbeans.modules.websvc.design.view.layout.BorderLayout;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit
 */
public class SampleMessageWidget extends AbstractTitledWidget {
    
    private static final Image INPUT_IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/input.png"); // NOI18N   
    private static final Image OUTPUT_IMAGE  = Utilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/output.png"); // NOI18N   
    private static final Color INPUT_COLOR = new Color(128,128,255);
    private static final Color OUTPUT_COLOR = new Color(102,204,102);

    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient EditorPaneWidget paneWidget;

    private MethodModel operation;
    private Type type;

    /**
     * Enum for type of messages.
     */
    public enum Type {
        /**
         * Represents the input message.
         */
        INPUT {
            public Image getIcon() {
                return INPUT_IMAGE;
            }
            public String getDescription() {
                return NbBundle.getMessage(SampleMessageWidget.class, "Hint_SampleInput");
            }
            String getTitle() {
                return NbBundle.getMessage(SampleMessageWidget.class, "TITLE_SampleInput");
            }
            Color getBorderColor() {
                return INPUT_COLOR;
            }
            SOAPMessage getMessage(MethodModel operation) {
                return operation.getSoapRequest();
            };
        },
        /**
         * Represents the output message.
         */
        OUTPUT {
            public Image getIcon() {
                return OUTPUT_IMAGE;
            }
            public String getDescription() {
                return NbBundle.getMessage(SampleMessageWidget.class, "Hint_SampleOutput");
            }
            String getTitle() {
                return NbBundle.getMessage(SampleMessageWidget.class, "TITLE_SampleOutput");
            }
            Color getBorderColor() {
                return OUTPUT_COLOR;
            }
            SOAPMessage getMessage(MethodModel operation) {
                return operation.getSoapResponse();
            };
        };

        /**
         * 
         * @return image the icon for this type of widget
         */
        public abstract Image getIcon();
        
        /**
         * 
         * @return image the icon for this type of widget
         */
        public abstract String getDescription();

        abstract String getTitle();
        
        abstract Color getBorderColor();

        abstract SOAPMessage getMessage(MethodModel operation);
    }

    /** 
     * Creates a new instance of SampleMessageWidget 
     * @param scene 
     * @param operation 
     * @param type 
     */
    public SampleMessageWidget(ObjectScene scene, MethodModel operation, Type type) {
        super(scene,0,TITLE_COLOR);
        this.operation = operation;
        this.type = type;


        headerLabelWidget = new ImageLabelWidget(scene, null, operation.getOperationName()+": ",type.getTitle());
        headerLabelWidget.getLabelWidget().setFont(scene.getFont().deriveFont(Font.BOLD));
        headerLabelWidget.getCommentWidget().setPaintAsDisabled(false);
        headerLabelWidget.setLabelForeground(type.getBorderColor());
        BorderLayout.addLayoutComponent(getHeaderWidget(), headerLabelWidget, BorderLayout.Constraint.CENTER);
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        BorderLayout.addLayoutComponent(getHeaderWidget(), buttons, BorderLayout.Constraint.EAST);
        final ButtonWidget closeButton = new ButtonWidget(getScene(), "x");
        closeButton.setLabelForeground(new Color(255,95,95));
        closeButton.getLabelWidget().setFont(scene.getFont().deriveFont(Font.BOLD));
        closeButton.setRoundedBorder(0, 4, 0,type.getBorderColor());
        closeButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                SampleMessageWidget.this.removeFromParent();
            }
        });
        buttons.addChild(closeButton);

        paneWidget = new EditorPaneWidget(scene, 
                Utils.getFormatedDocument(type.getMessage(operation)),"text/xml");
        paneWidget.setEditable(false);
        getContentWidget().addChild(paneWidget);
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(12, 6));
    }
}
