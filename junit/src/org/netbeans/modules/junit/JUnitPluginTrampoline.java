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

package org.netbeans.modules.junit;

import java.util.Map;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.netbeans.modules.junit.plugin.JUnitPlugin.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Marian Petras
 */
public abstract class JUnitPluginTrampoline {
    
    /** the trampoline singleton, defined by {@link JUnitPlugin} */
    public static JUnitPluginTrampoline DEFAULT;
    
    /**
     * Provokes initialization of class JUnitPlugin.
     */
    {
        Class c = JUnitPlugin.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Used by {@link JUnitPlugin}. */
    public JUnitPluginTrampoline() {}
    
    /**
     * Returns a specification of a Java element or file representing test
     * for the given source Java element or file.
     *
     * @param  sourceLocation  specification of a Java element or file
     * @return  specification of a corresponding test Java element or file,
     *          or {@code null} if no corresponding test Java file is available
     */
    public abstract Location getTestLocation(
            JUnitPlugin plugin,
            Location sourceLocation);
    
    /**
     * Returns a specification of a Java element or file that is tested
     * by the given test Java element or test file.
     *
     * @param  testLocation  specification of a Java element or file
     * @return  specification of a Java element or file that is tested
     *          by the given Java element or file.
     */
    public abstract Location getTestedLocation(
            JUnitPlugin plugin,
            Location testLocation);
    
    /**
     * Creates test classes for given source classes.
     *
     * @param  filesToTest  source files for which test classes should be
     *                      created
     * @param  targetRoot   root folder of the target source root
     * @param  params  parameters of creating test class
     *                 - each key is an {@code Integer} whose value is equal
     *                 to some of the constants defined in the class;
     *                 the value is either
     *                 a {@code String} (for key with value {@code CLASS_NAME})
     *                 or a {@code Boolean} (for other keys)
     * @return  created test files
     */
    public abstract FileObject[] createTests(
            JUnitPlugin plugin,
            FileObject[] filesToTest,
            FileObject targetRoot,
            Map<CreateTestParam, Object> params);

}
