/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.api.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;

/**
 * A base presenter for the device display component. This presenter occupies the whole device display visible in the screen editor and
 * is also responsible for its painting.
 *
 * @author breh
 */
public abstract class ScreenDisplayPresenter extends DynamicPresenter {

    public final DesignComponent getRelatedComponent () {
        return getComponent ();
    }

    public abstract boolean isTopLevelDisplay ();

    /**
     * Gets children DesignComponent. Component elements are visible elements representing design components in this display.
     * @return the children component
     */
    public abstract Collection<DesignComponent> getChildren ();

    /**
     * Gets actual view of the component. The JComponent is supposed to
     * cooperate nicely with various LayoutManagers (in MIDP there
     * are specific layout managers)
     * @return the view
     */
    public abstract JComponent getView();

    /**
     * Called immediately before the view component is to be added to the view tree. This method call is done in AWT thread.
     * @param deviceInfo the device info
     */
    public abstract void reload (ScreenDeviceInfo deviceInfo);

//    /**
//     * Gets selection shape at given point (within this display coordinates)
//     * @param point
//     * @return
//     */
//    public abstract Shape getHoverShape(Point point);

    /**
     * Gets selection shape at given point (within this display coordinates)
     * @return the shape
     */
    public abstract Shape getSelectionShape();
//    public abstract Shape getSelectionShape(Point point);

    /**
     * Returns a collection of all screen property descriptors
     * @return the collection
     */
    public abstract Collection<ScreenPropertyDescriptor> getPropertyDescriptors ();// {
      //  return Collections.emptySet ();
   // }
    
    /**
     * Returns true if component is dragable
     * @return boolean value
     */
    public boolean isDraggable() {
        return false;
    }

    /**
     * Returns component's view location, that can differ
     * from real location of view, returned by getView().getLocation().
     * <p>
     * If returns null, getView().getLocation() will be used.
     * <p>
     * In default implementation returns getView().getLocation();
     * @return actual component's view location
     */
    public Point getLocation(){
        return getView().getLocation();
    }

    public AcceptSuggestion createSuggestion(Transferable transferable) {
        return null;
    }

    protected void designChanged(DesignEvent event) {
    }

    protected void notifyAttached(DesignComponent component) {
    }

    protected void notifyDetached(DesignComponent component) {
    }
    
    protected DesignEventFilter getEventFilter() {
        return null;
    }

    protected void presenterChanged(PresenterEvent event) {
    }
   
}
