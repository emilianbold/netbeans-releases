/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.*;
import java.util.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.*;

/**
 * Implementation of the "Default" platform. The information here is extracted
 * from the NetBeans' own runtime.
 *
 * @author Svata Dedic
 */
public class DefaultPlatformImpl extends J2SEPlatformImpl {


    public static final String DEFAULT_PLATFORM_ANT_NAME = ".default";           //NOI18N

    private ClassPath standardLibs;
    
    static JavaPlatform create(String srcFolder, String javadocFolder) {
        Map platformProperties = new HashMap ();
        String javaHome = System.getProperty("jdk.home");       //NOI18N
        platformProperties.put (PLAT_PROP_PLATFORM_HOME,javaHome);
        platformProperties.put (PLAT_PROP_PLATFORM_SOURCES, getSources (javaHome, srcFolder));
        platformProperties.put (PLAT_PROP_PLATFORM_JAVADOC,javadocFolder);
        return new DefaultPlatformImpl(platformProperties, new HashMap(System.getProperties()));
    }
    
    private DefaultPlatformImpl(Map platformProperties, Map systemProperties) {
        super(NbBundle.getMessage(DefaultPlatformImpl.class,"TXT_DefaultPlatform"),
              DEFAULT_PLATFORM_ANT_NAME, platformProperties, systemProperties);
    }

    public void setAntName(String antName) {
        //Not used by default platform
        //antName == null
    }

    public ClassPath getStandardLibraries() {
        if (standardLibs != null)
            return standardLibs;
        String s = System.getProperty(SYSPROP_JAVA_CLASS_PATH);       //NOI18N
        if (s == null) {
            s = ""; // NOI18N
        }
        return standardLibs = Util.createClassPath (s);
    }

    private static String getSources (String javaHome, String src) {
        if (javaHome != null) {
            File f = new File (javaHome);
            f = new File (f, "src.zip");    //NOI18N
            if (f.exists() && f.canRead()) {
                return f.getAbsolutePath();
            }
        }
        return src;
    }

}