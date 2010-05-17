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

package com.sun.rave.designtime.markup;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;

/**
 * <p>A MarkupMouseRegion represents a portion (sub-region) of a markup component's rendered
 * markup that has special design-time behavior.  This special behavior may include a name,
 * description, right-click context menu, and/or custom behavior in response to mouse clicks.
 * </p>
 *
 * <p>An instance of MarkupMouseRegion is associated with a particular sub-region of markup via
 * a unique attribute name-value pair.  The 'MarkupDesignInfo.annoteRender' method adds the
 * additional design-time only attributes to the markup, as well as supplies an array of these
 * MarkupMouseRegion instances.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  BasicMarkupMouseRegion is available for convenient subclassing.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see BasicMarkupMouseRegion
 */
public interface MarkupMouseRegion extends DisplayItem {

    /**
     * Returns an array of DisplayAction objects - used to render a right-click context menu when
     * the user right-clicks on this mouse region.
     *
     * @return An array of DisplayAction objects
     */
    public DisplayAction[] getContextItems();

    /**
     * Returns <code>true</code> if this markup region wishes to respond to a mouse click (or series
     * of clicks).
     *
     * @return <code>true</code> if mouse clicks should be sent to this mouse region,
     *         <code>false</code> if not
     * @see regionClicked(int)
     */
    public boolean isClickable();

    /**
     * This method is called when a user clicks the mouse within the bounds of this mouse region.
     * This method is only called if the 'isClickable()' method returns <code>true</code>.
     *
     * @param clickCount The count of mouse clicks
     * @return A Result object
     * @see isClickable()
     */
    public Result regionClicked(int clickCount);

    /**
     * This method is called when an object from a design surface or palette is being dragged 'over'
     * a region represented by this MarkupMouseRegion.  If the 'sourceBean' or 'sourceClass' is of
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
     * @return <code>true</code> if the 'targetBean' cares to have an instance of type 'sourceClass'
     *         linked to it, <code>false</code> if not
     * @see linkBeans(DesignBean, DesignBean)
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass);

    /**
     * <P>This method is called when an object from a design surface or palette has been dropped
     * 'on' a region represented by this MarkupMouseRegion (to establish a link).  This method
     * will not be called unless the corresponding 'acceptLink' method call returned
     * <code>true</code>. Typically, this results in property settings on potentially both of the
     * DesignBean objects.</P>
     *
     * @param targetBean The target DesignBean instance that the user has dropped an object onto to
     *        establish a link
     * @param sourceBean The DesignBean instance that has been dropped.
     * @return A Result object, indicating success or failure and including messages for the user
     * @see acceptLink(DesignBean, DesignBean, Class)
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean);
}
