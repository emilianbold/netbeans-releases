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
package org.netbeans.modules.java.platform;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;



/**
 *
 * @author Tomas  Zezula
 */
public class PlatformSettings {
    private static final PlatformSettings INSTANCE = new PlatformSettings();
    private static final String PROP_PLATFORMS_FOLDER = "platformsFolder"; //NOI18N
    private static final String APPLE_JAVAVM_FRAMEWORK_PATH = "/System/Library/Frameworks/JavaVM.framework/Versions/";//NOI18N

    public PlatformSettings () {

    }

    public String displayName() {
        return NbBundle.getMessage(PlatformSettings.class,"TXT_PlatformSettings");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(PlatformSettings.class);
    }

    public File getPlatformsFolder () {
        String folderName = getPreferences().get(PROP_PLATFORMS_FOLDER, null);
        if (folderName == null) {
            File f;
            if (Utilities.isMac()) {
                f = new File (APPLE_JAVAVM_FRAMEWORK_PATH);
            }
            else {
                f = new File(System.getProperty("user.home"));  //NOI18N
                File tmp;
                while ((tmp = f.getParentFile())!=null) {
                    f = tmp;
                }
            }
            return f;
        }
        else {
            return new File (folderName);
        }
    }

    public void setPlatformsFolder (File file) {
        getPreferences().put(PROP_PLATFORMS_FOLDER, file.getAbsolutePath());
    }


    public synchronized static PlatformSettings getDefault () {
        return INSTANCE;
    }
}
