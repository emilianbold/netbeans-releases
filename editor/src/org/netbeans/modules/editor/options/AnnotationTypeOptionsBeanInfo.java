/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.beans.SimpleBeanInfo;
import java.awt.Image;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;

/** BeanInfo for AnnotationTypeOptions
 *
 * @author David Konecny
 * @since 07/2001
 */
public class AnnotationTypeOptionsBeanInfo extends SimpleBeanInfo {

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;

    /* PropertyDescriptotrs
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null) {
            try {
                descriptors = new PropertyDescriptor[] {
                                  new PropertyDescriptor("highlightColor", AnnotationTypeOptions.class), // NOI18N
                                  new PropertyDescriptor("foregroundColor", AnnotationTypeOptions.class), // NOI18N
                                  new PropertyDescriptor("inheritForegroundColor", AnnotationTypeOptions.class), // NOI18N
                                  new PropertyDescriptor("wholeLine", AnnotationTypeOptions.class, "isWholeLine", null) // NOI18N
                              };
            } catch (Exception e) {
                descriptors = new PropertyDescriptor[0];
            }
        }
        return descriptors;
    }

}

