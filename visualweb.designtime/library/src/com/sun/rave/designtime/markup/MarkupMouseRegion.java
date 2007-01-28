/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
