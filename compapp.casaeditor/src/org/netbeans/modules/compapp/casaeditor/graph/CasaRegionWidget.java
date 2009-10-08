/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.awt.InnerGlowBorderDrawer;
import org.openide.util.NbBundle;

/**
 *
 * @author jsandusky
 */
public class CasaRegionWidget extends LayerWidget {
    
    public static final int    MINIMUM_WIDTH = 20;
    
    private static final int    BORDER_WIDTH  = 1;
    
    private static final int    TITLE_Y_POS   = 20;
    
    private static final Color  BANNER_COLOR  = Color.GRAY;
    private static final Font   BANNER_FONT   = new Font("SansSerif", Font.BOLD, 14);  // NOI18N
    
    private static final String LABEL_TRUNCATED = NbBundle.getMessage(CasaRegionWidget.class, "LBL_Truncated"); // NOI18N
    
    private static int mTruncatedStringWidth = -1;
    
    private Dimension mRegionPreferredSize = new Dimension();
    private boolean mIsHighlighted;
    private Widget mBorderWidget;
    private LabelWidget mTitleWidget;
    private String mTitleText;
    private int mFullTitleStringWidth = -1;
    private LineBreakingLabelWidget mBannerWidget;
    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    
    
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
        mTitleText = label;
        mTitleWidget = new LabelWidget(scene, label);
        mTitleWidget.setForeground(titleColor);
        mTitleWidget.setFont(titleFont);
        mTitleWidget.setPreferredLocation(new Point(20, TITLE_Y_POS));
        addChild(mTitleWidget);
    }
    
    
    protected void notifyAdded() {
        mDependenciesRegistry.registerDependency(new Widget.Dependency() {
            public void revalidateDependency() {
                
                Scene scene = getScene();
                if (scene.getGraphics() == null) {
                    return;
                }
                
                // Adjust the border line.
                updateBorder();
                
                // Calculate label string widths if necessary.
                if (mTruncatedStringWidth < 0) {
                    // cache the truncated width
                    FontMetrics metrics = scene.getGraphics().getFontMetrics(mTitleWidget.getFont());
                    mTruncatedStringWidth = (int) metrics.getStringBounds(
                            LABEL_TRUNCATED, 
                            scene.getGraphics()).getWidth();
                }
                if (mFullTitleStringWidth < 0) {
                    // cache the title text width
                    FontMetrics metrics = scene.getGraphics().getFontMetrics(mTitleWidget.getFont());
                    mFullTitleStringWidth = (int) metrics.getStringBounds(
                            mTitleText, 
                            scene.getGraphics()).getWidth();
                }
                
                // Adjust the label positions if necessary.
                Dimension currentSize = CasaRegionWidget.this.getPreferredBounds().getSize();
                if (!mRegionPreferredSize.equals(currentSize)) {
                    mRegionPreferredSize = currentSize;
                    
                    if (mRegionPreferredSize.width < mFullTitleStringWidth) {
                        updateTitleLabel(mTitleWidget, LABEL_TRUNCATED, mTruncatedStringWidth, TITLE_Y_POS);
                    } else {
                        updateTitleLabel(mTitleWidget, mTitleText, mFullTitleStringWidth, TITLE_Y_POS);
                    }
                    
                    if (mBannerWidget != null) {
                        updateBannerLabel();
                    }
                }
            }
        });
    }
    
    protected void notifyRemoved() {
        mDependenciesRegistry.removeAllDependencies();
    }
    
    private void updateBannerLabel() {
        int height = CasaRegionWidget.this.getPreferredBounds().height - getTitleYOffset();
        mBannerWidget.setPreferredBounds(new Rectangle(
                0,
                0,
                CasaRegionWidget.this.getPreferredBounds().width,
                height));
        // set the preferred location AFTER the bounds are set, because only 
        // the location change causes the banner widget dependency to trigger -
        // and we want the dependency to trigger after the bounds are set.
        mBannerWidget.setPreferredLocation(new Point(0, height / 2));
    }
    
    private void updateTitleLabel(LabelWidget widget, String text, int stringWidth, int yPos) {
        if (!widget.getLabel().equals(text)) {
            widget.setLabel(text);
        }
        widget.setPreferredLocation(new Point(
                (mRegionPreferredSize.width - stringWidth) / 2,
                yPos));
    }
    
    private void updateBorder() {
        if (getPreferredBounds() != null) {
            mBorderWidget.setPreferredBounds(new Rectangle(
                    BORDER_WIDTH,
                    getPreferredBounds().height));
            mBorderWidget.setPreferredLocation(new Point(
                    getPreferredBounds().width - BORDER_WIDTH,
                    0));
        }
    }
    
    public boolean hasBanner() {
        return mBannerWidget != null;
    }
    
    public void setBanner(String bannerText) {
        if (bannerText == null) {
            removeChild(mBannerWidget);
            mBannerWidget = null;
            getScene().validate();
        } else {
            mBannerWidget = new LineBreakingLabelWidget(getScene());
            mBannerWidget.setText(bannerText, (Color) getBackground(), BANNER_FONT);
            addChild(mBannerWidget);
            getScene().validate();
            updateBannerLabel();
            mBannerWidget.animateVisible(BANNER_COLOR);
        }
        getScene().repaint();
    }
    
    public int getTitleYOffset() {
        return mTitleWidget.getPreferredLocation().y + mTitleWidget.getBounds().height;
    }
    
    public void persistWidth() {
        CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
        scene.persistWidth(this);
    }
    
    public void setCOLOR_REGION_TITLE(Color color) {
        mTitleWidget.setForeground(color);
    }
    
    public void setFONT_REGION_TITLE(Font font) {
        mTitleWidget.setFont(font);
    }

    public void setHighlighted(boolean isHighlighted) {
        if (mIsHighlighted != isHighlighted) {
            mIsHighlighted = isHighlighted;
            repaint();
        }
    }
    
    
    public static CasaRegionWidget createBindingRegion(Scene scene) {
        return new CasaRegionWidget(
                scene, 
                CasaFactory.getCasaCustomizer().getCOLOR_REGION_BINDING(), 
                NbBundle.getMessage(CasaRegionWidget.class, "LBL_BindingRegion"),   // NOI18N
                CasaFactory.getCasaCustomizer().getCOLOR_BC_REGION_TITLE(),
                CasaFactory.getCasaCustomizer().getFONT_BC_REGION_TITLE()
                );
    }
    
    public static CasaRegionWidget createEngineRegion(Scene scene) {
        return new CasaRegionWidget(
                scene, 
                CasaFactory.getCasaCustomizer().getCOLOR_REGION_ENGINE(), 
                NbBundle.getMessage(CasaRegionWidget.class, "LBL_EngineRegion"),    // NOI18N
                CasaFactory.getCasaCustomizer().getCOLOR_SU_REGION_TITLE(),
                CasaFactory.getCasaCustomizer().getFONT_SU_REGION_TITLE()
                );
    }
    
    public static CasaRegionWidget createExternalRegion(Scene scene) {
        return new CasaRegionWidget(
                scene, 
                CasaFactory.getCasaCustomizer().getCOLOR_REGION_EXTERNAL(), 
                NbBundle.getMessage(CasaRegionWidget.class, "LBL_ExternalRegion"),  // NOI18N
                CasaFactory.getCasaCustomizer().getCOLOR_EXT_SU_REGION_TITLE(),
                CasaFactory.getCasaCustomizer().getFONT_EXT_SU_REGION_TITLE()
                );
    }
    
    
    protected void paintWidget() {
        super.paintWidget();
        
        if (mIsHighlighted && getScene().getView() != null) {
            
            // If our bounds is larger than the visible rect, our border
            // will be limited to the visible rect.
            Rectangle rect = getScene().getView().getVisibleRect();
            rect = rect.intersection(new Rectangle(
                    getLocation(),
                    getBounds().getSize()));
            rect.x -= getLocation().x;
            rect.y -= getLocation().y;
            
            InnerGlowBorderDrawer.paintInnerGlowBorder(
                    getGraphics(), 
                    rect, 
                    CasaFactory.getCasaCustomizer().getCOLOR_SELECTION(), 
                    0.3f,
                    15);
        }
    }
}
