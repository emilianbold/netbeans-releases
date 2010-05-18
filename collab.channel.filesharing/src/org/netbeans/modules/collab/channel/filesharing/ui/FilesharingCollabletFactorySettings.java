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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        setLockTimeoutInterval(new Integer(6));//6 seconds
        setMaxSharedFileFolders(new Integer(20));//20 conversation folders
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
