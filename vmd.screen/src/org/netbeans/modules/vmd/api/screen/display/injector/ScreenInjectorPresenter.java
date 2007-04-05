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
package org.netbeans.modules.vmd.api.screen.display.injector;

import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.presenters.OrderablePresenter;
import org.netbeans.modules.vmd.screen.device.InjectorWindow;

import javax.swing.*;
import java.awt.*;

/**
 * ScreenInjectorPresenter is a presenter adding "behavior" to ScreenComponentPresenter and
 * ScreenPropertyPresenter presenters. This behavior can be added at runtime, so
 * other modules can install a functionality manipulating with the component
 * even the component does not know that.
 * 
 * TODO - shoudn't be ScreenInjectorPresenter defined rather on a higher level, so also other
 * view components (inspector/flow) can benefit from it?
 * 
 * TODO - this class will be perhaps redesigned based on the discussion
 * about data-biding we should have in April/May
 * 
 * @author breh
 */
public abstract class ScreenInjectorPresenter extends Presenter implements OrderablePresenter {

    /**
     * Gets view of the injector. Please make note this view can be aggregated
     * together with other injectors into a single view using grid bag layout.
     * 
     * In the case this method returns null, this injector is a simple injector,
     * which can perform only "default" action.
     * 
     * Please note, the component should listen on changes in the model, the
     * time for which it is being displayed is unknown and the model can be
     * updated while it is being displayed
     *
     * Called in AWT and under read-access on document. 
     * @return the view component
     */
    public abstract JComponent getViewComponent ();

    /**
     * Call to close the injector menu window.
     * @param viewComponent the view component
     */
    protected static void closeInjectorWindow (JComponent viewComponent) {
        Container container = viewComponent;
        while (container != null) {
            if (container instanceof InjectorWindow) {
                ((InjectorWindow) container).close ();
                return;
            }
            container = container.getParent ();
        }
        Debug.warning ("Cannot close injector window", viewComponent);
    }

}
