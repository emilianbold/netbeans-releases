/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.ui;

import org.openide.options.*;
import org.openide.util.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class FilesharingCollabletFactorySettings extends SystemOption {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    public static final String PROP_TEST = "test"; // NOI18N
    public static final String PROP_LOCK_TIMEOUT_INTERVAL = "lockTimeoutInterval"; // NOI18N
    public static final String PROP_MAX_SHARED_FILE_FOLDERS = "maxSharedFileFolders"; // NOI18N

    /**
     *
     *
     */
    public FilesharingCollabletFactorySettings() {
        super();
    }

    /**
     *
     *
     */
    protected void initialize() {
        super.initialize();

        // If you have more complex default values which might require
        // other parts of the module to already be installed, do not
        // put them here; e.g. make the getter return them as a
        // default if getProperty returns null. (The class might be
        // initialized partway through module installation.)
        setLockTimeoutInterval(new Integer(2));
        setMaxSharedFileFolders(new Integer(20));
    }

    /**
     *
     *
     */
    public String displayName() {
        return NbBundle.getMessage(
            FilesharingCollabletFactorySettings.class, "LBL_FilesharingCollabletFactorySettings_DisplayName"
        );
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        // If you provide context help then use:
        // return new HelpCtx(CollabSettings.class);
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static FilesharingCollabletFactorySettings getDefault() {
        FilesharingCollabletFactorySettings result = (FilesharingCollabletFactorySettings) findObject(
                FilesharingCollabletFactorySettings.class, true
            );
        assert result != null : "Default FilesharingCollabletFactorySettings object was null";

        return result;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static FilesharingCollabletFactorySettings getDefault(boolean value) {
        return (FilesharingCollabletFactorySettings) findObject(FilesharingCollabletFactorySettings.class, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Option property methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public String getTest() {
        return (String) getProperty(PROP_TEST);
    }

    /**
     *
     *
     */
    public void setTest(String value) {
        putProperty(PROP_TEST, value, true);
    }

    /**
     *
     *
     */
    public Integer getLockTimeoutInterval() {
        return (Integer) getProperty(PROP_LOCK_TIMEOUT_INTERVAL);
    }

    /**
     *
     *
     */
    public void setLockTimeoutInterval(Integer value) {
        putProperty(PROP_LOCK_TIMEOUT_INTERVAL, value, true);
    }

    /**
     *
     *
     */
    public Integer getMaxSharedFileFolders() {
        return (Integer) getProperty(PROP_MAX_SHARED_FILE_FOLDERS);
    }

    /**
     *
     *
     */
    public void setMaxSharedFileFolders(Integer value) {
        putProperty(PROP_MAX_SHARED_FILE_FOLDERS, value, true);
    }
}
