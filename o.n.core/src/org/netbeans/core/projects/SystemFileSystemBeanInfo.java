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

package org.netbeans.core.projects;

import java.awt.Image;
import java.beans.*;

/** Object that provides beaninfo for a SystemFileSystem.
*
* @author Ian Formanek
*/
public class SystemFileSystemBeanInfo extends SimpleBeanInfo {
    /** Icon for image data objects. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        desc = new PropertyDescriptor[0];
    }

    /** Provides the LocalFileSystem's icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/systemFS.gif"); // NOI18N
            icon32 = loadImage("/org/netbeans/core/resources/systemFS32.gif"); // NOI18N
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        BeanInfo[] beanInfos = new BeanInfo [1];
        beanInfos[0] = new org.netbeans.beaninfo.FileSystemBeanInfo ();
        return beanInfos;
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }
}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Jaroslav Tulach I18N
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/7/99   Ian Formanek    Cleaned loading of icons
 *  3    Gandalf   1.2         3/13/99  Jaroslav Tulach Places.roots ()
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
