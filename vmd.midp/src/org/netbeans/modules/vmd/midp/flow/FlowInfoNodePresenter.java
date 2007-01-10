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
package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.modules.vmd.api.flow.FlowNodePresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private class InfoNodeDecoratorBehaviour implements FlowNodeDescriptor.NodeDecorator, FlowNodeDescriptor.NodeBehaviour, FlowDescriptor.AcceptActionBehavior, FlowDescriptor.RenameActionBehaviour {

        public Widget createWidget (FlowNodeDescriptor descriptor, FlowScene scene) {
            VMDNodeWidget widget = new VMDNodeWidget (scene);
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
            widget.setNodeType (presenter.getDisplayName (InfoPresenter.NameType.SECONDARY));
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
            return AcceptSupport.isAcceptable (descriptor.getRepresentedComponent (), transferable);
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            AcceptSupport.accept (descriptor.getRepresentedComponent (), transferable);
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
    }

}
