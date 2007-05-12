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
 * <P>A BeanCreateInfoSet is a group version of the BeanCreateInfo interface.  It describes a
 * single item on a Palette that will create a set of beans in a visual designer.  This includes a
 * display name, description, icon, etc.  There is also (most importantly) a hook to
 * programmatically manipulate the newly created beans immediately after they have been created.
 * This is useful for setting the default state for the newly created set of beans.</P>
 *
 * <P>If the any of the specified JavaBeans have an associated DesignInfo, the DesignInfo's
 * 'beanCreatedSetup' method will be called before the BeanCreateInfoSet's 'beansCreatedSetup' method
 * will be called.  This gives the DesignInfo the "first crack", but it ultimately gives the
 * BeanCreateInfoSet the "last word".</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  The BasicBeanCreateInfoSet class can be used for convenience.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.impl.BasicBeanCreateInfoSet
 */
public interface BeanCreateInfoSet extends DisplayItem {

    /**
     * Returns an array of class names of the new JavaBeans to create when this BeanCreateInfoSet
     * is invoked in a visual designer.
     *
     * @return A String[] of fully qualified class names for the JavaBeans to create.
     */
    public String[] getBeanClassNames();

    /**
     * <p>A hook that gets called after the full set of JavaBean gets created.  This is useful for
     * a component author to setup an initial state for a set of JavaBeans when they are first
     * created.  Note that this method is only called one time after the JavaBeans are initially
     * created from the palette.  This is *not* a hook that is called each time the project is
     * reopened.</p>
     *
     * <P>If the any of the specified JavaBeans have an associated DesignInfo, the DesignInfo's
     * 'beanCreated' method will be called before each of the BeanCreateInfo's 'beanCreated' methods
     * are called.  Once all of the beans have been created, and the individual 'beanCreated' methods
     * have been called, this 'beansCreated' method will be called.  This gives the DesignInfo the
     * "first crack", but it ultimately gives the BeanCreateInfoSet the "last word".</P>
     *
     * @param designBeans The array of DesignBean objects representing the JavaBeans that have just been
     *        created.
     * @return A standard Result object, indicating success or failure - and optionally including
     *         messages for the user.
     */
    public Result beansCreatedSetup(DesignBean[] designBeans);
}
