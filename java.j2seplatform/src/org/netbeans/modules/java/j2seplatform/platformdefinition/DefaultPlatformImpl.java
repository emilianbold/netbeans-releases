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
import java.net.URL;
import java.util.*;
import java.net.MalformedURLException;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
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
        File javaHome = new File(System.getProperty("jdk.home"));       //NOI18N
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

    private static List getSources (File javaHome) {
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
                    //Test for src folder in the src.zip
                    URL url = FileUtil.getArchiveRoot(f.toURI().toURL());
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        fo = fo.getFileObject("src");    //NOI18N
                        if (fo != null) {
                            url = fo.getURL();
                        }
                    }
                    return Collections.singletonList (url);
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify (e);
            }
              catch (FileStateInvalidException e) {
                  ErrorManager.getDefault().notify (e);
              }
        }
        return null;
    }
    
    
    private static List getJavadoc (File javaHome) {
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