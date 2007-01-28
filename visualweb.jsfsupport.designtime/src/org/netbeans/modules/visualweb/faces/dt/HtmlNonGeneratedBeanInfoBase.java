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
package org.netbeans.modules.visualweb.faces.dt;
/*
 * Created on Mar 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
import java.beans.BeanDescriptor;
import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.base.CategoryDescriptors;
import com.sun.rave.designtime.Constants;

/**
 * @author eric
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HtmlNonGeneratedBeanInfoBase extends HtmlBeanInfoBase {

    public void annotateBeanDescriptor(BeanDescriptor beanDescriptor) {
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,
            new CategoryDescriptor[] {
            CategoryDescriptors.GENERAL,
            CategoryDescriptors.APPEARANCE,
            CategoryDescriptors.DATA,
            CategoryDescriptors.EVENTS,
            CategoryDescriptors.JAVASCRIPT,
            CategoryDescriptors.ADVANCED,
            CategoryDescriptors.INTERNAL});
    }

}
