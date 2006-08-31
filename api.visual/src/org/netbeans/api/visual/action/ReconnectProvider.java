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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * @author David Kaspar
 */
public interface ReconnectProvider {

    boolean isSourceReconnectable (ConnectionWidget connectionWidget);

    boolean isTargetReconnectable (ConnectionWidget connectionWidget);

    void reconnectingStarted (ConnectionWidget connectionWidget, boolean reconnectingSource);

    void reconnectingFinished (ConnectionWidget connectionWidget, boolean reconnectingSource);

    ConnectorState isReplacementWidget (ConnectionWidget connectionWidget, Widget replacementWidget, boolean recoonectingSource);

    boolean hasCustomReplacementWidgetResolver (Scene scene);

    Widget resolveReplacementWidget (Scene scene, Point sceneLocation);

    void reconnect (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource);

}
