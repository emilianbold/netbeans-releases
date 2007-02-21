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

/*
 * CasaEnginePinWidget.java
 *
 * Created on November 17, 2006, 12:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Image;
import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities.Directions;

/**
 *
 * @author rdara
 */
public class CasaPinWidgetEngine extends CasaPinWidget {
    
    private static final int LABEL_MAX_CHAR = 48;
    
    private LabelWidget mNameWidget;
    private Image mCurrentOriginalImage;
    private Image mCurrentDisplayedImage;
    private ImageWidget mCurrentImageWidget;
    private ImageWidget mArrowImageWidget;
    private boolean mIsMinimized;
    private boolean mIsSelected;
    
    private ImageWidget mEmptyWidget;
    
    
    public CasaPinWidgetEngine(Scene scene, Image image) {
        super(scene);
        
        mCurrentOriginalImage = image;
        mCurrentDisplayedImage = image;
        
        mArrowImageWidget = new ImageWidget(scene);
        mArrowImageWidget.setImage(image);
        
        mEmptyWidget = new ImageWidget(scene);
        mEmptyWidget.setPreferredBounds(mArrowImageWidget.getPreferredBounds());
                
        mNameWidget = new LabelWidget(scene);
        mNameWidget.setOpaque(false);
        
        switch(getDirection()) {
            case LEFT :
                setLayout(LayoutFactory.createHorizontalLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));
                addChild(mArrowImageWidget);
                addChild(mNameWidget);
                addChild(mEmptyWidget);
                break;
            case RIGHT :
                setLayout(RegionUtilities.createHorizontalLayoutWithJustifications(LayoutFactory.SerialAlignment.RIGHT_BOTTOM, 5));
                addChild(mEmptyWidget);
                addChild(mNameWidget);
                addChild(mArrowImageWidget);
                break;
        }
    }
    
    protected void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
        updateImage();
    }
    
    private void updateImage() {
        if (mIsSelected) {
            mCurrentDisplayedImage = createSelectedPinImage(mCurrentOriginalImage);
        } else {
            mCurrentDisplayedImage = mCurrentOriginalImage;
        }
        if (mIsMinimized) {
            mArrowImageWidget.setImage(null);
        } else {
            mArrowImageWidget.setImage(mCurrentDisplayedImage);
        }
    }
    
    protected void setPinName(String name) {
        mArrowImageWidget.setToolTipText(name);
        String displayedText = name;
        if (displayedText.length() > LABEL_MAX_CHAR) {
            displayedText = displayedText.substring(0, LABEL_MAX_CHAR) + "...";
        }
        mNameWidget.setLabel(displayedText);

        Widget widget = getParentWidget();
        while(!(widget instanceof CasaNodeWidgetEngine)) {
            widget = widget.getParentWidget();
        }
        if (getBounds() != null) {
            ((CasaNodeWidgetEngine) widget).readjustBounds();
        }
    }
    
    protected Directions getDirection() {
        return Directions.LEFT;
    }
    
    public Anchor getAnchor() {
        return RegionUtilities.createFixedDirectionalAnchor(this, getDirection(), 0);
    }
    
    protected void setMinimized(boolean isMinimized) {
        mIsMinimized = isMinimized;
        Rectangle rectangle = isMinimized ? new Rectangle() : null;
        mNameWidget.setPreferredBounds(rectangle);
        mArrowImageWidget.setPreferredBounds(rectangle);
        this.setPreferredBounds(rectangle);
        updateImage();
    }
    
    protected void readjustBounds(Rectangle rectangle) {
        mNameWidget.setPreferredBounds(rectangle);
        mArrowImageWidget.setPreferredBounds(rectangle);
        this.setPreferredBounds(rectangle);
    }
}
