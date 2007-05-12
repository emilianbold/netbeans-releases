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

package com.sun.rave.designtime;

/**
 * <P>A BeanCreateInfo describes an item on a Palette that will create a bean in a visual designer.
 * This includes a display name, description, icon, etc.  There is also (most importantly) a hook
 * to programmatically manipulate the newly created bean immediately after is has been created.
 * This is useful for setting a default state for the newly created bean.</P>
 *
 * <P>If the specified JavaBean has an associated DesignInfo, the DesignInfo's 'beanCreated'
 * method will be called before the BeanCreateInfo's 'beanCreated' method is called.  This gives the
 * DesignInfo the "first crack", but it gives the BeanCreateInfo the "last word".</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  The BasicBeanCreateInfo class can be used for convenience.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.impl.BasicBeanCreateInfo
 */
public interface BeanCreateInfo extends DisplayItem {

    /**
     * Returns the class name of the new JavaBean to create when this BeanCreateInfo is invoked in
     * a visual designer.
     *
     * @return The String fully qualified class name for the JavaBean to create.
     */
    public String getBeanClassName();

    /**
     * <p>A hook that gets called after this JavaBean gets created initially.  This is useful for a
     * component author to setup an initial state for their JavaBean when it is first created.  Note
     * that this method is only called one time after the JavaBeans are initially created from the
     * palette.  This is *not* a hook that is called each time the project is reopened.</p>

     * <p>NOTE: If the specified bean has an associated DesignInfo class - it will have "first
     * crack" at modifying the initial state of the bean.  This method will be called after the
     * DesignInfo one is called.</p>
     *
     * @return A standard Result object, indicating success or failure - and optionally including
     *         messages for the user.
     */
    public Result beanCreatedSetup(DesignBean designBean);
}
