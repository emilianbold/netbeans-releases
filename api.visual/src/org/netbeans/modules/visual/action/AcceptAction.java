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
