/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.annotations;

import org.openide.text.*;


/**
 * Region Annotation class
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public abstract class CollabRegionAnnotation extends Annotation {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String annotation;

    public CollabRegionAnnotation() {
    }

    public CollabRegionAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public abstract String getAnnotationType();

    public String getShortDescription() {
        return annotation;
    }

    public void setShortDescription(String description) {
        this.annotation = description;
    }
}
