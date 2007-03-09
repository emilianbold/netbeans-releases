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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.dnd.DnDConstants;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.widget.Widget;

/**
 * For better visual display of showing icon/name along the cursor for DND,
 * access to the WidgetAction.Adapter is needed.
 * Its best to extend from AcceptAction class, but as that class is declared asfinal,
 * there is no option left other than to copy/paste AcceptAction code here. 
 * @author rdara
 */
public class CasaPaletteAcceptAction extends WidgetAction.Adapter {
    
    private CasaAcceptProvider provider;

    public CasaPaletteAcceptAction (CasaAcceptProvider provider) {
        this.provider = provider;
    }

    public State dragOver (Widget widget, WidgetDropTargetDragEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());
        provider.positionIcon(widget, event.getPoint(), acceptable);
        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrag ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State dropActionChanged (Widget widget, WidgetDropTargetDragEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());
        //provider.acceptStarted(event.getTransferable());
        provider.positionIcon(widget, event.getPoint(), acceptable);
        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrag ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State drop (Widget widget, WidgetDropTargetDropEvent event) {
        ConnectorState acceptable = provider.isAcceptable (widget, event.getPoint (), event.getTransferable ());
        provider.acceptFinished();
        if (acceptable == ConnectorState.ACCEPT)
            provider.accept (widget, event.getPoint (), event.getTransferable ());

        if (acceptable == ConnectorState.ACCEPT) {
            event.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
            return State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP) {
            event.rejectDrop ();
            return State.CONSUMED;
        }
        return State.REJECTED;
    }
    
    /**
     * Called for handling a dragEnter event.
     * @param widget the widget where the action is assigned
     * @param event  the drop target drag event
     * @return the event state
     */
    public State dragEnter(Widget widget, WidgetDropTargetDragEvent event) {
        provider.acceptStarted(event.getTransferable());
        provider.positionIcon(widget, event.getPoint(), ConnectorState.REJECT);
        return State.REJECTED;
    }
    
    /**
     * Called for handling a dragExit event.
     * @param widget the widget where the action is assigned
     * @param event  the drop target event
     * @return the event state
     */
    public State dragExit(Widget widget, WidgetDropTargetEvent event) {
        provider.acceptFinished();
        return State.REJECTED;
    }
}
