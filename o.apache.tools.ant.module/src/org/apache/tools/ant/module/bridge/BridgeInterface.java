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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import org.openide.filesystems.FileObject;

/**
 * What is implemented by bridge.jar.
 * @author Jesse Glick
 */
public interface BridgeInterface {
    
    /**
     * Actually run a build script.
     * @param buildFile an Ant build script
     * @param targets a list of target names to run, or null to run the default target
     */
    boolean run(File buildFile, FileObject buildFileObject, List targets, InputStream in, PrintStream out, PrintStream err, Properties properties, int verbosity, boolean useStatusLine, String displayName);
    
    /**
     * Get some informational value of the Ant version.
     * @return the version
     */
    String getAntVersion();
    
    /**
     * Check whether Ant 1.6 is loaded.
     * If so, additional abilities may be possible, such as namespace support.
     */
    boolean isAnt16();
    
    /**
     * Get a proxy for IntrospectionHelper, to introspect task + type structure.
     */
    IntrospectionHelperProxy getIntrospectionHelper(Class clazz);
    
    /**
     * See Project.toBoolean.
     */
    boolean toBoolean(String val);
    
    /**
     * Get values of an enumeration class.
     * If it is not actually an enumeration class, return null.
     */
    String[] getEnumeratedValues(Class c);
    
}
