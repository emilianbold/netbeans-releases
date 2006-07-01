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
 */

package org.netbeans.beaninfo;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.filesystems.*;
import org.openide.util.Exceptions;

/** Object that provides beaninfo for {@link FileSystem}s.
*
* @author Ian Formanek
*/
public class FileSystemBeanInfo extends SimpleBeanInfo {

    /* Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                   new PropertyDescriptor ("readOnly", FileSystem.class, "isReadOnly", null), // 0 // NOI18N
                   new PropertyDescriptor ("valid", FileSystem.class, "isValid", null), // 1 // NOI18N
                   new PropertyDescriptor ("hidden", FileSystem.class, "isHidden", "setHidden"), // 2 // NOI18N
                   new PropertyDescriptor ("actions", FileSystem.class, "getActions", null), // 3 // NOI18N
                   new PropertyDescriptor ("displayName", FileSystem.class, "getDisplayName", null), // 4 // NOI18N
                   new PropertyDescriptor ("root", FileSystem.class, "getRoot", null), // 5 // NOI18N
                   new PropertyDescriptor ("status", FileSystem.class, "getStatus", null), // 6 // NOI18N
            };
            ResourceBundle bundle = NbBundle.getBundle(FileSystemBeanInfo.class);
            desc[0].setHidden(true);
            desc[1].setDisplayName (bundle.getString("PROP_valid"));
            desc[1].setHidden(true);
/*
            desc[1].setShortDescription (bundle.getString("HINT_valid"));
            desc[1].setExpert (true);
*/
            desc[2].setHidden(true);
            desc[3].setHidden (true);
            desc[4].setHidden (true);
            desc[5].setHidden (true);
            desc[6].setHidden (true);
            return desc;
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            return super.getPropertyDescriptors();
        }
    }

}
