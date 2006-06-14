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

package org.openide.cookies;

import java.io.IOException;
import org.openide.nodes.Node;

/**
 * Cookie that should be provided by all nodes that are able
 * to create a "instance".
 * Generally this is used to register objects declaratively in XML layers.
 *
 * @author Jaroslav Tulach
 */
public interface InstanceCookie extends Node.Cookie {

    /**
     * The name of {@link #instanceClass}.
     * Should be the same as <code>instanceClass().getName()</code>
     * but may be able to avoid actually loading the class.
     * @return the instance class name
     */
    public String instanceName();

    /**
     * The type that the instance is expected to be assignable to.
     * Can be used to test whether the instance is of an appropriate
     * class without actually creating it.
     *
     * @return the type (or perhaps some interesting supertype) of the instance
     * @exception IOException if metadata about the instance could not be read, etc.
     * @exception ClassNotFoundException if the instance type could not be loaded
     */
    public Class instanceClass() throws IOException, ClassNotFoundException;

    /**
     * Create an instance.
     * @return an object assignable to {@link #instanceClass}
     * @throws IOException for the same reasons as {@link #instanceClass}, or an object could not be deserialized, etc.
     * @throws ClassNotFoundException for the same reasons as {@link #instanceClass}
    */
    public Object instanceCreate() throws IOException, ClassNotFoundException;

    /**
     * Enhanced cookie that can answer queries about the type of the
     * instance it creates. It does not add any additional ability except to
     * improve performance, because it is not necessary to load
     * the actual class of the object into memory.
     *
     * @since 1.4
     */
    public interface Of extends InstanceCookie {
        /**
         * Checks if the object created by this cookie is an
         * instance of the given type. The same as
         * <code>type.isAssignableFrom(instanceClass())</code>
         * But this can prevent the actual class from being
         * loaded into the Java VM.
         *
         * @param type the class type we want to check
         * @return true if this cookie will produce an instance of the given type
        */
        public boolean instanceOf(Class type);
    }

}
