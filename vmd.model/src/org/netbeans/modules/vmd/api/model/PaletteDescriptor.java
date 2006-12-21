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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.model;

import java.awt.*;

/**
 * This immutable class represents a palette descriptor used in component descriptor.
 * <p>
 * It holds information about the category id, display name, small icon, and large icon.
 *
 * @author David Kaspar
 */
public final class PaletteDescriptor {

    private final String categoryID;
    private final String displayName;
    private final String toolTip;
    private final String smallIcon;
    private final String largeIcon;

    /**
     * Creates a new palette descriptor for describing always visible component descriptor by specifying category id, display name, small and large icon.
     * @param categoryID the category id
     * @param displayName the display name
     * @param smallIcon the small icon
     * @param largeIcon the large icon
     */
    public PaletteDescriptor (String categoryID, String displayName, String toolTip, String smallIcon, String largeIcon) {
        this.categoryID = categoryID;
        this.displayName = displayName;
        this.toolTip = toolTip;
        this.smallIcon = smallIcon;
        this.largeIcon = largeIcon;
    }

    /**
     * Return a palette category id.
     * @return the palette category
     */
    public String getCategoryID () {
        return categoryID;
    }

    /**
     * Return a display name.
     * @return the display name
     */
    public String getDisplayName () {
        return displayName;
    }

    /**
     * Return a tool tip.
     * @return the tool tip
     */
    public String getToolTip() {
        return toolTip;
    }

    /**
     * Returns a small icon.
     * @return the small icon
     */
    public String getSmallIcon () {
        return smallIcon;
    }

    /**
     * Returns a large icon.
     * @return the large icon
     */
    public String getLargeIcon () {
        return largeIcon;
    }

    public boolean equals (Object o) {
        if (this == o)
            return true;
        if (o == null || getClass () != o.getClass ())
            return false;

        final PaletteDescriptor descriptor = (PaletteDescriptor) o;

        if (displayName != null ? ! displayName.equals (descriptor.displayName) : descriptor.displayName != null)
            return false;
        if (largeIcon != null ? ! largeIcon.equals (descriptor.largeIcon) : descriptor.largeIcon != null)
            return false;
        if (smallIcon != null ? ! smallIcon.equals (descriptor.smallIcon) : descriptor.smallIcon != null)
            return false;

        return true;
    }

    public int hashCode () {
        int result;
        result = displayName != null ? displayName.hashCode () : 0;
        result = 29 * result + (toolTip != null ? toolTip.hashCode () : 0);
        result = 29 * result + (smallIcon != null ? smallIcon.hashCode () : 0);
        result = 29 * result + (largeIcon != null ? largeIcon.hashCode () : 0);
        return result;
    }

}
