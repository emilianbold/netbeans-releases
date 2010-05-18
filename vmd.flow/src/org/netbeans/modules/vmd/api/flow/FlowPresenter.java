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

package org.netbeans.modules.vmd.api.flow;

import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.flow.FlowAccessController;

import java.util.Collection;

/**
 * Note: Do not extends this class directly, use specific Flow*Presenters instead.
 * @author dave
 */
public abstract class FlowPresenter extends DynamicPresenter {

    private boolean visible = false;
    private FlowAccessController controller;

    // TODO - RequiresSuperCall annotation
    protected void notifyAttached (DesignComponent component) {
        controller = getComponent ().getDocument ().getListenerManager ().getAccessController (FlowAccessController.class);
        visible = true;
        controller.addDirtyPresenter (this);
    }

    // TODO - RequiresSuperCall annotation
    protected void notifyDetached (DesignComponent component) {
        visible = false;
        controller.addDirtyPresenter (this);
    }

    // TODO - RequiresSuperCall annotation
    protected boolean isVisible () {
        return visible;
    }

    public final FlowScene getScene () {
        return controller.getScene ();
    }

    protected final void designChanged (DesignEvent event) {
        firePresenterChanged ();
    }

    protected final void presenterChanged (PresenterEvent event) {
        controller.addDirtyPresenter (this);
    }

    public abstract Collection<? extends FlowDescriptor> getFlowDescriptors ();

    public abstract void updateDescriptors ();

    public abstract void resolveRemoveBadge ();

    public abstract void resolveRemoveEdge ();

    public abstract void resolveRemovePin ();

    public abstract void resolveRemoveNode ();

    public abstract void resolveAddNode ();

    public abstract void resolveAddPin ();

    public abstract void resolveAddEdge ();

    public abstract void resolveAddBadge ();

    public abstract void resolveUpdate ();

    static boolean equals (Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals (o2);
    }

    public interface FlowUIResolver {

        FlowDescriptor.Decorator getDecorator ();

        FlowDescriptor.Behaviour getBehaviour ();

    }

}
