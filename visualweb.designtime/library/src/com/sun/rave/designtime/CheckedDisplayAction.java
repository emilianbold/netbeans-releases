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
 * <P>The CheckedDisplayAction represents a checked display item (like a checkbox in a menu or on a
 * dialog).  The invoke method (inherited from DisplayAction) should toggle the checked state if
 * possible.</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  The BasicCheckedDisplayAction class can be used for convenience.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DisplayAction
 * @see com.sun.rave.designtime.impl.BasicCheckedDisplayAction
 */
public interface CheckedDisplayAction extends DisplayAction {

    /**
     * Returns the boolean checked state of this display item.
     *
     * @return <code>true</code> if this display action is checked, and <code>false</code> if not
     */
    public boolean isChecked();
}
