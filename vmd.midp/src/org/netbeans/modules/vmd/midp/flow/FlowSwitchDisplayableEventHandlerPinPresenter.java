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
 *
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
