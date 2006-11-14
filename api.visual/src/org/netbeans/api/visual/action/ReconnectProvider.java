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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * This interface controls a reconnect action.
 *
 * @author David Kaspar
 */
public interface ReconnectProvider {

    /**
     * Called for checking whether it is possible to reconnection a source of a specified connection widget.
     * @param connectionWidget the connection widget
     * @return if true, then it is possible to reconnection the source; if false, then is not allowed
     */
    boolean isSourceReconnectable (ConnectionWidget connectionWidget);

    /**
     * Called for checking whether it is possible to reconnection a target of a specified connection widget.
     * @param connectionWidget the connection widget
     * @return if true, then it is possible to reconnection the target; if false, then is not allowed
     */
    boolean isTargetReconnectable (ConnectionWidget connectionWidget);

    /**
     * Called to notify about the start of reconnecting.
     * @param connectionWidget the connection widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    void reconnectingStarted (ConnectionWidget connectionWidget, boolean reconnectingSource);

    /**
     * Called to notify about the finish of reconnecting.
     * @param connectionWidget the connection widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    void reconnectingFinished (ConnectionWidget connectionWidget, boolean reconnectingSource);

    /**
     * Called to check for possible replacement of a connection source/target.
     * Called only when the hasCustomReplacementWidgetResolver method return false.
     * @param connectionWidget the connection widget
     * @param replacementWidget the replacement widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    ConnectorState isReplacementWidget (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource);

    /**
     * Called to check whether the provider has a custom replacement widget resolver.
     * @param scene the scene where the resolver will be called
     * @return if true, then the resolveReplacementWidget method is called for resolving the replacement widget;
     *         if false, then the isReplacementWidget method is called for resolving the replacement widget
     */
    boolean hasCustomReplacementWidgetResolver (Scene scene);

    /**
     * Called to find the replacement widget of a possible connection.
     * Called only when a hasCustomReplacementWidgetResolver returns true.
     * @param scene the scene
     * @param sceneLocation the scene location
     * @return the replacement widget; null if no replacement widget found
     */
    Widget resolveReplacementWidget (Scene scene, Point sceneLocation);

    /**
     * Called for replacing a source/target with a new one.
     * This method is called only when the possible replacement is found and an user approves it.
     * @param connectionWidget the connection widget
     * @param replacementWidget the replacement widget
     * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
     */
    void reconnect (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource);

}
