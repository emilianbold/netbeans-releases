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
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.flow.FlowPinPresenter;
import org.netbeans.modules.vmd.api.flow.visual.*;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - rewrite to query target port to generate EventHandler component
// TODO - rewrite to use InfoPresenter
public abstract class FlowEventSourcePinPresenter extends FlowPinPresenter {

    private EventSourcePinDecoratorBehaviour ctrl = new EventSourcePinDecoratorBehaviour ();

    private FlowPinDescriptor pinDescriptor;

    protected abstract DesignComponent getComponentForAttachingPin ();

    protected abstract String getDisplayName ();

    protected abstract String getOrder ();

    protected boolean canRename () {
        return false;
    }

    protected String getRenameName () {
        return getDisplayName ();
    }

    protected void setRenameName (String name) {
    }

    protected String getPinID () {
        return FlowIDSupport.createEventSourcePinID (getComponent ());
    }

    protected boolean isVisible () {
        return super.isVisible ()  && getComponentForAttachingPin () != null;
    }

    protected final FlowNodeDescriptor getNodeDescriptor () {
        DesignComponent component = getComponentForAttachingPin ();
        FlowInfoNodePresenter presenter = component.getPresenter (FlowInfoNodePresenter.class);
        return presenter != null && presenter.isVisible () ? presenter.getNodeDescriptor () : null;
    }

    public final void updateDescriptors () {
        pinDescriptor = isVisible () ? new FlowPinDescriptor (getComponent (), getPinID ()) : null;
    }

    public final FlowPinDescriptor getPinDescriptor () {
        return pinDescriptor;
    }

    public FlowPinDescriptor.PinDecorator getDecorator () {
        return ctrl;
    }

    public FlowPinDescriptor.PinBehaviour getBehaviour () {
        return ctrl;
    }

    protected DesignEventFilter getEventFilter () {
        return new DesignEventFilter ().addComponentFilter (getComponent (), false).addHierarchyFilter (getComponent (), false);
    }

    public static FlowNodeDescriptor getNodeDescriptor (DesignComponent eventSourceComponent) {
        if (eventSourceComponent == null)
            return null;
        FlowEventSourcePinPresenter presenter = eventSourceComponent.getPresenter (FlowEventSourcePinPresenter.class);
        return presenter != null  &&  presenter.isVisible () ? presenter.getNodeDescriptor () : null;
    }

    public static FlowPinDescriptor getPinDescriptor (DesignComponent eventSourceComponent) {
        if (eventSourceComponent == null)
            return null;
        FlowEventSourcePinPresenter presenter = eventSourceComponent.getPresenter (FlowEventSourcePinPresenter.class);
        return presenter != null ? presenter.pinDescriptor : null;
    }

    protected class EventSourcePinDecoratorBehaviour implements FlowPinDescriptor.PinDecorator, FlowPinDescriptor.PinBehaviour, FlowDescriptor.BadgeDecorator, FlowDescriptor.AcceptActionBehaviour, FlowDescriptor.RenameActionBehaviour {

        public Widget createWidget (FlowPinDescriptor descriptor, FlowScene scene) {
            VMDPinWidget vmdPinWidget = new VMDPinWidget (scene, VMDFactory.getNetBeans60Scheme ());
            scene.addPinCommonActions (vmdPinWidget);
            vmdPinWidget.getActions ().addAction (0, scene.createRenameAction ());
            return vmdPinWidget;
        }

        public Anchor createAnchor (FlowPinDescriptor descriptor, FlowScene scene) {
            FlowNodeDescriptor node = scene.getPinNode (descriptor);
            Widget nodeWidget = scene.findWidget (node);
            Anchor anchor = AnchorFactory.createDirectionalAnchor (scene.findWidget (descriptor), AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 6);
            return ((VMDNodeWidget) nodeWidget).createAnchorPin (anchor);
        }

        public void update (FlowPinDescriptor descriptor, FlowScene scene) {
            VMDPinWidget widget = (VMDPinWidget) scene.findWidget (descriptor);
            widget.setPinName (FlowEventSourcePinPresenter.this.getDisplayName ());
        }

        public String getOrderCategory (FlowPinDescriptor descriptor) {
            return getOrder ();
        }

        public void updateBadges (FlowDescriptor descriptor, FlowScene scene, List<FlowBadgeDescriptor> badges) {
            VMDPinWidget widget = (VMDPinWidget) scene.findWidget (descriptor);
            ArrayList<Image> images = new ArrayList<Image> ();
            for (FlowBadgeDescriptor badge : badges) {
                Image image = scene.getDecorator (badge).getImage (badge);
                if (image != null)
                    images.add (image);
            }
            widget.setGlyphs (images);
        }

        public boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable) {
            return AcceptSupport.isAcceptable (descriptor.getRepresentedComponent (), transferable, null);
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            ComponentProducer.Result result = AcceptSupport.accept (descriptor.getRepresentedComponent (), transferable, null);
            AcceptSupport.selectComponentProducerResult (result);
        }

        public boolean isConnectionSource (FlowPinDescriptor pin) {
            return true;
        }

        public boolean isConnectionTarget (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            if (target == null)
                return true;
            return MidpDocumentSupport.isCreatableEventHandlerTo (target.getRepresentedComponent ());
        }

        public void createConnection (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            MidpDocumentSupport.updateEventHandlerFromTarget (getComponent (), target != null ? target.getRepresentedComponent () : null);
        }

        public boolean isEditable (FlowDescriptor descriptor) {
            return FlowEventSourcePinPresenter.this.canRename ();
        }

        public String getText (FlowDescriptor descriptor) {
            return FlowEventSourcePinPresenter.this.getRenameName ();
        }

        public void setText (FlowDescriptor descriptor, String text) {
            FlowEventSourcePinPresenter.this.setRenameName (text);
        }

    }

}
