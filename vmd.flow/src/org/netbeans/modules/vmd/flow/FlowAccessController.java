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
package org.netbeans.modules.vmd.flow;

import org.netbeans.modules.vmd.api.flow.FlowPresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.model.*;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author David Kaspar
 */
// TODO - incremental selection update in notifyEventFired method
// TODO - model->flow selection bug - ListCD, ListElementCD flow presenters vs. the descriptors (ListCD.elements belongs to each element - not to the list)
public class FlowAccessController implements AccessController {

    private DesignDocument document;
    private FlowScene scene;

    private HashSet<FlowPresenter> dirtyPresenters = new HashSet<FlowPresenter> ();

    private volatile long eventID = 0;

    public FlowAccessController (DesignDocument document) {
        this.document = document;
        scene = new FlowScene (document);
    }

    public void writeAccess (Runnable runnable) {
        runnable.run ();
    }

    public void notifyEventFiring (DesignEvent event) {
    }

    public void notifyEventFired (final DesignEvent event) {
        if (eventID < event.getEventID ())
            eventID = event.getEventID ();
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        scene.setCurrentEventIDForPreferredNodeLocationProcessing (eventID);
                        resolveDirty ();
                        scene.setCurrentEventIDForPreferredNodeLocationProcessing (null);

                        if (! FlowViewController.FLOW_ID.equals (document.getSelectionSourceID ())) {
                            HashSet<FlowDescriptor> objects = new HashSet<FlowDescriptor> ();
                            for (DesignComponent component : document.getSelectedComponents ()) {
                                for (FlowPresenter presenter : component.getPresenters (FlowPresenter.class))
                                    objects.addAll (presenter.getFlowDescriptors ());
                            }
                            scene.setSelectedObjects (objects);
                        }
                        scene.validate ();
                    }
                });
            }
        });
    }

    public void notifyComponentsCreated (Collection<DesignComponent> createdComponents) {
    }

    public FlowScene getScene () {
        return scene;
    }

    public JComponent getCreateView () {
        JComponent view = scene.getView ();
        if (view == null)
            view = scene.createView ();
        return view;
    }

    public JComponent createSatelliteView () {
        return scene.createSatelliteView ();
    }

    public void addDirtyPresenter (FlowPresenter presenter) {
        dirtyPresenters.add (presenter);
    }

    private void resolveDirty () {
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.updateDescriptors ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveRemoveBadge ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveRemoveEdge ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveRemovePin ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveRemoveNode ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveAddNode ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveAddPin ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveAddEdge ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveAddBadge ();
        for (FlowPresenter presenter : dirtyPresenters)
            presenter.resolveUpdate ();
        dirtyPresenters.clear ();
        scene.resolveOrderInNodeDescriptors ();
    }

    public static class Factory implements AccessControllerFactory {

        public AccessController createAccessController (DesignDocument document) {
            return new FlowAccessController (document);
        }

    }

}
