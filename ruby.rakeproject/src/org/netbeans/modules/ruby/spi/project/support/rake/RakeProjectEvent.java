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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.util.EventObject;

/**
 * Event object corresponding to a change made in an Ant project's metadata.
 * The event source is an {@link RakeProjectHelper}.
 * @see RakeProjectListener
 * @author Jesse Glick
 */
public final class RakeProjectEvent extends EventObject {

    private final String path;
    private final boolean expected;

    RakeProjectEvent(RakeProjectHelper helper, String path, boolean expected) {
        super(helper);
        this.path = path;
        this.expected = expected;
    }
    
    /**
     * Get the associated Ant project helper object.
     * @return the project helper which fired the event
     */
    public RakeProjectHelper getHelper() {
        return (RakeProjectHelper)getSource();
    }
    
    /**
     * Get the path to the modified (or created or deleted) file.
     * Paths typically used are:
     * <ol>
     * <li>{@link RakeProjectHelper#PROJECT_PROPERTIES_PATH}
     * <li>{@link RakeProjectHelper#PRIVATE_PROPERTIES_PATH}
     * <li>{@link RakeProjectHelper#PROJECT_XML_PATH}
     * <li>{@link RakeProjectHelper#PRIVATE_XML_PATH}
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
     * {@link RakeProjectHelper} or whether it represents a change
     * detected on disk.
     * @return true if the change was triggered by in-memory modification methods,
     *         false if occurred on disk in the metadata files and is being loaded
     */
    public boolean isExpected() {
        return expected;
    }
    
}
