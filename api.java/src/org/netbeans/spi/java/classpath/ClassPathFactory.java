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

package org.netbeans.spi.java.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.classpath.ClassPathAccessor;

/**
 * Most general way to create {@link ClassPath} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link ClassPathImplementation} and use this factory.
 * See also {@link org.netbeans.spi.java.classpath.support.ClassPathSupport}
 * for easier ways to create classpaths.
 * @since org.netbeans.api.java/1 1.4
 */
public final class ClassPathFactory {

    private ClassPathFactory() {
    }

    /**
     * Create API classpath instance for the given SPI classpath.
     * @param spiClasspath instance of SPI classpath
     * @return instance of API classpath
     */
    public static ClassPath createClassPath(ClassPathImplementation spiClasspath) {
        return ClassPathAccessor.DEFAULT.createClassPath(spiClasspath);
    }

}
