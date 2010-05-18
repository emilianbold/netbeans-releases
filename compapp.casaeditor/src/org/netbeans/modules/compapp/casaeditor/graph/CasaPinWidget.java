/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RescaleOp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.openide.util.Utilities;

/**
 * This class represents a pin widget in the VMD plug-in.
 *
 * @author David Kaspar
 * @author Jun Qian
 */
public abstract class CasaPinWidget extends ErrableWidget {
    
    public enum AnchorScheme {
        HORIZONTAL, LEFT, RIGHT, VERTICAL, TOP, BOTTOM
    };
    
    protected ImageWidget mImageWidget;
    
    // How much lighter/darker to make a highlighted version of the image.
    // value < 1 is darker, value > 1 is lighter
    private static final float HIGHLIGHT_LIGHT_FACTOR = 0.6f;
    
    private boolean mIsHighlighted;
    private Image mUnHighlightedImage;
    private GrayFilter mPinGrayFilter = new GrayFilter(true, 20);
    
    private Image mPinImage;
    private Image mClassicPinImage;
    
    /**
     * Creates a pin widget.
     * @param scene the scene
     */
    public CasaPinWidget(Scene scene, Image pinImage, Image classicPinImage) {
        super (scene);
        
        mPinImage = pinImage;
        mClassicPinImage = classicPinImage;
        
        mImageWidget = new ImageWidget(scene, getPinImage());
        
        setOpaque(false);
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }
        
    public void updatePinImage() {
        mImageWidget.setImage(getPinImage());
    }
    
    protected Image getPinImage() {
        return CasaFactory.getCasaCustomizer().getBOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE() ? 
            mClassicPinImage : mPinImage;
    }
    
    protected boolean hasPreferredLocation() {
        return false;
    }    
    
    protected int getErrorBadgeDeltaX() {
        return getBounds().width + 2;
    }
    
    protected int getErrorBadgeDeltaY() {
        return 2;
    }
    
    protected abstract void setPinName(String name);
        
    protected void setSelected(boolean isSelected) {
        Image originalImage = getPinImage();
        
        if (isSelected) {
            mImageWidget.setImage(createSelectedPinImage(originalImage));
        } else {
            mImageWidget.setImage(originalImage);
        }
    }
        
    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if ((!previousState.isSelected() && state.isSelected()) ||
            (!previousState.isFocused() && state.isFocused())   ) {
            setSelected(true);
        } else if ((previousState.isSelected() && !state.isSelected())||
                   (previousState.isFocused() && !state.isFocused())){
            setSelected(false);
        }
        
        if (!previousState.isHovered() && state.isHovered()) {
            hoverConnections();
        } else if (previousState.isHovered() && !state.isHovered()) {
            CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
            scene.setHighlightedObjects(Collections.emptySet());
        }
    }

    public Set<CasaComponent> getConnections() {
        CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
        CasaEndpointRef pinObject = (CasaEndpointRef) scene.findObject(this);
        Collection<CasaComponent> edges = scene.getEdges();
        Set<CasaComponent> pinConnections = new HashSet<CasaComponent>();
        for (CasaComponent edge : edges) {
            CasaComponent source = scene.getEdgeSource(edge);
            CasaComponent target = scene.getEdgeTarget(edge);
            if (
                    source == pinObject ||
                    target == pinObject) {
                pinConnections.add(edge);
            }
        }
        
        return pinConnections;
    } 
    
    private void hoverConnections() {
        CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
        Set<CasaComponent> pinConnections = getConnections();
        scene.setHighlightedObjects(pinConnections);
    }
    
    /**
     * Sets all pin properties at once.
     * @param name the pin name
     * @param glyphs the pin glyphs
     */
    public void setProperties(String name) {
        setPinName(name);
    }
    
    public Anchor getAnchor() {
        return AnchorFactory.createDirectionalAnchor (
                    this, 
                    AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 
                    0);
    }
    
    protected Image createSelectedPinImage(Image pinImage) {
        ImageProducer prod = new FilteredImageSource(
                pinImage.getSource(), 
                mPinGrayFilter);
        Image selectedImage = Toolkit.getDefaultToolkit().createImage(prod);
        return selectedImage;
    }
    
    public void setHighlighted(boolean isHighlighted) {
        if (mIsHighlighted != isHighlighted) {
            mIsHighlighted = isHighlighted;
            if (isHighlighted) {
                mUnHighlightedImage = mImageWidget.getImage();
                if (mUnHighlightedImage != null) {
                    mImageWidget.setImage(createHighlightedImage(mUnHighlightedImage));
                }
            } else {
                if (mUnHighlightedImage != null) {
                    mImageWidget.setImage(mUnHighlightedImage);
                }
            }
        }
    }
    
    private static Image createHighlightedImage(Image src) {
        RescaleOp convolution = new RescaleOp(HIGHLIGHT_LIGHT_FACTOR, 0, null);
        BufferedImage bs = toBufferedImage(src);
        return convolution.filter(bs, bs);
    }
    
    private static final BufferedImage toBufferedImage(Image img) {
        // load the image
        new ImageIcon(img);
        
        BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        
        return rep;
    }

    private static final BufferedImage createBufferedImage(int width, int height) {
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        
        ColorModel model = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration()
            .getColorModel(java.awt.Transparency.TRANSLUCENT);
        BufferedImage buffImage = new BufferedImage(
                model, 
                model.createCompatibleWritableRaster(width, height), 
                model.isAlphaPremultiplied(), 
                null);
        
        return buffImage;
    }    
    
}
