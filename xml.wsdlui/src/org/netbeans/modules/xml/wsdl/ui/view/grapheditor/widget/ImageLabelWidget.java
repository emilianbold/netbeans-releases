/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

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
        Font labelFont = font.deriveFont(Font.BOLD);
        
        Widget imageWidget = new ImageWidget(scene, image);
        
        textWidget = new LabelWidget(scene, text);
        textWidget.setFont(labelFont);
        
        addChild(imageWidget);
        addChild(textWidget);
        
        if (comment != null) {
            commentWidget = new LabelWidget(scene, comment);
            commentWidget.setFont(font);
            commentWidget.setForeground(COMMENT_COLOR);
            addChild(commentWidget);
        }
    }
    
    public String getLabel() {
        return textWidget.getLabel();
    }
    
    public void setLabel(String label) {
        textWidget.setLabel(label);
    }
    public String getComment() {
        if (commentWidget != null)
            return commentWidget.getLabel();
        return null;
    }
    
    public void setComment(String comment) {
        if (commentWidget != null)
            commentWidget.setLabel(comment);
    }
    
    
    private LabelWidget textWidget;
    private LabelWidget commentWidget;
    
    public static final Color COMMENT_COLOR = new Color(0x666666);
    public static final int DEFAULT_GAP = 4;
}
