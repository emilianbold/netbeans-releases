/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

/**
 * <P>A DisplayAction represents a menu item or dialog button - depending on where and how it is
 * used.  It has a 'displayName' and a 'description' so that it can display localized text to the
 * user.  DisplayActions may be enabled or disabled ('enabled' property) to appear grayed-out as
 * menu items or buttons.  A DisplyAction can be 'invoked', and thus execute custom behavior when
 * it is activated from a menu-pick or button-click.</P>
 *
 * <P>DisplayAction extends the DisplayItem interface, thus it includes 'displayName', 'description',
 * 'largeIcon', and 'smallIcon' properties.</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  The BasicDisplayAction class can be used for convenience.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.impl.BasicDisplayAction
 */
public interface DisplayAction extends DisplayItem {

    public static final DisplayAction[] EMPTY_ARRAY = {};

    /**
     * Returns <B>true</B> if this DisplayAction should be displayed as enabled, or <B>false</B> if
     * it should be disabled.
     *
     * @return <B>true</B> if this DisplayAction should be displayed as enabled, or <B>false</B> if
     *         it should be disabled.
     */
    public boolean isEnabled();

    /**
     * This method is called when a DisplayAction is selected from a context menu or 'clicked' when
     * it is displayed as a button.
     *
     * @return A standard Result object
     */
    public Result invoke();
}
