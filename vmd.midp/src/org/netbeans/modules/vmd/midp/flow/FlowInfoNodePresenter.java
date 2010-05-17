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

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.flow.FlowNodePresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.screen.actions.DesignerEditAction;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class FlowInfoNodePresenter extends FlowNodePresenter {

    private final InfoNodeDecoratorBehaviour ctrl = new InfoNodeDecoratorBehaviour ();

    private FlowNodeDescriptor descriptor;

    public FlowInfoNodePresenter () {
    }

    public void notifyAttached (DesignComponent component) {
        super.notifyAttached (component);
        addDependency (component, InfoPresenter.class);
        descriptor = new FlowNodeDescriptor (component, FlowIDSupport.createNodeID4SingleNode (component));
    }

    protected boolean isVisible () {
        return super.isVisible ()  &&  getComponent ().getParentComponent () != null;
    }

    public FlowNodeDescriptor getNodeDescriptor () {
        return descriptor;
    }

    public void updateDescriptors () {
    }

    public DesignEventFilter getEventFilter () {
        return new DesignEventFilter ().addComponentFilter (getComponent (), false).addHierarchyFilter (getComponent (), false);
    }

    public FlowNodeDescriptor.NodeDecorator getDecorator () {
        return ctrl;
    }

    public FlowNodeDescriptor.NodeBehaviour getBehaviour () {
        return ctrl;
    }

    private class InfoNodeDecoratorBehaviour implements FlowNodeDescriptor.NodeDecorator, FlowNodeDescriptor.NodeBehaviour, FlowDescriptor.AcceptActionBehaviour, FlowDescriptor.RenameActionBehaviour, FlowDescriptor.EditActionBehaviour {

        public Widget createWidget (FlowNodeDescriptor descriptor, FlowScene scene) {
            VMDNodeWidget widget = new VMDNodeWidget (scene, VMDFactory.getNetBeans60Scheme ());
            scene.addNodeCommonActions (widget);
            LabelWidget nodeNameWidget = widget.getNodeNameWidget ();
            widget.getActions ().addAction (ActionFactory.createForwardKeyEventsAction (nodeNameWidget, null));
            nodeNameWidget.getActions ().addAction (scene.createRenameAction ());
            return widget;
        }

        public void update (FlowNodeDescriptor descriptor, FlowScene scene) {
            InfoPresenter presenter = descriptor.getRepresentedComponent ().getPresenter (InfoPresenter.class);
            if (presenter == null)
                return;
            VMDNodeWidget widget = (VMDNodeWidget) scene.findWidget (descriptor);
            widget.setNodeImage (presenter.getIcon (InfoPresenter.IconType.COLOR_16x16));
            widget.setNodeName (presenter.getDisplayName (InfoPresenter.NameType.PRIMARY));
            widget.setToolTipText (InfoPresenter.getToolTip (presenter));
            scene.scheduleNodeDescriptorForOrdering (descriptor);
        }

        public void attachPinWidget (FlowNodeDescriptor descriptor, FlowScene scene, Widget pinWidget) {
            Widget widget = scene.findWidget (descriptor);
            ((VMDNodeWidget) widget).attachPinWidget (pinWidget);
        }

        public Anchor createAnchor (FlowNodeDescriptor node, FlowScene scene) {
            Widget widget = scene.findWidget (node);
            return ((VMDNodeWidget) widget).getNodeAnchor ();
        }

        public DesignComponent getComponentWithPinOrderPresenters () {
            return getComponent ();
        }

        public void orderPins (FlowNodeDescriptor node, FlowScene scene, Map<String, List<FlowPinDescriptor>> categories) {
            VMDNodeWidget nodeWidget = (VMDNodeWidget) scene.findWidget (node);

            HashMap<String, List<Widget>> order = new HashMap<String, List<Widget>> ();
            for (Map.Entry<String, List<FlowPinDescriptor>> entry : categories.entrySet ()) {
                ArrayList<Widget> widgets = new ArrayList<Widget> ();
                for (FlowPinDescriptor pin : entry.getValue ())
                    widgets.add (scene.findWidget (pin));
                order.put (entry.getKey (), widgets);
            }

            nodeWidget.sortPins (order);
        }

        public boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable) {
            return AcceptSupport.isAcceptable (descriptor.getRepresentedComponent (), transferable, null);
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            ComponentProducer.Result result = AcceptSupport.accept (descriptor.getRepresentedComponent (), transferable, null);
            AcceptSupport.selectComponentProducerResult (result);
        }

        public boolean isEditable (FlowDescriptor descriptor) {
            InfoPresenter presenter = descriptor.getRepresentedComponent ().getPresenter (InfoPresenter.class);
            return presenter != null  &&  presenter.isEditable ();
        }

        public String getText (FlowDescriptor descriptor) {
            InfoPresenter presenter = descriptor.getRepresentedComponent ().getPresenter (InfoPresenter.class);
            assert presenter != null;
            return presenter.getEditableName ();
        }

        public void setText (FlowDescriptor descriptor, String text) {
            InfoPresenter presenter = descriptor.getRepresentedComponent ().getPresenter (InfoPresenter.class);
            assert presenter != null;
            presenter.setEditableName (text);
        }

        public void edit (FlowDescriptor descriptor) {
            Collection<? extends ActionsPresenter> presenters = descriptor.getRepresentedComponent ().getPresenters (ActionsPresenter.class);
            for (ActionsPresenter presenter : presenters) {
                for (Action action : presenter.getActions ()) {
                    if (action instanceof DesignerEditAction) {
                        if (action.isEnabled ())
                            action.actionPerformed (null);
                        return;
                    }
                }
            }
        }
    }

}
