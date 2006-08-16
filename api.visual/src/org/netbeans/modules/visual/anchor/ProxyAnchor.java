/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class ProxyAnchor extends Anchor implements StateModel.Listener {

    private StateModel model;
    private Anchor[] anchors;
    private int index;

    public ProxyAnchor(StateModel model, Anchor... anchors) {
        super (null);
//        assert model != null  &&  model.getMaxStates () == anchors.length;
        this.model = model;
        this.anchors = anchors;
        this.index = model.getState ();
    }

    public StateModel getModel () {
        return model;
    }

    protected void notifyEntryAdded (Entry entry) {
        anchors[index].addEntry (entry);
    }

    protected void notifyEntryRemoved (Entry entry) {
        anchors[index].removeEntry (entry);
    }

    protected void notifyUsed () {
        model.addListener (this);
    }

    protected void notifyUnused () {
        model.removeListener (this);
    }

    public void stateChanged () {
        int state = getModel ().getState ();
        if (index == state)
            return;
        anchors[index].removeEntries (getEntries ());
        index = state;
        anchors[index].addEntries (getEntries ());
        revalidateDependency ();
    }

    public Point getRelatedSceneLocation () {
        return anchors[index].getRelatedSceneLocation ();
    }

    public Widget getRelatedWidget () {
        return anchors[index].getRelatedWidget();
    }

    public Anchor.Result compute (Anchor.Entry entry) {
        return anchors[index].compute (entry);
    }

}
