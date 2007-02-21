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
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author rdara
 */
public class CasaPinWidgetBinding extends CasaPinWidget {
    
    private Image mOriginalImage;
    private ImageWidget mImageWidget;
    
    public CasaPinWidgetBinding(Scene scene, Image arrowImage) {
        super(scene);
        mOriginalImage = arrowImage;
        mImageWidget = new ImageWidget(scene);
        mImageWidget.setImage(arrowImage);
        addChild(mImageWidget);
    }
    
    
    protected void setSelected(boolean isSelected) {
        if (isSelected) {
            mImageWidget.setImage(createSelectedPinImage(mOriginalImage));
        } else {
            mImageWidget.setImage(mOriginalImage);
        }
    }
    
    protected Rectangle getPinWidgetBounds() {
        return mImageWidget.getPreferredBounds();
    }
    
    protected void setPinName(String name) {
        // do nothing
    }
    
    protected void setMinimized(boolean isMinimized) {
        // do nothing, cannot minimize this widget
    }
}
