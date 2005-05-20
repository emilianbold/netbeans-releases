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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.openide.ErrorManager;

/**
 * TODO - Comment whole code!
 *
 * @author Martin Krauskopf
 */
final class ManifestManager {
    
    private String codeNameBase;
    private String releaseVersion;
    private String specificationVersion;
    private String localizingBundle;
    private String layer;
    private String classPath;
    
    private static final String OPENIDE_MODULE = "OpenIDE-Module"; // NOI18N
    private static final String OPENIDE_MODULE_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version"; // NOI18N
    private static final String OPENIDE_MODULE_LAYER = "OpenIDE-Module-Layer"; // NOI18N
    private static final String OPENIDE_MODULE_LOCALIZING_BUNDLE = "OpenIDE-Module-Localizing-Bundle"; // NOI18N
    private static final String CLASS_PATH = "Class-Path"; // NOI18N
    
    static final ManifestManager NULL_INSTANCE = new ManifestManager();
    
    private ManifestManager() {}
    
    private ManifestManager(String cnb, String releaseVersion, String specVer,
            String locBundle, String layer, String classPath) {
        this.codeNameBase = cnb;
        this.releaseVersion = releaseVersion;
        this.specificationVersion = specVer;
        this.localizingBundle = locBundle;
        this.layer = layer;
        this.classPath = classPath;
    }
    
    static ManifestManager getInstance(File manifest) {
        ManifestManager mm = null;
        InputStream mis = null;
        try {
            if (manifest.exists()) {
                mis = new FileInputStream(manifest); // NOI18N
                Manifest mf = new Manifest(mis);
                mm = ManifestManager.getInstance(mf);
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (mis != null) { mis.close(); }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return mm;
    }
    
    static ManifestManager getInstanceFromJAR(File jar) {
        ManifestManager mm = null;
        JarFile jf = null;
        try {
            jf = new JarFile(jar);
            mm = ManifestManager.getInstance(jf.getManifest());
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (jf != null) { jf.close(); }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return mm;
    }
    
    static ManifestManager getInstance(Manifest manifest) {
        Attributes attr = manifest.getMainAttributes();
        String codename = attr.getValue(OPENIDE_MODULE);
        String codenamebase = null;
        String releaseVersion = null;
        if (codename != null) {
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                codenamebase = codename;
            } else {
                codenamebase = codename.substring(0, slash);
                releaseVersion = codename.substring(slash + 1);
            }
        }
        ManifestManager mm = new ManifestManager(
                codenamebase, releaseVersion,
                attr.getValue(OPENIDE_MODULE_SPECIFICATION_VERSION),
                attr.getValue(OPENIDE_MODULE_LOCALIZING_BUNDLE),
                attr.getValue(OPENIDE_MODULE_LAYER),
                attr.getValue(CLASS_PATH));
        return mm;
    }
    
    String getCodeNameBase() {
        return codeNameBase;
    }
    
    String getReleaseVersion() {
        return releaseVersion;
    }
    
    String getSpecificationVersion() {
        return specificationVersion;
    }
    
    String getLocalizingBundle() {
        return localizingBundle;
    }
    
    String getLayer() {
        return layer;
    }
    
    String getClassPath() {
        return classPath;
    }
}
