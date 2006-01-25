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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * Data model used across the <em>New Update Center Wizard</em>.
 * @author Jiri Rechtacek
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    static private String AUTOUPDATE_TYPES = "Services/AutoupdateType"; //NOI18N
    static private String AUTOUPDATE_SERVICE_TYPE = "update_center.settings"; //NOI18N
    static private String UC_LOCALIZING_BUNDLE = "SystemFileSystem.localizingBundle"; //NOI18N
    static private String UC_ICON = "SystemFileSystem.icon"; //NOI18N

    private CreatedModifiedFiles cmf;
    
    // third panel data (Name, Icon, and Location)
    private String ucUrl;
    private String ucDisplayName;
    private String ucIconPath;
    
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
        
        // XXX check if the name service type is free
        cmf.add (cmf.createLayerEntry (AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE, url, null, null, null));
        
        // XXX check of the url_key is free
        String url_key = getProject ().getCodeNameBase ().replace ('.', '_') + "_update_center"; //NOI18N
        cmf.add (cmf.createLayerAttribute (AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE, "url_key", url_key)); //NOI18N
        cmf.add (cmf.createLayerAttribute (AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE, "enabled", Boolean.TRUE)); //NOI18N
        
        // write into bundle
        ManifestManager mm = ManifestManager.getInstance(getProject ().getManifest (), false);
        String localizingBundle = mm.getLocalizingBundle ();
        localizingBundle = localizingBundle.substring (0, localizingBundle.indexOf ('.'));
        localizingBundle = localizingBundle.replace ('/', '.');
        cmf.add (cmf.createLayerAttribute (AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE, UC_LOCALIZING_BUNDLE, localizingBundle));
        
        // handle icon
        if (getUpdateCenterIconPath () != null && new File (getUpdateCenterIconPath ()).exists()) {
            String relativeIconPath = addCreateIconOperation (cmf, getUpdateCenterIconPath ());
            URL iconUrl = null;
            try {
                iconUrl = new URL ("nbresloc:/" + relativeIconPath); // NOI18N
            } catch (MalformedURLException ex) {
                assert false : ex;
            }
            if (iconUrl != null) {
                cmf.add (cmf.createLayerAttribute (AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE, UC_ICON, iconUrl));
            }
        }
        
        cmf.add (cmf.bundleKeyDefaultBundle (AUTOUPDATE_TYPES + '/' + AUTOUPDATE_SERVICE_TYPE, ucDisplayName));
        cmf.add (cmf.bundleKeyDefaultBundle (url_key, ucUrl));
        
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
    
    void setUpdateCenterIconPath (String iconPath) {
        this.ucIconPath = iconPath;
    }
    
    String getUpdateCenterIconPath () {
        return ucIconPath != null ? ucIconPath : ""; //NOI18N
    }
    
}

