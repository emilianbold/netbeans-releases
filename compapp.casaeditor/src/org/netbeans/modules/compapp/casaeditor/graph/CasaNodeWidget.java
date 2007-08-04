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

import java.awt.Rectangle;
import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.*;


/**
 * This class represents a node widget in the VMD plug-in.
 * It implements the minimize ability. It allows to add pin widgets into the widget
 * using attachPinWidget method.
 *
 * @author David Kaspar
 */
public abstract class CasaNodeWidget extends Widget implements CycleFocusProvider {
    
    protected Widget mContainerWidget;
    
    private boolean mEditable = false;
    private boolean mWSPolicyAttached = false;
    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    
    
    public CasaNodeWidget(Scene scene) {
        super(scene);
    }
    
    
    protected void notifyRemoved() {
        getRegistry().removeAllDependencies();
    }
    
    public Rectangle getEntireBounds() {
        return new Rectangle(getLocation(), getBounds().getSize());
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
    
    /**
     * Returns an anchor for the given pin anchor.
     * Subclasses may return a proxy anchor or the same anchor passed-in.
     * @param anchor the original pin anchor
     * @return the extended pin anchor
     */
    protected abstract Anchor createAnchorPin(Anchor pinAnchor);
    
    
    public Anchor getPinAnchor(Widget pinMainWidget) {
        Anchor anchor = null;
        if (pinMainWidget != null) {
            assert pinMainWidget instanceof CasaPinWidget;
            anchor = ((CasaPinWidget) pinMainWidget).getAnchor();
            anchor = createAnchorPin(anchor);
        }
        return anchor;
    }
    
    public Widget getContainerWidget() {
        return mContainerWidget;
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
    
    protected DependenciesRegistry getRegistry() {
        return mDependenciesRegistry;
    }
    
    public void readjustBounds() {
        if (getBounds() != null) {
            revalidate();
            getScene().validate();
        }
    }

    public boolean switchPreviousFocus(Widget widget) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean switchNextFocus(Widget widget) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
