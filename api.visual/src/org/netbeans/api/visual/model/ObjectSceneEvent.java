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
