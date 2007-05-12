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
