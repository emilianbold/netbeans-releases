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
 */
package org.netbeans.modules.vmd.api.model.support;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author David Kaspar
 */
// TODO - merge with api.model.PropertValueSupport class
public class ArraySupport {

    public static void append (DesignComponent arrayComponent, String arrayPropertyName, DesignComponent itemToAppend) {
        PropertyValue value = arrayComponent.readProperty (arrayPropertyName);
        value = append (value, itemToAppend);
        arrayComponent.writeProperty (arrayPropertyName, value);
    }

    public static PropertyValue append (PropertyValue array, DesignComponent itemToAppend) {
        return append (array, PropertyValue.createComponentReference (itemToAppend));
    }

    public static PropertyValue append (PropertyValue array, PropertyValue itemToAppend) {
        ArrayList<PropertyValue> list = new ArrayList<PropertyValue> (array.getArray ());
        list.add (itemToAppend);
        array = PropertyValue.createArray (array.getType ().getComponentType (), list);
        return array;
    }

    public static void remove (DesignComponent arrayComponent, String arrayPropertyName, DesignComponent itemToRemove) {
        PropertyValue value = arrayComponent.readProperty (arrayPropertyName);
        value = remove (value, itemToRemove);
        arrayComponent.writeProperty (arrayPropertyName, value);
    }

    public static PropertyValue remove (PropertyValue array, DesignComponent itemToRemove) {
        ArrayList<PropertyValue> list = new ArrayList<PropertyValue> (array.getArray ());
        Iterator<PropertyValue> iterator = list.iterator ();
        while (iterator.hasNext ()) {
            PropertyValue value = iterator.next ();
            if (value.getComponent ().equals (itemToRemove))
                iterator.remove ();
        }
        array = PropertyValue.createArray (array.getType ().getComponentType (), list);
        return array;
    }

}
