/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.impl;

import java.awt.Image;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of DisplayAction to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DisplayAction
 */
public class BasicDisplayAction implements DisplayAction {

    protected String displayName;
    protected String description;
    protected String helpKey;
    protected Image smallIcon;
    protected Image largeIcon;
    protected boolean enabled = true;

    public BasicDisplayAction() {}

    public BasicDisplayAction(String displayName) {
        this.displayName = displayName;
    }

    public BasicDisplayAction(String displayName, String description) {
        this(displayName);
        this.description = description;
    }

    public BasicDisplayAction(String displayName, String description, String helpKey) {
        this(displayName, description);
        this.helpKey = helpKey;
    }

    public BasicDisplayAction(String displayName, String description, String helpKey,
        Image smallIcon) {
        this(displayName, description, helpKey);
        this.smallIcon = smallIcon;
    }

    public BasicDisplayAction(String displayName, String description, String helpKey,
        Image smallIcon, Image largeIcon) {
        this(displayName, description, helpKey, smallIcon);
        this.largeIcon = largeIcon;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSmallIcon(Image smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Image getSmallIcon() {
        return smallIcon;
    }

    public void setLargeIcon(Image largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Image getLargeIcon() {
        return largeIcon;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setHelpKey(String helpKey) {
        this.helpKey = helpKey;
    }

    public String getHelpKey() {
        return helpKey;
    }

    public Result invoke() {
        return Result.SUCCESS;
    }
}
