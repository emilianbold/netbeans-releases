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
 * AutoSizingPanel.java
 *
 * Created on May 27, 2006, 12:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.Autoscroll;
import javax.swing.JPanel;

/**
 *
 * @author girix
 */
public class AutoSizingPanel extends ABEBaseDropPanel{
    private static final long serialVersionUID = 7526472295622776147L;

    private boolean horizontalScaling = false;
    private boolean verticalScaling = false;
    private boolean fixedHeight = false;
    private boolean fixedWidth = false;
    private int fixedPanelHeight;
    private int fixedPanelWidth;
    
    private int interComponentSpacing = 0;
    
    public AutoSizingPanel(InstanceUIContext context){
        super(context);
    }
    
    public Dimension getPreferredSize() {
        int width = 0;
        int height = 0;
        for(Component child: this.getComponents()){
            Dimension dim = child.getPreferredSize();
            int curW = dim.width;
            int curH = dim.height;
            if(horizontalScaling)
                width += curW + getInterComponentSpacing();
            else
                width = width < curW ? curW : width;
            
            if(verticalScaling)
                height += curH + getInterComponentSpacing();
            else
                height = height < curH ? curH : height;
        }

        if(isFixedHeight()){
            height = getFixedPanelHeight();
        }
        if(isFixedWidth()){
            width = getFixedPanelWidth();
        }
        
        return new Dimension(width, height);
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public boolean isHorizontalScaling() {
        return horizontalScaling;
    }
    
    public void setHorizontalScaling(boolean horizontalScaling) {
        this.horizontalScaling = horizontalScaling;
    }
    
    public boolean isVerticalScaling() {
        return verticalScaling;
    }
    
    public void setVerticalScaling(boolean verticalScaling) {
        this.verticalScaling = verticalScaling;
    }

    public boolean isFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(boolean fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    public boolean isFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(boolean fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public int getFixedPanelHeight() {
        return fixedPanelHeight;
    }

    public void setFixedPanelHeight(int fixedPanelHeight) {
        this.fixedPanelHeight = fixedPanelHeight;
    }

    public int getFixedPanelWidth() {
        return fixedPanelWidth;
    }

    public void setFixedPanelWidth(int fixedPanelWidth) {
        this.fixedPanelWidth = fixedPanelWidth;
    }

    public int getInterComponentSpacing() {
        return interComponentSpacing;
    }

    public void setInterComponentSpacing(int interComponentSpacing) {
        this.interComponentSpacing = interComponentSpacing;
    }

    public void accept(UIVisitor visitor) {
        //noop
    }
    
    
    
}
