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
 * A wrapper object for the ObjectScene when it is passed to ObjectSceneListener.
 *
 * @author William Headrick
 */
public class ObjectSceneEvent {

    private ObjectScene objectScene = null;

    ObjectSceneEvent (ObjectScene objectScene) {
        this.objectScene = objectScene;
    }

    /**
     * Returns an instance of related object scene.
     * @return the object scene
     */
    public ObjectScene getObjectScene () {
        return objectScene;
    }

}
