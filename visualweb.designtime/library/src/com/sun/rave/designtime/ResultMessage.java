/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

import java.awt.Image;

/**
 * <p>A ResultMessage object represents a single message to a user about an operation that was just
 * completed (or failed).  ResultMessage objects are created and added to Result objects when
 * returning from an operation.</p>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see Result
 */
public class ResultMessage implements DisplayItem {

    /**
     * Used for type property - this message is informational, and may be displayed on the status
     * bar to the user.
     */
    public static final int TYPE_INFORMATION = 0;

    /**
     * Used for type property - this message is a warning, and may be displayed on the status bar
     * or in a dialog to the user.
     */
    public static final int TYPE_WARNING = 1;

    /**
     * Used for 'type' property - this message is critical, and will be displayed in a dialog to the
     * user.
     */
    public static final int TYPE_CRITICAL = 2;

    /**
     * Creates a new ResultMessage object with the specified type, displayName, and description.
     *
     * @param type The desired type of the message: TYPE_INFORMATION, TYPE_WARNING, or TYPE_CRITICAL.
     * @param displayName The desired display name of the message
     * @param description The desired description of the message
     * @return A newly created ResultMessage object
     */
    public static ResultMessage create(int type, String displayName, String description) {
        return new ResultMessage(type, displayName, description);
    }

    /**
     * Creates a new ResultMessage object with the specified type, displayName, description, and icon.
     *
     * @param type The desired type of the message: TYPE_INFORMATION, TYPE_WARNING, or TYPE_CRITICAL.
     * @param displayName The desired display name of the message
     * @param description The desired description of the message
     * @param smallIcon The desired image icon for the message
     * @return A newly created ResultMessage object
     */
    public static ResultMessage create(int type, String displayName, String description,
        Image smallIcon) {
        return new ResultMessage(type, displayName, description, smallIcon);
    }

    protected int type;
    protected String displayName;
    protected String description;
    protected Image smallIcon;
    protected Image largeIcon;
    protected String helpKey;

    public ResultMessage(int type, String displayName, String description) {
        if (type == TYPE_INFORMATION || type == TYPE_WARNING || type == TYPE_CRITICAL) {
            this.type = type;
        }
        else {
            throw new IllegalArgumentException(
                "Message type must be TYPE_INFORMATION (0), TYPE_WARNING (1), or TYPE_CRITICAL (2)"); // NOI18N
        }
        this.type = type;
        this.displayName = displayName;
        this.description = description;
    }

    public ResultMessage(int type, String displayName, String description, Image smallIcon) {
        this(type, displayName, description);
        this.smallIcon = smallIcon;
    }

    public void setMessageType(int type) {
        if (type == TYPE_INFORMATION || type == TYPE_WARNING || type == TYPE_CRITICAL) {
            this.type = type;
        }
        else {
            throw new IllegalArgumentException(
                "Message type must be TYPE_INFORMATION (0), TYPE_WARNING (1), or TYPE_CRITICAL (2)"); // NOI18N
        }
    }

    public int getMessageType() {
        return type;
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

    public void setHelpKey(String helpKey) {
        this.helpKey = helpKey;
    }

    public String getHelpKey() {
        return helpKey;
    }
}
