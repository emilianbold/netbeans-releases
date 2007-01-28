/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
