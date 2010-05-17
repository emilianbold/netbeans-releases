/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */package org.netbeans.modules.vmd.midp.converter.wizard;

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

    @Override
    public String toString () {
        return "ConverterItem: UID: "+ uid + ", ID: " + id + ", TypeID: " + typeid; // NOI18N
    }

    public Set<String> getPropertyNames () {
        return properties.keySet ();
    }

}
