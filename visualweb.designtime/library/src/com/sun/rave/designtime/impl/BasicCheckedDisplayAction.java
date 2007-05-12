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
