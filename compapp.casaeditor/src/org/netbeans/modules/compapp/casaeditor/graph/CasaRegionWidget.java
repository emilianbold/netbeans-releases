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
* CasaRegion.java
*
* Created on November 7, 2006, 4:38 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.openide.util.NbBundle;

/**
 *
 * @author jsandusky
 */
public class CasaRegionWidget extends LayerWidget {
    
    public static final int    MINIMUM_WIDTH = 20;
    
    private static final int    BORDER_WIDTH  = 1;
    
    private static final Color  LABEL_COLOR   = new Color(168, 168, 168);
    private static final Font   LABEL_FONT    = new Font("Dialog", Font.BOLD, 18);
    private static final int    LABEL_Y_POS   = 20;
    private static final String LABEL_TRUNCATED = NbBundle.getMessage(CasaRegionWidget.class, "LBL_Truncated");
    
    private static int mTruncatedStringWidth = -1;
    
    private LabelWidget mLabelWidget;
    private int mRegionPreferredWidth;
    private String mLabelText;
    private int mFullStringWidth = -1;
    
    private Widget mBorderWidget;
    
    
    /** Creates a new instance of CasaRegion */
    private CasaRegionWidget(final Scene scene, Color backgroundColor, String label, Color titleColor, Font titleFont) {
        super(scene);
        setBackground(backgroundColor);
        setOpaque(true);
        
        // Each region has a border line on the right side.
        mBorderWidget = new Widget(scene);
        mBorderWidget.setOpaque(true);
        mBorderWidget.setBackground(Color.LIGHT_GRAY);
        addChild(mBorderWidget);
        
        // Each region has a title label at the top.
        mLabelText = label;
        mLabelWidget = new LabelWidget(scene, label);
        mLabelWidget.setForeground(titleColor);
        mLabelWidget.setFont(titleFont);
        mLabelWidget.setPreferredLocation(new Point(20, LABEL_Y_POS));
        addChild(mLabelWidget);
        
        addDependency(new Widget.Dependency() {
            public void revalidateDependency() {
                if (scene.getGraphics() == null) {
                    return;
                }
                
                // Adjust the border line.
                updateBorder();
                
                // Calculate label string widths if necessary.
                if (mTruncatedStringWidth < 0) {
                    // cache the truncated width
                    FontMetrics metrics = scene.getGraphics().getFontMetrics(mLabelWidget.getFont());
                    mTruncatedStringWidth = (int) metrics.getStringBounds(
                            LABEL_TRUNCATED, 
                            scene.getGraphics()).getWidth();
                }
                if (mFullStringWidth < 0) {
                    // cache the label width
                    FontMetrics metrics = scene.getGraphics().getFontMetrics(mLabelWidget.getFont());
                    mFullStringWidth = (int) metrics.getStringBounds(
                            mLabelText, 
                            scene.getGraphics()).getWidth();
                }
                
                // Adjust the label position if necessary.
                if (mRegionPreferredWidth != CasaRegionWidget.this.getPreferredBounds().width) {
                    
                    mRegionPreferredWidth = CasaRegionWidget.this.getPreferredBounds().width;
                    
                    // Adjust the title label.
                    if (mRegionPreferredWidth < mFullStringWidth) {
                        if (!mLabelWidget.getLabel().equals(LABEL_TRUNCATED)) {
                            mLabelWidget.setLabel(LABEL_TRUNCATED);
                        }
                        mLabelWidget.setPreferredLocation(new Point(
                                (mRegionPreferredWidth - mTruncatedStringWidth) / 2, 
                                LABEL_Y_POS));
                    } else {
                        if (!mLabelWidget.getLabel().equals(mLabelText)) {
                            mLabelWidget.setLabel(mLabelText);
                        }
                        mLabelWidget.setPreferredLocation(new Point(
                                (mRegionPreferredWidth - mFullStringWidth) / 2, 
                                LABEL_Y_POS));
                    }
                }
            }
        });
    }
    
    
    private void updateBorder() {
        if (getPreferredBounds() != null) {
            mBorderWidget.setPreferredLocation(new Point(
                    getPreferredBounds().width - BORDER_WIDTH,
                    0));
            mBorderWidget.setPreferredBounds(new Rectangle(
                    BORDER_WIDTH,
                    getPreferredBounds().height));
        }
    }
    
    public int getLabelYOffset() {
        return mLabelWidget.getPreferredLocation().y + mLabelWidget.getPreferredBounds().height;
    }
    
    public void persistWidth() {
        CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
        CasaComponent component = (CasaComponent) scene.findObject(this);
        Rectangle bounds = getPreferredBounds();
        if (bounds == null) {
            bounds = getBounds();
        }
        CasaRegion region = (CasaRegion) component;
        if (region.getWidth() != bounds.width) {
            scene.getModel().setCasaRegionWidth(region, bounds.width);
        }
    }
    
    public void setCOLOR_REGION_TITLE(Color color) {
        mLabelWidget.setForeground(color);
    }
    
    public void setFONT_REGION_TITLE(Font font) {
        mLabelWidget.setFont(font);
    }

    public static CasaRegionWidget createBindingRegion(Scene scene) {
        return new CasaRegionWidget(
                scene, 
                CasaFactory.getCasaCustomizer().getCOLOR_REGION_BINDING(), 
                NbBundle.getMessage(CasaRegionWidget.class, "LBL_BindingRegion"),
                CasaFactory.getCasaCustomizer().getCOLOR_BC_REGION_TITLE(),
                CasaFactory.getCasaCustomizer().getFONT_BC_REGION_TITLE()
                );
    }
    
    public static CasaRegionWidget createEngineRegion(Scene scene) {
        return new CasaRegionWidget(
                scene, 
                CasaFactory.getCasaCustomizer().getCOLOR_REGION_ENGINE(), 
                NbBundle.getMessage(CasaRegionWidget.class, "LBL_EngineRegion"),
                CasaFactory.getCasaCustomizer().getCOLOR_SU_REGION_TITLE(),
                CasaFactory.getCasaCustomizer().getFONT_SU_REGION_TITLE()
                );
    }
    
    public static CasaRegionWidget createExternalRegion(Scene scene) {
        return new CasaRegionWidget(
                scene, 
                CasaFactory.getCasaCustomizer().getCOLOR_REGION_EXTERNAL(), 
                NbBundle.getMessage(CasaRegionWidget.class, "LBL_ExternalRegion"),
                CasaFactory.getCasaCustomizer().getCOLOR_EXT_SU_REGION_TITLE(),
                CasaFactory.getCasaCustomizer().getFONT_EXT_SU_REGION_TITLE()
                );
    }
}
