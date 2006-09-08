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

package org.netbeans.spi.project;

/**
 * Represents one user-selectable configuration of a particular project.
 * For example, it might represent a choice of main class and arguments.
 * Besides the implementor, only the project UI infrastructure is expected to use this class.
 *
 * @author Adam Sotona, Jesse Glick
 * @since org.netbeans.modules.projectapi/1 1.11
 * @see ProjectConfigurationProvider
 */
public interface ProjectConfiguration {

    /**
     * Provides a display name by which this configuration may be identified in the GUI.
     * @return a human-visible display name
     */
    String getDisplayName();

}
