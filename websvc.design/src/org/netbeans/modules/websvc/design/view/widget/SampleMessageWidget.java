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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import javax.swing.AbstractAction;
import javax.xml.soap.SOAPMessage;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit
 */
public class SampleMessageWidget extends AbstractTitledWidget {
    
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
            public ButtonImageWidget getIcon(Scene scene) {
                return new ButtonImageWidget(scene,16,getBorderColor(),true);
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
            public ButtonImageWidget getIcon(Scene scene) {
                return new ButtonImageWidget(scene,16,getBorderColor(),false);
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
         * @return InputImageWidget the image widget for this type of widget
         */
        public abstract ButtonImageWidget getIcon(Scene scene);
        
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
        super(scene,0,12,0,TITLE_COLOR);
        this.operation = operation;
        this.type = type;

        headerLabelWidget = new ImageLabelWidget(scene, null, operation.getOperationName()+": ",type.getTitle());
        headerLabelWidget.setLabelFont(scene.getFont().deriveFont(Font.BOLD));
        headerLabelWidget.setPaintAsDisabled(false);
        headerLabelWidget.setLabelForeground(type.getBorderColor());
        getHeaderWidget().addChild(new Widget(getScene()),1);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 8));
        getHeaderWidget().addChild(buttons);
        final ButtonWidget closeButton = new ButtonWidget(getScene(), (String)null);
        closeButton.setImage(new ExpanderImageWidget(scene,type.getBorderColor(),8));
        closeButton.setRoundedBorder(0, 4, 4,type.getBorderColor());
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

    private static class ExpanderImageWidget extends ImageLabelWidget.PaintableImageWidget {
        private static final Stroke STROKE = new BasicStroke(2.5F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        public ExpanderImageWidget(Scene scene, Color color, int size) {
            super(scene,color,size,size);
        }

        protected Stroke getImageStroke() {
            return STROKE;
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path;
            path = new GeneralPath();
            path.moveTo(0, 0);
            path.lineTo(width, height);
            path.moveTo(0, height);
            path.lineTo(width, 0);
            return path;
        }
    }

    private static class ButtonImageWidget extends ImageLabelWidget.PaintableImageWidget {
        private boolean isInput;
        public ButtonImageWidget(Scene scene, int size, Color color, boolean input) {
            super(scene, color, size, size);
            this.isInput = input;
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            int arrowWidth = height/3;
            float x1 = isInput?0:width;
            float x2 = isInput?3*width/4:width/4;
            float gap = (width-arrowWidth)/2;
            path.moveTo(x1, gap);
            path.lineTo(width/2, gap);
            path.lineTo(width/2, height/2-arrowWidth);
            path.lineTo(x2, height/2);
            path.lineTo(width/2, height/2+arrowWidth);
            path.lineTo(width/2, gap+arrowWidth);
            path.lineTo(x1, gap+arrowWidth);
            path.closePath();
            path.moveTo(width/2, height/8-1);
            path.lineTo(width-x1, height/8-1);
            path.moveTo(width/2, 7*height/8+1);
            path.lineTo(width-x1, 7*height/8+1);
            path.moveTo(x2, height/4);
            path.lineTo(width-x1, height/4);
            path.moveTo(x2, 3*height/4);
            path.lineTo(width-x1, 3*height/4);
            path.moveTo(isInput?7*width/8:width/8, height/2);
            path.lineTo(width-x1, height/2);
            return path;
        }

        protected void paintWidget() {
            Rectangle bounds = getImage().getBounds();
            Graphics2D gr = getGraphics();
            Paint oldPaint = gr.getPaint();
            gr.setPaint(new GradientPaint(0,bounds.y+(bounds.height/6),getForeground().brighter().brighter(),0,bounds.y+(bounds.height/2),getForeground().brighter(),true));
            gr.fill(getImage());
            gr.setPaint(oldPaint);
            super.paintWidget();
        }
    }
}
