/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.filesystems.*;

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
            desc[0].setDisplayName (bundle.getString("PROP_readOnly"));
            desc[0].setShortDescription (bundle.getString("HINT_readOnly"));
            desc[1].setDisplayName (bundle.getString("PROP_valid"));
            desc[1].setShortDescription (bundle.getString("HINT_valid"));
            desc[1].setExpert (true);
            desc[2].setDisplayName (bundle.getString("PROP_hidden"));
            desc[2].setShortDescription (bundle.getString("HINT_hidden"));
            desc[3].setHidden (true);
            desc[4].setHidden (true);
            desc[5].setHidden (true);
            desc[6].setHidden (true);
            return desc;
        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
            return super.getPropertyDescriptors();
        }
    }

}
