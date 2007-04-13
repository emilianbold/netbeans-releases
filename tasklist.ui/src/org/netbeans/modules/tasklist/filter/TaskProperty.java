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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.filter;

import org.netbeans.spi.tasklist.Task;
import org.openide.util.NbBundle;



/**
 * Lightweight property for indirect access to suggestion
 * properties. Replaces both reflection and property-getter-dispatchers
 * in filters and view columns. Represents an API to add properties to
 * task views and filters. We don't like bean properties and reflection
 * for effectivity reasons.
 *
 * A property serves to extract the value it represents from
 * a given Suggestion.
 *
 * Properties for different views/filters/... are difined in factories
 * named in plural like SuggestionProperties, TaskProperties, etc.
 */
abstract class TaskProperty {
    protected TaskProperty(String id, Class valueClass) {
        this.id = id;
    }
    
    public String getID() { return id;}
    
    /**
     * Returns human readable name of this property. The name is
     * retrieved from the bundle stored in the same directory as
     * the real class of this property with the key:
     * "LBL_" + getID() + "Property".
     * @return localized String
     */
    public String getName() {
        if (name == null) {
            name = NbBundle.getMessage(this.getClass(), "LBL_" + id + "Property"); //NOI18N //NOI18N
        }
        return name;
    }
    
    
    /**
     * Extract the value represented by this property from the given
     * suggestion.
     * @param obj the Suggestion to extract from
     * @return Object value extracted
     */
    public abstract Object getValue(Task t);
    
    public String toString() { return id;}
    
    /**
     * Returns class of values of this property.
     * @return Class
     */
    public Class getValueClass() { return valueClass;}
    
    
    ///////
    private String id;
    transient private String name;
    private Class valueClass;
}

