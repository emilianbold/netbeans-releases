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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public final class AcceptAction extends WidgetAction.Adapter {

    private AcceptProvider provider;

    public AcceptAction (AcceptProvider provider) {
        this.provider = provider;
    }

    public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
        return provider.isAcceptable (widget, event.getPoint (), event.getTransferable ()) != ConnectorState.REJECT ? State.CONSUMED : State.REJECTED;
    }

    public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
        return provider.isAcceptable (widget, event.getPoint (), event.getTransferable ()) != ConnectorState.REJECT ? State.CONSUMED : State.REJECTED;
    }

    public State drop (Widget widget, WidgetDropTargetDropEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());
        if (acceptable == ConnectorState.ACCEPT)
            provider.accept (widget, event.getPoint (), event.getTransferable ());
        return acceptable != ConnectorState.REJECT ? State.CONSUMED : State.REJECTED;
    }

}
