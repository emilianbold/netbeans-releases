/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package com.sun.rave.designtime;

import com.sun.rave.designtime.event.DesignBeanListener;

/**
 * <P>The DesignInfo interface is another type of BeanInfo interface to provide more live design-
 * time functionality for a JavaBean.  BeanInfo represents static meta-data about a JavaBean, while
 * DesignInfo provides dynamic design-time behavior.</P>
 *
 * <P>To provide a DesignInfo for a JavaBean, a component author must provide an implementation
 * of the DesignInfo interface available at design-time that matches the name of the JavaBean
 * class with "DesignInfo" appended to it.</P>
 *
 * <P>For example, a component author may supply a JavaBean class named 'com.company.Donkey', and
 * may also supply a corresponding 'com.company.DonkeyBeanInfo' (implements BeanInfo) as well as
 * 'com.company.DonkeyDesignInfo' (implements DesignInfo).  Note that these cannot be the same
 * class, as there is no gaurantee that the supplied BeanInfo class will be the same instance that
 * is used in the designer - typically, a BeanInfo class is 'deep-copied' into another instance
 * inside of an IDE.</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  BasicDesignInfo is supplied for convenience for subclassing.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.impl.BasicDesignInfo
 */
public interface DesignInfo extends DesignBeanListener {

    /**
     * Returns the class type of the JavaBean that this DesignInfo was designed to work with
     *
     * @return The JavaBean's class type object
     */
    public Class getBeanClass();

    /**
     * <p>Returns <code>true</code> if this child component (passed as 'childBean' and/or
     * 'childClass') can be added as a child to the specified parent component (passed as
     * 'parentBean').  This allows a component author to dynamically inspect the component hierarchy
     * to determine if a particular component may be inserted.</p>
     *
     * <p>This method is called on the DesignInfo representing the childBean component any time a
     * new component is being created, or dragged around in the visual designer.</p>
     *
     * <p>Note that the 'childBean' argument may be null if this operation is happening as a result
     * of a fresh component drop from the palette.  In that case, the child component instance will
     * not be created until the actual drop happens, thus these checks must be done with only the
     * child component's Class.</p>
     *
     * @param parentBean The DesignBean representing the potential parent component to receive the
     *        child
     * @param childBean The DesignBean representing the potential child component that is being
     *        created or reparented.  This argument may be null if this represents an initial drag
     *        from the palette, where the child bean has not been instantiated yet.
     * @param childClass The Class object representing the potential child component that is being
     *        created or reparented.
     * @return <code>true</code> if this parent bean is suitable for this child bean, or
     *         <code>false</code> if not
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass);

    /**
     * <p>Returns <code>true</code> if this child component (passed as 'childBean' and/or
     * 'childClass') can be added as a child to the specified parent component (passed as
     * 'parentBean').  This allows a component author to dynamically inspect the component hierarchy
     * to determine if a particular component may be inserted.</p>
     *
     * <p>This method is called on the DesignInfo representing the parentBean component any time a
     * new component is being created or dragged around in the visual designer.</p>
     *
     * <p>Note that the 'childBean' argument may be null if this operation is happening as a result
     * of a fresh component drop from the palette.  In that case, the child component instance will
     * not be created until the actual drop happens, thus these checks must be done with only the
     * child component's Class.</p>
     *
     * @param parentBean The DesignBean representing the potential parent component to receive the
     *        child
     * @param childBean The DesignBean representing the potential child component that is being
     *        created or reparented.  This argument may be null if this represents an initial drag
     *        from the palette, where the child bean has not been instantiated yet.
     * @param childClass The Class object representing the potential child component that is being
     *        created or reparented.
     * @return <code>true</code> if this child bean is suitable for this parent bean, or
     *         <code>false</code> if not
     */
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass);

    /**
     * Provides an opportunity for a DesignInfo to setup the initial state of a newly created
     * bean.  Anything can be done here, including property settings, event hooks, and even the
     * creation of other ancillary beans within the context.  Note that this method is only called
     * once after the component has been first created from the palette.
     *
     * @param designBean The bean that was just created
     * @return A Result object, indicating success or failure and including messages for the user
     */
    public Result beanCreatedSetup(DesignBean designBean);

    /**
     * Provides an opportunity for a DesignInfo to fix-up the state of a pasted bean. Anything can
     * be done here, including property settings, event hooks, and even the creation of other
     * ancillary beans within the context.
     *
     * @param designBean The bean that was just pasted from the clipboard
     * @return A Result object, indicating success or failure and including messages for the user
     */
    public Result beanPastedSetup(DesignBean designBean);

    /**
     * Provides an opportunity for a DesignInfo to cleanup just before a bean gets deleted.
     * Anything can be done here, including property settings, event hooks, and even the
     * creation/deletion of other ancillary beans within the context.  Note, however, that this
     * DesignBean will be deleted immediately upon the return of this method.  This is intended for
     * cleanup of ancillary items created in 'beanCreated'.
     *
     * @param designBean The bean that is about to be deleted
     * @return A Result object, indicating success or failure and including messages for the user
     */
    public Result beanDeletedCleanup(DesignBean designBean);

    /**
     * Returns the list (or hierarchy) of items to be included in a right-click context menu for
     * this bean at design-time.
     *
     * @param designBean The DesignBean that a user has right-clicked on
     * @return An array of DisplayAction objects representing a context menu to display to the user
     */
    public DisplayAction[] getContextItems(DesignBean designBean);

    /**
     * This method is called when an object from a design surface or palette is being dragged 'over'
     * a JavaBean type handled by this DesignInfo.  If the 'sourceBean' or 'sourceClass' is of
     * interest to the 'targetBean' instance or vice-versa (they can be "linked"), this method
     * should return <code>true</code>.  The user will then be presented with visual cues that this
     * is an appropriate place to 'drop' the item and establish a link.  If the user decides to drop
     * the item on this targetBean, the 'linkBeans' method will be called.  Note that the
     * 'sourceBean' argument may be null if this drag operation is originating from the palette,
     * because an instance of the bean will not have been created yet.
     *
     * @param targetBean The DesignBean instance that the user is 'hovering' the mouse over
     * @param sourceBean The DesignBean instance that the user may potentially 'drop' to link - may
     *        be null if this drag operation originated from the palette, because the instance will
     *        not have been created yet
     * @param sourceClass The class type of the object that the user may potentially 'drop' to link
     * @return <code>true</code> if the 'targetBean' cares to have the 'sourceBean' or an instance
     *         of type 'sourceClass' linked to it, <code>false</code> if not
     * @see linkBeans(DesignBean, DesignBean)
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass);

    /**
     * <P>This method is called when an object from a design surface or palette is being dropped or
     * has been dropped 'on' a JavaBean type handled by this DesignInfo (to establish a link). This
     * method will not be called unless the corresponding 'acceptLink' method call returned
     * <code>true</code> for at least one of the beans involved.  Typically, this results in new
     * property settings on potentially both of the DesignBean objects.</P>
     *
     * @param targetBean The target DesignBean instance that the user has 'dropped' an object onto
     *        to establish a link
     * @param sourceBean The DesignBean instance that has been 'dropped'
     * @return A Result object, indicating success or failure and including messages for the user
     * @see acceptLink(DesignBean, DesignBean, Class)
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean);
}
