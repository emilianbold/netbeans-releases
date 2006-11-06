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
