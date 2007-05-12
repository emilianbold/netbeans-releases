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
