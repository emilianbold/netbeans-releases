/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.net.URL;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Data model used across the <em>New Update Center Wizard</em>.
 * @author Jiri Rechtacek
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    static private String AUTOUPDATE_TYPES = "Services/AutoupdateType"; //NOI18N
    static private String AUTOUPDATE_SERVICE_TYPE = "update_center"; //NOI18N
    static private String AUTOUPDATE_SERVICE_TYPE_EXT = "settings"; //NOI18N
    static private String UC_LOCALIZING_BUNDLE = "SystemFileSystem.localizingBundle"; //NOI18N
    static private String AUTOUPDATE_MODULE = "org.netbeans.modules.autoupdate"; // NOI18N

    private CreatedModifiedFiles cmf;
    
    // third panel data (Name, and Location)
    private String ucUrl;
    private String ucDisplayName;

    // helper
    private String bundlePath;
    
    DataModel(WizardDescriptor wiz) {
        super(wiz);
    }
    
    private CreatedModifiedFiles regenerate () {
        if (cmf == null) {
            cmf = new CreatedModifiedFiles (getProject ());
        }
        URL url = DataModel.class.getResource ("update_center.xml"); //NOI18N
        assert url != null : "File 'update_center.xml must exist in package " + getClass ().getPackage () + "!";
        
        FileSystem layer = LayerUtils.layerForProject (getProject ()).layer (false);
        String pathToAutoUpdateType = AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE + '.' + AUTOUPDATE_SERVICE_TYPE_EXT;
        int sequence = 0;
        if (layer != null) {
            FileObject f;
            do {
                f = layer.findResource (pathToAutoUpdateType);
                if (f != null) {
                    pathToAutoUpdateType = AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE + '_' + ++sequence + '.' + AUTOUPDATE_SERVICE_TYPE_EXT;
                }
            } while (f != null);
        }
        cmf.add (cmf.createLayerEntry (pathToAutoUpdateType, url, null, null, null));
        
        String url_key_base = getProject ().getCodeNameBase ().replace ('.', '_') + '_' + AUTOUPDATE_SERVICE_TYPE; //NOI18N
        String url_key = sequence == 0 ? url_key_base : url_key_base + '_' + sequence; // NOI18N
        cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, "url_key", url_key)); //NOI18N
        cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, "enabled", Boolean.TRUE)); //NOI18N
        
        // write into bundle
        ManifestManager mm = ManifestManager.getInstance(getProject ().getManifest (), false);
        String localizingBundle = mm.getLocalizingBundle ();
        localizingBundle = localizingBundle.substring (0, localizingBundle.indexOf ('.'));
        localizingBundle = localizingBundle.replace ('/', '.');
        cmf.add (cmf.createLayerAttribute (pathToAutoUpdateType, UC_LOCALIZING_BUNDLE, localizingBundle));
        
        cmf.add (cmf.bundleKeyDefaultBundle (pathToAutoUpdateType, ucDisplayName));
        cmf.add (cmf.bundleKeyDefaultBundle (url_key, ucUrl));
        
        // add dependency to autoupdate module
        cmf.add (cmf.addModuleDependency (AUTOUPDATE_MODULE, null, null, false));
        
        return cmf;
    }
    
    CreatedModifiedFiles refreshCreatedModifiedFiles() {
        return regenerate ();
    }
    
    void setUpdateCenterURL (String url) {
        this.ucUrl = url;
    }
    
    String getUpdateCenterURL () {
        return ucUrl != null ? ucUrl : ""; //NOI18N
    }
    
    void setUpdateCenterDisplayName (String name) {
        this.ucDisplayName = name;
    }
    
    String getUpdateCenterDisplayName () {
        return ucDisplayName != null ? ucDisplayName : ""; //NOI18N
    }
    
}

