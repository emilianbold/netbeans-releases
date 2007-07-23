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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author David Kaspar
*/
class ConverterItem {

    private String uid;
    private String id;
    private String typeid;
    private HashMap<String, String> properties;
    private HashMap<String, ArrayList<String>> containerProperties;
    private boolean used;
    private DesignComponent component;
    private boolean clazz;

    public ConverterItem (String uid, String id, String typeid) {
        this.uid = uid;
        this.id = id;
        this.typeid = typeid;
        properties = new HashMap<String, String> ();
        containerProperties = new HashMap<String, ArrayList<String>> ();
    }

    public boolean isUsed () {
        return used;
    }

    public void setUsed (DesignComponent component) {
        this.component = component;
        used = true;
    }

    public boolean isClass () {
        return clazz;
    }

    public void setClass () {
        this.clazz = true;
    }

    public DesignComponent getRelatedComponent () {
        return component;
    }

    public String getUID () {
        return uid;
    }

    public String getID () {
        return id;
    }

    public String getTypeID () {
        return typeid;
    }

    public String getPropertyValue (String name) {
        return properties.get (name);
    }

    public ArrayList<String> getContainerPropertyValue (String name) {
        return containerProperties.get (name);
    }

    public void addProperty (String name, String value) {
        properties.put (name, value);
    }

    public void initContainerProperty (String name) {
        containerProperties.put (name, new ArrayList<String> ());
    }

    public void addContainerPropertyItem (String name, String item) {
        containerProperties.get (name).add (item);
    }

    public boolean isPropertyValueSet (String name) {
        return properties.containsKey (name);
    }


    public String toString () {
        return "ConverterItem: UID: "+ uid + ", ID: " + id + ", TypeID: " + typeid; // NOI18N
    }

    public Set<String> getPropertyNames () {
        return properties.keySet ();
    }

}
