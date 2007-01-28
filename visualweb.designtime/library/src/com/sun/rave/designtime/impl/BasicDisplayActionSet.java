/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.impl;

import java.util.ArrayList;
import java.awt.Image;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayActionSet;

/**
 * A basic implementation of DisplayActionSet to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DisplayActionSet
 */
public class BasicDisplayActionSet extends BasicDisplayAction implements DisplayActionSet {

    protected boolean popup = false;
    protected ArrayList actionList = new ArrayList();

    public BasicDisplayActionSet() {
        super();
    }

    public BasicDisplayActionSet(String displayName) {
        super(displayName);
    }

    public BasicDisplayActionSet(String displayName, String description) {
        super(displayName, description);
    }

    public BasicDisplayActionSet(String displayName, String description, String helpKey) {
        super(displayName, description, helpKey);
    }

    public BasicDisplayActionSet(String displayName, String description, String helpKey,
        Image smallIcon) {
        super(displayName, description, helpKey, smallIcon);
    }

    public BasicDisplayActionSet(String displayName, String description, String helpKey,
        Image smallIcon, Image largeIcon) {
        super(displayName, description, helpKey, smallIcon, largeIcon);
    }

    public int getDisplayActionCount() {
        return actionList.size();
    }

    public DisplayAction getDisplayAction(int index) {
        return (DisplayAction)actionList.get(index);
    }

    public DisplayAction[] getDisplayActions() {
        return (DisplayAction[])actionList.toArray(new DisplayAction[actionList.size()]);
    }

    public void addDisplayAction(DisplayAction action) {
        actionList.add(action);
    }

    public void addDisplayAction(int index, DisplayAction action) {
        actionList.add(index, action);
    }

    public void removeDisplayAction(DisplayAction action) {
        actionList.remove(action);
    }

    public void removeDisplayAction(int index) {
        actionList.remove(index);
    }

    public void setPopup(boolean popup) {
        this.popup = popup;
    }

    public boolean isPopup() {
        return popup;
    }
}
