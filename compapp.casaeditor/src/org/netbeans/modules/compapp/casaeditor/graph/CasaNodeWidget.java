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

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.*;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;


/**
 * This class represents a node widget in the VMD plug-in.
 * It implements the minimize ability. It allows to add pin widgets into the widget
 * using attachPinWidget method.
 *
 * @author David Kaspar
 */
public abstract class CasaNodeWidget extends Widget {
    
    protected Widget mHeader;
    protected List<Widget.Dependency> mDependencies = new ArrayList<Widget.Dependency>();
    
    private boolean mEditable = false;
    private boolean mWSPolicyAttached = false;
    
    /**
     * Creates a node widget.
     * @param scene the scene
     */
    public CasaNodeWidget(Scene scene) {
        super(scene);
    }
    
    
    public Rectangle getEntireBounds() {
        return getBounds();
    }
    
    public void persistLocation() {
        Point location = getPreferredLocation();
        if (location == null) {
            location = getLocation();
        }
        persistLocation(location);
    }
    
    public void persistLocation(Point location) {
        CasaModelGraphScene scene = (CasaModelGraphScene) getScene();
        CasaComponent component = (CasaComponent) scene.findObject(CasaNodeWidget.this);
        if (component instanceof CasaServiceEngineServiceUnit) {
            CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) component;
            if (su.getX() != location.x || su.getY() != location.y) {
                scene.setCasaLocation(su, location.x, location.y);
            }
        } else if (component instanceof CasaPort) {
            CasaPort port = (CasaPort) component;
            if (port.getX() != location.x || port.getY() != location.y) {
                scene.setCasaLocation(port, location.x, location.y);
            }
        }
    }
    
    
    /**
     * Initialization for the glass layer above the widget.
     * @param layer the glass layer
     */
    public abstract void initializeGlassLayer(LayerWidget layer);
    
    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public abstract void attachPinWidget(CasaPinWidget widget);
    
    /**
     * Sets all node properties at once.
     */
    public abstract void setNodeProperties(String nodeName, String nodeType);
    
    
    public Anchor getPinAnchor(Widget pinMainWidget) {
        Anchor anchor = null;
        if (pinMainWidget != null) {
            assert pinMainWidget instanceof CasaPinWidget;
            anchor = ((CasaPinWidget) pinMainWidget).getAnchor();
            anchor = createAnchorPin(anchor);
        }
        return anchor;
    }
    
    /**
     * Returns an anchor for the given pin anchor.
     * Subclasses may return a proxy anchor or the same anchor passed-in.
     * @param anchor the original pin anchor
     * @return the extended pin anchor
     */
    protected abstract Anchor createAnchorPin(Anchor pinAnchor);
    
    /**
     * Returns a header widget.
     * @return the header widget
     */
    public Widget getHeader() {
        return mHeader;
    }
    
    /**
     * A modified version of convertLocalToScene that attempts
     * to use preferred coordinates whenever possible.
     */
    public Point convertPreferredLocalToScene(Point localLocation) {
        Point sceneLocation = new Point(localLocation);
        Widget widget = this;
        while (widget != null) {
            if (widget == getScene())
                break;
            // check preferred location first
            Point location = widget.getPreferredLocation();
            if (location == null) {
                location = widget.getLocation();
            }
            sceneLocation.x += location.x;
            sceneLocation.y += location.y;
            widget = widget.getParentWidget();
        }
        return sceneLocation;
    }
    
    public void removeAllDependencies() {
        for (Widget.Dependency dependency : mDependencies) {
            removeDependency(dependency);
        }
    }
    
    public void invokeDependencies() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Widget.Dependency dependency : mDependencies) {
                    dependency.revalidateDependency();
                }
                getScene().validate();
            }
        });
    }
    
    public boolean isEditable() {
        return mEditable;
    }
    public void setEditable(boolean bValue) {
        mEditable = bValue;
    }
    
    public boolean isWSPolicyAttached() {
        return mWSPolicyAttached;
    }
    public void setWSPolicyAttached(boolean bValue) {
        mWSPolicyAttached = bValue;
    }
} 
