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
package org.netbeans.api.visual.model;

/**
 * This enum is used for specifying events in which an object scene listener is interested.
 *
 * @author David Kaspar
 */
public enum ObjectSceneEventType {

    /**
     * Related to ObjectSceneListener.objectAdded method.
     */
    OBJECT_ADDED,

    /**
     * Related to ObjectSceneListener.objectRemoved method.
     */
    OBJECT_REMOVED,

    /**
     * Related to ObjectSceneListener.objectStateChanged method.
     */
    OBJECT_STATE_CHANGED,

    /**
     * Related to ObjectSceneListener.selectionChanged method.
     */
    OBJECT_SELECTION_CHANGED,

    /**
     * Related to ObjectSceneListener.highlightingChanged method.
     */
    OBJECT_HIGHLIGHTING_CHANGED,

    /**
     * Related to ObjectSceneListener.hoverChanged method.
     */
    OBJECT_HOVER_CHANGED,

    /**
     * Related to ObjectSceneListener.focusChanged method.
     */
    OBJECT_FOCUS_CHANGED,

}
