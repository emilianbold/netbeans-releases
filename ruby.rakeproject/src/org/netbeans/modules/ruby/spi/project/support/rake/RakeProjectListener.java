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

import java.util.EventListener;

/**
 * Listener for changes in Ant project metadata.
 * Most changes are in-memory while the project is still modified, but changes
 * may also be on disk.
 * <p>Event methods are fired with read access to
 * {@link org.netbeans.api.project.ProjectManager#mutex}.
 * @author Jesse Glick
 */
public interface RakeProjectListener extends EventListener {

    /**
     * Called when a change was made to an XML project configuration file.
     * @param ev an event with details of the change
     */
    void configurationXmlChanged(RakeProjectEvent ev);
    
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
    void propertiesChanged(RakeProjectEvent ev);
    
}
