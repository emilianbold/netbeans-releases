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

import java.util.Set;

/**
 * Describes the structure of a task.
 * Each instance corresponds to one task or nested element in a build script.
 * SPI clients are forbidden to implement this interface;
 * new methods may be added in the future.
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public interface TaskStructure {
    
    /**
     * Get the element name.
     * XXX precise behavior w.r.t. namespaces etc.
     * @return a name, never null
     */
    String getName();
    
    /**
     * Get a single attribute.
     * It will be unevaluated as configured in the script.
     * If you wish to find the actual runtime value, you may
     * use {@link AntEvent#evaluate}.
     * @param name the attribute name
     * @return the raw value of that attribute, or null
     */
    String getAttribute(String name);
    
    /**
     * Get a set of all defined attribute names.
     * @return a set of names suitable for {@link #getAttribute}; may be empty but not null
     */
    Set/*<String>*/ getAttributeNames();
    
    /**
     * Get configured nested text.
     * It will be unevaluated as configured in the script.
     * If you wish to find the actual runtime value, you may
     * use {@link AntEvent#evaluate}.
     * @return the raw text contained in the element, or null
     */
    String getText();

    /**
     * Get any configured child elements.
     * @return a list of child structure elements; may be empty but not null
     */
    TaskStructure[] getChildren();
    
}
