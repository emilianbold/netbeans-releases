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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.api.screen.display;

import org.netbeans.modules.vmd.api.screen.ScreenInfoListener;

import java.util.*;
import java.awt.*;

/**
 * A base presenter for the device display component. This presenter
 * occupies the whole device display visible in the screen editor and
 * is also responsible for its painting.
 *
 * @author breh
 */
public abstract class ScreenDisplayPresenter extends ScreenComponentPresenter implements ScreenInfoListener {

    /**
     * Get child ScreenComponentElements. Screen component elements
     * are visible elements representing design components in this display.
     * @return
     */
    public abstract Collection<ScreenComponentPresenter> getScreenComponentElements();


    /**
     * Gets hoover shape at given point (within this display coordinates)
     * @param point
     * @return
     */
    public abstract Shape getHooverShape(Point point);

    /**
     * Gets selection shape at given point (within this display coordinates)
     * @param point
     * @return
     */
    public abstract Shape getSelectionShape(Point point);


    /**
     * Gets available injectors with mouse at this display point. If the supplied
     * point is null, this method shold return all display injectors available for
     * this component.
     * @param displayPoint if null, this method shold call return all injetors
     * for this component.
     * @return
     * // TODO - move this outside of the ScreenDisplayPresenter e.g. ScreenInjectorPresenter, to allow composition of multiple injectors
     */
    public java.util.List<ScreenInjectorPresenter> getAvailableInjectors(Point displayPoint) {
        return Collections.emptyList ();
    }

//    /**
//     * Is any injector available for this component
//     * @return
//     */
//    public boolean isInjectorAvailable() {
//        return false;
//    }

    /**
     * Called immediately before the view component is to be added to the view tree. This method call is done in AWT thread.
     * @param screenSize TODO
     * @param deviceTheme TODO
     */
    public void showNotify(Dimension screenSize, ScreenDeviceInfo.DeviceTheme deviceTheme) {
    }

    /**
     * Called immediately after the view component has been removed from the view tree. This method call is done in AWT thread.
     */
    public void hideNotify() {
    }

    /**
     * Returns the actual display size used for viewing the component. Some components can "grow" the display components off the screen.
     * Please note, this method is called always after the showNotify() methos call
     * @return
     */
    public abstract Dimension getActualDisplaySize();

    // TODO
    // drag & drop stuff needs to be added !!!

}
