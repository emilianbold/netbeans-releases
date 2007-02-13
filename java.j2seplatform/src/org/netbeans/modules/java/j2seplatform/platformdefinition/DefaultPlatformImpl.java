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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.net.MalformedURLException;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.*;

/**
 * Implementation of the "Default" platform. The information here is extracted
 * from the NetBeans' own runtime.
 *
 * @author Svata Dedic
 */
public class DefaultPlatformImpl extends J2SEPlatformImpl {


    public static final String DEFAULT_PLATFORM_ANT_NAME = "default_platform";           //NOI18N

    private ClassPath standardLibs;
    
    static JavaPlatform create(Map properties, List sources, List javadoc) {
        if (properties == null) {
            properties = new HashMap ();
        }
        File javaHome = FileUtil.normalizeFile(new File(System.getProperty("jdk.home")));       //NOI18N
        List installFolders = new ArrayList ();
        try {
            installFolders.add (javaHome.toURI().toURL());
        } catch (MalformedURLException mue) {
            ErrorManager.getDefault().notify (mue);
        }
        if (sources == null) {
            sources = getSources (javaHome);
        }
        if (javadoc == null) {
            javadoc = getJavadoc (javaHome);
        }
        return new DefaultPlatformImpl(installFolders, properties, new HashMap(System.getProperties()), sources,javadoc);
    }
    
    private DefaultPlatformImpl(List installFolders, Map platformProperties, Map systemProperties, List sources, List javadoc) {
        super(null,DEFAULT_PLATFORM_ANT_NAME,
              installFolders, platformProperties, systemProperties, sources, javadoc);
    }

    public void setAntName(String antName) {
        throw new UnsupportedOperationException (); //Default platform ant name can not be changed
    }
    
    public String getDisplayName () {
        String displayName = super.getDisplayName();
        if (displayName == null) {
            displayName = NbBundle.getMessage(DefaultPlatformImpl.class,"TXT_DefaultPlatform", getSpecification().getVersion().toString());
            this.internalSetDisplayName (displayName);
        }
        return displayName;
    }
    
    public void setDisplayName(String name) {
        throw new UnsupportedOperationException (); //Default platform name can not be changed
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

    static List getSources (File javaHome) {
        if (javaHome != null) {
            try {
                File f;
                //On VMS, the root of the "src.zip" is "src", and this causes
                //problems with NetBeans 4.0. So use the modified "src.zip" shipped 
                //with the OpenVMS NetBeans 4.0 kit.
                if (Utilities.getOperatingSystem() == Utilities.OS_VMS) {
                    String srcHome = 
                        System.getProperty("netbeans.openvms.j2seplatform.default.srcdir");
                    if (srcHome != null)
                        f = new File(srcHome, "src.zip");
                    else
                        f = new File (javaHome, "src.zip");
                } else {
                    f = new File (javaHome, "src.zip");    //NOI18N
                    //If src.zip does not exist, try src.jar (it is on some platforms)
                    if (!f.exists()) {
                        f = new File (javaHome, "src.jar");    //NOI18N
                    }
                }
                if (f.exists() && f.canRead()) {
                    URL url = FileUtil.getArchiveRoot(f.toURI().toURL());
                    return Collections.singletonList (url);
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify (e);
            }              
        }
        return null;
    }
    
    
    static List getJavadoc (File javaHome) {
        if (javaHome != null ) {
            File f = new File (javaHome,"docs"); //NOI18N
            if (f.isDirectory() && f.canRead()) {
                try {
                    return Collections.singletonList(f.toURI().toURL());
                } catch (MalformedURLException mue) {
                    ErrorManager.getDefault().notify (mue);
                }
            }                        
        }
        return null;
    }

}