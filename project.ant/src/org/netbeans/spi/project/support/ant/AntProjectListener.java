/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.util.EventListener;

/**
 * Listener for changes in Ant project metadata.
 * Most changes are in-memory while the project is still modified, but changes
 * may also be on disk.
 * <p>Event methods are fired with read access to
 * {@link org.netbeans.api.project.ProjectManager#mutex}.
 * @author Jesse Glick
 */
public interface AntProjectListener extends EventListener {
    
    /**
     * Called when a change was made to an XML project configuration file.
     * @param ev an event with details of the change
     */
    void configurationXmlChanged(AntProjectEvent ev);
    
    /**
     * Called when a change was made to a properties file that might be shared with Ant.
     * <p class="nonnormative">
     * Note: normally you would not use this event to detect property changes.
     * Use the property change listener from {@link PropertyEvaluator} instead to find
     * changes in the interpreted values of Ant properties, possibly coming from multiple
     * properties files.
     * </p>
     * @param ev an event with details of the change
     */
    void propertiesChanged(AntProjectEvent ev);
    
}
