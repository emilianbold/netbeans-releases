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
package org.netbeans.modules.collab.channel.filesharing.config;

import org.openide.filesystems.*;
import org.openide.util.*;

import java.beans.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.config.FilesharingProcessorConfig;
import org.netbeans.modules.collab.channel.filesharing.mdc.*;


/**
 *
 *
 * @author  Ayub Khan <ayub.khan@sun.com>
 */
public class FilesharingProcessorConfigManager extends Object {
    /* instance */
    private static FilesharingProcessorConfigManager instance = null;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /*notifierConfig*/
    private FilesharingProcessorConfig processorConfig = null;

    /**
     *
     *
     */
    public FilesharingProcessorConfigManager() {
        super();
    }

    /**
     *
     * @return ID
     */
    public String getID() {
        return "fncmanager"; // NOI18N
    }

    /**
     *
     * @return displayName
     */
    public String getDisplayName() {
        return NbBundle.getMessage(
            FilesharingNotifierConfigManager.class, "LBL_FilesharingNotifierConfigManager_DisplayName"
        );
    }

    /**
     *
     * @param currentVersion
     * @return FilesharingNotifierConfig
     */
    public FilesharingProcessorConfig getProcessorConfig(String currentVersion, boolean createNew) {
        if (createNew || (processorConfig == null)) {
            processorConfig = new FilesharingProcessorConfig(currentVersion);
        }

        return processorConfig;
    }

    /**
     *
     * @param currentVersion
     * @return FilesharingNotifierConfig
     */
    public FilesharingProcessorConfig getProcessorConfig(String currentVersion) {
        return getProcessorConfig(currentVersion, false);
    }

    /**
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     *
     * @return CollabFilesystem Manager
     */
    public static FilesharingProcessorConfigManager getDefault() {
        if (instance == null) {
            instance = new FilesharingProcessorConfigManager();
        }

        return instance;
    }
}
