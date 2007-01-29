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

import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignUtil;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import com.sun.rave.designtime.Constants;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.ImageHyperlink} component.
 */
public class ImageHyperlinkBeanInfo extends ImageHyperlinkBeanInfoBase {

    public ImageHyperlinkBeanInfo() {
        super();
        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("action")) //NOI18N
                descriptors[i].setHidden(true);
        }
        this.getBeanDescriptor().setValue(
            Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
            new String[] { "*text://a" }); // NOI18N

        // This is set here rather than using <resize-constraints> metadata
        // in conf-image-renderer.xml because we need to do a bitwise
        // or of two constants, which the DTD (or code generator) doesn't
        // allow
        this.getBeanDescriptor().setValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS,
            new Integer(Constants.ResizeConstraints.MAINTAIN_ASPECT_RATIO|Constants.ResizeConstraints.ANY));
    }

    private EventSetDescriptor[] eventSetDescriptors;

    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null)
            eventSetDescriptors = DesignUtil.generateCommandEventSetDescriptors(this);
        return eventSetDescriptors;
    }
}
