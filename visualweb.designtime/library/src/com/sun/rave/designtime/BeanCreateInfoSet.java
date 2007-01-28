/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
