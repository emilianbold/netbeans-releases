/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
