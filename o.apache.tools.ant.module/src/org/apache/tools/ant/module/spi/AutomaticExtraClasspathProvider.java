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

package org.apache.tools.ant.module.spi;

import java.io.File;

/**
 * Permits a module to register special additional JARs (or directories)
 * to be added to Ant's primary classpath by default.
 * This should <em>only</em> be used to supply libraries needed by
 * standard "optional" tasks that ship with Ant - e.g. <code>junit.jar</code>
 * as needed by the <code>&lt;junit&gt;</code> task.
 * Register instances to default lookup.
 * @since org.apache.tools.ant.module/3 3.8
 * @author Jesse Glick
 */
public interface AutomaticExtraClasspathProvider {
    
    /**
     * Return a (possibly empty) list of items to add to the
     * automatic classpath used by default when running Ant.
     * Note that the result is not permitted to change between calls
     * in the same VM session.
     * <p>
     * The user may be able to override this path, so there is no
     * firm guarantee that the items will be available when Ant is run.
     * However by default they will be.
     * @return zero or more directories or JARs to add to Ant's startup classpath
     * @see org.openide.modules.InstalledFileLocator
     */
    File[] getClasspathItems();
    
}
