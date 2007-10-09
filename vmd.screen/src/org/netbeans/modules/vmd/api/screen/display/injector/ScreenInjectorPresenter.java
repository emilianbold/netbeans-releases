/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
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
package org.netbeans.modules.vmd.api.screen.display.injector;

import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.presenters.OrderablePresenter;

import javax.swing.*;

/**
 * @author breh
 */
public abstract class ScreenInjectorPresenter extends Presenter implements OrderablePresenter {

    public abstract boolean isEnabled ();

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

}
