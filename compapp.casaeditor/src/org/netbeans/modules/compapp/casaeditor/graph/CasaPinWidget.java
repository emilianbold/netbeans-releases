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
import org.netbeans.api.visual.widget.Widget;

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
 */
public abstract class CasaPinWidget extends Widget {
    
    public enum AnchorScheme {
        HORIZONTAL, LEFT, RIGHT, VERTICAL, TOP, BOTTOM
    };

    protected static final Image IMAGE_ARROW_LEFT_PROVIDES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/providesLeft.png"); // NOI18N
    protected static final Image IMAGE_ARROW_RIGHT_CONSUMES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/consumesRight.png"); // NOI18N
    protected static final Image IMAGE_ARROW_RIGHT_PROVIDES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/providesRight.png"); // NOI18N
    protected static final Image IMAGE_ARROW_LEFT_CONSUMES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/consumesLeft.png"); // NOI18N
    
    protected ImageWidget mImageWidget;
    
    // How much lighter/darker to make a highlighted version of the image.
    // value < 1 is darker, value > 1 is lighter
    private static final float HIGHLIGHT_LIGHT_FACTOR = 0.6f;
    
    private boolean mIsHighlighted;
    private Image mUnHighlightedImage;
    private GrayFilter mPinGrayFilter = new GrayFilter(true, 20);
    
    
    /**
     * Creates a pin widget.
     * @param scene the scene
     */
    public CasaPinWidget(Scene scene) {
        super (scene);
        
        mImageWidget = new ImageWidget(scene);
        
        setOpaque(false);
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    
    protected abstract void setPinName(String name);
    
    protected abstract void setSelected(boolean isSelected);
    
    
    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
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

    private void hoverConnections() {
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
        scene.setHighlightedObjects(pinConnections);
    }
    
    /**
     * Sets all pin properties at once.
     * @param name the pin name
     * @param glyphs the pin glyphs
     */
    public void setProperties (String name) {
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
    
    private Image createHighlightedImage(Image src) {
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
