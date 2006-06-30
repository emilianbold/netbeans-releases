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
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;



/**
 *
 * @author Tomas  Zezula
 */
public class PlatformSettings extends SystemOption {

    static final long serialVersionUID = -4636292603494310012L;

    private static PlatformSettings instance;

    private static final String PROP_PLATFORMS_FOLDER = "platformsFolder"; //NOI18N
    private static final String APPLE_JAVAVM_FRAMEWORK_PATH = "/System/Library/Frameworks/JavaVM.framework/Versions/";//NOI18N

    public PlatformSettings () {

    }

    public String displayName() {
        return NbBundle.getMessage(PlatformSettings.class,"TXT_PlatformSettings");
    }

    public File getPlatformsFolder () {
        String folderName = (String)this.getProperty(PROP_PLATFORMS_FOLDER);
        if (folderName == null) {
            File f;
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
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
        this.putProperty(PROP_PLATFORMS_FOLDER,file.getAbsolutePath(),true);
    }


    public synchronized static PlatformSettings getDefault () {
        if (instance == null) {
            instance = (PlatformSettings) SystemOption.findObject(PlatformSettings.class,true);
        }
        return instance;
    }
}
