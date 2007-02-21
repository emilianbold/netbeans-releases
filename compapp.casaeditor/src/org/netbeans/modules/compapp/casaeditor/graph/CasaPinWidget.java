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

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.GrayFilter;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
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
            "org/netbeans/modules/compapp/casaeditor/graph/resources/arrow_bc_provides.png"); // NOI18N
    protected static final Image IMAGE_ARROW_RIGHT_CONSUMES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/arrow_bc_consumes.png"); // NOI18N
    protected static final Image IMAGE_ARROW_RIGHT_PROVIDES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/arrow_se_provides.png"); // NOI18N */
    protected static final Image IMAGE_ARROW_LEFT_CONSUMES = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/arrow_se_consumes.png"); // NOI18N

    
    private GrayFilter mPinGrayFilter = new GrayFilter(true, 20);
    
    
    /**
     * Creates a pin widget.
     * @param scene the scene
     */
    public CasaPinWidget(Scene scene) {
        super (scene);
        setOpaque(false);
        setLayout(LayoutFactory.createHorizontalLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    
    protected abstract void setPinName(String name);
    
    protected abstract void setSelected(boolean isSelected);
    
    protected abstract void setMinimized(boolean isMinimized);
    
    
    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if (!previousState.isSelected() && state.isSelected()) {
            setSelected(true);
        } else if (previousState.isSelected() && !state.isSelected()) {
            setSelected(false);
        }
        
        if (state.isHovered()) {
            hoverConnections();
        }
    }

    private void hoverConnections() {
        CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
        CasaEndpointRef pinObject = (CasaEndpointRef) scene.findObject(this);
        Collection<CasaComponent> edges = scene.getEdges();
        List<CasaComponent> pinConnections = new ArrayList<CasaComponent>();
        for (CasaComponent edge : edges) {
            CasaComponent source = scene.getEdgeSource(edge);
            CasaComponent target = scene.getEdgeTarget(edge);
            if (
                    source == pinObject ||
                    target == pinObject) {
                pinConnections.add(edge);
            }
        }
        for (CasaComponent pinConnection : pinConnections) {
            scene.setHoveredObject(pinConnection);
        }
    }
    
    protected Image createSelectedPinImage(Image pinImage) {
        ImageProducer prod = new FilteredImageSource(
                pinImage.getSource(), 
                mPinGrayFilter);
        Image selectedImage = Toolkit.getDefaultToolkit().createImage(prod);
        return selectedImage;
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
    
    protected void readjustBounds(Rectangle rectangle) {
    }

}
