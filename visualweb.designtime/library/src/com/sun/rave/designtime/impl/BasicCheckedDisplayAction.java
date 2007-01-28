/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.impl;

import java.awt.Image;
import com.sun.rave.designtime.CheckedDisplayAction;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of CheckedDisplayAction to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see CheckedDisplayAction
 */
public class BasicCheckedDisplayAction extends BasicDisplayAction implements CheckedDisplayAction {

    protected boolean checked = false;

    public BasicCheckedDisplayAction() {
        super();
    }

    public BasicCheckedDisplayAction(String displayName) {
        super(displayName);
    }

    public BasicCheckedDisplayAction(String displayName, String description) {
        super(displayName, description);
    }

    public BasicCheckedDisplayAction(String displayName, String description, String helpKey) {
        super(displayName, description, helpKey);
    }

    public BasicCheckedDisplayAction(String displayName, String description, String helpKey,
        Image smallIcon) {
        super(displayName, description, helpKey, smallIcon);
    }

    public BasicCheckedDisplayAction(String displayName, String description, String helpKey,
        Image smallIcon, Image largeIcon) {
        super(displayName, description, helpKey, smallIcon, largeIcon);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public Result invoke() {
        setChecked(!isChecked());
        return super.invoke();
    }
}
