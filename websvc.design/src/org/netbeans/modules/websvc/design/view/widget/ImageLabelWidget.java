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
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit Bhate
 * @author anjeleevich
 */
public class ImageLabelWidget extends Widget {
    
    private ImageWidget imageWidget;
    private LabelWidget labelWidget;
    private LabelWidget commentWidget;
    
    public ImageLabelWidget(Scene scene, Image image, String text) {
        this(scene, image, text, null, DEFAULT_GAP);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image, String text,
            int hgap) {
        this(scene, image, text, null, hgap);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image,
            String text, String comment) {
        this(scene, image, text, comment, DEFAULT_GAP);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image,
            String label,
            String comment, int hgap) {
        super(scene);
        
        setLayout(LayoutFactory.createHorizontalFlowLayout(
                SerialAlignment.JUSTIFY, hgap));
        
        Font font = scene.getDefaultFont();
        setImage(image);
        setLabel(label);
        setComment(comment);
    }
    
    public boolean isPaintAsDisabled() {
        if(labelWidget!=null) {
            return labelWidget.isPaintAsDisabled();
        }
        if(imageWidget!=null) {
            return imageWidget.isPaintAsDisabled();
        }
        return false;
    }
    
    public void setPaintAsDisabled(boolean flag) {
        if(labelWidget!=null) {
            labelWidget.setPaintAsDisabled(flag);
        }
        if(imageWidget!=null) {
            imageWidget.setPaintAsDisabled(flag);
        }
    }
    
    public void setLabelForeground(Color forground) {
        if(labelWidget!=null) {
            labelWidget.setForeground(forground);
        }
        if(commentWidget!=null) {
            commentWidget.setForeground(forground);
        }
    }
    
    public void setLabel(String label) {
        if(label==null) {
            if (labelWidget!=null) {
                labelWidget.removeFromParent();
                labelWidget = null;
            }
        } else {
            if(labelWidget==null) {
                labelWidget = new LabelWidget(getScene(),label);
                labelWidget.setFont(getScene().getFont());
            } else {
                labelWidget.setLabel(label);
            }
            if(labelWidget.getParentWidget()!=this) {
                labelWidget.removeFromParent();
                addChild(labelWidget);
            }
        }
    }
    
    public LabelWidget getLabelWidget() {
        return labelWidget;
    }
    
    public LabelWidget getCommentWidget() {
        return commentWidget;
    }
    
    public void setImage(Image image) {
        if(image==null) {
            if (imageWidget!=null) {
                imageWidget.removeFromParent();
                imageWidget = null;
            }
        } else {
            if(imageWidget==null) {
                imageWidget = new ImageWidget(getScene(),image);
            } else {
                imageWidget.setImage(image);
            }
            if(imageWidget.getParentWidget()!=this) {
                imageWidget.removeFromParent();
                addChild(imageWidget);
            }
        }
    }
    
    public void setComment(String comment) {
        if(comment==null) {
            if (commentWidget!=null) {
                commentWidget.removeFromParent();
                commentWidget = null;
            }
        } else {
            if(commentWidget==null) {
                commentWidget = new LabelWidget(getScene(),comment);
                commentWidget.setFont(getScene().getFont());
                commentWidget.setPaintAsDisabled(true);
            } else {
                commentWidget.setLabel(comment);
            }
            if(commentWidget.getParentWidget()!=this) {
                commentWidget.removeFromParent();
                addChild(commentWidget);
            }
        }
    }
    
    public String getLabel() {
        return labelWidget==null?null:labelWidget.getLabel();
    }
    
    public Image getImage() {
        return imageWidget==null?null:imageWidget.getImage();
    }
    
    public String getComment() {
        return commentWidget==null?null:commentWidget.getLabel();
    }
    
    public static final Color COMMENT_COLOR = new Color(0x666666);
    public static final int DEFAULT_GAP = 4;
}
