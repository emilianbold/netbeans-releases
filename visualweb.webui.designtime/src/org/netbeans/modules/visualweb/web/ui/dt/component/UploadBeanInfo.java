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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.beans.BeanDescriptor;

import com.sun.rave.designtime.Constants;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Upload}component.
 */
public class UploadBeanInfo extends UploadBeanInfoBase {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        // Do not allow component to be resized
        // This is set here rather than in XML metadata to get IDE support, but
        // maybe this isn't a good enough reason.
        beanDescriptor.setValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS,
                new Integer(Constants.ResizeConstraints.NONE));
        beanDescriptor.setValue(
            Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
            new String[] { "label://label" }); // NOI18N

        return beanDescriptor;
    }
}
