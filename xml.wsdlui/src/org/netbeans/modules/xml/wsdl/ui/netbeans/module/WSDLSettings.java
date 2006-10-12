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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import java.io.ObjectInput;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Manages the WSDL editor options.
 *
 * @author  Nathan Fiedler
 */
public class WSDLSettings extends SystemOption {
    /** serial version UID - DO NOT CHANGE */
    private static final long serialVersionUID = -6445293321879342418L;
    /** Name of the connection timeout setting. */
    public static final String PROP_VIEW_MODE = "viewMode";

    /**
     * The view mode of the editor (e.g. tree, column).
     */
    public static enum ViewMode {
        TREE, COLUMN
    };

    public String displayName() {
        return NbBundle.getMessage(getClass(), "CTL_WSDLSettings_name");
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the instance.
     */
    public static WSDLSettings getDefault() {
        return (WSDLSettings) findObject(WSDLSettings.class, true);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Retrieves the view mode value.
     *
     * @return  view mode.
     */
    public ViewMode getViewMode() {
        String mode = (String) getProperty(PROP_VIEW_MODE);
        try {
            return ViewMode.valueOf(mode);
        } catch (IllegalArgumentException iae) {
            // The enum values changed, use a reasonable default.
            return ViewMode.COLUMN;
        }
    }

    protected void initialize() {
        super.initialize();
        setDefaults();
    }

    public void readExternal(ObjectInput in) throws
            IOException, ClassNotFoundException {
        super.readExternal(in);
        // Upgrade the restored instance to include the latest settings.
        setDefaults();
    }

    /**
     * For those properties that have null values, set them to the default.
     */
    private void setDefaults() {
        if (getProperty(PROP_VIEW_MODE) == null) {
            putProperty(PROP_VIEW_MODE, ViewMode.TREE.toString());
        }
    }

    /**
     * Sets the view mode value.
     *
     * @param  mode  new view mode value.
     */
    public void setViewMode(ViewMode mode) {
        // Store the enum as a String.
        putProperty(PROP_VIEW_MODE, mode.toString(), true);
    }
}
