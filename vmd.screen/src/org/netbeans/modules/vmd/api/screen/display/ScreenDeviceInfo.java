/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.screen.display;

import java.awt.Dimension;
import java.util.Collection;

/**
 * Main class hodling bas device info for the device.
 *
 * TODO this class should be made abstract and the implementation
 * should be moved either to the MIDP module or to a completely
 * separate module defining L&F of the view
 *
 * @author breh
 *
 */
public abstract class ScreenDeviceInfo {

    public enum Edge {
        TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT
    }
   
    /**
     * Gets device info ID. Used to identify the device info.
     * @return
     */
    public abstract String getDeviceInfoID();
    
    /**
     * Gets device view resource (e.g. colors, images, fonts  ...)
     * @return
     */
    public abstract DeviceTheme getDeviceTheme();
    
    /**
     * Gets current screen size
     * @return
     */
    public abstract Dimension getCurrentScreenSize();
    
    /**
     * Checks whether this device support custom screen sizes
     * @return
     */
    public abstract boolean supportsCustomScreenSize();
    
    /**
     * Returns a collection of the arbitrary sceeen sizes
     * @return
     */
    public abstract Collection<Dimension> getCustomScreenSizes();
    
    /**
     * returns true if this device supports arbitrary screen size
     * @return
     */
    public abstract boolean supportsArbitraryScreenSize();
    
    /**
     * Sets the arbitrary screen size to be used
     * @param newScreenSize
     */
    public abstract void setArbitraryScreenSize(Dimension newScreenSize);
    
    /**
     * Gets image for the given edge of the device
     * @param edge
     * @return
     */
    public abstract DeviceBorder getDeviceBorder(Edge edge);
    
    protected abstract void loadImages();
   
}
