/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.util.EventObject;

/**
 * Event object corresponding to a change made in an Ant project's metadata.
 * The event source is an {@link AntProjectHelper}.
 * @see AntProjectListener
 * @author Jesse Glick
 */
public final class AntProjectEvent extends EventObject {
    
    private final String path;
    private final boolean expected;
    
    AntProjectEvent(AntProjectHelper helper, String path, boolean expected) {
        super(helper);
        this.path = path;
        this.expected = expected;
    }
    
    /**
     * Get the associated Ant project helper object.
     * @return the project helper which fired the event
     */
    public AntProjectHelper getHelper() {
        return (AntProjectHelper)getSource();
    }
    
    /**
     * Get the path to the modified (or created or deleted) file.
     * Paths typically used are:
     * <ol>
     * <li>{@link AntProjectHelper#PROJECT_PROPERTIES_PATH}
     * <li>{@link AntProjectHelper#PRIVATE_PROPERTIES_PATH}
     * <li>{@link AntProjectHelper#PROJECT_XML_PATH}
     * <li>{@link AntProjectHelper#PRIVATE_XML_PATH}
     * </ol>
     * However for properties files, other paths may exist if the project
     * uses them for some purpose.
     * @return a project-relative path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Check whether the change was produced by calling methods on
     * {@link AntProjectHelper} or whether it represents a change
     * detected on disk.
     * @return true if the change was triggered by in-memory modification methods,
     *         false if occurred on disk in the metadata files and is being loaded
     */
    public boolean isExpected() {
        return expected;
    }
    
}
