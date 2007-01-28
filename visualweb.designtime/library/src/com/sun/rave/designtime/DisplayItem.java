/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

import java.awt.Image;

/**
 * <P>The DisplayItem interface describes the basic information needed to display an action in a
 * menu or a button.  Several interfaces in Creator Design-Time API extend this one to provide a
 * basic name, description, icon, etc.</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  There are several Basic* classes that implement this interface
 * for you.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DisplayItem {

    /**
     * Returns a display name for this item.  This will be used to show in a menu or as a button
     * label, depending on the subinterface.
     *
     * @return A String representing the display name for this item.
     */
    public String getDisplayName();

    /**
     * Returns a description for this item.  This will be used as a tooltip in a menu or on a
     * button, depending on the subinterface.
     *
     * @return A String representing the description for this item.
     */
    public String getDescription();

    /**
     * Returns a large image icon for this item.  Generally "large" means 32x32 pixels.
     *
     * @return An Image representing the large icon for this item.
     */
    public Image getLargeIcon();

    /**
     * Returns a small image icon for this item.  Generally "small" means 16x16 pixels.
     *
     * @return An Image representing the large icon for this item.
     */
    public Image getSmallIcon();

    /**
     * Returns the help key for this item.  This is usually a key used to look up a help context
     * item in an online help facility.
     *
     * @return A String representing the help key for this item.
     */
    public String getHelpKey();
}
