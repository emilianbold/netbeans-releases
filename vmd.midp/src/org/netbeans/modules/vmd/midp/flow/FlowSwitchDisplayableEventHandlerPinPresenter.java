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
package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;
import org.openide.util.NbBundle;

import java.awt.datatransfer.Transferable;

/**
 * @author David Kaspar
 */
// TODO - rewrite to query target port to generate EventHandler component
// TODO - rewrite to use InfoPresenter
public final class FlowSwitchDisplayableEventHandlerPinPresenter extends FlowEventSourcePinPresenter {

    private EventHandlerPinDecoratorBehaviour ctrl = new EventHandlerPinDecoratorBehaviour ();

    protected DesignComponent getComponentForAttachingPin () {
        return getComponent ().readProperty (SwitchDisplayableEventHandlerCD.PROP_ALERT).getComponent ();
    }

    protected String getDisplayName () {
        return NbBundle.getMessage (FlowSwitchDisplayableEventHandlerPinPresenter.class, "DISP_FlowPin_Via"); // NOI18N
    }

    protected String getOrder () {
        return FlowAlertViaPinOrderPresenter.CATEGORY_ID;
    }

    protected String getPinID () {
        return FlowIDSupport.createEventHandlerTargetPinID (getComponent (), getComponentForAttachingPin ());
    }

    protected boolean isVisible () {
        return super.isVisible ()  && getComponent ().getParentComponent () != null;
    }

    public FlowPinDescriptor.PinDecorator getDecorator () {
        return ctrl;
    }

    public FlowPinDescriptor.PinBehaviour getBehaviour () {
        return ctrl;
    }

    protected class EventHandlerPinDecoratorBehaviour extends EventSourcePinDecoratorBehaviour {

        public Anchor createAnchor (FlowPinDescriptor descriptor, FlowScene scene) {
            VMDPinWidget pinWidget = ((VMDPinWidget) scene.findWidget (descriptor));
            FlowNodeDescriptor node = scene.getPinNode (descriptor);
            VMDNodeWidget nodeWidget = (VMDNodeWidget) scene.findWidget (node);
            return nodeWidget.createAnchorPin (pinWidget.createAnchor ());
        }

        public boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable) {
            return false;
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            throw Debug.illegalState ();
        }

        public boolean isConnectionSource (FlowPinDescriptor pin) {
            return false; // TODO
        }

        public boolean isConnectionTarget (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            return false; // TODO
//            if (target == null)
//                return true;
//            return MidpDocumentSupport.isCreatableEventHandlerTo (target.getRepresentedComponent ());
        }

        public void createConnection (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            throw Debug.illegalState (); // TODO
//            MidpDocumentSupport.updateEventHandlerFromTarget (getComponent (), target != null ? target.getRepresentedComponent () : null);
        }

        public boolean isEditable (FlowDescriptor descriptor) {
            return false;
        }

        public String getText (FlowDescriptor descriptor) {
            throw Debug.illegalState ();
        }

        public void setText (FlowDescriptor descriptor, String text) {
            throw Debug.illegalState ();
        }

    }

}
