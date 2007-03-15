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

    public ImageLabelWidget(Scene scene, Image image, String text) {
        this(scene, image, text, null, DEFAULT_GAP);
    }
    

    public ImageLabelWidget(Scene scene, Image image, String text, 
            int hgap) 
    {
        this(scene, image, text, null, hgap);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image, 
            String text, String comment) 
    {
        this(scene, image, text, comment, DEFAULT_GAP);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image, 
            String text, 
            String comment, int hgap) 
    {
        super(scene);
        
        setLayout(LayoutFactory.createHorizontalFlowLayout(
                SerialAlignment.CENTER, hgap));
        
        Font font = scene.getDefaultFont();
        
        Widget imageWidget = new ImageWidget(scene, image);
        
        Widget textWidget = new LabelWidget(scene, text);
        textWidget.setFont(font);
        
        addChild(imageWidget);
        addChild(textWidget);
        
        if (comment != null) {
            Widget commentWidget = new LabelWidget(scene, comment);
            commentWidget.setFont(font);
            commentWidget.setForeground(COMMENT_COLOR);
            addChild(commentWidget);
        }
    }
    
    public static final Color COMMENT_COLOR = new Color(0x666666);
    public static final int DEFAULT_GAP = 4;
}
