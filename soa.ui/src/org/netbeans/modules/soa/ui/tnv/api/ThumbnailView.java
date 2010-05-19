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

package org.netbeans.modules.soa.ui.tnv.api;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * Describes behaviour of a thumbnail view.
 *
 * @author nk160297
 */
public interface ThumbnailView extends ObservableChangeTracker.UpdateListener {
    
    /**
     * Returns the associated scroll pane.
     */
    JScrollPane getScrollPane();
    
    /**
     * Returns the associated observable component.
     */
    JComponent getContent();
    
    /**
     * Associates the Thumbnail view with the specified scroll pane
     */
    void setScrollPane(JScrollPane newValue);
    
    /**
     * Gets the size of view in screen coordinates.
     */
    Dimension getSize();
    
    /**
     * Sets the size of view in screen coordinates.
     */
    void setSize(Dimension prefSize);
    
    /**
     * Repaints view.
     */
    void repaint();
    
    /**
     * Set the fixed zoom value.
     */
    double getZoom();
    
    void setZoom(double newValue);
    
    /**
     * Returns the UI component which represents the thumbnail view.
     */
    JComponent getUIComponent();
    
    
    /**
     * Indicates if it necessary to repaint main view right away
     * after the thumbnail view has changed.
     */ 
    boolean isRepaintMainViewRightAway();
    
    /**
     * Set flag which indicates if it necessary to repaint main view right away
     */ 
    void repaintMainViewRightAway(boolean newValue);
 
    /**
     * Indicates if it necessary to repaint thumbnail view right away 
     * after the main view has changed.
     */ 
    boolean isRepaintThumbnailViewRightAway();
    
    void repaintThumbnailViewRightAway(boolean newValue);
    
}
