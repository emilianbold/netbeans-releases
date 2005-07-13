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
package org.netbeans.modules.java.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathImplementation;


public abstract class ClassPathAccessor {

    public static ClassPathAccessor DEFAULT;
    
    // force loading of ClassPath class. That will set DEFAULT variable.
    static {
        Class c = ClassPath.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public abstract ClassPath createClassPath(ClassPathImplementation spiClasspath);

    public abstract ClassPathImplementation getClassPathImpl (ClassPath cp);

}
